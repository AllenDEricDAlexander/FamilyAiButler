# FamilyAiButler 一期业务需求分析

## 分析边界

本文档基于 `docs/api/openapi-summary.md` 中已整理的 OpenAPI 暴露能力进行业务分析，用于约束一期前端页面、接口接入和后端补齐范围。

已确认的后端服务边界：

| 服务               | 已暴露业务能力                                                     | 不应扩展解读的能力                          |
|------------------|-------------------------------------------------------------|------------------------------------|
| `family-core`    | 密码管理、分类管理、随机密码生成、密码强度检查                                     | 不包含家庭成员、共享空间、审计日志、附件上传、导入导出        |
| `family-uaa`     | 登录、注册、退出、密码恢复/重置、Profile、会话/设备删除、RBAC、OAuth Client、JWK、授权决策 | 不包含验证码发送、当前用户 `me` 查询、菜单树、前端路由权限配置 |
| `family-ai-qwen` | 图片转文本描述                                                     | 不包含通用文本聊天、模型列表、会话历史、图片生成、任务队列      |

前端接入硬约束：

- 前端所有请求必须通过 Nginx `/api` 前缀访问。
- 前端禁止直接请求 `127.0.0.1:9527`、`/base`、`/uaa`、`/ai` 等 gateway 内部地址。
- 前端禁止使用假数据、mock 数据或静态伪造业务结果。
- 缺少接口的数据只能展示空态、隐藏入口或禁用操作，并标注为后端缺口，不能在前端补造。

## 业务目标

一期目标是提供一个可登录、可鉴权接入、可管理个人密码资料、可维护密码分类、可调用图片理解能力的最小可用版本。

可衡量用户结果：

- 用户能够通过 UAA 登录链路获取令牌，并用令牌访问受保护业务接口。
- 用户能够维护密码条目，包括新增、修改、删除、查询详情、分页或列表查询。
- 用户能够维护分类和分类类型，并将其作为密码管理的基础数据。
- 用户能够提交图片输入，获得 Qwen 图片理解文本结果。
- 所有页面数据均来自 OpenAPI 已暴露接口；接口缺失时页面以空态或禁用态呈现。

## 用户角色

| 角色       | 一期可支持行为                                   | 依据                                                        |
|----------|-------------------------------------------|-----------------------------------------------------------|
| 未登录用户    | 登录、注册、发起密码恢复、提交密码重置                       | `family-uaa` 暴露登录、注册、密码恢复/重置接口；gateway 忽略规则显示这些路径预期无需 JWT |
| 已登录普通用户  | 退出登录、维护 Profile、管理密码、管理分类、调用图片理解          | 三份 OpenAPI 均声明 `Authorization`，业务接口按受保护接口处理               |
| 权限管理员    | 维护角色、资源、账号角色绑定、角色资源绑定、OAuth Client、授权决策验证 | `family-uaa` 暴露 RBAC、OAuth Client、authorization decide 接口 |
| 系统/网关集成方 | 获取 JWK、调用授权决策接口、通过令牌访问资源服务                | `/.well-known/jwks.json` 与 `/authorization/decide` 已暴露    |

角色边界：

- 当前 OpenAPI 没有家庭成员、家庭空间、租户或组织角色接口，不能设计家庭管理员/成员协作能力。
- 当前 OpenAPI 没有前端菜单、按钮权限、资源树查询接口，权限管理员页面只能围绕已暴露的 RBAC 原子操作设计。

## 业务域划分

### 认证与账号域

归属服务：`family-uaa`

支持范围：

- 密码登录：`POST /api/uaa/auth/login/password`
- 邮箱验证码登录：`POST /api/uaa/auth/login/email-code`
- 短信验证码登录：`POST /api/uaa/auth/login/sms-code`
- 注册：`POST /api/uaa/account/register`
- 当前会话退出：`POST /api/uaa/auth/logout/current`
- 全部会话退出：`POST /api/uaa/auth/logout/all`
- 密码恢复：`POST /api/uaa/auth/password/recovery`
- 密码重置：`POST /api/uaa/auth/password/reset`

约束和缺口：

- OpenAPI 全局声明 `Authorization`，但 gateway 忽略规则显示登录、注册、密码恢复、密码重置预期无需 JWT，两者不一致。
- 没有验证码发送接口；邮箱/短信验证码登录入口不能完整闭环，只能在已有验证码的前提下提交登录。
- 没有当前用户 `me` 接口；登录后首页或用户菜单如需账号信息，必须依赖令牌返回内容或 Profile 按 `accountId` 查询，不能假造用户信息。

### Profile 域

归属服务：`family-uaa`

支持范围：

- 创建 Profile：`POST /api/uaa/profile`
- 更新 Profile：`PUT /api/uaa/profile`
- 查询账号 Profile 列表：`GET /api/uaa/profile/account/{accountId}`
- 删除 Profile：`DELETE /api/uaa/profile/{profileId}`

约束和缺口：

- 查询入口依赖 `accountId`，OpenAPI 未暴露当前登录账号查询接口。
- Profile 是账号附属资料，不等同于账号安全设置；当前没有改密码、绑定邮箱、绑定手机等账号设置接口。

### 密码管理域

归属服务：`family-core`

支持范围：

- 新增密码：`POST /api/base/password/password/add`
- 修改密码：`PUT /api/base/password`
- 删除密码：`DELETE /api/base/password`
- 主键详情：`GET /api/base/password/{id}`
- 业务主键详情：`GET /api/base/password/business/{businessId}`
- 列表查询：`GET /api/base/password/password/list`
- 分页查询：`GET /api/base/password/password/list/{pageNum}/{pageSize}`
- 随机密码生成：`GET /api/base/password/generate/{passwordLength}` 及重载路径
- 密码强度检查：`GET /api/base/password/checkValid/{password}`、`GET /api/base/password/checkStrength/{password}`

约束和缺口：

- OpenAPI 中部分 GET 列表接口带请求体 `PasswordViewDTO`，前端需联调确认实际是否支持请求体查询。
- 新增接口同时出现 query 参数 `passwordView` 和 body `PasswordViewDTO`，请求契约不清晰。
- 没有收藏列表、最近访问、回收站、批量导入导出、分享、附件、密码泄露检测等接口。

### 分类管理域

归属服务：`family-core`

支持范围：

- 分类类型列表：`GET /api/base/category/type/list`
- 分类列表：`GET /api/base/category/list`
- 分类 CRUD：`POST|PUT /api/base/category/category`、`GET|DELETE /api/base/category/category/{id}`
- 分类类型 CRUD：`POST|PUT /api/base/category/category/type`、`GET|DELETE /api/base/category/category/type/{id}`

约束和缺口：

- 没有分类树、排序、启停、移动、合并接口。
- `CategoryDTO` 仅暴露 `id`、`name`、`description`、`parentId`，页面不要设计超出这些字段的配置项。

### AI 图片理解域

归属服务：`family-ai-qwen`

支持范围：

- 图片转文本描述：`GET|POST|PUT|DELETE|PATCH /api/ai/ai/v1/image2Message`

约束和缺口：

- OpenAPI path 已包含 `/ai/v1/image2Message`，叠加 gateway `/ai` 后，前端路径是 `/api/ai/ai/v1/image2Message`，该路径需要联调确认。
- 接口参数是 query `file` string，未暴露标准 multipart 上传 schema，前端不能假设支持文件上传组件直传。
- 没有文本聊天、模型列表、会话历史、结果保存接口，一期页面只能做图片理解单次调用或空态提示。

### 权限与集成域

归属服务：`family-uaa`

支持范围：

- 授权决策：`POST /api/uaa/authorization/decide`
- JWK：`GET /api/uaa/.well-known/jwks.json`
- 角色维护：`POST /api/uaa/rbac/roles`
- 权限资源维护：`POST /api/uaa/rbac/resources`
- 角色资源绑定：`POST /api/uaa/rbac/role-resources`
- 账号角色绑定：`POST /api/uaa/rbac/account-roles`
- 账号权限查询：`GET /api/uaa/rbac/accounts/{accountId}/permissions`
- OAuth Client 查询/新增/详情：`GET|POST /api/uaa/oauth-clients`、`GET /api/uaa/oauth-clients/{clientId}`

约束和缺口：

- RBAC 当前偏原子操作，没有角色列表、资源列表、账号列表、绑定关系列表、删除或分页查询接口。
- 管理后台若要完整展示权限管理，需要后端补查询类接口；否则只能提供提交型表单或隐藏管理入口。

## 核心流程

### 登录与鉴权流程

1. 用户打开登录页。
2. 前端调用 `/api/uaa/auth/login/password` 或验证码登录接口。
3. 登录成功后保存后端返回的 token pair。
4. 后续访问 `family-core`、`family-uaa` 受保护接口、`family-ai-qwen` 时统一携带 `Authorization`。
5. 用户退出时调用 `/api/uaa/auth/logout/current`；如需退出所有设备，调用 `/api/uaa/auth/logout/all`。

验收边界：

- 登录、注册、密码恢复、密码重置必须走 `/api/uaa/**`。
- 不能因为 OpenAPI security 标记不一致就在前端绕过认证规则；以运行时鉴权结果为准。
- 没有 token 或 token 失效时，受保护页面应进入未登录或无权限状态。

### 密码资料管理流程

1. 用户进入密码列表页。
2. 前端调用分页或列表接口展示真实数据。
3. 用户新增或编辑时，表单字段只使用 `PasswordViewDTO` 已暴露字段。
4. 保存成功后重新查询列表或详情。
5. 删除时调用 `DELETE /api/base/password`，成功后刷新列表。
6. 随机密码和强度检查只能调用已暴露的生成、校验接口。

验收边界：

- 接口失败或无数据时展示空态，不能填充假账号、假密码、假分类。
- 未确认列表 GET body 契约前，前端应保留联调风险，不把复杂筛选作为一期强依赖。

### 分类维护流程

1. 用户进入分类管理页。
2. 前端查询分类类型列表和分类列表。
3. 用户新增、编辑、删除分类或分类类型。
4. 操作成功后刷新对应列表。

验收边界：

- 分类树可根据 `parentId` 在前端展示，但不能假设后端支持拖拽排序、移动、合并。
- 没有分类数据时只展示空态或新增入口。

### 图片理解流程

1. 用户进入 AI 图片理解页。
2. 前端收集接口当前要求的 `file` string 参数。
3. 调用 `/api/ai/ai/v1/image2Message`。
4. 展示后端返回的文本描述。

验收边界：

- 不提供通用聊天窗口、历史会话列表、模型选择器，除非后端补接口。
- 如果联调确认路径或上传契约不可用，页面只能展示空态或禁用态。

### 权限管理流程

1. 权限管理员进入 RBAC 管理页。
2. 前端可提交角色、资源、账号角色绑定、角色资源绑定。
3. 前端可按账号查询权限。
4. 如需展示列表、编辑已有配置或删除配置，需要后端补接口。

验收边界：

- 没有列表接口时不能在前端硬编码角色、资源或账号清单。
- RBAC 管理页一期建议默认延后，或只做调试型表单。

## 一期页面清单

| 页面               | 一期状态      | 支撑服务                   | 页面说明                                |
|------------------|-----------|------------------------|-------------------------------------|
| 登录页              | 可做        | `family-uaa`           | 支持密码登录；验证码登录入口受限于缺少验证码发送接口          |
| 注册页              | 可做        | `family-uaa`           | 支持账号注册表单                            |
| 忘记密码/重置密码页       | 可做但需联调    | `family-uaa`           | 有恢复和重置接口，但验证码发放/校验体验需确认             |
| 应用框架/鉴权拦截        | 可做        | `family-uaa` + gateway | 基于 token pair 和 `Authorization` 请求头 |
| 个人资料页            | 可做但依赖账号标识 | `family-uaa`           | 可维护 Profile；缺当前用户 `me` 查询           |
| 密码列表页            | 可做        | `family-core`          | 展示真实列表或空态                           |
| 密码新增/编辑页         | 可做        | `family-core`          | 字段限制在 `PasswordViewDTO`             |
| 密码详情页            | 可做        | `family-core`          | 按 `id` 或 `businessId` 查询            |
| 随机密码生成器          | 可做        | `family-core`          | 调用生成和强度检查接口                         |
| 分类管理页            | 可做        | `family-core`          | 分类和分类类型 CRUD                        |
| AI 图片理解页         | 可做但需联调    | `family-ai-qwen`       | 仅图片转文本，不是聊天页                        |
| 权限管理页            | 建议延后      | `family-uaa`           | 原子写接口较多，缺列表/删除/分页能力                 |
| OAuth Client 管理页 | 建议延后      | `family-uaa`           | 可查列表、新增、详情，但缺更新/删除                  |
| 会话/设备管理页         | 建议延后      | `family-uaa`           | 仅有删除接口，缺会话/设备列表                     |

## 页面到接口映射

### 认证页面

| 页面    | 接口                             | 前端路径                              | 备注                 |
|-------|--------------------------------|-----------------------------------|--------------------|
| 登录页   | `POST /auth/login/password`    | `/api/uaa/auth/login/password`    | 密码登录主路径            |
| 登录页   | `POST /auth/login/email-code`  | `/api/uaa/auth/login/email-code`  | 缺验证码发送接口           |
| 登录页   | `POST /auth/login/sms-code`    | `/api/uaa/auth/login/sms-code`    | 缺验证码发送接口           |
| 注册页   | `POST /account/register`       | `/api/uaa/account/register`       | 注册成功后的自动登录能力未暴露    |
| 忘记密码页 | `POST /auth/password/recovery` | `/api/uaa/auth/password/recovery` | 返回值语义需联调           |
| 重置密码页 | `POST /auth/password/reset`    | `/api/uaa/auth/password/reset`    | 需要验证码或 recovery 结果 |
| 退出登录  | `POST /auth/logout/current`    | `/api/uaa/auth/logout/current`    | 需要 `Authorization` |

### 用户资料页面

| 页面    | 接口                                 | 前端路径                                   | 备注                |
|-------|------------------------------------|----------------------------------------|-------------------|
| 个人资料页 | `GET /profile/account/{accountId}` | `/api/uaa/profile/account/{accountId}` | 依赖 `accountId` 来源 |
| 个人资料页 | `POST /profile`                    | `/api/uaa/profile`                     | 创建资料              |
| 个人资料页 | `PUT /profile`                     | `/api/uaa/profile`                     | 更新资料              |
| 个人资料页 | `DELETE /profile/{profileId}`      | `/api/uaa/profile/{profileId}`         | 删除资料              |

### 密码管理页面

| 页面      | 接口                                                                                    | 前端路径                                                                                       | 备注                         |
|---------|---------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|----------------------------|
| 密码列表页   | `GET /password/password/list`                                                         | `/api/base/password/password/list`                                                         | 列表接口                       |
| 密码列表页   | `GET /password/password/list/{pageNum}/{pageSize}`                                    | `/api/base/password/password/list/{pageNum}/{pageSize}`                                    | 分页接口                       |
| 密码详情页   | `GET /password/{id}`                                                                  | `/api/base/password/{id}`                                                                  | 主键详情                       |
| 密码详情页   | `GET /password/business/{businessId}`                                                 | `/api/base/password/business/{businessId}`                                                 | 业务主键详情                     |
| 密码新增页   | `POST /password/password/add`                                                         | `/api/base/password/password/add`                                                          | 请求契约需确认 query/body 并存问题    |
| 密码编辑页   | `PUT /password`                                                                       | `/api/base/password`                                                                       | 修改                         |
| 密码列表页   | `DELETE /password`                                                                    | `/api/base/password`                                                                       | 删除，body 为 `array<integer>` |
| 随机密码生成器 | `GET /password/generate/{passwordLength}`                                             | `/api/base/password/generate/{passwordLength}`                                             | 基础生成                       |
| 随机密码生成器 | `GET /password/generate/{passwordLength}/{needSpecialCharacters}`                     | `/api/base/password/generate/{passwordLength}/{needSpecialCharacters}`                     | 特殊字符开关                     |
| 随机密码生成器 | `GET /password/generate/{passwordLength}/{needSpecialCharacters}/{specialCharacters}` | `/api/base/password/generate/{passwordLength}/{needSpecialCharacters}/{specialCharacters}` | 自定义特殊字符                    |
| 随机密码生成器 | `GET /password/checkValid/{password}`                                                 | `/api/base/password/checkValid/{password}`                                                 | 合法性检查                      |
| 随机密码生成器 | `GET /password/checkStrength/{password}`                                              | `/api/base/password/checkStrength/{password}`                                              | 强度详情                       |

### 分类管理页面

| 页面     | 接口                                    | 前端路径                                    | 备注     |
|--------|---------------------------------------|-----------------------------------------|--------|
| 分类管理页  | `GET /category/type/list`             | `/api/base/category/type/list`          | 分类类型列表 |
| 分类管理页  | `GET /category/list`                  | `/api/base/category/list`               | 分类列表   |
| 分类管理页  | `POST /category/category`             | `/api/base/category/category`           | 新增分类   |
| 分类管理页  | `PUT /category/category`              | `/api/base/category/category`           | 更新分类   |
| 分类管理页  | `GET /category/category/{id}`         | `/api/base/category/category/{id}`      | 分类详情   |
| 分类管理页  | `DELETE /category/category/{id}`      | `/api/base/category/category/{id}`      | 删除分类   |
| 分类类型管理 | `POST /category/category/type`        | `/api/base/category/category/type`      | 新增分类类型 |
| 分类类型管理 | `PUT /category/category/type`         | `/api/base/category/category/type`      | 更新分类类型 |
| 分类类型管理 | `GET /category/category/type/{id}`    | `/api/base/category/category/type/{id}` | 分类类型详情 |
| 分类类型管理 | `DELETE /category/category/type/{id}` | `/api/base/category/category/type/{id}` | 删除分类类型 |

### AI 图片理解页面

| 页面       | 接口                            | 前端路径                          | 备注                  |
|----------|-------------------------------|-------------------------------|---------------------|
| AI 图片理解页 | `GET /ai/v1/image2Message`    | `/api/ai/ai/v1/image2Message` | query `file` string |
| AI 图片理解页 | `POST /ai/v1/image2Message`   | `/api/ai/ai/v1/image2Message` | query `file` string |
| AI 图片理解页 | `PUT /ai/v1/image2Message`    | `/api/ai/ai/v1/image2Message` | query `file` string |
| AI 图片理解页 | `DELETE /ai/v1/image2Message` | `/api/ai/ai/v1/image2Message` | query `file` string |
| AI 图片理解页 | `PATCH /ai/v1/image2Message`  | `/api/ai/ai/v1/image2Message` | query `file` string |

### 权限与集成页面

| 页面              | 接口                                           | 前端路径                                             | 备注      |
|-----------------|----------------------------------------------|--------------------------------------------------|---------|
| 权限调试页           | `POST /authorization/decide`                 | `/api/uaa/authorization/decide`                  | 授权决策    |
| 系统集成            | `GET /.well-known/jwks.json`                 | `/api/uaa/.well-known/jwks.json`                 | JWK     |
| RBAC 管理         | `POST /rbac/roles`                           | `/api/uaa/rbac/roles`                            | 新增或更新角色 |
| RBAC 管理         | `POST /rbac/resources`                       | `/api/uaa/rbac/resources`                        | 新增或更新资源 |
| RBAC 管理         | `POST /rbac/role-resources`                  | `/api/uaa/rbac/role-resources`                   | 绑定角色资源  |
| RBAC 管理         | `POST /rbac/account-roles`                   | `/api/uaa/rbac/account-roles`                    | 绑定账号角色  |
| RBAC 管理         | `GET /rbac/accounts/{accountId}/permissions` | `/api/uaa/rbac/accounts/{accountId}/permissions` | 查询账号权限  |
| OAuth Client 管理 | `GET /oauth-clients`                         | `/api/uaa/oauth-clients`                         | 客户端列表   |
| OAuth Client 管理 | `POST /oauth-clients`                        | `/api/uaa/oauth-clients`                         | 新增客户端   |
| OAuth Client 管理 | `GET /oauth-clients/{clientId}`              | `/api/uaa/oauth-clients/{clientId}`              | 客户端详情   |

## 三个服务分别支持的页面

| 服务               | 可直接支持页面                                 | 受限支持页面                     | 不支持页面                                 |
|------------------|-----------------------------------------|----------------------------|---------------------------------------|
| `family-core`    | 密码列表、密码详情、密码新增/编辑、随机密码生成器、分类管理          | 复杂筛选密码列表、分类树展示             | 家庭协作、审计日志、导入导出、密码分享                   |
| `family-uaa`     | 登录、注册、退出、Profile、权限调试、OAuth Client 基础管理 | 验证码登录、忘记密码、RBAC 管理、会话/设备管理 | 当前用户 `me`、验证码发送、菜单权限、账号列表、角色/资源列表完整后台 |
| `family-ai-qwen` | AI 图片理解单次调用页                            | 文件输入方式需联调的图片理解页            | 通用 AI 聊天、模型管理、历史记录、图片生成               |

## 认证/登录链路评估

结论：当前认证/登录链路可以支撑一期最小登录和受保护接口访问，但不足以无歧义支撑完整前端账号体验。

已足够的部分：

- 已暴露密码登录、邮箱验证码登录、短信验证码登录、注册、退出、密码恢复、密码重置接口。
- 已暴露 token pair 响应 schema 名称，说明登录成功后预期可获得访问令牌。
- 三个服务 OpenAPI 均声明 `Authorization`，一期前端可以统一使用 `Authorization` 头接入受保护接口。
- gateway 忽略规则显示登录、注册、密码恢复、密码重置预期为公开入口。

不足和风险：

- OpenAPI 全局 security 与公开登录/注册路径存在描述冲突，需要后端修正 OpenAPI 或联调确认。
- 没有验证码发送接口，验证码登录与密码恢复的闭环不足。
- 没有当前登录用户信息接口，前端无法稳定获取 `accountId`、用户名、头像等当前用户展示数据。
- `JWK` 也被 OpenAPI 标记为 `Authorization`，但 JWK 通常用于令牌校验集成，是否应公开需要产品/后端决策。

前端一期处理原则：

- 登录成功前只开放登录、注册、忘记密码、重置密码页面。
- 登录成功后统一给所有业务请求加 `Authorization`。
- 当前用户资料缺接口时，不展示假头像、假昵称、假账号，只展示空态或从 token 可解析字段中展示已确认字段。

## 后端缺口清单

| 优先级 | 缺口                                          | 影响页面            | 建议后端补齐                              |
|-----|---------------------------------------------|-----------------|-------------------------------------|
| P0  | OpenAPI security 与公开登录/注册/密码恢复路径不一致         | 登录、注册、忘记密码      | 为公开接口覆盖 security，或输出明确鉴权规则          |
| P0  | 缺当前用户 `me` 接口                               | 应用框架、个人资料、用户菜单  | 新增 `GET /uaa/account/me` 或等价接口      |
| P0  | `Result<T>` OpenAPI schema 未展开真实 `data` 类型  | 所有页面            | 修正统一响应包装的 OpenAPI schema            |
| P0  | 前端路径必须经 `/api` 的规则需要在联调环境固化                 | 所有页面            | Nginx/gateway 文档和运行配置保持一致           |
| P1  | 缺验证码发送接口                                    | 验证码登录、密码恢复      | 新增发送邮箱/短信验证码接口                      |
| P1  | 密码新增、列表查询请求契约不清晰                            | 密码新增、密码列表       | 明确 query/body 使用方式，避免 GET body      |
| P1  | Qwen 图片接口 `file` 参数不是标准上传契约                 | AI 图片理解         | 明确是 URL/base64/文件标识，或改为 multipart   |
| P1  | Qwen 路径 `/api/ai/ai/v1/image2Message` 形态需确认 | AI 图片理解         | 对齐 gateway prefix 与 Controller path |
| P2  | 缺角色、资源、账号、绑定关系列表/删除接口                       | RBAC 管理         | 补查询、分页、删除能力                         |
| P2  | 缺会话/设备列表接口                                  | 会话/设备管理         | 补列表查询后再做管理页                         |
| P2  | 缺 OAuth Client 更新/删除接口                      | OAuth Client 管理 | 补完整 CRUD                            |
| P2  | 缺分类排序、移动、树查询接口                              | 分类管理            | 如要树形管理再补接口                          |
| P3  | 缺密码导入导出、分享、审计、回收站能力                         | 密码高级功能          | 后续版本评估                              |

## 范围控制

一期明确纳入：

- 登录、注册、退出、忘记密码/重置密码基础链路。
- 密码管理 CRUD、列表、详情、随机密码、强度检查。
- 分类和分类类型 CRUD。
- Profile 基础维护。
- 图片理解单次调用。
- 统一 `/api` 请求前缀和 `Authorization` 请求头处理。

一期明确不纳入：

- 假数据、mock 数据、静态伪造页面数据。
- 直接请求 `127.0.0.1:9527` 或绕过 Nginx `/api`。
- 通用 AI 聊天、模型选择、AI 会话历史。
- 家庭成员、家庭空间、共享密码。
- 完整 RBAC 后台、菜单权限、按钮权限。
- 会话/设备列表管理。
- 密码导入导出、分享、审计、回收站。

延期但可预留入口：

- RBAC 管理页。
- OAuth Client 管理页。
- 会话/设备管理页。
- 高级密码管理功能。

## 验收标准

通用验收：

- 所有业务请求 URL 均以 `/api` 开头。
- 代码和配置中不得出现前端直接请求 `127.0.0.1:9527` 的业务调用。
- 页面数据必须来自真实接口响应。
- 接口缺失、失败或返回空数据时展示空态、禁用态或隐藏入口，不展示假数据。
- 受保护接口统一携带 `Authorization`。

页面验收：

- 登录页可以通过 `/api/uaa/auth/login/password` 完成密码登录联调。
- 注册页可以通过 `/api/uaa/account/register` 提交注册。
- 密码列表、详情、新增、编辑、删除均映射到 `family-core` 已暴露接口。
- 分类和分类类型管理均映射到 `family-core` 已暴露接口。
- AI 页面只展示图片理解能力，不出现聊天历史、模型列表或文本问答承诺。
- RBAC、OAuth、会话/设备页面如后端缺查询能力，必须延后或展示明确空态。

失败边界：

- 任一页面依赖未暴露接口却展示完整功能，视为需求越界。
- 任一页面使用 mock 或硬编码业务数据，视为验收失败。
- 任一请求绕过 `/api` 直接访问 gateway 或服务地址，视为验收失败。
- 将 `family-ai-qwen` 描述为通用聊天服务，视为验收失败。

## 待产品/后端决策点

| 决策点         | 需要确认的问题                                                                  | 不确认的影响               |
|-------------|--------------------------------------------------------------------------|----------------------|
| 登录公开接口      | OpenAPI 是否要去掉登录、注册、密码恢复、密码重置的 `Authorization` 标记                         | 前端 SDK 和接口拦截规则容易冲突   |
| 当前用户信息      | 是否新增当前用户 `me` 接口                                                         | 用户菜单、Profile 默认加载不稳定 |
| 验证码能力       | 是否一期支持发送邮箱/短信验证码                                                         | 验证码登录和忘记密码体验不完整      |
| Qwen 路径     | 外部路径到底是 `/api/ai/ai/v1/image2Message` 还是需要调整为 `/api/ai/v1/image2Message` | AI 页面联调失败风险          |
| Qwen 文件参数   | `file` string 的真实语义是 URL、base64、文件 ID 还是 multipart 文件                    | 前端输入组件无法定型           |
| RBAC 后台     | 一期是否需要完整权限管理页                                                            | 若需要，后端需补列表、分页、删除接口   |
| 响应包装 schema | 是否修复 OpenAPI 中 `Result<T>.data` 类型                                       | 前端类型生成和运行时解析风险       |

## 工程交付建议

建议工程按以下顺序推进：

1. 先固化前端 API 客户端规则：统一 `/api` 前缀、统一 `Authorization` 注入、统一 `Result` 包装处理、统一空态处理。
2. 优先实现登录、密码管理、分类管理三个可闭环页面。
3. Profile 页面只在能拿到 `accountId` 的前提下实现，否则先保留空态。
4. AI 图片理解页先做最小联调页，确认路径和 `file` 参数语义后再扩展体验。
5. RBAC、OAuth Client、会话/设备管理页在后端补齐查询接口后再进入一期主流程。
