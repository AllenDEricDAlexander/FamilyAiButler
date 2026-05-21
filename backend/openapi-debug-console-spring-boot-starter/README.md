# OpenAPI Debug Console Spring Boot Starter

一个通用的 Spring Boot Starter，用于在业务服务生成 OpenAPI JSON，并在 Spring Cloud Gateway / Spring WebFlux 中聚合为内部接口调试控制台。

## 功能边界

- 业务模块：通过 Springdoc 生成 `/v3/api-docs`，默认补充 OpenAPI `Info` 和 `Authorization` 安全描述。
- 网关模块：聚合多个业务模块的 OpenAPI JSON，提供登录、接口浏览、自动测试数据、代理调试、轻量压力测试、Markdown/PDF 导出。
- 服务发现：网关侧可使用 `http://service-name` 形式配置文档和调试地址，Starter 会在存在 `ReactiveDiscoveryClient` 或
  `DiscoveryClient` 时解析到实际服务实例。
- 状态管理：账号、密码、服务列表、环境开关、签名、导出、压测均来自配置文件，不落库。
- 控制台交互：登录使用一次性 challenge + HMAC proof，前端不提交明文密码；登录后的控制台 API 请求默认带 HMAC 签名。
- 环境保护：`mode=auto` 时，`prod` 默认只读，其他环境默认可调试。
- 默认保护：控制台默认关闭，业务模块只生成 OpenAPI JSON，不会暴露调试页面；只有显式配置 `egon.openapi.console.enabled=true`
  的网关模块才开放页面。
- 导出策略：Markdown/PDF 由请求实时生成并直接返回，不在服务端留存。

## 业务模块配置

业务模块只负责生产 OpenAPI JSON，不需要开放调试页面。

```yaml
egon:
  openapi:
    console:
      producer:
        enabled: true
        title: Demo Service API
        description: Demo service interfaces
        version: v1
        access-control:
          enabled: true
          token: <internal-openapi-doc-token>
```

## 网关模块配置

网关模块负责聚合服务、登录控制台、代理调试请求和导出文档。账号密码等敏感配置建议放在 `application-dev.yml`、Nacos
对应环境配置或部署平台密钥中，不要放在公共 `application.yml`。

```yaml
egon:
  openapi:
    console:
      enabled: true
      mode: auto
      base-path: /openapi-console
      title: Demo API Debug Console
      environment: dev
      auth:
        username: admin
        password: "{sha256}<password-sha256-hex>"
        session-secret: <high-entropy-session-secret>
        secure-cookie: false
        ttl: 8h
        challenge-ttl: 2m
        request-signature-ttl: 5m
        request-signing-enabled: true
      producer:
        enabled: false
      client:
        engine: VIRTUAL_THREAD
      services:
        - id: demo-service
          name: Demo Service
          group: demo
          open-api-url: http://demo-service/v3/api-docs
          open-api-access-header: X-OpenAPI-Console-Token
          open-api-access-token: <internal-openapi-doc-token>
          base-url: http://demo-service
          order: 10
```

默认页面地址：`/openapi-console/index.html`。

## 密码配置

`egon.openapi.console.auth.password` 支持三种格式：

```yaml
egon:
  openapi:
    console:
      auth:
        # 推荐：保存密码 SHA-256 摘要，服务端不会反解密码
        password: "{sha256}<password-sha256-hex>"

        # 仅建议本地或 dev 临时使用
        # password: "{plain}<raw-password>"

        # 兼容旧配置，未加前缀时按明文处理
        # password: "<raw-password>"
```

生成 SHA-256 摘要时不要带换行。macOS / Linux 示例：

```bash
printf '%s' 'OpenApiDev@2026' | shasum -a 256
```

然后填写：

```yaml
password: "{sha256}<上面命令输出的第一段 hex>"
```

当前实现不会也不应该“解密 password”。`{sha256}` 是单向摘要，登录校验流程是：

1. Spring Boot 通过 `@ConfigurationProperties(prefix = "egon.openapi.console")` 把 yml / Nacos 配置绑定到
   `ApiDocConsoleProperties.Auth.password`。
2. 前端请求 `/login-challenge` 获取一次性 `challengeId` 和 `nonce`。
3. 前端只在浏览器内使用用户输入的明文密码计算 `SHA-256(password)`，再用这个摘要对 challenge 载荷做 HMAC proof。
4. 后端在 `ApiDocConsoleSessionService#configuredPasswordDigest()` 中读取配置：
    - `{sha256}`：把配置中的 hex 摘要转成字节。
    - `{plain}`：先对配置明文做 SHA-256 摘要。
    - 无前缀：兼容旧明文配置，也先做 SHA-256 摘要。
5. 后端使用配置得到的密码摘要重新计算 HMAC proof，并与前端提交的 proof 做常量时间比较。

因此，配置文件里填 `{sha256}` 后，服务端不需要明文密码，也没有解密步骤。

## 控制台请求签名

登录成功后，控制台会生成一段本次会话使用的请求签名密钥。后续控制台自身 API 都会带以下请求头：

```http
X-OpenAPI-Console-Timestamp: <timestamp>
X-OpenAPI-Console-Nonce: <nonce>
X-OpenAPI-Console-Signature: <hmac-sha256>
```

后端校验签名、时间窗口和 nonce，降低控制台 API 被重放或伪造调用的风险。会话 Cookie 使用 `session-secret` 派生 AES-GCM
密钥加密载荷，再做 HMAC 完整性保护。`session-secret` 必须使用高强度随机值，且生产环境不要使用示例值。

控制台签名只保护控制台前后端交互。点击“发送请求”或执行压测时，Starter 代理到具体业务接口的请求不会自动添加这套控制台签名；业务接口自己的鉴权仍由请求
Headers、Bearer Token、Basic Auth、API Key 等调试参数决定。

## OpenAPI 文档内部 Token

`open-api-access-header` / `open-api-access-token` 是服务端拉取 `/v3/api-docs` 时使用的内部访问凭据，只配置在 gateway /
Nacos / yml 中，不会下发给前端页面，也不需要在浏览器页面中录入。前端访问 `/openapi-console/api/openapi/{serviceId}`
时只携带控制台登录会话和控制台 API 签名，Starter 后端会在聚合 OpenAPI JSON 时自动把内部 header 注入到请求里。

业务接口调试所需的 JWT、Basic Auth 或 API Key 仍然在页面的 `Auth` 页签配置，这类认证会进入“发送请求”和“压力测试”的业务请求，不用于读取
OpenAPI 文档 JSON。

## 关键配置项

| 配置项                                 | 默认值                       | 说明                                             |
|-------------------------------------|---------------------------|------------------------------------------------|
| `enabled`                           | `false`                   | 是否开放控制台页面                                      |
| `mode`                              | `AUTO`                    | `AUTO` / `OFF` / `READ_ONLY` / `FULL`          |
| `base-path`                         | `/openapi-console`        | 控制台访问路径                                        |
| `auth.username`                     | `admin`                   | 控制台登录用户名                                       |
| `auth.password`                     | `OpenApi@123456`          | 控制台登录密码配置，开启控制台时禁止使用默认值                        |
| `auth.session-secret`               | 示例值                       | 会话签名和加密密钥，开启控制台时必须替换                           |
| `auth.ttl`                          | `8h`                      | 登录会话有效期                                        |
| `auth.challenge-ttl`                | `2m`                      | 登录 challenge 有效期                               |
| `auth.request-signature-ttl`        | `5m`                      | 控制台 API 请求签名时间窗口                               |
| `auth.request-signing-enabled`      | `true`                    | 是否校验控制台 API 请求签名                               |
| `client.engine`                     | `AUTO`                    | HTTP 客户端引擎，`AUTO` 默认选择虚拟线程客户端；可显式设置 `REACTIVE` |
| `services[].open-api-access-header` | `X-OpenAPI-Console-Token` | 后端聚合 OpenAPI JSON 时注入的内部 header 名称             |
| `services[].open-api-access-token`  | 空                         | 后端聚合 OpenAPI JSON 时注入的内部 token                 |
| `load-test.max-requests`            | `50`                      | 单次压测最大请求数                                      |
| `load-test.max-concurrency`         | `5`                       | 单次压测最大并发数                                      |
| `load-test.max-active-runs`         | `2`                       | 当前 JVM 同时运行的压测任务数                              |
| `load-test.max-active-concurrency`  | `50`                      | 当前 JVM 压测全局并发上限                                |
| `export.enabled`                    | `true`                    | 是否允许导出文档                                       |

## 环境建议

- dev：可以 `mode=auto` 或 `FULL`，可临时使用 `{plain}`，但建议尽早改成 `{sha256}`。
- test/staging：建议使用 `{sha256}`，开启控制台 API 签名。
- prod：建议 `mode=OFF`；如必须开放，至少使用 `READ_ONLY`、HTTPS、强密码、强 `session-secret`、`secure-cookie=true`。
