# family-uaa P0 DDD 重构计划

> **执行要求:** 本计划作为 UAA 重构实施依据。每批 coding 开始前需要用户确认批次范围；执行时按任务顺序推进，每完成一个任务必须运行对应验证命令，并保持代码风格符合
`backend/code_style.md`。

## 0. 执行记录

### 2026-05-20 第一批已执行

本批按 Task 1 到 Task 5 落地了可编译、可测试的最小认证底座：

- 已建立 `backend/family-uaa` 聚合模块，包含 `uaa-facade` 和 `uaa-core`。
- 已将 `backend/pom.xml` 的 UAA reactor 入口调整为 `family-uaa`。
- 已建立 `uaa-facade` 稳定契约，包含账号、认证、Token、Profile facade 及 DTO。
- 已建立 `uaa-core` DDD/COLA 根结构：`adapter`、`application`、`domain`、`infrastructure`。
- 已建立 Account、Credential、Profile、Device、Session、TokenRecord、TokenClaims 等核心领域对象。
- 已建立账号注册、账号状态变更、注销申请/确认、密码登录、验证码登录、找回/重置密码、Token 校验/刷新/撤销、会话退出、设备移除等第一批用例入口。
- 已建立 `AccountGateway`、`CredentialGateway`、`ProfileGateway`、`DeviceGateway`、`SessionGateway`、`TokenGateway`、
  `SecurityNotificationGateway` 等领域端口。
- 已提供 `infrastructure.gatewayimpl` 下的内存实现，作为应用服务单元测试辅助实现。
- 已补充 facade 结构测试、core DDD 结构测试、账号领域测试、账号应用测试、认证 Token 应用测试。

### 2026-05-20 第一批补齐已执行

本批在第一批认证底座上继续补齐旧模块清理和 PostgreSQL 持久化：

- 已删除旧 UAA 单体实现，UAA reactor 入口统一收敛到新的 `backend/family-uaa` 聚合模块。
- 已为 `uaa-core` 补齐 `application.yml`、`application-dev.yml`、`application-prod.yml`、
  `db/migration/V1__uaa_p0_schema.sql` 和 `logback-spring.xml`。
- 已接入本机 PostgreSQL 配置，开发环境使用 `familyaibutler` 数据库，连接参数沿用配置文件中的环境变量默认值。
- 已建立 MyBatis Plus 持久化层：`dataobject`、`mapper`、`converter`、`gatewayimpl`。
- 已完成 Account、Credential、Profile、Device、AuthSession、RefreshToken、AccessToken、SecurityChallenge 的基础表结构和领域网关落库。
- RefreshToken 与 AccessToken 均按 SHA-256 哈希落库，不保存 Token 明文；持久化测试覆盖长 Access Token 写入和读回。
- 已将运行时领域网关切换为 MyBatis Plus 实现，内存实现不再作为 Spring Bean 注册。
- 已增加 `UaaPersistenceIntegrationTest`，覆盖账号、凭证、Profile、设备、会话、刷新 Token、访问 Token 声明的写入与读回。
- 已清理 Token 领域对象还原逻辑和 logback 过期滚动策略配置。

### 2026-05-20 第二批接入能力已执行

本批围绕“前端密码模式登录、UAA 签发 JWT、网关校验、资源服务再向 UAA 决策”的接入链路补齐最小闭环：

- 已在 `family-common` 增加 `FamilyJwtClaims` 和 `FamilySecurityHeaders`，JWT
  只携带账号、Profile、Client、Session、Device、版本和风险等级等身份声明，不携带用户权限或接入方权限。
- 已改造 `FamilyJwtService`，支持 UAA 身份声明签发和解析，保留原有通用 JWT 方法并继续使用
  `family.security.jwt.enabled=true` 作为 opt-in 开关。
- 已新增 OAuth Client 领域模型、网关端口、内存实现、MyBatis Plus PO/Mapper/Gateway，并在 PostgreSQL schema 中增加
  `uaa_oauth_client` 和默认 `family-web` 接入方。
- 已改造密码登录，先校验接入方是否合法且支持密码模式，再签发真实 JWT access token；refresh token 仍保持不透明随机串并按哈希落库。
- 已改造 Token 校验与刷新，访问令牌先做 JWT 签名/过期校验，再查 UAA 落库记录以支持撤销判断。
- 已新增 `AuthorizationFacade`、授权决策 DTO、`AuthorizationServiceImpl` 和 `/authorization/decide` 接口，资源访问决策会同时检查
  JWT、Token 记录和 OAuth Client 资源范围。
- 已改造 `family-gateway` JWT 过滤器，校验 JWT 后清除外部伪造身份头，并注入 `X-Family-*` 可信身份头给下游服务。
- 已更新 gateway UAA 登录/注册/找回密码白名单，OpenAPI Console 仍走自身认证配置，不纳入 UAA 业务授权。
- 已让 `family-core` 依赖 `uaa-facade`，增加 fail-closed 的 `FamilyResourceAuthorizationService`，并增加基于 Spring
  `RestClient` 的 `UaaRestAuthorizationFacade` 调用 UAA 授权决策接口。

已验证命令：

```text
mvn -pl family-uaa/uaa-facade test
mvn -pl family-uaa/uaa-core -am -Dtest=AccountDomainServiceTest,AccountServiceTest,AuthTokenServiceTest,UaaDddArchitectureTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl family-uaa/uaa-core -am -Dtest=AccountDomainServiceTest,AccountServiceTest,AuthTokenServiceTest,UaaDddArchitectureTest,UaaPersistenceIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl family-common -Dtest=FamilyJwtServiceTest test
mvn -pl family-uaa/uaa-core -am -Dtest=AuthTokenServiceTest,AuthorizationDecisionServiceTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl family-uaa/uaa-core -am -Dtest=AccountDomainServiceTest,AccountServiceTest,AuthTokenServiceTest,AuthorizationDecisionServiceTest,UaaDddArchitectureTest,UaaPersistenceIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl family-gateway -am -Dtest=JwtTokenFilterTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl family-core -am -Dtest=UaaRestAuthorizationFacadeTest,FamilyResourceAuthorizationServiceTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl family-core -am -Dtest=UaaRestAuthorizationFacadeTest,FamilyResourceAuthorizationServiceTest,FamilyCoreDddStructureTest -Dsurefire.failIfNoSpecifiedTests=false test
git diff --check
```

后续仍需在后续批次继续完成：

```text
真实短信 / 邮件通知网关
审计日志落库
设备列表 / 当前会话列表 / 管理员强制下线
OAuth2 Authorization Code + PKCE / OIDC Discovery / UserInfo / ID Token / Consent
完整 OAuth Client 管理能力：禁用、Secret 轮换、Redirect URI、Allowed Origins、审计日志
RBAC3 高阶能力：角色继承、互斥角色、职责分离、组织 / 项目 / 频道 / 租户 / 数据范围约束
组织 / 项目 / 开放平台
API Key / Service Account
套餐 / 权益 / 额度 / 内容访问判断
完整风控策略、风险事件、限流
用户自助中心和管理后台 API
SSO 接入 Demo、接入文档、错误码规范
```

### 2026-05-20 第三批动态授权能力已执行

本批围绕“微服务之间默认不可信、权限动态变化、页面/按钮/接口三级 RBAC”的目标继续补齐动态授权闭环：

- 已为 `FamilyJwtService` 增加 `RS256` 签发/验签能力，支持配置 RSA 私钥/公钥和 `kid`，并提供 JWK Set 导出能力；UAA 新增
  `/.well-known/jwks.json` 公钥发布端点。
- 已新增 `OAuthClientFacade`、OAuth Client 创建/查询/列表 DTO、`OAuthClientServiceImpl` 和 `/oauth-clients` 管理接口；Client
  Secret 按 SHA-256 哈希保存，不在响应中返回明文。
- 已新增 `RbacFacade`、RBAC 管理 DTO 和 `PAGE` / `BUTTON` / `API` 权限资源类型；UAA core 建立
  Role、PermissionResource、RoleResource、AccountRole 领域模型、应用服务、Web 接口、内存网关和 MyBatis Plus 持久化。
- 已在 PostgreSQL schema 中增加 `uaa_role`、`uaa_permission_resource`、`uaa_role_resource`、`uaa_account_role`，并在集成测试中覆盖
  RBAC 写入和按账号读回。
- 已将 `/authorization/decide` 决策链扩展为 JWT 验签、Token 落库记录、账号状态、authVersion、entitlementVersion、riskLevel、OAuth
  Client 资源范围、RBAC API 权限依次判断；JWT 仍不携带权限集合。
- 已将 `family-gateway` 改为 JWT 本地校验后调用 UAA 授权决策接口，UAA 拒绝时返回 403，不再只凭本地 JWT 合法性转发到微服务。
- 已新增 `backend/family-uaa/uaa-resource-server-spring-boot-starter`，提供资源服务通用 Servlet Filter、REST 授权客户端和自动配置；
  `family-core` 改为依赖该 starter，并通过配置指定 `service-name: family-core`。
- OpenAPI Console 相关接口仍保持独立认证边界，不纳入 UAA 业务模块鉴权。

已验证命令：

```text
mvn -pl family-common -Dtest=FamilyJwtServiceTest test
mvn -pl family-uaa/uaa-core -am -Dtest=JwkControllerTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl family-uaa/uaa-core -am -Dtest=OAuthClientServiceTest,RbacServiceTest,AuthorizationDecisionServiceTest,JwkControllerTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl family-gateway -am -Dtest=JwtTokenFilterTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl family-uaa/uaa-resource-server-spring-boot-starter -am -Dtest=UaaResourceAuthorizationFilterTest,RestUaaResourceAuthorizationClientTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl family-core -am -Dtest=FamilyCoreDddStructureTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl family-uaa/uaa-core -am -Dtest=UaaPersistenceIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false test
git diff --check
```

### 2026-05-20 gateway 模块分层适配已执行

本批在不适配 AI 模块的前提下，将 `family-gateway` 对齐到当前后端 DDD/code style：

- 已将 gateway 过滤器移动到 `adapter/filter`，全局异常处理器移动到 `adapter/handler`。
- 已新增 `GatewayAccessService` 和 `GatewayAccessDecision`，由应用层统一处理 Token 校验、资源解析和 UAA 授权决策。
- 已新增 `AuthorizationDecisionGateway`、`TokenGateway` 领域端口，避免过滤器直接依赖 UAA HTTP 实现或 JWT 基础设施。
- 已将 UAA 授权调用实现移动到 `infrastructure/gatewayimpl/UaaAuthorizationDecisionGatewayImpl`。
- 已将 JWT 适配实现移动到 `infrastructure/security/FamilyJwtTokenGatewayImpl`。
- 已将 gateway 配置移动到 `infrastructure/configuration`，清理旧 `config`、`filter`、`security`、`util`、`exception`、
  `handler` 根包。
- 已新增 `GatewayDddStructureTest`，防止 gateway 回退到旧横向包结构。
- AI 模块本批未做适配，后续已在 AI 模块改动完成后单独接入 UAA。

已验证命令：

```text
mvn -pl family-gateway -am -Dtest=GatewayDddStructureTest,JwtTokenFilterTest -Dsurefire.failIfNoSpecifiedTests=false test
```

### 2026-05-20 AI 模块 UAA 接入已执行

本批在 AI 模块当前改动完成后，将 `family-ai/qwen-ai` 接入 UAA 资源服务通用 starter：

- 已为 `qwen-ai` 增加 `uaa-resource-server-spring-boot-starter` 依赖，复用资源服务统一授权过滤器。
- 已在 `qwen-ai` 的 `application.yml` 增加 `family.uaa.resource-server` 配置，指定 `service-name: family-ai-qwen` 和 UAA
  授权服务地址。
- 已配置 AI 模块放行 `/actuator/**`、`/v3/api-docs/**`、`/swagger-ui/**`、`/openapi-console/**`，业务接口默认需要携带 JWT
  并调用 UAA 决策。
- 已在 `QwenDddStructureTest` 中增加 UAA starter 接入约束，防止后续移除依赖或配置。
- gateway 已有 `/ai/**` 到 `family-ai-qwen` 的路由和资源服务解析，AI 模块接入后可和 `family-core` 一样走“gateway 准入 +
  资源服务二次决策”闭环。

已验证命令：

```text
mvn -pl family-ai/qwen-ai -am -Dtest=QwenDddStructureTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn -pl family-ai/qwen-ai -am test
```

## 0.1 当前完成状态总览

| 范围                       | 状态      | 说明                                                                                                                      |
|--------------------------|---------|-------------------------------------------------------------------------------------------------------------------------|
| Maven 模块拆分               | 已完成     | `backend/family-uaa` 已包含 `uaa-facade`、`uaa-core`、`uaa-resource-server-spring-boot-starter`，旧 UAA 单体实现已删除。               |
| DDD/COLA 分层              | 已完成     | `uaa-core` 已按 `adapter`、`application`、`domain`、`infrastructure` 分层。                                                     |
| facade 稳定契约              | 已完成     | 已提供 Account、Auth、Token、Profile、Authorization、OAuthClient、Rbac facade 及 DTO / 枚举。                                        |
| PostgreSQL 持久化           | 已完成     | 账号、凭证、Profile、设备、会话、Token、OAuth Client、RBAC 基础表已落库，并有集成测试覆盖。                                                            |
| JWT / JWK                | 已完成     | JWT 只携带身份声明和版本号，支持 HS512 / RS256，UAA 暴露 `/.well-known/jwks.json`。                                                       |
| OAuth Client 基础管理        | 已完成基础能力 | 已支持创建、查询、列表、Secret 哈希、Grant / Scope / Resource Pattern / TTL；禁用、轮换、Redirect URI、Allowed Origins、审计仍未完成。                 |
| RBAC 页面 / 按钮 / 接口资源      | 已完成基础能力 | 已支持角色、资源、角色资源、账号角色和账号权限查询，API 权限进入动态授权决策；角色继承、互斥、职责分离、数据范围仍未完成。                                                         |
| 统一授权决策                   | 已完成基础闭环 | `/authorization/decide` 已校验 JWT、Token 落库、账号状态、版本、风险等级、OAuth Client 资源范围和 RBAC API 权限。                                   |
| gateway 微服务准入            | 已完成     | `family-gateway` 已本地验签并调用 UAA 决策，拒绝时返回 403，并向下游注入可信身份头；gateway 模块已按 adapter / application / domain / infrastructure 分层。 |
| 资源服务通用接入                 | 已完成基础能力 | 已新增 `uaa-resource-server-spring-boot-starter`，`family-core` 和 `family-ai-qwen` 已通过 starter 接入 UAA 授权决策。                 |
| OAuth2 / OIDC / SSO 标准协议 | 部分完成    | Token、Refresh、JWK 和网关接入已完成；Authorization Code + PKCE、Discovery、UserInfo、ID Token、Consent 仍未完成。                          |
| 权益 / 风控 / 审计             | 部分完成    | JWT 和决策链已有版本号、riskLevel 基础校验；完整权益模型、风控策略、风险事件、审计落库仍未完成。                                                                 |

## 1. 目标

将 `backend/family-uaa` 按 `backend/family-uaa/readme.md` 中 P0/MVP 要求重构为统一 UAA / CIAM / RBAC3 / OAuth2 / OIDC /
SSO 身份权限平台底座。

首期重构目标不是兼容旧 CRUD 代码，而是直接建立清晰的 DDD/COLA 分层、稳定 facade 模块、可扩展的领域模型和 P0 能力闭环：

```text
账号注册 / 登录 / 找回 / 注销
Access Token / Refresh Token / Token 撤销
Account + Profile
Device + Session
OAuth2 / OIDC / SSO
OAuth Client
RBAC3
组织 / 项目基础模型
API Key / Service Account
套餐 / 权益 / 额度
统一权限决策 / 权益判断 / 内容访问判断
基础风控
审计日志
用户自助中心接口
管理后台接口
业务系统 SSO 接入能力
资源服务 Token 校验和权限判断能力
```

## 2. 总体架构

### 2.1 Maven 模块拆分

```text
backend
├── family-uaa
│   ├── pom.xml
│   ├── uaa-facade
│   │   └── 对其他模块暴露的稳定契约，只包含接口、DTO、枚举、常量
│   ├── uaa-core
│   │   └── UAA 服务实现，按 DDD/COLA 分层承载业务实现
│   └── uaa-resource-server-spring-boot-starter
│       └── 资源服务通用接入 starter，封装 Token 解析、UAA 授权决策调用和 Servlet Filter
├── family-common
│   └── 保留跨模块通用能力，例如 Result、异常、JWT 底层签发工具
├── family-core
│   └── 作为首个资源服务通过 starter 接入 UAA 授权
└── family-gateway
    └── 作为统一入口，负责路由、网关鉴权和 SSO/OIDC 接入转发
```

### 2.2 依赖方向

```text
family-core       -> family-uaa/uaa-resource-server-spring-boot-starter
uaa-resource-server-spring-boot-starter -> family-uaa/uaa-facade
family-gateway    -> family-uaa/uaa-facade
family-uaa/uaa-core   -> family-uaa/uaa-facade
family-uaa/uaa-core   -> family-common
family-uaa/uaa-facade -> 不依赖 uaa-core / Spring Web / MyBatis / JPA
```

`uaa-facade` 必须保持轻量，不能出现以下依赖和对象：

```text
Controller
ServiceImpl
Mapper
Repository
PO / DO / Entity
HttpServletRequest
HttpServletResponse
Spring Security Authentication
MyBatis Page
JPA Entity
```

### 2.3 uaa-core 内部 DDD/COLA 结构

```text
backend/family-uaa/uaa-core
├── pom.xml
├── readme.md
└── src
    ├── main
    │   ├── java
    │   │   └── top/egon/familyaibutler/uaa
    │   │       ├── UserAuthenticationAuthorizationApplication.java
    │   │       ├── adapter
    │   │       │   ├── AccountController.java
    │   │       │   ├── AuthController.java
    │   │       │   ├── ProfileController.java
    │   │       │   ├── SessionController.java
    │   │       │   ├── DeviceController.java
    │   │       │   ├── OAuthClientController.java
    │   │       │   ├── OAuthAuthorizeController.java
    │   │       │   ├── OidcController.java
    │   │       │   ├── RbacController.java
    │   │       │   ├── OrganizationController.java
    │   │       │   ├── ProjectController.java
    │   │       │   ├── ApiKeyController.java
    │   │       │   ├── ServiceAccountController.java
    │   │       │   ├── EntitlementController.java
    │   │       │   ├── ContentAccessController.java
    │   │       │   ├── RiskController.java
    │   │       │   ├── AuditController.java
    │   │       │   ├── UserSelfCenterController.java
    │   │       │   ├── AdminConsoleController.java
    │   │       │   ├── assembler
    │   │       │   └── filter
    │   │       ├── application
    │   │       │   ├── AccountServiceI.java
    │   │       │   ├── AccountServiceImpl.java
    │   │       │   ├── AuthServiceI.java
    │   │       │   ├── AuthServiceImpl.java
    │   │       │   ├── TokenServiceI.java
    │   │       │   ├── TokenServiceImpl.java
    │   │       │   ├── ProfileServiceI.java
    │   │       │   ├── ProfileServiceImpl.java
    │   │       │   ├── SessionServiceI.java
    │   │       │   ├── SessionServiceImpl.java
    │   │       │   ├── DeviceServiceI.java
    │   │       │   ├── DeviceServiceImpl.java
    │   │       │   ├── OAuthClientServiceI.java
    │   │       │   ├── OAuthClientServiceImpl.java
    │   │       │   ├── OAuthAuthorizationServiceI.java
    │   │       │   ├── OAuthAuthorizationServiceImpl.java
    │   │       │   ├── RbacServiceI.java
    │   │       │   ├── RbacServiceImpl.java
    │   │       │   ├── OrganizationServiceI.java
    │   │       │   ├── OrganizationServiceImpl.java
    │   │       │   ├── ProjectServiceI.java
    │   │       │   ├── ProjectServiceImpl.java
    │   │       │   ├── OpenPlatformServiceI.java
    │   │       │   ├── OpenPlatformServiceImpl.java
    │   │       │   ├── EntitlementServiceI.java
    │   │       │   ├── EntitlementServiceImpl.java
    │   │       │   ├── AuthorizationDecisionServiceI.java
    │   │       │   ├── AuthorizationDecisionServiceImpl.java
    │   │       │   ├── RiskServiceI.java
    │   │       │   ├── RiskServiceImpl.java
    │   │       │   ├── AuditServiceI.java
    │   │       │   ├── AuditServiceImpl.java
    │   │       │   ├── dto
    │   │       │   └── executor
    │   │       │       ├── command
    │   │       │       └── query
    │   │       ├── domain
    │   │       │   ├── gateway
    │   │       │   ├── model
    │   │       │   │   ├── aggregate
    │   │       │   │   ├── entity
    │   │       │   │   ├── valueobject
    │   │       │   │   └── enums
    │   │       │   ├── service
    │   │       │   └── event
    │   │       └── infrastructure
    │   │           ├── configuration
    │   │           ├── security
    │   │           ├── gatewayimpl
    │   │           └── persistence
    │   │               ├── jpa
    │   │               │   ├── entity
    │   │               │   ├── repository
    │   │               │   ├── service
    │   │               │   └── converter
    │   │               └── mp
    │   │                   ├── dataobject
    │   │                   ├── mapper
    │   │                   ├── service
    │   │                   └── converter
    │   └── resources
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       ├── db/migration
    │       └── mapper
    └── test
        ├── java/top/egon/familyaibutler/uaa
        │   ├── architecture
        │   ├── application
        │   ├── domain
        │   └── infrastructure
        └── resources
```

## 3. 代码风格约束

执行编码时必须遵守以下约束：

1. 新增 Java 文件必须补齐文件级注释，格式使用 `@BelongsProject`、`@BelongsPackage`、`@FileName`、`@Author`、`@CreateTime`、
   `@Description`、`@Version`。
2. 新增类必须补齐类级注释，格式沿用 `backend/code_style.md`。
3. public / protected 方法必须补齐 JavaDoc；private 方法承载独立逻辑时也要写。
4. Controller 只做协议适配，不能直接访问 Mapper、Repository、PO、DO。
5. application 只做用例编排，不能依赖 adapter，新增逻辑优先通过 domain gateway 收口。
6. domain 不依赖 Spring Web、MyBatis、JPA、PO、DTO、infrastructure。
7. infrastructure 实现持久化、缓存、外部调用和 Spring 配置。
8. 不新增根包 `controller`、`service`、`mapper`、`po`、`repository`、`configuration`、`filter`、`utils`、`vo`。
9. 不为了形式拆方法。方法少于 100 行、逻辑清晰、职责单一时可以保持在一个方法中。
10. 所有对外接口返回 `Result<T>` 或 `PageResult<T>`；facade 返回自身定义的响应 DTO，不返回持久化对象。

## 4. facade 模块设计

### 4.1 uaa-facade 目录

```text
backend/family-uaa/uaa-facade
├── pom.xml
└── src/main/java/top/egon/familyaibutler/uaa/facade
    ├── AccountFacade.java
    ├── AuthFacade.java
    ├── TokenFacade.java
    ├── ProfileFacade.java
    ├── AuthorizationFacade.java
    ├── EntitlementFacade.java
    ├── ContentAccessFacade.java
    ├── OpenPlatformFacade.java
    ├── AuditFacade.java
    ├── RiskFacade.java
    ├── dto
    │   ├── account
    │   ├── auth
    │   ├── token
    │   ├── profile
    │   ├── authorization
    │   ├── entitlement
    │   ├── content
    │   ├── openplatform
    │   ├── audit
    │   └── risk
    ├── enums
    └── constants
```

### 4.2 facade 接口边界

`AccountFacade`:

```text
registerByUsername
registerByEmail
registerByPhone
findAccountSummary
changeAccountStatus
requestAccountDeletion
confirmAccountDeletion
```

`AuthFacade`:

```text
loginByPassword
loginByEmailCode
loginBySmsCode
requestStepUpChallenge
verifyStepUpChallenge
logoutCurrentSession
logoutAllSessions
```

`TokenFacade`:

```text
issueTokenPair
refreshAccessToken
validateAccessToken
revokeToken
revokeAccountTokens
revokeDeviceTokens
```

`ProfileFacade`:

```text
createProfile
updateProfile
deleteProfile
switchProfile
listProfiles
```

`AuthorizationFacade`:

```text
decide
hasPermission
listGrantedPermissions
```

`EntitlementFacade`:

```text
checkFeature
checkQuota
checkPlan
```

`ContentAccessFacade`:

```text
canView
canPlay
decideContentAccess
```

`OpenPlatformFacade`:

```text
createApiKey
rotateApiKey
disableApiKey
deleteApiKey
createServiceAccount
disableServiceAccount
```

`RiskFacade`:

```text
evaluateLoginRisk
evaluateRegisterRisk
recordRiskEvent
```

`AuditFacade`:

```text
recordAuditEvent
queryAuditEvents
```

## 5. 领域模型设计

### 5.1 聚合

```text
Account
Profile
AuthSession
OAuthClient
OAuthAuthorization
Role
Organization
Project
DeveloperApp
ApiKeyCredential
ServiceAccount
Plan
Subscription
Entitlement
Quota
RiskEvent
AuditLog
```

### 5.2 实体

```text
Credential
Identity
Device
Permission
Resource
RoleHierarchy
RoleConstraint
OrganizationMember
ProjectMember
Consent
TokenRecord
QuotaUsage
ContentPolicySnapshot
```

### 5.3 值对象

```text
AccountId
ProfileId
DeviceId
SessionId
ClientId
TokenId
RoleId
PermissionCode
ResourceRef
ScopeCode
TenantId
RiskLevel
AuditActor
AuthVersion
EntitlementVersion
SessionVersion
RiskVersion
```

### 5.4 枚举

```text
AccountStatus
AccountType
ProfileType
CredentialType
DeviceType
SessionStatus
TokenType
TokenStatus
OAuthGrantType
OAuthClientStatus
PermissionAction
DataScopeType
RoleConstraintType
DecisionResult
RiskAction
RiskLevel
AuditEventType
AuditResult
PlanStatus
SubscriptionStatus
EntitlementStatus
QuotaPeriodType
ApiKeyStatus
ServiceAccountStatus
```

## 6. 数据表规划

首期建议使用 Flyway 或 Liquibase 管理建表脚本。如果项目暂不引入迁移框架，也必须将 SQL 放到
`backend/family-uaa/uaa-core/src/main/resources/db/migration`，避免散落在代码中。

### 6.1 账号认证

```text
uaa_account
uaa_credential
uaa_identity
uaa_profile
uaa_device
uaa_auth_session
uaa_token_record
uaa_verification_code
uaa_step_up_challenge
```

### 6.2 OAuth / OIDC / SSO

```text
uaa_oauth_client
uaa_oauth_client_redirect_uri
uaa_oauth_client_origin
uaa_oauth_client_scope
uaa_oauth_authorization
uaa_oauth_consent
uaa_oauth_authorization_code
uaa_oidc_jwk
```

### 6.3 RBAC3

```text
uaa_role
uaa_permission
uaa_resource
uaa_role_permission
uaa_account_role
uaa_role_hierarchy
uaa_role_constraint
uaa_data_scope
```

### 6.4 组织项目开放平台

```text
uaa_organization
uaa_organization_member
uaa_project
uaa_project_member
uaa_developer_app
uaa_api_key
uaa_service_account
uaa_service_account_secret
```

### 6.5 权益额度内容访问

```text
uaa_plan
uaa_plan_entitlement
uaa_subscription
uaa_account_entitlement
uaa_quota
uaa_quota_usage
uaa_content_access_policy
```

### 6.6 风控审计

```text
uaa_risk_event
uaa_risk_rule
uaa_audit_log
```

## 7. 分阶段实施计划

### Task 1: 建立 facade 模块骨架

**文件:**

```text
Modify: backend/pom.xml
Create: backend/family-uaa/pom.xml
Create: backend/family-uaa/uaa-facade/pom.xml
Create: backend/family-uaa/uaa-facade/src/main/java/top/egon/familyaibutler/uaa/facade/**/*.java
Create: backend/family-uaa/uaa-facade/src/test/java/top/egon/familyaibutler/uaa/facade/architecture/UaaFacadeArchitectureTest.java
```

**步骤:**

- [ ] 在 `backend/pom.xml` 增加 `family-uaa` 聚合 module。
- [ ] 新增 `backend/family-uaa/pom.xml`，聚合 `uaa-facade` 和 `uaa-core`，并保证 `uaa-facade` 位于 `uaa-core` 之前。
- [ ] 新增 `backend/family-uaa/uaa-facade/pom.xml`，依赖只允许 `lombok`、`jakarta.validation-api`、
  `spring-boot-starter-test` 测试依赖。
- [ ] 创建 facade 接口和 DTO 包，DTO 使用 Java record 或 Lombok 普通类。
- [ ] 所有 facade 文件补齐文件级、类级、方法级注释。
- [ ] 写 `UaaFacadeArchitectureTest`，断言 facade 源码不包含 `infrastructure`、`adapter`、`Controller`、`Mapper`、
  `Repository`、`PO`、`DO`、`Entity`、`org.springframework.web`、`com.baomidou`、`jakarta.persistence`。
- [ ] 运行 `mvn -pl family-uaa/uaa-facade test`。

**验收:**

```text
uaa-facade 可单独编译测试。
其他模块可以只依赖 uaa-facade 获取 UAA 契约。
facade 不泄露 UAA 实现、Web、持久化、Spring Security 细节。
```

### Task 2: 重建 family-uaa / uaa-core 模块结构

**文件:**

```text
Modify: backend/family-uaa/uaa-core/pom.xml
Modify: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/UserAuthenticationAuthorizationApplication.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/architecture/UaaDddArchitectureTest.java
```

**步骤:**

- [ ] `uaa-core` 增加对 `uaa-facade` 的依赖。
- [ ] 清理旧根包结构，根包只保留启动类、`adapter`、`application`、`domain`、`infrastructure`。
- [ ] `@MapperScan` 指向 `top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper`。
- [ ] `@ComponentScan` 只扫描 `top.egon.familyaibutler.uaa` 和必要的 common 包，避免无关包进入 UAA。
- [ ] 写结构测试，断言根包下不存在旧式 `controller`、`service`、`mapper`、`po`、`repository`、`configuration`、`filter`、
  `utils`、`vo`。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=UaaDddArchitectureTest test`。

**验收:**

```text
uaa-core 目录结构完全符合 code_style.md。
旧 CRUD 结构不会回退。
```

### Task 3: 建立 Account + Credential + Profile 领域

**文件:**

```text
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/Account.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/entity/Credential.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/Profile.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/enums/AccountStatus.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/enums/AccountType.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/enums/ProfileType.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/service/AccountDomainService.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/gateway/AccountGateway.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/gateway/ProfileGateway.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/domain/AccountDomainServiceTest.java
```

**步骤:**

- [ ] 建立 `Account` 聚合，包含账号状态、账号类型、邮箱、手机号、用户名、权限版本、权益版本、会话版本、风险版本。
- [ ] 建立 `Credential` 实体，承载密码、验证码、第三方身份等凭证元数据。
- [ ] 建立 `Profile` 聚合，区分 `MAIN`、`KIDS`、`CREATOR`、`BUSINESS`、`DEVELOPER`。
- [ ] `AccountDomainService` 实现账号注册规则、状态流转规则、注销前置校验入口。
- [ ] domain 测试覆盖：默认创建 Main Profile、禁用账号不可登录、删除 Profile 不等于注销 Account、账号状态流转合法性。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=AccountDomainServiceTest test`。

**验收:**

```text
Account 和 Profile 分离。
账号状态不再只有 enable / locked 两个布尔值。
domain 不依赖 PO / Mapper / Spring Web。
```

### Task 4: 建立账号应用服务和 Web 接口

**文件:**

```text
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/AccountServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/AccountServiceImpl.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/AccountCommandService.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/AccountQueryService.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/dto/account/*.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/AccountController.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/ProfileController.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/assembler/AccountWebAssembler.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/assembler/ProfileWebAssembler.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/application/AccountServiceTest.java
```

**步骤:**

- [ ] 定义注册、资料查询、账号状态变更、注销申请、注销确认的 Command / Query / Response。
- [ ] Controller 入参和出参只使用 `application.dto`，不出现 PO。
- [ ] 应用服务通过 `AccountGateway`、`ProfileGateway` 访问持久化。
- [ ] 注册接口支持用户名、邮箱、手机号三种 P0 注册方式。
- [ ] 找回密码先建立应用用例和审计事件，不接短信/邮件真实网关时用 domain gateway 抽象外部通知。
- [ ] 注销流程包含二次验证校验入口、会话和 Token 撤销编排入口。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=AccountServiceTest test`。

**验收:**

```text
账号注册 / 找回 / 注销具备用例入口。
Web 层不暴露持久化对象。
AccountFacade 可由 AccountServiceImpl 实现。
```

### Task 5: 建立 Token + Session + Device 能力

**文件:**

```text
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/AuthSession.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/entity/Device.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/entity/TokenRecord.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/service/TokenDomainService.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/AuthServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/TokenServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/SessionServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/DeviceServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/AuthController.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/SessionController.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/DeviceController.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/application/AuthTokenServiceTest.java
```

**步骤:**

- [ ] 登录返回 `TokenPairResponse`，包含 access token、refresh token、过期时间、sessionId、deviceId、profileId。
- [ ] Access Token 只放 README 要求的必要身份和版本信息：`sub`、`profile_id`、`client_id`、`scope`、`session_id`、`device_id`、
  `auth_version`、`entitlement_version`、`risk_level`、`exp`。
- [ ] Refresh Token 落库保存哈希、jti、设备、会话、客户端和过期时间。
- [ ] 支持刷新 access token、撤销单个 token、按账号撤销、按设备撤销、按 client 撤销。
- [ ] 自动登记登录设备，支持查看设备、移除设备、踢出单个会话、全部设备退出。
- [ ] 账号封禁后编排撤销会话和 Token。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=AuthTokenServiceTest test`。

**验收:**

```text
Access Token / Refresh Token / Token 撤销达到 P0。
设备登记、设备列表、远程退出达到 P0。
JWT 不携带完整权限、完整权益、完整内容列表。
```

### Task 6: 建立 OAuth2 / OIDC / SSO 能力

**文件:**

```text
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/OAuthClient.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/OAuthAuthorization.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/entity/Consent.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/service/OAuthDomainService.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/OAuthClientServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/OAuthAuthorizationServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/OAuthClientController.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/OAuthAuthorizeController.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/OidcController.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/application/OAuthAuthorizationServiceTest.java
```

**步骤:**

- [ ] OAuth Client 支持创建、禁用、secret 轮换、redirect uri 白名单、allowed origins、grant type、scope、token 有效期。
- [ ] Authorization Code 支持 PKCE，校验 `code_challenge`、`code_verifier`、`state`、`nonce`。
- [ ] 支持 Client Credentials。
- [ ] 支持授权同意记录和授权撤销。
- [ ] OIDC 提供 Discovery Endpoint、JWKS Endpoint、UserInfo Endpoint、ID Token。
- [ ] SSO 使用 UAA 会话作为统一登录态，支持本应用退出、全局退出、退出后撤销 refresh token。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=OAuthAuthorizationServiceTest test`。

**验收:**

```text
OAuth2 Authorization Code + PKCE 达到 P0。
OIDC 登录达到 P0。
SSO 单点登录和单点退出具备服务端能力。
OAuth Client 管理达到 P0。
```

### Task 7: 建立 RBAC3 权限模型

**文件:**

```text
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/Role.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/entity/Permission.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/entity/Resource.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/entity/RoleHierarchy.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/entity/RoleConstraint.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/service/RbacDomainService.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/RbacServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/RbacController.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/domain/RbacDomainServiceTest.java
```

**步骤:**

- [ ] 支持角色、权限、资源、账号角色授权。
- [ ] 支持角色父子关系。
- [ ] 支持多层角色继承和继承权限展开计算。
- [ ] 支持角色继承循环检测。
- [ ] 支持静态职责分离，互斥角色不能分配给同一账号。
- [ ] 支持组织、项目、频道、租户、数据范围约束。
- [ ] 权限变更后递增 `auth_version`，旧 Token 可被资源服务识别为过期授权。
- [ ] 所有权限变更写审计日志。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=RbacDomainServiceTest test`。

**验收:**

```text
RBAC3 基础能力、角色继承、互斥角色、后台权限达到 P0。
权限版本号可用于资源服务判断旧 Token 是否可信。
```

### Task 8: 建立组织 / 项目 / 开放平台能力

**文件:**

```text
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/Organization.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/Project.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/DeveloperApp.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/ApiKeyCredential.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/ServiceAccount.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/OrganizationServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/ProjectServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/OpenPlatformServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/OrganizationController.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/ProjectController.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/ApiKeyController.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/ServiceAccountController.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/application/OpenPlatformServiceTest.java
```

**步骤:**

- [ ] 支持创建组织、邀请成员、成员加入/退出、组织角色、组织禁用。
- [ ] 支持组织下创建项目、项目成员、项目角色、项目禁用。
- [ ] 支持 API Key 创建、禁用、删除、轮换、scope、额度限制、最后使用时间、使用审计。
- [ ] API Key 原文只展示一次，库中只保存哈希。
- [ ] 支持 Service Account 创建、绑定项目、角色授权、密钥管理、禁用、访问审计。
- [ ] Service Account 按最小权限原则，只能获得明确授权的 scope / role。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=OpenPlatformServiceTest test`。

**验收:**

```text
组织 / 项目基础模型达到 MVP。
API Key 和 Service Account 达到 P0。
Client Secret / API Key / Service Account Secret 不明文存储。
```

### Task 9: 建立套餐 / 权益 / 额度 / 内容访问判断

**文件:**

```text
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/Plan.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/Subscription.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/Entitlement.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/entity/Quota.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/entity/QuotaUsage.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/service/EntitlementDomainService.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/service/ContentAccessDomainService.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/EntitlementServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/AuthorizationDecisionServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/EntitlementController.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/ContentAccessController.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/domain/EntitlementDomainServiceTest.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/domain/ContentAccessDomainServiceTest.java
```

**步骤:**

- [ ] 支持套餐配置、套餐绑定权益、账号订阅套餐。
- [ ] 支持权益生效时间、失效时间、暂停、恢复、取消、过期、欠费冻结。
- [ ] 权益变更后递增 `entitlement_version`。
- [ ] 支持按天、按月、总量、并发、接口限流额度。
- [ ] 提供权益判断接口，返回 `allowed`、`reason`、`matchedEntitlements`、`remainingQuota`。
- [ ] 内容访问判断组合认证结果、账号状态、Profile、地区、年龄、会员权益、设备限制、并发限制、风控状态。
- [ ] 内容下架、地区不允许、儿童 Profile 分级不满足、会员权益不足时返回明确拒绝原因。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=EntitlementDomainServiceTest,ContentAccessDomainServiceTest test`。

**验收:**

```text
套餐与权益基础模型达到 P0。
权益判断接口达到 P0。
内容访问判断接口达到 P0。
```

### Task 10: 建立统一授权决策服务

**文件:**

```text
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/service/AuthorizationDecisionDomainService.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/AuthorizationDecisionServiceImpl.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/AuthorizationDecisionController.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/application/AuthorizationDecisionServiceTest.java
```

**步骤:**

- [ ] 决策输入包含 subject、resource、action、context。
- [ ] 决策流程依次组合身份认证、RBAC3、Entitlement、ABAC、ReBAC 扩展位、Risk Policy、合规扩展位。
- [ ] 首期 ReBAC 和合规策略保留 gateway 接口，不实现完整社交关系。
- [ ] 决策输出包含 `allowed`、`reason`、`matchedPolicies`、`obligations`。
- [ ] facade 暴露 `AuthorizationFacade.decide`。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=AuthorizationDecisionServiceTest test`。

**验收:**

```text
资源服务可以通过 facade 或 HTTP 接口进行权限判断。
至少 family-core 可以完成 Token 校验和权限判断接入。
```

### Task 11: 建立基础风控

**文件:**

```text
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/RiskEvent.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/service/RiskDomainService.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/RiskServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/RiskServiceImpl.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/RiskController.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/domain/RiskDomainServiceTest.java
```

**步骤:**

- [ ] 支持登录风险评分。
- [ ] 支持新设备风险识别。
- [ ] 支持异地登录风险识别。
- [ ] 支持账号注册风控。
- [ ] 支持账号封禁和解封。
- [ ] 支持 API 限流命中事件。
- [ ] 风控事件写审计日志。
- [ ] 风控状态变化后递增 `risk_version`。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=RiskDomainServiceTest test`。

**验收:**

```text
基础风控达到 P0。
登录、注册、设备、API 调用均可以接入风控判断。
```

### Task 12: 建立审计日志

**文件:**

```text
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/aggregate/AuditLog.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/service/AuditDomainService.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/AuditServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/AuditServiceImpl.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/AuditController.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/application/AuditServiceTest.java
```

**步骤:**

- [ ] 审计字段包含事件 ID、事件类型、操作主体、主体类型、目标对象、目标类型、动作、结果、失败原因、操作时间、IP、设备
  ID、User-Agent、Client ID、Session ID、Trace ID、变更前、变更后、风险等级、租户/组织/项目/频道上下文。
- [ ] 登录成功、登录失败、Token 签发、Token 刷新、Token 撤销必须写审计。
- [ ] OAuth 授权、授权撤销、Scope 变更必须写审计。
- [ ] 权限变更必须写审计。
- [ ] 权益变更必须写审计。
- [ ] 管理员高危操作必须记录变更前后。
- [ ] 普通管理员不能删除审计日志。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=AuditServiceTest test`。

**验收:**

```text
登录审计、授权审计、权限审计、管理操作审计达到 P0。
审计日志支持后台查询。
```

### Task 13: 建立用户自助中心和管理后台 API

**文件:**

```text
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/UserSelfCenterController.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/AdminConsoleController.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/UserSelfCenterServiceI.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/application/AdminConsoleServiceI.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/application/UserSelfCenterServiceTest.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/application/AdminConsoleServiceTest.java
```

**步骤:**

- [ ] 用户自助中心支持个人资料、安全设置、登录设备、登录历史、Profile 管理、第三方授权撤销、API Key、注销账号。
- [ ] 管理后台支持用户查询、冻结、解封、重置 MFA 预留入口、查看设备、权限管理、Client 管理、组织管理、开发者管理、订阅权益管理、风控管理、审计日志。
- [ ] 管理后台账号与 C 端账号隔离。
- [ ] 后台高危操作接入二次确认和审计。
- [ ] 敏感字段脱敏返回。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=UserSelfCenterServiceTest,AdminConsoleServiceTest test`。

**验收:**

```text
用户自助中心达到 MVP。
管理后台达到 MVP。
后台权限使用 RBAC3，不复用 C 端自然权限。
```

### Task 14: 建立持久化实现

**文件:**

```text
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/infrastructure/gatewayimpl/*GatewayImpl.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/infrastructure/persistence/jpa/entity/*.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/infrastructure/persistence/jpa/repository/*.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/infrastructure/persistence/jpa/converter/*.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/infrastructure/persistence/mp/dataobject/*.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/infrastructure/persistence/mp/mapper/*.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/infrastructure/persistence/mp/converter/*.java
Create: backend/family-uaa/uaa-core/src/main/resources/mapper/*.xml
Create: backend/family-uaa/uaa-core/src/main/resources/db/migration/V1__uaa_p0_schema.sql
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/infrastructure/UaaPersistenceMappingTest.java
```

**步骤:**

- [ ] 简单写模型、聚合持久化优先使用 JPA。
- [ ] 复杂查询、分页、统计、动态 SQL 使用 MyBatis Plus。
- [ ] PO / JPA Entity 只存在于 infrastructure。
- [ ] domain gateway 由 infrastructure.gatewayimpl 实现。
- [ ] converter 完成 domain 与 persistence object 互转。
- [ ] `application.yml` 中 JPA 必须保持 `ddl-auto: validate`。
- [ ] MyBatis XML 使用 PostgreSQL 语法，不使用 MySQL `on duplicate key update`。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=UaaPersistenceMappingTest test`。

**验收:**

```text
持久化实现不污染 application 和 domain。
SQL 与 PostgreSQL 匹配。
数据库结构有可审计的迁移脚本。
```

### Task 15: 安全配置和过滤器重构

**文件:**

```text
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/infrastructure/configuration/UaaSecurityConfig.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/infrastructure/security/UaaUserDetails.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/infrastructure/security/UaaUserDetailsService.java
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/adapter/filter/UaaJwtAuthenticationFilter.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/infrastructure/UaaSecurityConfigTest.java
```

**步骤:**

- [ ] 持久化对象不再实现 `UserDetails`。
- [ ] `UaaUserDetails` 作为 Spring Security 适配对象。
- [ ] 登录、注册、验证码、OAuth authorize、OIDC discovery、JWKS、UserInfo 按安全策略配置放行或鉴权。
- [ ] OPTIONS 请求放行，避免浏览器预检失败。
- [ ] CORS 配置从配置文件读取，不在代码中写死 localhost。
- [ ] 认证失败和授权失败统一返回 `Result` 错误结构。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=UaaSecurityConfigTest test`。

**验收:**

```text
Spring Security 适配与领域模型隔离。
UAA 可正常处理登录、token 校验、OAuth/OIDC 端点。
```

### Task 16: family-core 资源服务接入

**文件:**

```text
Modify: backend/family-core/pom.xml
Create: backend/family-core/src/main/java/top/egon/familyaibutler/family/infrastructure/security/ResourceAuthorizationService.java
Create: backend/family-core/src/test/java/top/egon/familyaibutler/family/infrastructure/security/ResourceAuthorizationServiceTest.java
```

**步骤:**

- [ ] `family-core` 依赖 `uaa-facade`。
- [ ] 资源服务从 Token 中读取 subject、profile、session、device、auth_version、entitlement_version、risk_level。
- [ ] 调用 `AuthorizationFacade` 完成权限判断。
- [ ] 调用 `EntitlementFacade` 或 `ContentAccessFacade` 完成权益和内容访问判断。
- [ ] 保持 UAA facade 为接口依赖，不直接依赖 `uaa-core` 实现类。
- [ ] 运行 `mvn -pl family-core -am test`。

**验收:**

```text
至少一个资源服务完成 Token 校验和权限判断。
满足 readme.md MVP 第 20 条。
```

### Task 17: family-gateway / SSO 接入

**文件:**

```text
Modify: backend/family-gateway/pom.xml
Modify: backend/family-gateway/src/main/java/top/egon/familyaibutler/gateway/filter/JwtTokenFilter.java
Create: backend/family-gateway/src/test/java/top/egon/familyaibutler/gateway/filter/JwtTokenFilterTest.java
```

**步骤:**

- [ ] `family-gateway` 依赖 `uaa-facade`。
- [ ] 网关白名单包括 UAA 登录、注册、OAuth/OIDC discovery、JWKS、授权入口。
- [ ] 网关解析 access token 后保留必要请求头给资源服务。
- [ ] 网关对撤销、过期、版本不可信的 token 返回统一认证失败。
- [ ] 网关允许 OPTIONS 请求。
- [ ] 配置至少一个业务系统通过 OIDC/SSO 访问 UAA。
- [ ] 运行 `mvn -pl family-gateway -am test`。

**验收:**

```text
至少一个业务系统完成 SSO 接入。
满足 readme.md MVP 第 19 条。
```

### Task 18: 接入文档和错误码规范

**文件:**

```text
Create: backend/family-uaa/uaa-core/docs/oauth2-oidc-integration.md
Create: backend/family-uaa/uaa-core/docs/resource-server-integration.md
Create: backend/family-uaa/uaa-core/docs/api-key-service-account.md
Create: backend/family-uaa/uaa-core/src/main/java/top/egon/familyaibutler/uaa/domain/model/enums/UaaErrorCode.java
Create: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/domain/UaaErrorCodeTest.java
```

**步骤:**

- [ ] 写 OAuth2/OIDC 接入文档。
- [ ] 写 Resource Server Token 校验和权限判断接入规范。
- [ ] 写 API Key / Service Account 使用规范。
- [ ] 定义认证、Token、权限、权益、额度、设备、风控、OAuth、内容、账号错误码。
- [ ] 错误返回包含 `code`、`message`、`reason`、`traceId`、`required`。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -Dtest=UaaErrorCodeTest test`。

**验收:**

```text
满足 INT-01 到 INT-09 的 P0 接入文档和错误码要求。
```

### Task 19: 全量结构、编译、测试验证

**文件:**

```text
Modify: backend/family-uaa/uaa-core/src/test/java/top/egon/familyaibutler/uaa/architecture/UaaDddArchitectureTest.java
Create: backend/family-uaa/uaa-facade/src/test/java/top/egon/familyaibutler/uaa/facade/architecture/UaaFacadeArchitectureTest.java
```

**步骤:**

- [ ] 运行 `mvn -pl family-uaa/uaa-facade test`。
- [ ] 运行 `mvn -pl family-uaa/uaa-core -am test`。
- [ ] 运行 `mvn -pl family-core -am test`。
- [ ] 运行 `mvn -pl family-gateway -am test`。
- [ ] 运行 `mvn -pl family-uaa/uaa-facade,family-uaa/uaa-core,family-core,family-gateway -am test`。
- [ ] 运行 `git diff --check`。
- [ ] 检查所有新增 Java 文件是否有文件级、类级、方法级注释。
- [ ] 检查 facade 是否仍然没有实现层依赖。

**验收:**

```text
所有相关模块测试通过。
DDD 分层结构测试通过。
facade 边界测试通过。
没有格式空白错误。
```

## 8. P0 需求到任务映射

| P0 / MVP 需求                      | 对应任务                   | 当前状态                                                                                                         |
|----------------------------------|------------------------|--------------------------------------------------------------------------------------------------------------|
| 账号注册 / 登录 / 找回 / 注销              | Task 3, Task 4, Task 5 | 部分完成：基础注册、状态变更、注销申请/确认、密码登录、找回/重置入口已完成；真实通知和完整自助流程未完成。                                                       |
| 密码登录 / 验证码登录                     | Task 5                 | 部分完成：密码登录已接入 OAuth Client 和 JWT；验证码登录入口已建立，真实短信 / 邮件网关未接。                                                    |
| 账号状态管理                           | Task 3, Task 4         | 部分完成：账号状态模型和接口已完成，风控联动和审计未完成。                                                                                |
| 设备登记与远程退出                        | Task 5                 | 部分完成：登录设备登记、设备移除、会话 / Token 撤销已完成；设备列表、可信设备、并发设备控制未完成。                                                       |
| OAuth2 Authorization Code + PKCE | Task 6                 | 未完成。                                                                                                         |
| OIDC 登录                          | Task 6                 | 部分完成：JWT / Refresh Token / JWK 基础已完成；Discovery、UserInfo、ID Token、Consent 未完成。                                |
| SSO 单点登录                         | Task 6, Task 17        | 部分完成：gateway 和资源服务接入闭环已完成；标准 SSO 登录、单点退出和接入 Demo 未完成。                                                        |
| Access Token / Refresh Token     | Task 5                 | 已完成：Access Token 使用 JWT，Refresh Token 为不透明随机串，均可落库和撤销。                                                       |
| Token 撤销                         | Task 5                 | 已完成：支持 Token 落库状态和撤销判断。                                                                                      |
| OAuth Client 管理                  | Task 6                 | 部分完成：创建、查询、列表、Secret 哈希、Grant / Scope / Resource Pattern / TTL 已完成；禁用、轮换、Redirect URI、Allowed Origins、审计未完成。 |
| RBAC3 基础能力                       | Task 7                 | 部分完成：角色、资源、绑定、账号权限查询和 API 动态决策已完成；角色继承、互斥、职责分离、数据范围未完成。                                                      |
| 角色继承                             | Task 7                 | 未完成。                                                                                                         |
| 互斥角色                             | Task 7                 | 未完成。                                                                                                         |
| 后台权限管理                           | Task 7, Task 13        | 部分完成：RBAC facade 和 Web 接口已完成；管理后台完整 API 和审计未完成。                                                              |
| 用户自助中心                           | Task 13                | 未完成。                                                                                                         |
| 管理后台                             | Task 13                | 未完成。                                                                                                         |
| 套餐与权益基础模型                        | Task 9                 | 未完成。                                                                                                         |
| 权益判断接口                           | Task 9                 | 部分完成：Token 中有 entitlementVersion，授权决策有版本一致性校验；权益模型和权益判断接口未完成。                                                |
| 内容访问判断接口                         | Task 9, Task 10        | 未完成。                                                                                                         |
| API Key                          | Task 8                 | 未完成。                                                                                                         |
| Service Account                  | Task 8                 | 未完成。                                                                                                         |
| 登录审计                             | Task 12                | 未完成。                                                                                                         |
| 授权审计                             | Task 12                | 未完成。                                                                                                         |
| 权限审计                             | Task 7, Task 12        | 未完成。                                                                                                         |
| 管理操作审计                           | Task 12, Task 13       | 未完成。                                                                                                         |
| 基础风控                             | Task 11                | 部分完成：授权决策已对 `riskLevel` 做基础拒绝；完整风险事件、策略、限流未完成。                                                               |
| 业务系统 SSO 接入                      | Task 17                | 部分完成：gateway 和 `family-core` 资源服务接入已完成；SSO Demo 和接入文档未完成。                                                    |
| 资源服务 Token 校验和权限判断               | Task 16                | 已完成基础能力：gateway 调 UAA 准入决策，资源服务 starter 已提供通用 Filter 和 REST 决策客户端。                                           |

## 9. 风险和控制点

1. P0 范围很大，执行时必须按任务交付，不要一次性大爆炸式提交。
2. `uaa-facade` 是稳定契约，第一轮必须先定接口边界，避免其他模块直接依赖 UAA 实现。
3. Token 不能塞完整权限和权益，只能放身份与版本号。
4. 权限、权益、会话、风控状态变更必须更新对应版本号。
5. API Key、Client Secret、Refresh Token、Service Account Secret 只能保存哈希。
6. OAuth2/OIDC 如果引入 Spring Authorization Server，需要先确认依赖版本与 Spring Boot 3.5.13 兼容；如果不引入，则首期按自研最小实现做
   Authorization Code + PKCE 和 OIDC 必需端点。
7. 真实短信、邮件、Push、支付、内容系统不在 UAA 直接实现，用 domain gateway 预留外部能力端口。
8. 数据库迁移脚本必须使用 PostgreSQL 语法。
9. 编码中不得为了拆层制造多层一两行 private 方法链。
10. 当前工作区存在未提交变更，执行前需要再次确认哪些文件属于本次重构范围，避免误覆盖用户其他改动。

## 10. 执行顺序建议

截至 2026-05-20，第一批、第一批补齐、第二批接入、第三批动态授权均已执行。后续建议从剩余未完成能力继续按以下顺序推进：

```text
Task 6 剩余：OAuth2 Authorization Code + PKCE / OIDC Discovery / UserInfo / ID Token / Consent
Task 7 剩余：RBAC3 角色继承 / 互斥角色 / 职责分离 / 数据范围约束
Task 8 组织 / 项目 / 开放平台 / API Key / Service Account
Task 9 权益 / 订阅 / 额度 / 内容访问
Task 11 完整风控策略 / 风险事件 / 限流
Task 12 审计日志落库
Task 13 用户自助中心和管理后台 API
Task 18 接入文档 / 错误码 / SSO Demo
Task 19 全量验证
```

已经完成的能力后续只做缺口补齐和回归测试，不再重复重构底座结构。

## 11. 用户确认点

开始 coding 前需要用户确认以下决策：

1. `family-uaa` 聚合模块名、`uaa-core` 服务实现模块名、`uaa-facade` 契约模块名是否确认。
2. OAuth2/OIDC 首期是否允许引入 Spring Authorization Server；如果不引入，则按自研最小 P0 实现。
3. 数据库迁移是否引入 Flyway；如果不引入，仍创建 `db/migration` SQL 作为版本化脚本。
4. 第一批是否只做 Task 1 到 Task 5。
