# FamilyAiButler OpenAPI Summary

## Boundary

本文档整理当前本机后端暴露的 OpenAPI3 JSON，面向前端一期多端应用接入使用。分析边界限定为：

- `family-core`：密码管理、分类管理。
- `family-uaa`：认证/账号、密码恢复/重置、Profile、RBAC/OAuth/JWK/会话管理。
- `family-ai-qwen`：Qwen 图片理解能力。

本次没有修改前端代码或后端代码。

## Source And Access Result

用户指定的 OpenAPI Console 聚合地址当前都要求控制台认证，直接拉取结果如下：

| Service          | Requested URL                                                      | HTTP status | Result   |
|------------------|--------------------------------------------------------------------|-------------|----------|
| `family-core`    | `http://127.0.0.1:9527/openapi-console/api/openapi/family-core`    | `401`       | 未返回 JSON |
| `family-uaa`     | `http://127.0.0.1:9527/openapi-console/api/openapi/family-uaa`     | `401`       | 未返回 JSON |
| `family-ai-qwen` | `http://127.0.0.1:9527/openapi-console/api/openapi/family-ai-qwen` | `401`       | 未返回 JSON |

为完成前端 API 摘要，改用 gateway 配置中的生产者 OpenAPI 地址读取，同样来自当前运行中的本机后端：

| Service          | Producer URL                             | HTTP status | OpenAPI title                | Version  | Operation count |
|------------------|------------------------------------------|-------------|------------------------------|----------|-----------------|
| `family-core`    | `http://127.0.0.1:9527/base/v3/api-docs` | `200`       | `FamilyAIButler-BaseModel`   | `v0.0.1` | 31              |
| `family-uaa`     | `http://127.0.0.1:9527/uaa/v3/api-docs`  | `200`       | `FamilyAiButler UAA API`     | `v0.0.1` | 33              |
| `family-ai-qwen` | `http://127.0.0.1:9527/ai/v3/api-docs`   | `200`       | `FamilyAiButler Qwen AI API` | `v0.0.1` | 14              |

The specs include OpenAPI Console internal endpoints under `/openapi-console/api/**` in each service. Those endpoints
are not listed in the business API tables below because they are console implementation APIs, not application APIs.

## Frontend Path Rule

前端请求需要走 Nginx 前缀：`/api` + gateway route prefix + OpenAPI path。

| Service          | Gateway route prefix | Frontend path form         |
|------------------|----------------------|----------------------------|
| `family-core`    | `/base`              | `/api/base` + OpenAPI path |
| `family-uaa`     | `/uaa`               | `/api/uaa` + OpenAPI path  |
| `family-ai-qwen` | `/ai`                | `/api/ai` + OpenAPI path   |

注意：`family-ai-qwen` 的 OpenAPI path 已经是 `/ai/v1/image2Message`。按当前规则得到的前端路径是
`/api/ai/ai/v1/image2Message`。该路径形态需要前端联调时重点验证，避免 gateway `StripPrefix=1` 与服务内 Controller
前缀叠加造成误用。

## Auth And Error Model Notes

- 三份 OpenAPI 均声明全局 `security: [{"Authorization":[]}]`，security scheme 名称为 `Authorization`。
- 这表示按 OpenAPI 文档，业务接口都需要 `Authorization` 请求头。
- gateway 配置中对 `/uaa/auth/login/password`、`/uaa/auth/login/email-code`、`/uaa/auth/login/sms-code`、
  `/uaa/account/register`、`/uaa/auth/password/recovery`、`/uaa/auth/password/reset` 有 JWT 忽略规则；这与 OpenAPI 全局
  security 描述不完全一致，登录/注册/找回密码是否真正无鉴权应以运行时验证为准。
- 响应多数包装为 `Result*` / `PageResult*`，OpenAPI 当前把 `data` 字段描述为 `string` 默认值，未展开真实业务
  DTO。前端生成类型时不要直接信任 `data:string`，这些端点需要运行时样例或后端 schema 修正。
- 部分响应状态码在 OpenAPI 中写成 `10000`，这更像业务成功码，不是标准 HTTP status。前端应按实际 HTTP status +
  `code/success/message/data` 包装共同处理。

## Phase 1 Page Candidates

| Page       | Primary service  | Usable endpoints               |
|------------|------------------|--------------------------------|
| 认证/账号      | `family-uaa`     | 登录、注册、退出、Profile、JWK           |
| 密码管理       | `family-core`    | 密码新增/修改/删除/查询、随机密码生成、强度检查      |
| 分类管理       | `family-core`    | 分类类型列表、分类列表、分类/分类类型 CRUD       |
| AI 问答或模型能力 | `family-ai-qwen` | 当前 OpenAPI 只暴露图片转文本描述，不是通用文本问答 |

## family-core APIs

| Tag      | Method   | OpenAPI path                                                                      | Frontend path                                                                              | Summary      | Request params                                                                                                                | Request body      | Response schema                    | Auth            |
|----------|----------|-----------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|--------------|-------------------------------------------------------------------------------------------------------------------------------|-------------------|------------------------------------|-----------------|
| 密码管理相关接口 | `PUT`    | `/password`                                                                       | `/api/base/password`                                                                       | 修改数据         | -                                                                                                                             | `PasswordViewDTO` | `ResultBoolean`                    | `Authorization` |
| 密码管理相关接口 | `DELETE` | `/password`                                                                       | `/api/base/password`                                                                       | 删除数据         | -                                                                                                                             | `array<integer>`  | `ResultBoolean`                    | `Authorization` |
| 密码管理相关接口 | `POST`   | `/password/password/add`                                                          | `/api/base/password/password/add`                                                          | 添加一个账号密码     | query `passwordView` required string                                                                                          | `PasswordViewDTO` | `Result` via response key `10000`  | `Authorization` |
| 密码管理相关接口 | `GET`    | `/password/{id}`                                                                  | `/api/base/password/{id}`                                                                  | 通过主键查询单条数据   | path `id` required integer                                                                                                    | -                 | `ResultPasswordViewDTO`            | `Authorization` |
| 密码管理相关接口 | `GET`    | `/password/password/list`                                                         | `/api/base/password/password/list`                                                         | 获取账号密码列表     | -                                                                                                                             | `PasswordViewDTO` | `Result` via response key `10000`  | `Authorization` |
| 密码管理相关接口 | `GET`    | `/password/password/list/{pageNum}/{pageSize}`                                    | `/api/base/password/password/list/{pageNum}/{pageSize}`                                    | 获取账号密码列表     | path `pageNum` required string; path `pageSize` required string                                                               | `PasswordViewDTO` | `Result` via response key `10000`  | `Authorization` |
| 密码管理相关接口 | `GET`    | `/password/generate/{passwordLength}`                                             | `/api/base/password/generate/{passwordLength}`                                             | 生成一个随机密码     | path `passwordLength` required string                                                                                         | -                 | `Result` via response key `10000`  | `Authorization` |
| 密码管理相关接口 | `GET`    | `/password/generate/{passwordLength}/{needSpecialCharacters}`                     | `/api/base/password/generate/{passwordLength}/{needSpecialCharacters}`                     | 生成一个随机密码     | path `passwordLength` required string; path `needSpecialCharacters` required string                                           | -                 | `Result` via response key `10000`  | `Authorization` |
| 密码管理相关接口 | `GET`    | `/password/generate/{passwordLength}/{needSpecialCharacters}/{specialCharacters}` | `/api/base/password/generate/{passwordLength}/{needSpecialCharacters}/{specialCharacters}` | 生成一个随机密码     | path `passwordLength` required string; path `needSpecialCharacters` required string; path `specialCharacters` required string | -                 | `Result` via response key `10000`  | `Authorization` |
| 密码管理相关接口 | `GET`    | `/password/checkValid/{password}`                                                 | `/api/base/password/checkValid/{password}`                                                 | 检查密码强度       | path `password` required string                                                                                               | -                 | `boolean` via response key `10000` | `Authorization` |
| 密码管理相关接口 | `GET`    | `/password/checkStrength/{password}`                                              | `/api/base/password/checkStrength/{password}`                                              | 检查密码强度       | path `password` required string                                                                                               | -                 | `ResultStrengthDTO`                | `Authorization` |
| 密码管理相关接口 | `GET`    | `/password/business/{businessId}`                                                 | `/api/base/password/business/{businessId}`                                                 | 通过业务主键查询单条数据 | path `businessId` required string                                                                                             | -                 | `ResultPasswordViewDTO`            | `Authorization` |
| 分类管理相关接口 | `GET`    | `/category/type/list`                                                             | `/api/base/category/type/list`                                                             | 获取所有分类类型     | -                                                                                                                             | -                 | `PageResultCategoryTypeDTO`        | `Authorization` |
| 分类管理相关接口 | `GET`    | `/category/list`                                                                  | `/api/base/category/list`                                                                  | 获取所有分类       | -                                                                                                                             | -                 | `PageResultCategoryDTO`            | `Authorization` |
| 分类管理相关接口 | `POST`   | `/category/category`                                                              | `/api/base/category/category`                                                              | 添加分类         | -                                                                                                                             | `CategoryDTO`     | `ResultCategoryDTO`                | `Authorization` |
| 分类管理相关接口 | `PUT`    | `/category/category`                                                              | `/api/base/category/category`                                                              | 更新分类         | -                                                                                                                             | `CategoryDTO`     | `ResultCategoryDTO`                | `Authorization` |
| 分类管理相关接口 | `GET`    | `/category/category/{id}`                                                         | `/api/base/category/category/{id}`                                                         | 获取指定分类       | path `id` required integer                                                                                                    | -                 | `ResultCategoryDTO`                | `Authorization` |
| 分类管理相关接口 | `DELETE` | `/category/category/{id}`                                                         | `/api/base/category/category/{id}`                                                         | 删除分类         | path `id` required integer                                                                                                    | -                 | `ResultBoolean`                    | `Authorization` |
| 分类管理相关接口 | `POST`   | `/category/category/type`                                                         | `/api/base/category/category/type`                                                         | 添加分类类型       | -                                                                                                                             | `CategoryTypeDTO` | `ResultCategoryTypeDTO`            | `Authorization` |
| 分类管理相关接口 | `PUT`    | `/category/category/type`                                                         | `/api/base/category/category/type`                                                         | 更新分类类型       | -                                                                                                                             | `CategoryTypeDTO` | `ResultCategoryTypeDTO`            | `Authorization` |
| 分类管理相关接口 | `GET`    | `/category/category/type/{id}`                                                    | `/api/base/category/category/type/{id}`                                                    | 获取指定分类类型     | path `id` required integer                                                                                                    | -                 | `ResultCategoryTypeDTO`            | `Authorization` |
| 分类管理相关接口 | `DELETE` | `/category/category/type/{id}`                                                    | `/api/base/category/category/type/{id}`                                                    | 删除分类类型       | path `id` required integer                                                                                                    | -                 | `ResultBoolean`                    | `Authorization` |

### family-core Request Schemas

- `PasswordViewDTO`: `name`, `password*`, `description`, `accountNumber*`, `websit*`, `likeStatus`, `category`,
  `lastViewTime`.
- `CategoryDTO`: `id`, `name`, `description`, `parentId`.
- `CategoryTypeDTO`: `id`, `typeName`, `description`, `createTime`, `updateTime`.

## family-uaa APIs

| Tag                        | Method   | OpenAPI path                             | Frontend path                                    | Summary | Request params                                                | Request body                      | Response schema                       | Auth                                                              |
|----------------------------|----------|------------------------------------------|--------------------------------------------------|---------|---------------------------------------------------------------|-----------------------------------|---------------------------------------|-------------------------------------------------------------------|
| `auth-controller`          | `POST`   | `/auth/login/password`                   | `/api/uaa/auth/login/password`                   | -       | -                                                             | `PasswordLoginRequest`            | `ResultTokenPairResponse`             | OpenAPI says `Authorization`; gateway ignore list suggests no JWT |
| `auth-controller`          | `POST`   | `/auth/login/email-code`                 | `/api/uaa/auth/login/email-code`                 | -       | -                                                             | `VerifyCodeLoginRequest`          | `ResultTokenPairResponse`             | OpenAPI says `Authorization`; gateway ignore list suggests no JWT |
| `auth-controller`          | `POST`   | `/auth/login/sms-code`                   | `/api/uaa/auth/login/sms-code`                   | -       | -                                                             | `VerifyCodeLoginRequest`          | `ResultTokenPairResponse`             | OpenAPI says `Authorization`; gateway ignore list suggests no JWT |
| `account-controller`       | `POST`   | `/account/register`                      | `/api/uaa/account/register`                      | -       | -                                                             | `RegisterAccountRequest`          | `ResultAccountResponse`               | OpenAPI says `Authorization`; gateway ignore list suggests no JWT |
| `auth-controller`          | `POST`   | `/auth/password/recovery`                | `/api/uaa/auth/password/recovery`                | -       | -                                                             | `PasswordRecoveryRequest`         | `ResultString`                        | OpenAPI says `Authorization`; gateway ignore list suggests no JWT |
| `auth-controller`          | `POST`   | `/auth/password/reset`                   | `/api/uaa/auth/password/reset`                   | -       | -                                                             | `ResetPasswordRequest`            | `ResultBoolean`                       | OpenAPI says `Authorization`; gateway ignore list suggests no JWT |
| `auth-controller`          | `POST`   | `/auth/logout/current`                   | `/api/uaa/auth/logout/current`                   | -       | -                                                             | `LogoutRequest`                   | `ResultBoolean`                       | `Authorization`                                                   |
| `auth-controller`          | `POST`   | `/auth/logout/all`                       | `/api/uaa/auth/logout/all`                       | -       | -                                                             | `LogoutRequest`                   | `ResultBoolean`                       | `Authorization`                                                   |
| `profile-controller`       | `POST`   | `/profile`                               | `/api/uaa/profile`                               | -       | -                                                             | `ProfileRequest`                  | `ResultProfileResponse`               | `Authorization`                                                   |
| `profile-controller`       | `PUT`    | `/profile`                               | `/api/uaa/profile`                               | -       | -                                                             | `ProfileRequest`                  | `ResultProfileResponse`               | `Authorization`                                                   |
| `profile-controller`       | `GET`    | `/profile/account/{accountId}`           | `/api/uaa/profile/account/{accountId}`           | -       | path `accountId` required string                              | -                                 | `ResultListProfileResponse`           | `Authorization`                                                   |
| `profile-controller`       | `DELETE` | `/profile/{profileId}`                   | `/api/uaa/profile/{profileId}`                   | -       | path `profileId` required string                              | -                                 | `ResultBoolean`                       | `Authorization`                                                   |
| `session-controller`       | `DELETE` | `/session/{sessionId}`                   | `/api/uaa/session/{sessionId}`                   | -       | path `sessionId` required string                              | -                                 | `ResultBoolean`                       | `Authorization`                                                   |
| `device-controller`        | `DELETE` | `/device/{deviceId}`                     | `/api/uaa/device/{deviceId}`                     | -       | path `deviceId` required string                               | -                                 | `ResultBoolean`                       | `Authorization`                                                   |
| `jwk-controller`           | `GET`    | `/.well-known/jwks.json`                 | `/api/uaa/.well-known/jwks.json`                 | -       | -                                                             | -                                 | `ResultMapStringObject`               | `Authorization`                                                   |
| `authorization-controller` | `POST`   | `/authorization/decide`                  | `/api/uaa/authorization/decide`                  | -       | -                                                             | `AuthorizationDecisionRequest`    | `ResultAuthorizationDecisionResponse` | `Authorization`                                                   |
| `rbac-controller`          | `POST`   | `/rbac/roles`                            | `/api/uaa/rbac/roles`                            | -       | -                                                             | `UpsertRoleRequest`               | `ResultRoleResponse`                  | `Authorization`                                                   |
| `rbac-controller`          | `POST`   | `/rbac/resources`                        | `/api/uaa/rbac/resources`                        | -       | -                                                             | `UpsertPermissionResourceRequest` | `ResultPermissionResourceResponse`    | `Authorization`                                                   |
| `rbac-controller`          | `POST`   | `/rbac/role-resources`                   | `/api/uaa/rbac/role-resources`                   | -       | -                                                             | `BindRoleResourceRequest`         | `ResultBoolean`                       | `Authorization`                                                   |
| `rbac-controller`          | `POST`   | `/rbac/account-roles`                    | `/api/uaa/rbac/account-roles`                    | -       | -                                                             | `BindAccountRoleRequest`          | `ResultBoolean`                       | `Authorization`                                                   |
| `rbac-controller`          | `GET`    | `/rbac/accounts/{accountId}/permissions` | `/api/uaa/rbac/accounts/{accountId}/permissions` | -       | path `accountId` required string; query `resourceType` string | -                                 | `ResultUserPermissionResponse`        | `Authorization`                                                   |
| `o-auth-client-controller` | `GET`    | `/oauth-clients`                         | `/api/uaa/oauth-clients`                         | -       | -                                                             | -                                 | `ResultListOAuthClientResponse`       | `Authorization`                                                   |
| `o-auth-client-controller` | `POST`   | `/oauth-clients`                         | `/api/uaa/oauth-clients`                         | -       | -                                                             | `CreateOAuthClientRequest`        | `ResultOAuthClientResponse`           | `Authorization`                                                   |
| `o-auth-client-controller` | `GET`    | `/oauth-clients/{clientId}`              | `/api/uaa/oauth-clients/{clientId}`              | -       | path `clientId` required string                               | -                                 | `ResultOAuthClientResponse`           | `Authorization`                                                   |

### family-uaa Request Schemas

- `PasswordLoginRequest`: `principal*`, `password*`, `clientId`, `clientSecret`, `deviceName`, `deviceFingerprint`.
- `VerifyCodeLoginRequest`: `principal*`, `verifyCode*`, `clientId`, `deviceName`, `deviceFingerprint`.
- `RegisterAccountRequest`: `username`, `email`, `phone`, `password*`.
- `PasswordRecoveryRequest`: `principal*`, `channel`.
- `ResetPasswordRequest`: `principal*`, `verificationCode*`, `newPassword*`.
- `ProfileRequest`: `profileId`, `accountId`, `nickname`, `avatar`, `language`, `region`, `profileType`.
- `AuthorizationDecisionRequest`: `accessToken`, `resourceService`, `resourcePath`, `action`.

## family-ai-qwen APIs

| Tag           | Method   | OpenAPI path           | Frontend path                 | Summary  | Request params               | Request body | Response schema                   | Auth            |
|---------------|----------|------------------------|-------------------------------|----------|------------------------------|--------------|-----------------------------------|-----------------|
| Qwen 图片理解相关接口 | `GET`    | `/ai/v1/image2Message` | `/api/ai/ai/v1/image2Message` | 图片生成文本描述 | query `file` required string | -            | `string` via response key `10000` | `Authorization` |
| Qwen 图片理解相关接口 | `POST`   | `/ai/v1/image2Message` | `/api/ai/ai/v1/image2Message` | 图片生成文本描述 | query `file` required string | -            | `string` via response key `10000` | `Authorization` |
| Qwen 图片理解相关接口 | `PUT`    | `/ai/v1/image2Message` | `/api/ai/ai/v1/image2Message` | 图片生成文本描述 | query `file` required string | -            | `string` via response key `10000` | `Authorization` |
| Qwen 图片理解相关接口 | `DELETE` | `/ai/v1/image2Message` | `/api/ai/ai/v1/image2Message` | 图片生成文本描述 | query `file` required string | -            | `string` via response key `10000` | `Authorization` |
| Qwen 图片理解相关接口 | `PATCH`  | `/ai/v1/image2Message` | `/api/ai/ai/v1/image2Message` | 图片生成文本描述 | query `file` required string | -            | `string` via response key `10000` | `Authorization` |

## Primary Risks

1. The requested OpenAPI Console URLs cannot be consumed by unauthenticated scripts right now. They return `401`, so
   automated doc generation should either implement the console challenge/signature flow with the active runtime
   credential or read the producer `/v3/api-docs` endpoints with the internal docs token.
2. OpenAPI response schemas are not contract-faithful for `Result<T>` payloads: `data` is documented as `string`, even
   for DTO/list responses. This is the main front-end type generation risk.
3. OpenAPI globally marks all UAA endpoints as requiring `Authorization`, but gateway ignore rules indicate the
   login/register/password-recovery paths are intended to be unauthenticated. This should be fixed in OpenAPI or
   explicitly documented before front-end SDK generation.
4. Qwen has only image-to-message capability in the current spec. There is no general text chat/model-list endpoint
   exposed by the pulled OpenAPI JSON.
5. Qwen frontend path may be surprising: gateway prefix `/ai` plus OpenAPI path `/ai/v1/image2Message` produces
   `/api/ai/ai/v1/image2Message`.

## Validation

- Normal path: fetched producer OpenAPI JSON for `family-core`, `family-uaa`, and `family-ai-qwen` through gateway with
  HTTP `200`.
- Failure path: direct unauthenticated access to the three requested `/openapi-console/api/openapi/{serviceId}` URLs
  returned HTTP `401`.
- Integration edge: probed Qwen business endpoint paths without JWT and received HTTP `401`, confirming authorization is
  enforced, but path shape still needs authenticated runtime verification.

## Prioritized Next Actions

1. Fix OpenAPI `Result<T>` schema generation so `data` reflects actual DTO/list payloads.
2. Override OpenAPI security for public UAA endpoints, especially login/register/password recovery/reset.
3. Decide whether Qwen external path should be `/api/ai/v1/image2Message` or `/api/ai/ai/v1/image2Message`, then align
   gateway/controller/OpenAPI.
4. Add at least one success example and one error example per一期 page endpoint group after runtime sample calls are
   available.
