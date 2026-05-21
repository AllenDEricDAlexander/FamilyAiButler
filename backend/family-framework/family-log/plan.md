# Family Log MDC Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.
> Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将当前 `family-log` 从单一 AOP 日志模块升级为支持 Servlet、WebFlux、HTTP、Dubbo、gRPC、多线程和虚拟线程的统一日志链路模块。

**Architecture:** 当前单个 `family-log` jar 不适合同时被 `family-core`、`family-uaa` 和 `family-gateway` 依赖，因为
gateway 是 WebFlux，不能引入 Servlet Filter 或任何会触发 Servlet 类加载的自动配置。将 `family-log` 改造为父 POM，拆出纯 MDC
核心模块、Servlet 适配模块、WebFlux 适配模块、HTTP 客户端适配模块、Dubbo 适配模块和 gRPC 适配模块，各业务服务只按运行时模型引入对应
starter。

**Tech Stack:** Java 21、Spring Boot 3.5、SLF4J MDC、Spring MVC、Spring WebFlux、Spring Cloud
Gateway、RestClient、Dubbo、gRPC、JUnit 5、ApplicationContextRunner。

---

## 1. 需求分析

### 1.1 当前现状

当前 `backend/family-framework/family-log` 只有：

- `LogAspect`
- `FamilyLogAutoConfiguration`
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

当前能力只覆盖 Servlet 环境下 Controller AOP 日志，缺少：

- 统一 `LogUtil`
- SLF4J MDC 标准键
- HTTP 请求入口 trace 信息填充和清理
- WebFlux / Gateway 请求入口 trace 信息填充和清理
- RestClient 出站 HTTP trace 透传
- Dubbo / gRPC trace 透传
- 普通线程池、`CompletableFuture`、虚拟线程的 MDC 传递
- 可继承的 `TraceRouteRunnable` 风格封装
- 日志格式中 MDC 字段输出

### 1.2 当前方案是否可行

当前保持单个 `family-log` jar 再用 `@ConditionalOnWebApplication` 区分 Servlet/WebFlux，理论上可以做，但不建议。

原因：

- gateway 是 WebFlux，之前已经出现过共享 starter 暴露 Servlet 类型导致 gateway 启动失败的问题。
- 只要同一个 jar 里存在 Servlet Filter、`HttpServletRequest`、Spring MVC AOP 等类型，后续自动配置、Bean 方法签名、测试或 IDE
  重构都有机会重新引入 WebFlux 启动风险。
- `family-log` 后续还要支持 Dubbo、gRPC，这些依赖也不应该强行进入所有业务模块。
- core/uaa 需要 Servlet 入口 MDC，gateway 需要 WebFlux/Reactor MDC，两者模型不同，不应放在同一个 starter 里靠大量条件判断混在一起。

结论：拆分是更稳的方案。`family-log` 作为父 POM 管理日志子模块；业务模块依赖具体 starter。

### 1.3 目标模块结构

```text
backend/family-framework/family-log/
  pom.xml                              # 日志体系父 POM，packaging=pom
  plan.md
  family-log-core/                    # 纯 MDC/LogUtil/上下文传递能力
  family-log-servlet-spring-boot-starter/
  family-log-webflux-spring-boot-starter/
  family-log-http-spring-boot-starter/
  family-log-dubbo-spring-boot-starter/
  family-log-grpc-spring-boot-starter/
```

### 1.4 业务模块依赖关系

```text
family-core
  -> family-log-servlet-spring-boot-starter
  -> family-log-http-spring-boot-starter

family-uaa/uaa-core
  -> family-log-servlet-spring-boot-starter
  -> family-log-http-spring-boot-starter

family-gateway
  -> family-log-webflux-spring-boot-starter
  -> family-log-http-spring-boot-starter

future dubbo provider/consumer module
  -> family-log-dubbo-spring-boot-starter

future grpc server/client module
  -> family-log-grpc-spring-boot-starter
```

`family-log-core` 不直接被业务模块依赖，除非业务代码只需要手动使用 `FamilyLogUtil` 和 `TraceRouteRunnable`。

---

## 2. MDC 规范

### 2.1 MDC Key

统一定义在 `FamilyLogMdcKeys`：

```text
traceId
spanId
parentSpanId
requestId
accountId
profileId
clientId
sessionId
deviceId
riskLevel
requestMethod
requestUri
remoteIp
rpcSystem
rpcService
rpcMethod
```

### 2.2 Header / Metadata 规范

入站读取顺序：

```text
traceId:
  X-Trace-Id
  X-B3-TraceId
  traceparent

requestId:
  X-Request-Id
  X-Correlation-Id

identity:
  X-Family-Account-Id
  X-Family-Profile-Id
  X-Family-Client-Id
  X-Family-Session-Id
  X-Family-Device-Id
  X-Family-Risk-Level
```

出站透传：

```text
X-Trace-Id <- traceId
X-Request-Id <- requestId
X-Family-Account-Id <- accountId
X-Family-Profile-Id <- profileId
X-Family-Client-Id <- clientId
X-Family-Session-Id <- sessionId
X-Family-Device-Id <- deviceId
X-Family-Risk-Level <- riskLevel
```

### 2.3 清理原则

- 入站请求开始时保存当前线程旧 MDC。
- 请求处理期间只覆盖 family 规范内的 MDC key。
- 请求结束时恢复旧 MDC，而不是简单 `MDC.clear()`，避免影响宿主框架或其他组件已有 MDC。
- 多线程执行结束时恢复子线程执行前 MDC，避免线程池复用串号。
- 虚拟线程虽然通常不复用，但仍统一执行恢复逻辑，保证行为一致。

---

## 3. 配置设计

### 3.1 配置前缀

```yaml
family:
  log:
    enabled: true
    mdc-enabled: true
    request-log-enabled: true
    servlet-enabled: true
    webflux-enabled: true
    http-enabled: true
    dubbo-enabled: true
    grpc-enabled: true
    async-enabled: true
    response-trace-header-enabled: true
    max-payload-length: 2000
    trace-header-names:
      - X-Trace-Id
      - X-B3-TraceId
      - traceparent
    request-id-header-names:
      - X-Request-Id
      - X-Correlation-Id
    propagation-header-names:
      - X-Trace-Id
      - X-Request-Id
      - X-Family-Account-Id
      - X-Family-Profile-Id
      - X-Family-Client-Id
      - X-Family-Session-Id
      - X-Family-Device-Id
      - X-Family-Risk-Level
```

### 3.2 默认策略

- `family-log-core` 不注册 Web/RPC Bean。
- Servlet starter 默认启用 Servlet Filter 和 Controller AOP。
- WebFlux starter 默认启用 WebFilter，不做 Servlet AOP。
- HTTP starter 默认支持 `RestClient`，如检测到 `WebClient` 再支持 WebClient。
- Dubbo/gRPC starter 只在 classpath 存在对应类型时启用。

---

## 4. 文件计划

### 4.1 调整 Maven 模块

**Modify:** `backend/family-framework/family-log/pom.xml`

- 改为 `packaging=pom`
- 增加 modules：
    - `family-log-core`
    - `family-log-servlet-spring-boot-starter`
    - `family-log-webflux-spring-boot-starter`
    - `family-log-http-spring-boot-starter`
    - `family-log-dubbo-spring-boot-starter`
    - `family-log-grpc-spring-boot-starter`

**Modify:** `backend/pom.xml`

dependencyManagement 增加：

- `family-log-core`
- `family-log-servlet-spring-boot-starter`
- `family-log-webflux-spring-boot-starter`
- `family-log-http-spring-boot-starter`
- `family-log-dubbo-spring-boot-starter`
- `family-log-grpc-spring-boot-starter`

保留 `family-log` 作为父 POM，不再作为业务依赖 artifact。

### 4.2 `family-log-core`

**Create:** `backend/family-framework/family-log/family-log-core/pom.xml`

依赖：

- `slf4j-api`
- `spring-core`
- `spring-boot-autoconfigure`
- `lombok` provided
- `spring-boot-starter-test` test

**Create:** `FamilyLogProperties`

职责：

- 管理 `family.log` 配置。
- 提供 trace/request header 默认值。
- 提供 MDC 透传 key 白名单。

**Create:** `FamilyLogMdcKeys`

职责：

- 定义 MDC key。
- 定义默认 header 名称。
- 提供 `familyKeys()` 和 `propagationKeys()`。

**Create:** `FamilyLogContext`

职责：

- 保存一次日志上下文快照。
- 支持从 Map 构建、写入 MDC、恢复旧 MDC。

**Create:** `FamilyLogUtil`

职责：

- `traceId()`
- `requestId()`
- `putTraceIdIfAbsent()`
- `putIfNotBlank(String key, String value)`
- `copyContext()`
- `restore(Map<String, String> context)`
- `clearFamilyKeys()`
- `wrap(Runnable runnable)`
- `wrap(Callable<T> callable)`
- `wrap(Supplier<T> supplier)`
- `decorateExecutor(Executor executor)`

**Create:** `TraceRouteRunnable`

职责：

- 对齐用户公司侧风格：业务继承 `TraceRouteRunnable`，实现 `doRun()`。
- 构造时捕获父线程 MDC。
- `run()` 中设置父 MDC，执行 `doRun()`，最后恢复子线程原 MDC。

**Create:** `TraceRouteCallable<T>`

职责：

- 与 `TraceRouteRunnable` 同模型，支持返回值。

**Create:** `TraceRouteSupplier<T>`

职责：

- 用于 `CompletableFuture.supplyAsync()`。

**Create:** `FamilyMdcTaskDecorator`

职责：

- 实现 Spring `TaskDecorator`。
- 对 `ThreadPoolTaskExecutor` 和普通 Spring async 场景传递 MDC。

**Create:** `FamilyLogCoreAutoConfiguration`

职责：

- 注册 `FamilyLogProperties`。
- 注册 `FamilyMdcTaskDecorator`。

### 4.3 `family-log-servlet-spring-boot-starter`

**Move:** 当前 `LogAspect`

从：

```text
backend/family-framework/family-log/src/main/java/top/egon/familyaibutler/framework/log/LogAspect.java
```

到：

```text
backend/family-framework/family-log/family-log-servlet-spring-boot-starter/src/main/java/top/egon/familyaibutler/framework/log/servlet/FamilyServletLogAspect.java
```

**Create:** `FamilyMdcServletFilter`

职责：

- `OncePerRequestFilter`
- 从请求头解析 trace/request/identity 信息。
- 没有 traceId/requestId 时生成 UUID。
- 设置 `requestMethod`、`requestUri`、`remoteIp`。
- 请求结束写回响应头 `X-Trace-Id`、`X-Request-Id`。
- finally 恢复旧 MDC。

**Create:** `FamilyServletLogAutoConfiguration`

职责：

- `@ConditionalOnWebApplication(type = SERVLET)`
- 注册 `FamilyMdcServletFilter`
- 注册 `FamilyServletLogAspect`

**Pointcut 修正：**

当前：

```java
execution(public * top.egon..controller.*Controller.*(..))
```

调整为：

```java
execution(public * top.egon..*Controller.*(..))
```

原因：当前 DDD 结构 Controller 在 `adapter` 包下，不一定命中 `controller` 包名。

### 4.4 `family-log-webflux-spring-boot-starter`

**Create:** `FamilyMdcWebFluxFilter`

职责：

- 实现 `WebFilter`。
- 从 `ServerWebExchange` 请求头解析 trace/request/identity 信息。
- 将上下文写入 MDC。
- 同时写入 Reactor Context，避免异步链切线程后丢失。
- 响应前写回 `X-Trace-Id`、`X-Request-Id`。
- 请求结束恢复旧 MDC。

**Create:** `FamilyMdcGatewayGlobalFilter`

职责：

- 如果检测到 Spring Cloud Gateway classpath，则注册 `GlobalFilter`。
- order 要早于鉴权过滤器，建议 `Ordered.HIGHEST_PRECEDENCE + 20`。
- 在 gateway 最前面生成/继承 traceId，后续 `JwtTokenFilter`、授权调用和下游转发都能拿到 MDC。

**Create:** `FamilyWebFluxLogAutoConfiguration`

职责：

- `@ConditionalOnWebApplication(type = REACTIVE)`
- 不引用任何 Servlet 类型。
- 只注册 WebFlux/Gateway Bean。

### 4.5 `family-log-http-spring-boot-starter`

**Create:** `FamilyMdcRestClientCustomizer`

职责：

- 实现 `RestClientCustomizer`。
- 给 `RestClient.Builder` 增加 request interceptor。
- 从 MDC 取 trace/request/identity key，写入 HTTP header。

当前项目使用点：

- `uaa-resource-server-spring-boot-starter` 的 `RestUaaResourceAuthorizationClient`

**Create:** `FamilyMdcWebClientCustomizer`

职责：

- 实现 `WebClientCustomizer`。
- 从 MDC/Reactor Context 取 trace/request/identity key，写入 HTTP header。

当前项目使用点：

- gateway 的 `UaaAuthorizationDecisionGatewayImpl`

**Create:** `FamilyHttpLogAutoConfiguration`

职责：

- 有 `RestClient` 时注册 RestClient customizer。
- 有 `WebClient` 时注册 WebClient customizer。
- 不依赖 Servlet。

### 4.6 `family-log-dubbo-spring-boot-starter`

**Create:** `FamilyDubboTraceFilter`

职责：

- 实现 Dubbo Filter。
- consumer 侧：从 MDC 写入 Dubbo attachment。
- provider 侧：从 Dubbo attachment 读取并填充 MDC。
- provider 调用结束恢复旧 MDC。

**Create:** Dubbo SPI 文件

```text
META-INF/dubbo/org.apache.dubbo.rpc.Filter
```

内容：

```text
familyTrace=top.egon.familyaibutler.framework.log.dubbo.FamilyDubboTraceFilter
```

**Create:** `FamilyDubboLogAutoConfiguration`

职责：

- 只在 Dubbo classpath 存在时启用。
- 不主动拉入 gateway/core 当前不需要的 Dubbo 依赖。

### 4.7 `family-log-grpc-spring-boot-starter`

**Create:** `FamilyGrpcTraceClientInterceptor`

职责：

- client 侧：从 MDC 写入 gRPC Metadata。

**Create:** `FamilyGrpcTraceServerInterceptor`

职责：

- server 侧：从 gRPC Metadata 读取 trace/request/identity，填充 MDC。
- 请求结束恢复旧 MDC。

**Create:** `FamilyGrpcLogAutoConfiguration`

职责：

- 只在 gRPC classpath 存在时启用。
- 如果项目后续选定 `grpc-spring-boot-starter`，再补对应 starter 的 Bean 注册注解。

---

## 5. 业务模块调整计划

### 5.1 core

**Modify:** `backend/family-core/pom.xml`

替换：

```xml
<artifactId>family-log</artifactId>
```

为：

```xml
<artifactId>family-log-servlet-spring-boot-starter</artifactId>
```

新增：

```xml
<artifactId>family-log-http-spring-boot-starter</artifactId>
```

### 5.2 uaa-core

**Modify:** `backend/family-uaa/uaa-core/pom.xml`

替换：

```xml
<artifactId>family-log</artifactId>
```

为：

```xml
<artifactId>family-log-servlet-spring-boot-starter</artifactId>
```

新增：

```xml
<artifactId>family-log-http-spring-boot-starter</artifactId>
```

### 5.3 gateway

**Modify:** `backend/family-gateway/pom.xml`

新增：

```xml
<artifactId>family-log-webflux-spring-boot-starter</artifactId>
<artifactId>family-log-http-spring-boot-starter</artifactId>
```

gateway 不引入 Servlet starter。

### 5.4 qwen-ai

qwen-ai 当前未引入 `family-log`。如果需要接口日志和 MDC：

```xml
<artifactId>family-log-servlet-spring-boot-starter</artifactId>
<artifactId>family-log-http-spring-boot-starter</artifactId>
```

AI 图片接口可能有大对象参数，默认依赖 `max-payload-length` 截断日志。

---

## 6. Logback 调整计划

### 6.1 Pattern 统一增加 MDC

需要调整：

- `backend/family-core/src/main/resources/log/logback-spring.xml`
- `backend/family-uaa/uaa-core/src/main/resources/log/logback-spring.xml`
- `backend/family-gateway/src/main/resources/log/logback-spring.xml`
- `backend/family-ai/qwen-ai/src/main/resources/log/logback-spring.xml`，如果 qwen-ai 引入日志 starter

推荐 pattern 片段：

```text
traceId=%X{traceId:-} requestId=%X{requestId:-} accountId=%X{accountId:-}
```

控制台示例：

```xml
<property name="CONSOLE_LOG_PATTERN"
          value="%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level ${PID:- } --- [%15.15thread] traceId=%X{traceId:-} requestId=%X{requestId:-} accountId=%X{accountId:-} %-40.40logger{39} : %msg%n"/>
```

文件示例：

```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level traceId=%X{traceId:-} requestId=%X{requestId:-} accountId=%X{accountId:-} %logger{50} - %msg%n</pattern>
```

---

## 7. 测试计划

### 7.1 core 模块测试

**Create:** `family-log-core/src/test/java/.../FamilyLogUtilTest.java`

覆盖：

- `putTraceIdIfAbsent` 无 traceId 时生成。
- 已有 traceId 时不覆盖。
- `wrap(Runnable)` 能把父线程 MDC 带到子线程。
- 子线程执行后恢复旧 MDC。
- `TraceRouteRunnable` 支持继承式使用。
- 虚拟线程执行 `TraceRouteRunnable` 后能读取父 MDC。

### 7.2 servlet starter 测试

**Create:** `family-log-servlet-spring-boot-starter/src/test/java/.../FamilyMdcServletFilterTest.java`

覆盖：

- 入站 header 填充 MDC。
- 缺少 traceId 时自动生成。
- response 写回 traceId/requestId。
- filter chain 结束后清理/恢复 MDC。
- Controller AOP pointcut 能命中 `adapter.*Controller`。

### 7.3 webflux starter 测试

**Create:** `family-log-webflux-spring-boot-starter/src/test/java/.../FamilyMdcWebFluxFilterTest.java`

覆盖：

- WebFlux 请求中 MDC 可用。
- Reactor 异步链中 traceId 不丢失。
- 请求结束后清理/恢复 MDC。
- 不加载任何 Servlet 类型。

**Create:** `family-gateway/src/test/java/.../GatewayLogAutoConfigurationTest.java`

覆盖：

- gateway 引入 webflux log starter 后 Spring Context 能启动。
- classpath 中没有由 log starter 强制带入 Servlet starter。

### 7.4 HTTP starter 测试

**Create:** `family-log-http-spring-boot-starter/src/test/java/.../FamilyMdcRestClientCustomizerTest.java`

覆盖：

- RestClient 出站请求带 `X-Trace-Id`。
- RestClient 出站请求带 `X-Request-Id`。
- 空 MDC 不写空 header。

**Create:** `family-log-http-spring-boot-starter/src/test/java/.../FamilyMdcWebClientCustomizerTest.java`

覆盖：

- WebClient 出站请求带 trace header。
- Reactor Context 中的 trace 信息优先可用。

### 7.5 Dubbo / gRPC 测试

先做单元测试，不引入真实服务启动：

- Dubbo：mock invocation/attachment，验证 consumer 写入、provider 读取、finally 清理。
- gRPC：mock Metadata，验证 client 写入、server 读取、finally 清理。

### 7.6 编译验证

执行：

```bash
mvn -pl family-framework/family-log -am test
mvn -pl family-core,family-uaa/uaa-core,family-gateway -am -DskipTests compile
mvn -DskipTests compile
```

### 7.7 本地启动验证

不启动 Docker，只用本地 JDK：

```bash
./ops/scripts/backend-local.sh all dev restart
./ops/scripts/backend-local.sh all dev status
```

验证点：

- `family-core` 启动成功。
- `family-uaa` 启动成功。
- `family-gateway` 启动成功。
- gateway 日志包含 `traceId`。
- gateway 调 core 时，core 日志 traceId 与 gateway 一致。
- core 调 UAA 授权时，UAA 日志 traceId 与 core 一致。

---

## 8. 实施任务清单

> **复核状态（2026-05-21）：** 代码层面已完成 Maven 拆分、MDC 核心、Servlet/WebFlux/Gateway/HTTP/Dubbo/gRPC
> 透传、TaskDecorator/TraceRoute 包装、业务模块依赖接入和 logback pattern。`FamilyLogUtil` 已在 `family-log-core`
> 中补充结构化业务日志
> Builder。Gateway 缺口已修复：`FamilyMdcGatewayGlobalFilter` 现在会读取 identity header，并 mutate 下游 route request
> header，保证 gateway -> core 普通路由可继续透传 trace/request/identity。运行时级别的 gateway -> core -> uaa
> 三段真实日志一致性仍需在本地服务启动后验证；本次按约束未启动 Docker，也未执行 `backend-local.sh`。

### Task 1: Maven 拆分

- [x] 将 `family-log/pom.xml` 改成父 POM。
- [x] 新建 6 个日志子模块 POM。
- [x] 更新 `backend/pom.xml` dependencyManagement。
- [x] 更新 `family-framework/pom.xml` 保持只包含 `family-log` 父模块。
- [x] 执行 `mvn -pl family-framework/family-log -am -DskipTests compile`。复核说明：该命令只会编译父 POM
  和上游模块，不会进入子模块；实际子模块验证已改用 `mvn -f family-framework/family-log/pom.xml test`。

### Task 2: 实现 core MDC 能力

- [x] 新建 `FamilyLogProperties`。
- [x] 新建 `FamilyLogMdcKeys`。
- [x] 新建 `FamilyLogContext`。
- [x] 新建 `FamilyLogUtil`。复核说明：已补充结构化业务日志 Builder，支持 `bizDebug` / `bizInfo` / `bizWarn` / `bizError`、
  `Phase`、字段排序、基础脱敏、集合截断、异常日志。
- [x] 新建 `TraceRouteRunnable`。
- [x] 新建 `TraceRouteCallable`。
- [x] 新建 `TraceRouteSupplier`。
- [x] 新建 `FamilyMdcTaskDecorator`。
- [x] 新建 `FamilyLogCoreAutoConfiguration`。
- [x] 补齐单元测试。

### Task 3: 实现 Servlet 日志 starter

- [x] 移动并改造 `LogAspect` 为 `FamilyServletLogAspect`。
- [x] 新建 `FamilyMdcServletFilter`。
- [x] 新建 `FamilyServletLogAutoConfiguration`。
- [x] 补齐 Servlet Filter 和 AOP 测试。
- [x] core/uaa-core 依赖切到 Servlet starter。

### Task 4: 实现 WebFlux/Gateway 日志 starter

- [x] 新建 `FamilyMdcWebFluxFilter`。
- [x] 新建 `FamilyMdcGatewayGlobalFilter`。复核说明：已修复 gateway 不 mutate 下游 route request header、未读取 identity
  header 的缺口。
- [x] 新建 `FamilyWebFluxLogAutoConfiguration`。
- [x] 补齐 WebFlux/Reactor MDC 测试。
- [x] gateway 引入 WebFlux starter，且未引入 Servlet starter。
- [x] 验证 gateway 编译和上下文启动。复核说明：已验证 gateway 编译；未做完整运行时启动。

### Task 5: 实现 HTTP 出站透传

- [x] 新建 `FamilyMdcRestClientCustomizer`。
- [x] 新建 `FamilyMdcWebClientCustomizer`。
- [x] 新建 `FamilyHttpLogAutoConfiguration`。
- [x] core/uaa/gateway 按需引入 HTTP starter。
- [x] 补齐 RestClient/WebClient header 透传测试。

### Task 6: 预留并实现 RPC 透传

- [x] 新建 Dubbo starter。
- [x] 新建 `FamilyDubboTraceFilter`。复核说明：已补充 consumer/provider 实际透传测试，并给 Dubbo service context 缺少 URL
  的边界做兜底。
- [x] 新建 Dubbo SPI 文件。
- [x] 新建 gRPC starter。
- [x] 新建 gRPC client/server interceptor。
- [x] 补齐 Dubbo/gRPC metadata 透传测试。

### Task 7: 调整日志格式

- [x] core logback 增加 MDC 字段。
- [x] uaa-core logback 增加 MDC 字段。
- [x] gateway logback 增加 MDC 字段。
- [x] qwen-ai 如引入日志 starter，再同步调整 logback。

### Task 8: 全量验证

- [x] `mvn -f family-framework/family-log/pom.xml test`
- [x] `mvn -pl family-core,family-uaa/uaa-core,family-gateway -am -DskipTests compile`
- [x] `mvn -DskipTests compile`
- [ ] `./ops/scripts/backend-local.sh all dev restart`。未执行：本轮按要求不启动 Docker，也未启动本地运行时服务。
- [ ] 通过一次 gateway -> core -> uaa 请求，确认三段日志 traceId 一致。未执行：需要本地 gateway/core/uaa
  服务和可用依赖环境；本轮已用单元测试验证 gateway route request header mutate、HTTP client 透传和各入口/出站边界。

---

## 9. 风险和约束

### 9.1 gateway 风险

gateway 只能引入：

- `family-log-core`
- `family-log-webflux-spring-boot-starter`
- `family-log-http-spring-boot-starter`

不能引入：

- `family-log-servlet-spring-boot-starter`

### 9.2 Dubbo / gRPC 依赖风险

当前项目没有实际 Dubbo/gRPC 模块。Dubbo/gRPC starter 可以先完成接口和单元测试，但不要让 core/uaa/gateway 默认依赖它们。

### 9.3 虚拟线程

虚拟线程不复用线程，但仍必须显式传递 MDC。原因是 MDC 基于 ThreadLocal，虚拟线程不会自动继承父线程 MDC，除非使用封装器捕获并恢复。

### 9.4 AOP 大对象日志

AI 图片、文件上传、Multipart、Servlet/WebFlux request/response、流式对象必须跳过或截断，避免日志过大。

### 9.5 Logback 格式

只做 MDC 写入不够，必须同步改 logback pattern，否则日志文件看不到 traceId。
