# OpenAPI Debug Console 渗透测试报告

测试日期：2026-05-19
测试目标：FamilyAiButler OpenAPI Debug Console、网关聚合入口、各业务模块 OpenAPI JSON 暴露面
测试环境：本机开发环境，`family-gateway` 端口 `9527`，`family-core` 端口 `39090`，`family-uaa` 端口 `39092`，`family-ai-qwen`
端口 `39091`
测试方式：黑盒 HTTP 验证 + 白盒源码复核。未执行破坏性操作，未进行密码爆破，未进行高并发压测。

## 一、结论摘要

OpenAPI Debug Console 自身的核心接口已经具备基础登录保护：未登录访问服务目录、OpenAPI 聚合接口、接口调试接口均返回 `401`。会话
Cookie 具备 `HttpOnly`、`SameSite=Lax`，控制台响应也补充了 CSP、`X-Frame-Options`、`X-Content-Type-Options` 等基础安全响应头。

当前主要风险不在 console 登录接口本身，而在业务模块原生 `/v3/api-docs` 暴露面。攻击者不需要登录 console，也可以直接通过网关路由或服务端口访问
OpenAPI JSON，拿到接口路径、方法、参数、模型结构、鉴权 Header 名称和部分业务语义。这会绕过“调试页面需要账号密码登录”的预期。

## 二、风险总览

| 编号   | 风险等级 | 状态  | 问题                                    |
|------|------|-----|---------------------------------------|
| F-01 | 高    | 已确认 | `/v3/api-docs` 可绕过 console 登录直接访问     |
| F-02 | 中    | 已确认 | console 使用默认账号、默认密码和默认会话签名密钥          |
| F-03 | 中    | 已确认 | console 登录接口缺少限流、锁定和审计                |
| F-04 | 中    | 已确认 | FULL 模式下 console 是受认证保护的内部接口代理和轻量压测入口 |
| F-05 | 低    | 已确认 | 当前 Cookie 未启用 `Secure` 属性             |
| F-06 | 低    | 已确认 | console 静态页面可未登录访问，虽然核心 API 已鉴权       |
| I-01 | 信息   | 已确认 | 非网关模块 console 静态入口未公开，但不同模块返回行为不一致    |

## 三、已验证的正向控制

1. 未登录访问 `GET /openapi-console/api/catalog` 返回 `401`。
2. 未登录访问 `GET /openapi-console/api/openapi/family-uaa` 返回 `401`。
3. 未登录访问 `POST /openapi-console/api/execute` 返回 `401`。
4. 错误账号密码登录返回 `401`。
5. 正确登录后，服务目录和聚合 OpenAPI JSON 可访问。
6. `ApiDocConsoleController` 对 `catalog`、`openapi`、`execute`、`load-test`、`export` 均做了 session 校验。
7. `ApiDocConsoleSessionService` 使用 HMAC-SHA256 签名会话 Cookie，并校验过期时间。
8. `ApiDocConsoleSecurityWebFilter` 已设置 CSP、`X-Frame-Options: DENY`、`X-Content-Type-Options: nosniff`、
   `Referrer-Policy: no-referrer`。
9. 调试代理请求只允许访问配置中的 `serviceId`，没有发现任意 Host 的直接 SSRF 入口。
10. 调试代理屏蔽了 `Host`、`Content-Length`、`Transfer-Encoding` 请求头，并对响应体有大小上限。
11. 轻量压测有 `max-requests` 和 `max-concurrency` 上限配置。

## 四、发现详情

### F-01 `/v3/api-docs` 可绕过 console 登录直接访问

风险等级：高
影响范围：`family-gateway`、`family-core`、`family-uaa`、`family-ai-qwen`

#### 验证结果

未登录情况下可以直接访问以下地址：

| 地址                                       | 结果                            |
|------------------------------------------|-------------------------------|
| `http://127.0.0.1:9527/uaa/v3/api-docs`  | `200`，返回 UAA OpenAPI JSON     |
| `http://127.0.0.1:9527/base/v3/api-docs` | `200`，返回 Core OpenAPI JSON    |
| `http://127.0.0.1:9527/ai/v3/api-docs`   | `200`，返回 Qwen AI OpenAPI JSON |
| `http://127.0.0.1:39092/v3/api-docs`     | `200`，返回 UAA OpenAPI JSON     |
| `http://127.0.0.1:39090/v3/api-docs`     | `200`，返回 Core OpenAPI JSON    |
| `http://127.0.0.1:39091/v3/api-docs`     | `200`，返回 Qwen AI OpenAPI JSON |

OpenAPI JSON 中包含接口路径、HTTP 方法、参数、请求体 schema、响应 schema、鉴权 Header 名称和服务说明。例如 UAA 文档会暴露
`/user/login`、`/user/getList`、`/role`、`/permission` 等接口结构。

#### 根因定位

1. `family-uaa` 的 Spring Security 白名单包含 `/v3/api-docs/**`：
   `backend/family-uaa/src/main/java/top/egon/familyaibutler/uaa/configuration/SecurityConfig.java:73-88`。
2. 业务模块配置中 `springdoc.api-docs.enabled=true`，并固定开放 `/v3/api-docs`：
   `backend/family-core/src/main/resources/application.yml:94-107`、
   `backend/family-uaa/src/main/resources/application.yml:79-92`、
   `backend/family-ai/qwen-ai/src/main/resources/application.yml:86-99`。
3. 网关 Nacos 配置和本地配置都把 `/v3/api-docs` 放入 JWT 白名单：
   `backend/family-gateway/src/main/resources/backup/dev.txt:68-80`、
   `backend/family-gateway/src/main/resources/application.yml:20-32`。
4. 网关 `JwtTokenFilter` 当前没有注册为 Spring Bean，类上的 `@Component` 被注释：
   `backend/family-gateway/src/main/java/top/egon/familyaibutler/gateway/filter/JwtTokenFilter.java:32-35`
   。即使后续启用，当前白名单仍会放行 `/v3/api-docs`。
5. 网关路由会将 `/base/**`、`/uaa/**`、`/ai/**` 转发到对应服务，并 `StripPrefix=1`，所以 `/{route}/v3/api-docs` 会变成后端服务的
   `/v3/api-docs`：`backend/family-gateway/src/main/resources/backup/dev.txt:43-67`。

#### 影响

该问题绕过了 console 登录保护。攻击者只要能访问网关或业务服务端口，就可以直接获取接口地图，用于后续未授权访问尝试、参数构造、敏感接口枚举和自动化扫描。对生产环境来说，这类接口地图泄露通常属于高风险信息泄露。

#### 修复建议

1. 从网关 JWT 白名单删除 `/v3/api-docs`。生产环境不要通过 `/{service}/v3/api-docs` 公开 OpenAPI JSON。
2. 启用或重建网关认证过滤链，确保除登录、注册、健康检查、console 静态资源等明确白名单外的请求都必须鉴权。
3. 生产环境设置 `springdoc.api-docs.enabled=false`，或至少只允许内网、VPN、固定管理网段访问。
4. 业务服务端口不要直接暴露到公网或办公网，只允许网关、Nacos 内部网络访问。
5. console 聚合 OpenAPI JSON 应走服务端内部 `WebClient` 拉取，浏览器侧不应能直接访问业务模块 `/v3/api-docs`。
6. 如果开发环境需要保留 Swagger/OpenAPI 原生入口，建议通过 profile 区分：`dev=true`，`test/prod=false` 或内网限流加鉴权。

### F-02 console 使用默认账号、默认密码和默认会话签名密钥

风险等级：中
影响范围：`family-gateway` OpenAPI Debug Console

#### 验证结果

当前配置可使用默认账号密码登录 console。登录成功后会发放 `OPENAPI_DEBUG_CONSOLE_SESSION` Cookie。报告中不记录原始 Cookie
值。

相关配置：

1. `backend/family-gateway/src/main/resources/backup/dev.txt:90-95` 配置了 `username`、`password`、`session-secret`。
2. `backend/family-gateway/src/main/resources/application.yml:42-47` 也有相同默认值。
3. starter 默认值中也存在默认账号密码和默认密钥：
   `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/ApiDocConsoleProperties.java:86-103`。

#### 影响

如果共享开发环境、测试环境或生产环境沿用默认值，攻击者可以直接登录 console。登录后可导出接口文档，在 FULL 模式下还可以通过
console 代理调用内部服务，并执行轻量压测。

#### 修复建议

1. `enabled=true` 时启动阶段校验：禁止使用 starter 默认密码和默认 `session-secret`。
2. 配置密码优先使用 `{sha256}` 格式，不使用明文默认值。
3. `session-secret` 使用至少 32 字节随机值，并按环境隔离。
4. 示例配置中保留占位说明，不放可直接使用的默认密码。

### F-03 console 登录接口缺少限流、锁定和审计

风险等级：中
影响范围：`POST /openapi-console/api/login`

#### 验证结果

`ApiDocConsoleController.login` 只校验账号密码，失败直接返回 `401`：
`backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/ApiDocConsoleController.java:59-75`
。未看到针对登录失败次数、来源 IP、用户名维度的限流或短期锁定逻辑。

#### 影响

在默认账号存在的情况下，登录接口没有限流会放大弱口令和默认口令风险。即使生产环境关闭 console，测试环境或共享开发环境仍可能成为入口。

#### 修复建议

1. 增加内存级限流：按 `IP + username` 统计失败次数，超过阈值短期锁定。
2. 在网关层为 `/openapi-console/api/login` 单独配置 Redis RateLimiter。
3. 对登录成功、失败、锁定、登出记录审计日志，但不要记录密码和 session token。
4. 默认失败响应保持统一，避免区分用户名不存在和密码错误。

### F-04 FULL 模式下 console 是受认证保护的内部接口代理和轻量压测入口

风险等级：中
影响范围：`POST /openapi-console/api/execute`、`POST /openapi-console/api/load-test`

#### 验证结果

`execute` 会根据配置中的 `serviceId` 和 `baseUrl` 拼接目标请求，并把用户输入的路径、Query、Header、Body 代理到后端服务：
`backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/ApiDocConsoleService.java:127-147`、
`266-275`、`319-331`。

`load-test` 会重复执行同一个调试请求，并受 `maxRequests`、`maxConcurrency` 限制：
`backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/ApiDocConsoleService.java:155-164`
。当前 dev 配置为 `max-requests: 200`、`max-concurrency: 20`：
`backend/family-gateway/src/main/resources/backup/dev.txt:103-106`。

#### 影响

这不是未授权漏洞，因为 controller 已校验 session，且 READ_ONLY 模式会禁止执行。但如果 console
凭据泄露或默认密码被猜中，攻击者可以利用该入口调用内部服务接口，或对业务服务做轻量 DoS。

#### 修复建议

1. 生产环境默认 `mode=off`，确需开放时只允许 `READ_ONLY`。
2. 共享开发环境建议降低压测上限，例如 `max-requests=50`、`max-concurrency=5`。
3. 为每个服务增加可选的 path allowlist/denylist，禁止通过 console 调用敏感管理接口。
4. 对 `execute` 和 `load-test` 增加审计日志，记录用户、服务、方法、路径、状态码、耗时，不记录敏感 Header 和 Body。
5. 对压测接口增加二次确认或单独开关，默认关闭。

### F-05 当前 Cookie 未启用 `Secure` 属性

风险等级：低
影响范围：`OPENAPI_DEBUG_CONSOLE_SESSION`

#### 验证结果

登录成功返回的 Set-Cookie 包含 `HttpOnly`、`SameSite=Lax`、`Path=/openapi-console`，但当前 dev 配置 `secure-cookie=false`
，所以没有 `Secure` 属性。代码位置：
`backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/ApiDocConsoleSessionService.java:106-115`。

#### 影响

本机 HTTP 开发环境可以接受。但如果测试环境或生产环境通过 HTTP 暴露，session cookie 会走明文链路。

#### 修复建议

1. HTTPS 环境设置 `secure-cookie=true`。
2. 当 `environment=prod` 或 active profile 包含 `prod` 时，自动强制 `secure-cookie=true`。
3. 生产环境配合 HTTPS、HSTS、反向代理安全头统一管理。

### F-06 console 静态页面可未登录访问

风险等级：低
影响范围：`GET /openapi-console/index.html`

#### 验证结果

未登录访问 `http://127.0.0.1:9527/openapi-console/index.html` 返回 `200`。核心数据接口仍需要登录，未登录访问会返回 `401`。

#### 影响

攻击者可以知道 console 存在、标题、前端资源结构和 API 路径。该问题本身不直接导致接口调试能力暴露，但会为后续弱口令尝试提供入口信息。

#### 修复建议

1. 开发环境可以保留当前行为。
2. 更严格的做法是未登录只返回最小登录页，不暴露完整前端调试资源。
3. 生产环境仍建议 `mode=off` 或 `enabled=false`。

### I-01 非网关模块 console 静态入口未公开，但返回行为不一致

风险等级：信息
影响范围：`family-core`、`family-uaa`

#### 验证结果

未登录访问 `family-core` 的 `/openapi-console/index.html` 返回 `404`，访问 `family-uaa` 的 `/openapi-console/index.html`
返回 `401`。这说明非网关模块没有公开可用 console 页面，但 UAA 会被 Spring Security 拦截并暴露该路径存在鉴权处理。

#### 修复建议

1. 生产者模块只引入 OpenAPI 生产能力，不打包或不映射 console 静态资源。
2. 对 `egon.openapi.console.enabled=false` 的模块，优先在 starter filter 层统一返回 `404`，避免不同模块暴露不一致行为。

## 五、修复优先级

### P0 立即处理

1. 生产和共享测试环境移除网关白名单 `/v3/api-docs`。
2. 生产环境关闭业务模块 `springdoc.api-docs.enabled`，或仅允许内网访问。
3. 替换 console 默认账号密码和默认 `session-secret`。

### P1 尽快处理

1. 为 `/openapi-console/api/login` 增加限流、锁定和审计。
2. 启用或重建网关 JWT 过滤链，确认 `JwtTokenFilter` 实际生效。
3. FULL 模式只允许开发环境使用，共享环境降低压测阈值。

### P2 持续加固

1. 增加 console 代理 path allowlist/denylist。
2. 生产 profile 自动强制 `secure-cookie=true`。
3. 统一非网关模块 disabled console 的返回行为。

## 六、建议的验收标准

整改后建议至少验证以下结果：

| 验收项                                                            | 期望结果                                |
|----------------------------------------------------------------|-------------------------------------|
| 未登录访问 `/openapi-console/api/catalog`                           | `401` 或生产环境 `404`                   |
| 未登录访问 `/openapi-console/api/openapi/{serviceId}`               | `401` 或生产环境 `404`                   |
| 未登录访问 `/uaa/v3/api-docs`、`/base/v3/api-docs`、`/ai/v3/api-docs` | 生产环境不可访问，返回 `401`、`403` 或 `404`     |
| 未登录直接访问业务服务 `/v3/api-docs`                                     | 生产环境不可访问，服务端口不应外部暴露                 |
| 使用 starter 默认账号密码登录                                            | 应失败或启动阶段直接拒绝启动                      |
| 连续错误登录                                                         | 应被限流或短期锁定                           |
| 生产环境登录 Set-Cookie                                              | 必须包含 `HttpOnly`、`Secure`、`SameSite` |
| 生产环境 `execute`、`load-test`                                     | 应关闭或只读禁止                            |

## 七、附录：本次验证命令摘要

以下命令仅在本机开发环境执行，未包含原始 session token。

```bash
curl -o /tmp/sec-catalog.out -w '%{http_code}\n' \
  http://127.0.0.1:9527/openapi-console/api/catalog

curl -o /tmp/sec-openapi.out -w '%{http_code}\n' \
  http://127.0.0.1:9527/openapi-console/api/openapi/family-uaa

curl -o /tmp/sec-execute.out -w '%{http_code}\n' \
  -H 'Content-Type: application/json' \
  -d '{"serviceId":"family-uaa","method":"GET","path":"/user/getList","headers":{},"query":{},"body":""}' \
  http://127.0.0.1:9527/openapi-console/api/execute

curl -D /tmp/sec-login.headers -c /tmp/sec-cookie.jar -o /tmp/sec-login-good.out -w '%{http_code}\n' \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"OpenApi@123456"}' \
  http://127.0.0.1:9527/openapi-console/api/login

curl -o /tmp/sec-gw-uaa-v3.out -w '%{http_code} %{size_download}\n' \
  http://127.0.0.1:9527/uaa/v3/api-docs

curl -o /tmp/sec-gw-core-v3.out -w '%{http_code} %{size_download}\n' \
  http://127.0.0.1:9527/base/v3/api-docs

curl -o /tmp/sec-gw-ai-v3.out -w '%{http_code} %{size_download}\n' \
  http://127.0.0.1:9527/ai/v3/api-docs

curl -o /tmp/sec-direct-uaa-v3.out -w '%{http_code} %{size_download}\n' \
  http://127.0.0.1:39092/v3/api-docs
```

## 八、本轮修复记录

修复日期：2026-05-19

1. 针对 F-01：starter 新增业务模块 OpenAPI JSON 内部访问控制过滤器，`/v3/api-docs` 默认可配置为需要
   `X-OpenAPI-Console-Token`；gateway 聚合服务端拉取 OpenAPI JSON 时会自动携带服务配置中的内部 token；gateway 新增原生
   OpenAPI 文档路径阻断过滤器，阻断 `/base/v3/api-docs`、`/uaa/v3/api-docs`、`/ai/v3/api-docs` 等外部绕过路径。
2. 针对 F-02：console 开启时新增默认密码和默认 `session-secret` 启动校验；gateway 本地配置和 Nacos 备份配置已替换为
   SHA-256 密码配置和非默认 session secret。
3. 针对 F-03：console 登录新增按客户端和用户名维度的失败计数与短期锁定能力。
4. 针对 F-04：调试代理新增服务级 path allowlist/denylist 配置能力；dev 配置中的轻量压测上限从 `200/20` 收紧为 `50/5`。
5. 针对 F-05：当 `environment=prod` 或 active profile 包含 `prod` 时，session Cookie 自动强制 `Secure`。
6. 已补充 `ApiDocConsoleSecurityTest` 安全回归测试，覆盖默认凭据拒绝、登录失败锁定、生产 Cookie Secure、OpenAPI JSON 内部
   token 过滤、gateway 聚合拉取内部 Header。
