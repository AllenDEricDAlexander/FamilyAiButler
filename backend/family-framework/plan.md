# Family Framework Module Split Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.
> Steps use checkbox (`- [ ]`) syntax for tracking. Do not change code until the user confirms this plan.

**Goal:** 将当前 `family-common` 治理为 `family-framework` 下的框架模块集合，分两阶段完成：第一阶段建立 `family-framework`
父 POM、保留现有 `family-common` 子模块并新增 `family-log`；第二阶段继续细拆 `common-web`、`common-mybatis`、
`common-security`。

**Architecture:** `backend/pom.xml` 继续作为整个后端根父 POM，新增 `backend/family-framework/pom.xml` 作为框架层二级父
POM。第一阶段不改 `family-common` 的 Java 包名和 artifactId，降低业务模块 import 改动面；第二阶段再把 Web、MyBatis、Security
能力从 `family-common` 拆成独立 starter 风格模块，让 gateway 这类 WebFlux 模块只依赖纯 common 和 security API。

**Tech Stack:** Maven multi-module、Spring Boot 3.5.13、Java 21、Spring AOP、Spring Boot AutoConfiguration、JUnit 5。

---

## 现状与边界

当前 `backend/family-common` 同时承载通用返回、异常、Jackson/MyBatis 配置、MyBatis Plus 扩展、JWT 安全和日志切面。这个模块被
`family-core`、`family-uaa`、`family-gateway`、`family-ai/ai-common` 依赖，其中 `family-gateway` 是 WebFlux 模块，之前已经出现过
common/starter 中 Servlet 类型影响 gateway 启动的问题。

本计划分两阶段执行。第一阶段做低风险结构迁移：

```text
backend
├── pom.xml
├── family-framework
│   ├── pom.xml
│   ├── family-common
│   │   ├── pom.xml
│   │   └── src/...
│   └── family-log
│       ├── pom.xml
│       └── src/...
├── family-core
├── family-uaa
├── family-gateway
└── family-ai
```

第二阶段继续细拆 framework：

```text
backend/family-framework
├── pom.xml
├── family-common
├── family-common-web
├── family-common-mybatis
├── family-common-security
└── family-log
```

不做这些事：

- 不把 `top.egon.familyaibutler.common` 包名改成 `framework.common`。
- 第一阶段不把 `family-common` 再拆成 `common-web`、`common-mybatis`、`common-security`。
- 不调整业务模块 DDD 包结构。
- 不启动 Docker。

---

## 文件变更清单

### 第一阶段创建

- `backend/family-framework/pom.xml`  
  框架层父 POM，`packaging=pom`，管理 `family-common` 和 `family-log`。

- `backend/family-framework/family-log/pom.xml`  
  日志模块 POM，承接 AOP、Web 参数类型、Jackson 依赖。

-

`backend/family-framework/family-log/src/main/java/top/egon/familyaibutler/framework/log/FamilyLogAutoConfiguration.java`  
日志自动配置类，按配置开关注入 `LogAspect`。

- `backend/family-framework/family-log/src/main/java/top/egon/familyaibutler/framework/log/LogAspect.java`  
  从 common 移出的日志切面，pointcut 同时兼容旧 `controller` 和当前 `adapter`。

-

`backend/family-framework/family-log/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`  
Spring Boot 自动配置注册文件。

- `backend/family-framework/family-log/src/test/java/top/egon/familyaibutler/framework/log/LogAspectTest.java`  
  覆盖参数过滤和 pointcut 关键表达式。

### 第一阶段移动

- `backend/family-common` -> `backend/family-framework/family-common`

### 第一阶段修改

- `backend/pom.xml`  
  `<module>family-common</module>` 改为 `<module>family-framework</module>`；dependencyManagement 补 `family-common` 和
  `family-log`。

- `backend/family-framework/family-common/pom.xml`  
  parent 改为 `family-framework`；移除日志切面需要但 common 不再需要的直接依赖。

- `backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/aspect/LogAspect.java`  
  删除，迁移到 `family-log`。

- `backend/family-core/pom.xml`  
  `family-common` 依赖保持不变；新增 `family-log` 依赖用于接口日志。

- `backend/family-uaa/pom.xml`  
  `family-common` 依赖保持不变；新增 `family-log` 依赖用于接口日志。

- `backend/family-ai/ai-common/pom.xml`  
  `family-common` 依赖保持不变；不新增 `family-log`。

- `backend/family-ai/qwen-ai/pom.xml`  
  如需要 qwen-ai 接口日志，在本模块新增 `family-log`；否则先不引入，避免 AI 上传接口日志输出过大。

- `backend/family-gateway/pom.xml`  
  不引入 `family-log`。继续只依赖 `family-common` 和 gateway 必需 starter。

- `ops/scripts/backend-local.sh`  
  如果脚本只按 Maven module path 构建业务模块，不需要改；如果有硬编码 `family-common` 路径，改为
  `family-framework/family-common`。

### 第二阶段创建

- `backend/family-framework/family-common-web/pom.xml`  
  WebMVC 通用能力模块，承接统一返回、全局异常处理、Jackson 配置、Springdoc 注解依赖。

- `backend/family-framework/family-common-mybatis/pom.xml`  
  MyBatis Plus 通用能力模块，承接 `EgonMapper`、`IEgonService`、自定义 SQL 注入器、自动填充处理器和 MyBatis Plus 配置。

- `backend/family-framework/family-common-security/pom.xml`  
  安全通用能力模块，承接 JWT claims/properties/service 和安全请求头常量。

-

`backend/family-framework/family-common-web/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`  
Web 通用自动配置注册。

-

`backend/family-framework/family-common-mybatis/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`  
MyBatis 通用自动配置注册。

-

`backend/family-framework/family-common-security/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`  
Security 通用自动配置注册。

### 第二阶段移动

- `family-common/configuration/JacksonConfig.java` ->
  `family-common-web/autoconfigure/FamilyCommonWebAutoConfiguration.java`
- `family-common/configuration/LocalDateTimeSerializerConfig.java` -> 合并进
  `family-common-web/autoconfigure/FamilyCommonWebAutoConfiguration.java`
- `family-common/handler/GlobalExceptionHandler.java` -> `family-common-web/handler/GlobalExceptionHandler.java`
- `family-common/extention/**` -> `family-common-mybatis/extention/**`
- `family-common/configuration/MybatisPlusConfig.java` ->
  `family-common-mybatis/autoconfigure/FamilyMybatisAutoConfiguration.java`
- `family-common/handler/MybatisPlusMetaFieldHandler.java` ->
  `family-common-mybatis/handler/MybatisPlusMetaFieldHandler.java`
- `family-common/security/**` -> `family-common-security/security/**`

### 第二阶段修改

- `backend/family-framework/pom.xml`  
  modules 增加 `family-common-web`、`family-common-mybatis`、`family-common-security`。

- `backend/pom.xml`  
  dependencyManagement 增加三个二阶段模块。

- `family-common/pom.xml`  
  移除 Web、MyBatis、JPA、JWT 重依赖，只保留纯 Java/common 所需依赖。

- `family-core/pom.xml`  
  按实际使用引入 `family-common-web`、`family-common-mybatis`、`family-common-security`。

- `family-uaa/pom.xml`  
  按实际使用引入 `family-common-web`、`family-common-mybatis`、`family-common-security`。

- `family-gateway/pom.xml`  
  不再依赖包含 WebMVC/JPA/MyBatis 的大 common，只保留 `family-common` 和 `family-common-security`，删除针对
  `family-common` 的 Web/JPA/MyBatis exclusions。

- `family-ai/ai-common/pom.xml`、`family-ai/qwen-ai/pom.xml`  
  按实际需要只引入纯 common；默认不引入 web/mybatis/security。

---

## 阶段一：建立 `family-framework` 并抽出 `family-log`

阶段一目标是先把原 `family-common` 迁移到 `family-framework/family-common`，并把日志切面抽到 `family-log`
。阶段一完成后，业务模块仍主要依赖 `family-common`，风险和改动面可控。

## Task 1: 建立 `family-framework` 父 POM

**Files:**

- Create: `backend/family-framework/pom.xml`
- Modify: `backend/pom.xml`

- [ ] **Step 1: 创建 framework 父 POM**

创建 `backend/family-framework/pom.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>top.egon</groupId>
        <artifactId>family-ai-butler-backend</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>family-framework</artifactId>
    <packaging>pom</packaging>

    <modules>
        <module>family-common</module>
        <module>family-log</module>
    </modules>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
</project>
```

- [ ] **Step 2: 修改根 POM modules**

在 `backend/pom.xml` 中把：

```xml
<module>family-common</module>
```

改为：

```xml
<module>family-framework</module>
```

- [ ] **Step 3: 在根 POM 管理内部框架模块版本**

在 `backend/pom.xml` 的 `<dependencyManagement><dependencies>` 中补充：

```xml
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-log</artifactId>
    <version>${project.version}</version>
</dependency>
```

- [ ] **Step 4: 验证 framework 父 POM 被 Maven 识别**

Run:

```bash
mvn -pl family-framework -N validate
```

Expected:

```text
BUILD SUCCESS
```

---

## Task 2: 移动现有 `family-common` 模块

**Files:**

- Move: `backend/family-common` -> `backend/family-framework/family-common`
- Modify: `backend/family-framework/family-common/pom.xml`
- Check: all `pom.xml` files that reference `family-common`

- [ ] **Step 1: 移动模块目录**

Run from repo root:

```bash
mkdir -p backend/family-framework
git mv backend/family-common backend/family-framework/family-common
```

Expected:

```text
backend/family-framework/family-common/pom.xml exists
backend/family-common does not exist
```

- [ ] **Step 2: 修改 common 模块 parent**

在 `backend/family-framework/family-common/pom.xml` 中把 parent 改为：

```xml
<parent>
    <groupId>top.egon</groupId>
    <artifactId>family-framework</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</parent>
```

保留：

```xml
<artifactId>family-common</artifactId>
```

- [ ] **Step 3: 先保持 `family-common` Java 包名不变**

确认 `backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common` 仍存在。业务模块当前大量
import `top.egon.familyaibutler.common.*`，第一阶段不要改包名。

- [ ] **Step 4: 验证 common 模块编译**

Run:

```bash
mvn -pl family-framework/family-common -am -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

---

## Task 3: 新建 `family-log` 模块

**Files:**

- Create: `backend/family-framework/family-log/pom.xml`
- Create:
  `backend/family-framework/family-log/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Create:
  `backend/family-framework/family-log/src/main/java/top/egon/familyaibutler/framework/log/FamilyLogAutoConfiguration.java`
- Create: `backend/family-framework/family-log/src/main/java/top/egon/familyaibutler/framework/log/LogAspect.java`

- [ ] **Step 1: 创建 family-log POM**

创建 `backend/family-framework/family-log/pom.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>top.egon</groupId>
        <artifactId>family-framework</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>family-log</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <skip>true</skip>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建自动配置注册文件**

创建
`backend/family-framework/family-log/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`：

```text
top.egon.familyaibutler.framework.log.FamilyLogAutoConfiguration
```

- [ ] **Step 3: 创建日志自动配置类**

创建
`backend/family-framework/family-log/src/main/java/top/egon/familyaibutler/framework/log/FamilyLogAutoConfiguration.java`：

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log
 * @FileName: FamilyLogAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-12:00
 * @Description: 家庭框架日志自动配置文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log
 * @ClassName: FamilyLogAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026-05-21 12:00
 * @Description: 家庭框架日志自动配置
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass(Aspect.class)
@ConditionalOnProperty(prefix = "family.framework.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FamilyLogAutoConfiguration {

    /**
     * 创建统一接口日志切面。
     *
     * @return 统一接口日志切面
     */
    @Bean
    @ConditionalOnMissingBean
    public LogAspect familyLogAspect() {
        return new LogAspect();
    }
}
```

- [ ] **Step 4: 创建新的日志切面**

创建 `backend/family-framework/family-log/src/main/java/top/egon/familyaibutler/framework/log/LogAspect.java`，逻辑从旧
`common.aspect.LogAspect` 迁移，并修正 pointcut：

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log
 * @FileName: LogAspect.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-12:00
 * @Description: 统一接口日志切面文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log
 * @ClassName: LogAspect
 * @Author: atluofu
 * @CreateTime: 2026-05-21 12:00
 * @Description: 统一接口日志切面
 * @Version: 1.0
 */
@Aspect
@Slf4j
public class LogAspect {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 匹配旧 controller 包和 DDD adapter 包下的 Controller 方法。
     */
    @Pointcut("execution(public * top.egon..controller..*Controller.*(..)) || execution(public * top.egon..adapter..*Controller.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 记录接口入参、出参和耗时。
     *
     * @param proceedingJoinPoint 切点上下文
     * @return 接口返回值
     * @throws Throwable 目标方法异常
     */
    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = methodSignature.getMethod();
        log.info("===============请求内容===============");
        log.info("请求地址:{}", proceedingJoinPoint.getTarget().getClass().getName());
        log.info("请求方式:{}", method);
        log.info("请求类方法:{}", proceedingJoinPoint.getSignature().getName());
        Object[] arguments = printableArguments(proceedingJoinPoint.getArgs());
        log.info("请求参数: {}", OBJECT_MAPPER.writeValueAsString(arguments));
        log.info("===============请求内容===============");
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        log.info("===============返回内容===============");
        log.info("返回参数: {}", OBJECT_MAPPER.writeValueAsString(result));
        log.info("------------- 结束 耗时：{} ms -------------", System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * 过滤不可序列化或不适合打印的接口参数。
     *
     * @param args 原始参数
     * @return 可打印参数
     */
    Object[] printableArguments(Object[] args) {
        Object[] arguments = new Object[args.length];
        for (int index = 0; index < args.length; index++) {
            if (args[index] instanceof ServletRequest
                    || args[index] instanceof ServletResponse
                    || args[index] instanceof MultipartFile) {
                continue;
            }
            arguments[index] = args[index];
        }
        return arguments;
    }
}
```

- [ ] **Step 5: 删除旧日志切面**

删除：

```text
backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/aspect/LogAspect.java
```

---

## Task 4: 收敛 `family-common` 依赖

**Files:**

- Modify: `backend/family-framework/family-common/pom.xml`

- [ ] **Step 1: 移除 common 中日志专属依赖**

如果 `family-common` 中只剩公共返回、异常、配置、MyBatis 扩展、JWT，则从 `family-common/pom.xml` 移除：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

- [ ] **Step 2: 保留 common 仍实际使用的依赖**

`family-common` 第一阶段保留这些依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
</dependency>
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-jsqlparser</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
</dependency>
```

说明：`family-gateway` 当前已经对 `family-common` 做 Web/JPA/MyBatis exclusions；本阶段不扩大改动面。下一阶段再拆
`common-web`、`common-mybatis`、`common-security` 才能彻底去掉这些 exclusions。

- [ ] **Step 3: 验证 common 编译和测试**

Run:

```bash
mvn -pl family-framework/family-common test
```

Expected:

```text
BUILD SUCCESS
```

---

## Task 5: 调整业务模块依赖

**Files:**

- Modify: `backend/family-core/pom.xml`
- Modify: `backend/family-uaa/pom.xml`
- Review: `backend/family-gateway/pom.xml`
- Review: `backend/family-ai/ai-common/pom.xml`
- Review: `backend/family-ai/qwen-ai/pom.xml`

- [ ] **Step 1: core 引入 family-log**

在 `backend/family-core/pom.xml` 中保留：

```xml
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

新增：

```xml
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-log</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

- [ ] **Step 2: uaa 引入 family-log**

在 `backend/family-uaa/pom.xml` 中保留 `family-common`，新增：

```xml
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-log</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

- [ ] **Step 3: gateway 不引入 family-log**

确认 `backend/family-gateway/pom.xml` 没有：

```xml
<artifactId>family-log</artifactId>
```

原因：gateway 是 WebFlux，当前日志切面依赖 Servlet/WebMVC 参数类型，第一阶段不要引入，避免再次污染 gateway classpath。

- [ ] **Step 4: ai-common 暂不引入 family-log**

确认 `backend/family-ai/ai-common/pom.xml` 不新增 `family-log`。

- [ ] **Step 5: qwen-ai 按需决定是否引入 family-log**

默认不引入 `family-log`。qwen-ai 的上传图片接口可能产生大请求体日志，本阶段不自动开启。

---

## Task 6: 更新脚本和路径引用

**Files:**

- Review/Modify: `ops/scripts/backend-local.sh`
- Review/Modify: docs that mention `backend/family-common`

- [ ] **Step 1: 扫描硬编码路径**

Run:

```bash
rg -n "backend/family-common|family-common" backend ops README.md
```

Expected:

```text
业务 pom 里的 artifactId=family-common 可以保留；
文件路径 backend/family-common 必须改为 backend/family-framework/family-common。
```

- [ ] **Step 2: 检查 backend-local.sh**

如果 `ops/scripts/backend-local.sh` 只通过业务模块路径构建：

```bash
mvn -pl "${maven_module}" -am install -DskipTests -Dspring-boot.repackage.skip=true
```

则不需要修改；`-am` 会自动构建 `family-framework/family-common`。

如果发现脚本硬编码 `family-common`，改成：

```text
family-framework/family-common
```

---

## Task 7: 补充最小测试

**Files:**

- Create: `backend/family-framework/family-log/src/test/java/top/egon/familyaibutler/framework/log/LogAspectTest.java`
- Existing:
  `backend/family-framework/family-common/src/test/java/top/egon/familyaibutler/common/security/jwt/FamilyJwtServiceTest.java`

- [ ] **Step 1: 为 LogAspect 参数过滤写单测**

创建 `backend/family-framework/family-log/src/test/java/top/egon/familyaibutler/framework/log/LogAspectTest.java`：

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log
 * @FileName: LogAspectTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-12:00
 * @Description: 统一接口日志切面测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log
 * @ClassName: LogAspectTest
 * @Author: atluofu
 * @CreateTime: 2026-05-21 12:00
 * @Description: 统一接口日志切面测试
 * @Version: 1.0
 */
class LogAspectTest {

    /**
     * 校验普通参数会保留在日志参数数组中。
     */
    @Test
    void printableArgumentsShouldKeepNormalArguments() {
        LogAspect logAspect = new LogAspect();
        Object[] arguments = logAspect.printableArguments(new Object[]{"account", 1});
        assertThat(arguments).containsExactly("account", 1);
    }
}
```

- [ ] **Step 2: 跑 family-log 测试**

Run:

```bash
mvn -pl family-framework/family-log test
```

Expected:

```text
BUILD SUCCESS
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

- [ ] **Step 3: 跑 family-common 现有测试**

Run:

```bash
mvn -pl family-framework/family-common test
```

Expected:

```text
BUILD SUCCESS
```

---

## Task 8: 全量编译和本地启动验证

**Files:**

- No source file changes in this task.

- [ ] **Step 1: 后端全量编译**

Run:

```bash
mvn -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 2: 重启后端应用**

Run from repo root:

```bash
./ops/scripts/backend-local.sh all dev restart
```

Expected:

```text
family-core dev health check passed
family-uaa dev health check passed
family-ai-qwen dev health check passed
family-gateway dev health check passed
```

注意：不要启动 Docker。只使用本地 JDK 脚本。

- [ ] **Step 3: 查询运行状态**

Run:

```bash
./ops/scripts/backend-local.sh all dev status
```

Expected:

```text
family-core dev status: running
family-uaa dev status: running
family-ai-qwen dev status: running
family-gateway dev status: running
```

- [ ] **Step 4: 检查 gateway 日志没有 servlet 相关启动错误**

Run:

```bash
tail -n 120 ops/.runtime/family-gateway-dev.log
```

Expected:

```text
Started FamilyGateway
```

不应出现：

```text
NoClassDefFoundError: jakarta/servlet/http/HttpServletRequest
```

---

## Task 9: 提交前自检

**Files:**

- Review: all changed files.

- [ ] **Step 1: 检查无空白错误**

Run:

```bash
git diff --check
```

Expected:

```text
无输出
```

- [ ] **Step 2: 检查移动结果**

Run:

```bash
test -f backend/family-framework/pom.xml
test -f backend/family-framework/family-common/pom.xml
test -f backend/family-framework/family-log/pom.xml
test ! -d backend/family-common
```

Expected:

```text
命令退出码为 0
```

- [ ] **Step 3: 检查业务 import 未被大面积改动**

Run:

```bash
rg -n "top\\.egon\\.familyaibutler\\.common" backend/family-core backend/family-uaa backend/family-gateway backend/family-ai
```

Expected:

```text
仍然能搜到业务模块引用 common 的 import；
本阶段不要求改包名。
```

- [ ] **Step 4: 检查 common 不再包含 LogAspect**

Run:

```bash
test ! -f backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/aspect/LogAspect.java
test -f backend/family-framework/family-log/src/main/java/top/egon/familyaibutler/framework/log/LogAspect.java
```

Expected:

```text
命令退出码为 0
```

---

## 阶段二：细拆 `common-web`、`common-mybatis`、`common-security`

阶段二目标是继续降低 `family-common` 的依赖重量和组件扫描风险。阶段二完成后，`family-common`
只保留纯通用模型、返回对象、异常和转换基础接口；WebMVC、MyBatis Plus、JWT 安全能力分别进入独立模块，业务模块按需依赖。

阶段二模块职责：

```text
family-common
├── enums
├── exception
├── pojo
└── utils

family-common-web
├── autoconfigure
└── handler

family-common-mybatis
├── autoconfigure
├── extention
└── handler

family-common-security
└── security
```

---

## Task 10: 新增二阶段框架模块 POM

**Files:**

- Modify: `backend/family-framework/pom.xml`
- Modify: `backend/pom.xml`
- Create: `backend/family-framework/family-common-web/pom.xml`
- Create: `backend/family-framework/family-common-mybatis/pom.xml`
- Create: `backend/family-framework/family-common-security/pom.xml`

- [ ] **Step 1: 在 framework 父 POM 增加二阶段模块**

在 `backend/family-framework/pom.xml` 中把 modules 调整为：

```xml
<modules>
    <module>family-common</module>
    <module>family-common-web</module>
    <module>family-common-mybatis</module>
    <module>family-common-security</module>
    <module>family-log</module>
</modules>
```

- [ ] **Step 2: 在根 POM dependencyManagement 增加二阶段模块**

在 `backend/pom.xml` 的 `<dependencyManagement><dependencies>` 中补充：

```xml
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common-web</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common-mybatis</artifactId>
    <version>${project.version}</version>
</dependency>
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common-security</artifactId>
    <version>${project.version}</version>
</dependency>
```

- [ ] **Step 3: 创建 family-common-web POM**

创建 `backend/family-framework/family-common-web/pom.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>top.egon</groupId>
        <artifactId>family-framework</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>family-common-web</artifactId>

    <dependencies>
        <dependency>
            <groupId>top.egon</groupId>
            <artifactId>family-common</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 4: 创建 family-common-mybatis POM**

创建 `backend/family-framework/family-common-mybatis/pom.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>top.egon</groupId>
        <artifactId>family-framework</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>family-common-mybatis</artifactId>

    <dependencies>
        <dependency>
            <groupId>top.egon</groupId>
            <artifactId>family-common</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-jsqlparser</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 5: 创建 family-common-security POM**

创建 `backend/family-framework/family-common-security/pom.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>top.egon</groupId>
        <artifactId>family-framework</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>family-common-security</artifactId>

    <dependencies>
        <dependency>
            <groupId>top.egon</groupId>
            <artifactId>family-common</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-autoconfigure</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **Step 6: 验证空模块 POM 可解析**

Run:

```bash
mvn -pl family-framework/family-common-web,family-framework/family-common-mybatis,family-framework/family-common-security -am -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

---

## Task 11: 拆出 `family-common-web`

**Files:**

- Move:
  `backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/handler/GlobalExceptionHandler.java`
- Create:
  `backend/family-framework/family-common-web/src/main/java/top/egon/familyaibutler/common/web/autoconfigure/FamilyCommonWebAutoConfiguration.java`
- Create:
  `backend/family-framework/family-common-web/src/main/java/top/egon/familyaibutler/common/web/handler/GlobalExceptionHandler.java`
- Create:
  `backend/family-framework/family-common-web/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Delete/Merge:
  `backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/configuration/JacksonConfig.java`
- Delete/Merge:
  `backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/configuration/LocalDateTimeSerializerConfig.java`

- [ ] **Step 1: 创建 Web 自动配置注册文件**

创建
`backend/family-framework/family-common-web/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`：

```text
top.egon.familyaibutler.common.web.autoconfigure.FamilyCommonWebAutoConfiguration
```

- [ ] **Step 2: 创建 Web 自动配置类**

创建
`backend/family-framework/family-common-web/src/main/java/top/egon/familyaibutler/common/web/autoconfigure/FamilyCommonWebAutoConfiguration.java`：

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.web.autoconfigure
 * @FileName: FamilyCommonWebAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-12:30
 * @Description: Web 通用能力自动配置文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.common.web.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.DispatcherServlet;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.web.autoconfigure
 * @ClassName: FamilyCommonWebAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026-05-21 12:30
 * @Description: Web 通用能力自动配置
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass(DispatcherServlet.class)
public class FamilyCommonWebAutoConfiguration {

    /**
     * 创建 ObjectMapper。
     *
     * @param builder Jackson 构造器
     * @return ObjectMapper
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    /**
     * 创建 LocalDateTime 序列化器。
     *
     * @return LocalDateTimeSerializer
     */
    @Bean
    @ConditionalOnMissingBean
    public LocalDateTimeSerializer localDateTimeSerializer() {
        return new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 注册 LocalDateTime 序列化规则。
     *
     * @param localDateTimeSerializer LocalDateTime 序列化器
     * @return Jackson 自定义器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer(LocalDateTimeSerializer localDateTimeSerializer) {
        return builder -> builder.serializerByType(LocalDateTime.class, localDateTimeSerializer);
    }
}
```

- [ ] **Step 3: 迁移全局异常处理器**

将 `GlobalExceptionHandler` 移动到 `family-common-web`，包名改为：

```java
package top.egon.familyaibutler.common.web.handler;
```

类级注释同步更新 `@BelongsPackage`。保留原逻辑：

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理系统异常。
     *
     * @param e 系统异常
     * @return 统一失败响应
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handlerException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.INVALID_PARAM.getCode(), e.getMessage(), ResultCode.INVALID_PARAM.getMessage());
    }
}
```

- [ ] **Step 4: 删除 common 中 Web 配置类**

删除：

```text
backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/configuration/JacksonConfig.java
backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/configuration/LocalDateTimeSerializerConfig.java
backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/handler/GlobalExceptionHandler.java
```

- [ ] **Step 5: 验证 Web 模块编译**

Run:

```bash
mvn -pl family-framework/family-common-web -am -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

---

## Task 12: 拆出 `family-common-mybatis`

**Files:**

- Move: `backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/extention/**`
- Move:
  `backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/handler/MybatisPlusMetaFieldHandler.java`
- Create:
  `backend/family-framework/family-common-mybatis/src/main/java/top/egon/familyaibutler/common/mybatis/autoconfigure/FamilyMybatisAutoConfiguration.java`
- Create:
  `backend/family-framework/family-common-mybatis/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Delete/Merge:
  `backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/configuration/MybatisPlusConfig.java`

- [ ] **Step 1: 创建 MyBatis 自动配置注册文件**

创建
`backend/family-framework/family-common-mybatis/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`：

```text
top.egon.familyaibutler.common.mybatis.autoconfigure.FamilyMybatisAutoConfiguration
```

- [ ] **Step 2: 移动 MyBatis 扩展类**

移动目录：

```text
backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/extention
```

到：

```text
backend/family-framework/family-common-mybatis/src/main/java/top/egon/familyaibutler/common/mybatis/extention
```

所有类包名从：

```java
top.egon.familyaibutler.common.extention
```

调整为：

```java
top.egon.familyaibutler.common.mybatis.extention
```

同步更新 `@BelongsPackage`。

- [ ] **Step 3: 移动自动填充处理器**

移动 `MybatisPlusMetaFieldHandler` 到：

```text
backend/family-framework/family-common-mybatis/src/main/java/top/egon/familyaibutler/common/mybatis/handler/MybatisPlusMetaFieldHandler.java
```

包名：

```java
package top.egon.familyaibutler.common.mybatis.handler;
```

- [ ] **Step 4: 创建 MyBatis 自动配置类**

创建
`backend/family-framework/family-common-mybatis/src/main/java/top/egon/familyaibutler/common/mybatis/autoconfigure/FamilyMybatisAutoConfiguration.java`：

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.mybatis.autoconfigure
 * @FileName: FamilyMybatisAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-12:30
 * @Description: MyBatis Plus 通用能力自动配置文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.common.mybatis.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import top.egon.familyaibutler.common.mybatis.extention.injector.BaseInjector;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.mybatis.autoconfigure
 * @ClassName: FamilyMybatisAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026-05-21 12:30
 * @Description: MyBatis Plus 通用能力自动配置
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass(MybatisPlusInterceptor.class)
public class FamilyMybatisAutoConfiguration {

    /**
     * 创建 MyBatis Plus 拦截器。
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /**
     * 创建防全表更新和删除拦截器。
     *
     * @return MybatisPlusInterceptor
     */
    @Bean
    @Profile({"dev", "test"})
    public MybatisPlusInterceptor blockAttackInnerInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

    /**
     * 创建自定义 SQL 注入器。
     *
     * @return BaseInjector
     */
    @Bean
    public BaseInjector injectBaseInjector() {
        return new BaseInjector();
    }
}
```

- [ ] **Step 5: 更新业务模块 MyBatis import**

将业务模块里旧 import：

```java
import top.egon.familyaibutler.common.extention.EgonMapper;
import top.egon.familyaibutler.common.extention.IEgonService;
import top.egon.familyaibutler.common.extention.IEgonServiceImpl;
```

改为：

```java
import top.egon.familyaibutler.common.mybatis.extention.EgonMapper;
import top.egon.familyaibutler.common.mybatis.extention.IEgonService;
import top.egon.familyaibutler.common.mybatis.extention.IEgonServiceImpl;
```

- [ ] **Step 6: 删除 common 中 MyBatis 配置类**

删除：

```text
backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/configuration/MybatisPlusConfig.java
backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/handler/MybatisPlusMetaFieldHandler.java
backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/extention
```

- [ ] **Step 7: 验证 MyBatis 模块和 core 编译**

Run:

```bash
mvn -pl family-framework/family-common-mybatis,family-core -am -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

---

## Task 13: 拆出 `family-common-security`

**Files:**

- Move: `backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/security/**`
- Create:
  `backend/family-framework/family-common-security/src/main/java/top/egon/familyaibutler/common/security/autoconfigure/FamilySecurityAutoConfiguration.java`
- Create:
  `backend/family-framework/family-common-security/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Modify: gateway/uaa imports if package is changed.

- [ ] **Step 1: 创建 Security 自动配置注册文件**

创建
`backend/family-framework/family-common-security/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`：

```text
top.egon.familyaibutler.common.security.autoconfigure.FamilySecurityAutoConfiguration
```

- [ ] **Step 2: 移动 security 包**

移动：

```text
backend/family-framework/family-common/src/main/java/top/egon/familyaibutler/common/security
```

到：

```text
backend/family-framework/family-common-security/src/main/java/top/egon/familyaibutler/common/security
```

说明：安全包名保持 `top.egon.familyaibutler.common.security` 不变，减少 gateway/uaa import 改动。

- [ ] **Step 3: 创建 Security 自动配置类**

创建
`backend/family-framework/family-common-security/src/main/java/top/egon/familyaibutler/common/security/autoconfigure/FamilySecurityAutoConfiguration.java`：

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.security.autoconfigure
 * @FileName: FamilySecurityAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-12:30
 * @Description: 安全通用能力自动配置文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.common.security.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtProperties;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtService;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.security.autoconfigure
 * @ClassName: FamilySecurityAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026-05-21 12:30
 * @Description: 安全通用能力自动配置
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass(FamilyJwtService.class)
@EnableConfigurationProperties(FamilyJwtProperties.class)
public class FamilySecurityAutoConfiguration {

    /**
     * 创建统一 JWT 服务。
     *
     * @param properties JWT 配置
     * @return 统一 JWT 服务
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "family.security.jwt", name = "enabled", havingValue = "true")
    public FamilyJwtService familyJwtService(FamilyJwtProperties properties) {
        return new FamilyJwtService(properties);
    }
}
```

- [ ] **Step 4: 将 FamilyJwtProperties 改为纯配置属性类**

从 `FamilyJwtProperties` 移除：

```java
@Configuration
```

保留：

```java
@ConfigurationProperties(prefix = "family.security.jwt")
```

原因：属性类由 `FamilySecurityAutoConfiguration` 通过 `@EnableConfigurationProperties` 注册，避免被业务模块 ComponentScan
意外注册。

- [ ] **Step 5: 将 FamilyJwtService 改为普通类**

从 `FamilyJwtService` 移除：

```java
@Component
@ConditionalOnProperty(prefix = "family.security.jwt", name = "enabled", havingValue = "true")
```

服务 Bean 由 `FamilySecurityAutoConfiguration` 控制。

- [ ] **Step 6: 验证 security 测试迁移**

把现有测试：

```text
backend/family-framework/family-common/src/test/java/top/egon/familyaibutler/common/security/jwt/FamilyJwtServiceTest.java
```

移动到：

```text
backend/family-framework/family-common-security/src/test/java/top/egon/familyaibutler/common/security/jwt/FamilyJwtServiceTest.java
```

Run:

```bash
mvn -pl family-framework/family-common-security test
```

Expected:

```text
BUILD SUCCESS
```

---

## Task 14: 收敛 `family-common` 为纯 common

**Files:**

- Modify: `backend/family-framework/family-common/pom.xml`
- Review: `backend/family-framework/family-common/src/main/java`

- [ ] **Step 1: common 只保留纯公共代码**

`family-common` 保留目录：

```text
enums
exception
pojo
utils
```

确认不存在：

```text
aspect
configuration
extention
handler
security
```

- [ ] **Step 2: 收敛 common POM 依赖**

`backend/family-framework/family-common/pom.xml` 依赖收敛到：

```xml
<dependencies>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-common</artifactId>
        <version>${springdoc.version}</version>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

如果 `springdoc-openapi-starter-common` 在当前依赖源不可用，则暂时保留 `io.swagger.core.v3:swagger-annotations`，不要为了
Schema 注解继续引入 WebMVC UI。

- [ ] **Step 3: 验证 common 不再拖入 Web/JPA/MyBatis/JWT**

Run:

```bash
mvn -pl family-framework/family-common dependency:tree -Dincludes=org.springframework.boot:spring-boot-starter-web,org.springframework.boot:spring-boot-starter-data-jpa,com.baomidou:mybatis-plus-spring-boot3-starter,io.jsonwebtoken:jjwt-api
```

Expected:

```text
输出中不应出现 family-common 直接依赖上述重依赖。
```

---

## Task 15: 按需更新业务模块依赖

**Files:**

- Modify: `backend/family-core/pom.xml`
- Modify: `backend/family-uaa/pom.xml`
- Modify: `backend/family-gateway/pom.xml`
- Modify: `backend/family-ai/ai-common/pom.xml`
- Modify: `backend/family-ai/qwen-ai/pom.xml`

- [ ] **Step 1: core 引入所需二阶段模块**

`family-core` 应依赖：

```xml
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common</artifactId>
</dependency>
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common-web</artifactId>
</dependency>
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common-mybatis</artifactId>
</dependency>
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-log</artifactId>
</dependency>
```

默认不引入 `family-common-security`，除非 core 开始直接解析 JWT。

- [ ] **Step 2: uaa 引入所需二阶段模块**

`family-uaa` 应依赖：

```xml
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common</artifactId>
</dependency>
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common-web</artifactId>
</dependency>
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common-mybatis</artifactId>
</dependency>
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common-security</artifactId>
</dependency>
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-log</artifactId>
</dependency>
```

- [ ] **Step 3: gateway 只引入纯 common 和 security**

`family-gateway` 应依赖：

```xml
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common</artifactId>
</dependency>
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common-security</artifactId>
</dependency>
```

并删除 `family-common` 依赖下针对 Web/JPA/MyBatis/PostgreSQL 的 exclusions。完成后 gateway 的 classpath 不应再因为 common
拉入这些依赖。

- [ ] **Step 4: ai 模块只引入必要依赖**

`family-ai/ai-common` 默认保留：

```xml
<dependency>
    <groupId>top.egon</groupId>
    <artifactId>family-common</artifactId>
</dependency>
```

`qwen-ai` 如需 Web 返回对象或全局异常处理，再显式引入 `family-common-web`；否则不引入。

- [ ] **Step 5: 编译所有业务模块**

Run:

```bash
mvn -pl family-core,family-uaa,family-gateway,family-ai/qwen-ai -am -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

---

## Task 16: 二阶段验证和启动

**Files:**

- No source file changes in this task.

- [ ] **Step 1: 跑 framework 模块测试**

Run:

```bash
mvn -pl family-framework -am test
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 2: 后端全量编译**

Run:

```bash
mvn -DskipTests compile
```

Expected:

```text
BUILD SUCCESS
```

- [ ] **Step 3: 本地 JDK 重启全部后端**

Run:

```bash
./ops/scripts/backend-local.sh all dev restart
```

Expected:

```text
family-core dev health check passed
family-uaa dev health check passed
family-ai-qwen dev health check passed
family-gateway dev health check passed
```

不要启动 Docker。

- [ ] **Step 4: 验证 gateway 不再依赖 WebMVC/JPA/MyBatis**

Run:

```bash
mvn -pl family-gateway dependency:tree -Dincludes=org.springframework.boot:spring-boot-starter-web,org.springframework.boot:spring-boot-starter-data-jpa,com.baomidou:mybatis-plus-spring-boot3-starter,org.postgresql:postgresql
```

Expected:

```text
输出中不应出现上述依赖由 family-common 引入。
```

- [ ] **Step 5: 检查 gateway 日志**

Run:

```bash
tail -n 120 ops/.runtime/family-gateway-dev.log
```

Expected:

```text
Started FamilyGateway
```

不应出现：

```text
NoClassDefFoundError: jakarta/servlet
```

---

## 风险点与处理

1. **gateway classpath 风险**  
   gateway 是 WebFlux，不能引入 `family-log`。如果后续必须给 gateway 做日志，应单独做 WebFlux 版本日志模块，例如
   `family-log-webflux`。

2. **common 依赖仍偏重**  
   第一阶段保留 Web/JPA/MyBatis/JWT 混合依赖，是为了降低改动面。第二阶段通过 `family-common-web`、`family-common-mybatis`、
   `family-common-security` 解决。

3. **ComponentScan 风险**  
   core/qwen-ai 当前扫描 `top.egon.familyaibutler.common`。第一阶段只移走日志切面；第二阶段应把 Web、MyBatis、Security 的
   Bean 全部迁到自动配置模块，`family-common` 中不再保留 `@Component`、`@Configuration`、`@RestControllerAdvice`。

4. **二阶段包名选择风险**  
   `family-common-security` 保持 `top.egon.familyaibutler.common.security` 包名不变，减少 gateway/uaa 改动；
   `family-common-mybatis` 建议改到 `top.egon.familyaibutler.common.mybatis`，因为业务代码 import 较少且语义更清晰。

5. **Git 工作区风险**  
   当前项目已有较多未提交改动。执行时只移动/修改本计划列出的文件，不回滚其他改动。

---

## 确认后执行顺序

推荐按下面顺序执行，每个阶段都可独立验证：

阶段一：

1. Task 1 + Task 2：先让 Maven 模块目录迁移成立。
2. Task 3 + Task 4：抽出 `family-log` 并收敛 common 依赖。
3. Task 5 + Task 6：调整业务模块依赖和路径引用。
4. Task 7：补最小测试。
5. Task 8 + Task 9：全量编译、脚本启动、自检。

阶段二：

1. Task 10：先建三个二阶段模块和 POM 管理。
2. Task 11：拆 `family-common-web`。
3. Task 12：拆 `family-common-mybatis`。
4. Task 13：拆 `family-common-security`。
5. Task 14 + Task 15：收敛 `family-common` 并调整业务模块依赖。
6. Task 16：跑 framework 测试、全量编译和本地 JDK 启动验证。

用户确认后再开始编码执行。
