# 统一 UAA / CIAM / RBAC3 / OAuth2 / SSO / ToC 权益平台需求方案

## 1. 方案定位

本系统定位为一套面向 **ToB + ToC + 开放平台 + 内容/会员业务** 的统一身份认证、权限控制、单点登录和权益授权平台。

它不是单纯的后台登录系统，也不是普通 RBAC 权限系统，而是类似以下产品形态的统一账号与授权底座：

| 参考形态      | 对应能力                                      |
|-----------|-------------------------------------------|
| OpenAI 类  | 账号、组织、项目、API Key、Service Account、额度、开发者权限 |
| X.com 类   | 账号、公开身份、社交关系、风控、内容发布权限                    |
| Netflix 类 | 账号、Profile、会员套餐、设备限制、内容观看权益               |
| YouTube 类 | 账号、频道、创作者、频道团队权限、内容审核、收益权限                |

最终系统应具备：

```text
统一账号体系
统一认证登录
统一 OAuth2 / OIDC / SSO
统一 RBAC3 权限
统一会员权益
统一内容访问授权
统一设备会话管理
统一开放平台授权
统一风控与审计
统一管理后台与用户自助中心
```

推荐产品名称：

```text
统一身份认证与权益授权平台
```

英文可称为：

```text
Unified Identity, Access & Entitlement Platform
```

---

# 2. 建设目标

## 2.1 总体目标

建设一套统一身份与权限平台，为内部后台、C 端用户、创作者、开发者、第三方应用、业务微服务提供统一认证、授权、单点登录、权限判断、会员权益判断、设备会话管理和安全审计能力。

## 2.2 核心目标

| 目标             | 说明                                               |
|----------------|--------------------------------------------------|
| 统一账号           | 支持 C 端账号、后台账号、组织账号、开发者账号、服务账号                    |
| 统一登录           | 支持密码、验证码、第三方登录、MFA、Passkey 预留                    |
| 统一 SSO         | 所有业务系统接入统一认证中心，实现单点登录和单点退出                       |
| 统一 OAuth2/OIDC | 支持标准授权、Token 签发、用户授权同意、第三方应用接入                   |
| 统一 RBAC3       | 支持角色继承、职责分离、互斥角色、动态角色、数据权限                       |
| 统一权益           | 支持会员套餐、功能权益、内容权益、API 额度、设备限制                     |
| 统一 Profile     | 支持主 Profile、儿童 Profile、创作者 Profile、品牌 Profile    |
| 统一设备会话         | 支持设备登录、可信设备、远程登出、并发控制                            |
| 统一开放平台         | 支持 OAuth Client、API Key、Service Account、Scope 审核 |
| 统一风控           | 支持异常登录、账号封禁、内容限制、接口限流、风险策略                       |
| 统一审计           | 记录登录、授权、权限、权益、风控、管理操作等日志                         |

---

# 3. 系统边界

## 3.1 系统包含范围

| 模块                        | 是否包含 |
|---------------------------|------|
| C 端账号体系                   | 包含   |
| 后台管理员体系                   | 包含   |
| OAuth2 授权中心               | 包含   |
| OIDC 登录认证                 | 包含   |
| SSO 单点登录                  | 包含   |
| RBAC3 权限管理                | 包含   |
| 会员权益管理                    | 包含   |
| 内容访问授权                    | 包含   |
| 设备与会话管理                   | 包含   |
| Profile 管理                | 包含   |
| 组织 / 项目 / 频道权限            | 包含   |
| API Key / Service Account | 包含   |
| 第三方应用授权                   | 包含   |
| 风控策略                      | 包含   |
| 审计日志                      | 包含   |
| 用户自助中心                    | 包含   |
| 运营管理后台                    | 包含   |
| 业务系统接入能力                  | 包含   |

## 3.2 系统不直接包含范围

| 模块      | 说明                              |
|---------|---------------------------------|
| 内容生产系统  | 本系统只判断内容能不能访问，不负责内容生产           |
| 支付网关    | 本系统保存订阅和权益状态，但支付扣款可由外部支付系统完成    |
| 推荐算法    | 本系统可提供身份和 Profile 数据，但不负责推荐逻辑   |
| 视频播放服务  | 本系统判断播放权限，不负责流媒体播放              |
| 社交内容主业务 | 本系统提供账号、关系、权限和风控，不负责完整 Feed 业务  |
| 消息推送底层  | 本系统触发通知事件，具体短信、邮件、Push 可由通知服务完成 |

---

# 4. 目标用户与主体类型

## 4.1 主体类型

系统中不应只存在一个简单的 `User`，而要区分不同主体。

| 主体              | 说明                         |
|-----------------|----------------------------|
| Account         | C 端用户账号，负责登录和身份识别          |
| Profile         | 账号下的使用身份，例如个人、儿童、创作者、品牌    |
| Admin User      | 内部管理员、客服、运营、审核员            |
| Organization    | 组织、团队、公司、工作室               |
| Project         | 组织下的项目，例如 API 项目、业务项目      |
| Channel         | 创作者频道、内容空间                 |
| Developer App   | 第三方开发者应用                   |
| Service Account | 服务账号、机器账号                  |
| API Key         | 开发者或项目调用 API 的凭证           |
| OAuth Client    | 接入 OAuth2/OIDC 的业务系统或第三方应用 |
| Device          | 用户登录设备                     |
| Session         | 用户登录会话                     |
| Subscription    | 用户订阅关系                     |
| Entitlement     | 用户实际拥有的权益                  |
| Resource        | 被访问的资源，例如接口、菜单、内容、视频、频道、项目 |

---

# 5. 总体业务架构

```text
                         用户 / 管理员 / 开发者 / 创作者
                                      │
                                      ▼
                            Web / App / TV / API
                                      │
                                      ▼
                              API Gateway / BFF
                                      │
        ┌─────────────────────────────┼─────────────────────────────┐
        ▼                             ▼                             ▼
统一认证授权中心              ToC 账号中心                  管理与运营后台
OAuth2 / OIDC / SSO           Account / Profile              Admin Console
Token / Session / MFA         Device / Security              Audit / Risk
        │                             │                             │
        ▼                             ▼                             ▼
 RBAC3 权限中心              权益授权中心                   风控审计中心
Role / Permission             Plan / Subscription             Risk / Audit
Hierarchy / Constraint        Feature / Quota                 Login / Action
        │                             │                             │
        └───────────────┬─────────────┴───────────────┬─────────────┘
                        ▼                             ▼
                 业务资源服务                    开放平台服务
        Content / Creator / Social          API Key / Developer App
        SaaS / Backend / Microservice       Webhook / Scope / Consent
```

---

# 6. 总体权限模型

本系统不能只使用 RBAC。ToC 场景下，权限判断需要同时考虑角色、属性、关系、权益、风险和合规。

因此采用：

```text
RBAC3 + ABAC + ReBAC + Entitlement + Risk Policy
```

## 6.1 权限模型说明

| 模型          | 用途                       |
|-------------|--------------------------|
| RBAC3       | 后台权限、组织权限、项目权限、频道团队权限    |
| ABAC        | 根据地区、年龄、设备、账号状态、风险等级判断   |
| ReBAC       | 根据关注、拉黑、好友、家庭成员、频道成员关系判断 |
| Entitlement | 根据套餐、订阅、额度、购买记录判断        |
| Risk Policy | 根据账号风险、设备风险、支付风险、内容风险判断  |

## 6.2 综合判断公式

```text
是否允许访问 =
    身份认证结果
AND RBAC3 权限
AND 权益授权结果
AND 属性策略结果
AND 关系策略结果
AND 风控策略结果
AND 合规策略结果
```

示例一：用户能否观看某视频。

```text
用户已登录
AND 账号状态正常
AND 用户所在地区允许观看
AND Profile 年龄满足内容分级
AND 用户套餐包含该内容
AND 设备数量未超过限制
AND 并发播放未超过限制
AND 账号未被风控限制
```

示例二：用户能否创建 API Key。

```text
用户已登录
AND 用户属于某组织
AND 用户在该项目中拥有 api_key:create 权限
AND 项目未欠费
AND API Key 数量未超限
AND 当前操作通过二次验证
```

示例三：创作者能否发布视频。

```text
用户已登录
AND 用户属于该频道团队
AND 用户拥有 channel:content:publish 权限
AND 频道状态正常
AND 用户未被禁言
AND 内容审核策略允许发布
```

---

# 7. 账号体系需求

## 7.1 账号注册

| 编号     | 需求                                  | 优先级 |
|--------|-------------------------------------|-----|
| ACC-01 | 支持邮箱注册                              | P0  |
| ACC-02 | 支持手机号注册                             | P0  |
| ACC-03 | 支持用户名注册                             | P0  |
| ACC-04 | 支持第三方账号注册，例如 Google、Apple、微信、GitHub | P1  |
| ACC-05 | 支持邀请注册                              | P1  |
| ACC-06 | 支持开发者账号注册                           | P1  |
| ACC-07 | 支持创作者账号申请                           | P1  |
| ACC-08 | 支持企业 / 组织账号申请                       | P1  |
| ACC-09 | 支持游客账号升级为正式账号                       | P2  |
| ACC-10 | 支持账号注册风控校验                          | P0  |

## 7.2 账号登录

| 编号     | 需求                       | 优先级 |
|--------|--------------------------|-----|
| ACC-11 | 支持用户名 + 密码登录             | P0  |
| ACC-12 | 支持邮箱 + 密码登录              | P0  |
| ACC-13 | 支持手机号 + 密码登录             | P0  |
| ACC-14 | 支持邮箱验证码登录                | P0  |
| ACC-15 | 支持短信验证码登录                | P0  |
| ACC-16 | 支持第三方登录                  | P1  |
| ACC-17 | 支持 Magic Link 邮件登录       | P1  |
| ACC-18 | 支持 Passkey / WebAuthn 预留 | P1  |
| ACC-19 | 支持 MFA 多因素认证             | P1  |
| ACC-20 | 支持高危操作二次认证               | P0  |

## 7.3 账号状态

| 状态               | 说明       |
|------------------|----------|
| NORMAL           | 正常       |
| UNVERIFIED       | 未验证      |
| LOCKED           | 登录失败锁定   |
| SUSPENDED        | 临时冻结     |
| BANNED           | 永久封禁     |
| DELETED          | 已注销      |
| RISK_LIMITED     | 风控限制     |
| PENDING_REVIEW   | 待审核      |
| PASSWORD_EXPIRED | 密码过期     |
| MFA_REQUIRED     | 需要完成 MFA |

## 7.4 账号找回与恢复

| 编号     | 需求               | 优先级 |
|--------|------------------|-----|
| ACC-21 | 支持邮箱找回密码         | P0  |
| ACC-22 | 支持手机找回密码         | P0  |
| ACC-23 | 支持备用码恢复 MFA      | P1  |
| ACC-24 | 支持账号申诉恢复         | P1  |
| ACC-25 | 支持账号恢复过程风控       | P0  |
| ACC-26 | 支持重置密码后撤销旧 Token | P0  |
| ACC-27 | 支持找回成功后通知用户      | P0  |

## 7.5 账号绑定与合并

| 编号     | 需求        | 优先级 |
|--------|-----------|-----|
| ACC-28 | 支持绑定邮箱    | P0  |
| ACC-29 | 支持绑定手机号   | P0  |
| ACC-30 | 支持绑定第三方账号 | P1  |
| ACC-31 | 支持解绑第三方账号 | P1  |
| ACC-32 | 支持账号冲突检测  | P1  |
| ACC-33 | 支持账号合并流程  | P2  |
| ACC-34 | 支持绑定和解绑审计 | P0  |

## 7.6 账号注销

| 编号     | 需求                    | 优先级 |
|--------|-----------------------|-----|
| ACC-35 | 用户可主动申请注销账号           | P0  |
| ACC-36 | 注销前校验未完成订单、订阅、余额、组织责任 | P0  |
| ACC-37 | 注销需二次验证               | P0  |
| ACC-38 | 注销后撤销所有会话和 Token      | P0  |
| ACC-39 | 注销后保留必要审计记录           | P0  |
| ACC-40 | 支持冷静期恢复，是否启用由业务配置     | P1  |

---

# 8. Profile 需求

## 8.1 Profile 类型

| 类型        | 说明              |
|-----------|-----------------|
| MAIN      | 主 Profile       |
| KIDS      | 儿童 Profile      |
| CREATOR   | 创作者 Profile     |
| BUSINESS  | 品牌 / 商业 Profile |
| DEVELOPER | 开发者 Profile     |
| ANONYMOUS | 游客 / 匿名 Profile |

## 8.2 Profile 功能

| 编号     | 需求                       | 优先级 |
|--------|--------------------------|-----|
| PRO-01 | 一个账号支持多个 Profile         | P0  |
| PRO-02 | 支持创建、编辑、删除 Profile       | P0  |
| PRO-03 | 支持切换当前 Profile           | P0  |
| PRO-04 | 支持 Profile 头像、昵称、语言、地区设置 | P0  |
| PRO-05 | 支持儿童 Profile             | P1  |
| PRO-06 | 支持儿童内容分级控制               | P1  |
| PRO-07 | 支持 Profile 隐私设置          | P1  |
| PRO-08 | 支持创作者 Profile            | P1  |
| PRO-09 | 支持品牌 Profile             | P2  |
| PRO-10 | 支持 Profile 级内容历史和偏好      | P2  |

## 8.3 Account 与 Profile 规则

```text
Account 负责登录、认证、订阅、账单和安全。
Profile 负责展示身份、内容偏好、年龄分级和使用体验。
```

强制规则：

| 规则                            | 说明 |
|-------------------------------|----|
| 一个 Account 至少有一个 Main Profile | 必须 |
| 儿童 Profile 不允许执行高危操作          | 必须 |
| 儿童 Profile 不允许管理订阅和支付         | 必须 |
| Profile 删除不等于 Account 注销      | 必须 |
| 切换 Profile 不应重新登录             | 必须 |
| 访问内容时必须携带当前 Profile 上下文       | 必须 |

---

# 9. 认证与会话需求

## 9.1 认证方式

| 认证方式    | 说明                                         | 优先级 |
|---------|--------------------------------------------|-----|
| 密码认证    | 用户名 / 邮箱 / 手机号 + 密码                        | P0  |
| 验证码认证   | 邮箱验证码、短信验证码                                | P0  |
| 第三方认证   | Google、Apple、微信、GitHub 等                   | P1  |
| MFA     | TOTP、短信、邮箱、安全密钥                            | P1  |
| Passkey | 无密码登录                                      | P1  |
| 临时认证    | 高危操作二次验证                                   | P0  |
| 服务认证    | Client Credentials、API Key、Service Account | P0  |

## 9.2 登录安全策略

| 编号      | 需求                        | 优先级 |
|---------|---------------------------|-----|
| AUTH-01 | 登录失败次数限制                  | P0  |
| AUTH-02 | 登录失败账号锁定                  | P0  |
| AUTH-03 | 支持图形验证码 / 行为验证码           | P1  |
| AUTH-04 | 新设备登录提醒                   | P0  |
| AUTH-05 | 异地登录提醒                    | P0  |
| AUTH-06 | 高风险登录要求 MFA               | P0  |
| AUTH-07 | 密码复杂度策略                   | P0  |
| AUTH-08 | 密码有效期策略                   | P1  |
| AUTH-09 | 密码历史记录限制                  | P1  |
| AUTH-10 | 登录后记录 IP、设备、User-Agent、地区 | P0  |

## 9.3 会话管理

| 编号     | 需求              | 优先级 |
|--------|-----------------|-----|
| SES-01 | 支持 Web 会话       | P0  |
| SES-02 | 支持 App 会话       | P0  |
| SES-03 | 支持 TV / 大屏设备会话  | P1  |
| SES-04 | 支持 CLI / API 会话 | P1  |
| SES-05 | 支持查看当前登录会话      | P0  |
| SES-06 | 支持踢出单个会话        | P0  |
| SES-07 | 支持全部设备退出        | P0  |
| SES-08 | 支持管理员强制下线       | P0  |
| SES-09 | 支持账号封禁后自动撤销会话   | P0  |
| SES-10 | 支持订阅降级后刷新权益会话   | P0  |

---

# 10. 设备管理需求

## 10.1 设备类型

| 类型      | 说明      |
|---------|---------|
| WEB     | 浏览器     |
| MOBILE  | 手机 App  |
| DESKTOP | 桌面客户端   |
| TV      | 电视 / 大屏 |
| CLI     | 命令行     |
| API     | API 调用端 |
| UNKNOWN | 未识别设备   |

## 10.2 设备功能

| 编号     | 需求           | 优先级 |
|--------|--------------|-----|
| DEV-01 | 自动登记登录设备     | P0  |
| DEV-02 | 支持设备名称展示     | P0  |
| DEV-03 | 支持用户查看设备列表   | P0  |
| DEV-04 | 支持用户移除设备     | P0  |
| DEV-05 | 支持可信设备标记     | P1  |
| DEV-06 | 支持设备风险评分     | P1  |
| DEV-07 | 支持新设备登录验证    | P0  |
| DEV-08 | 支持套餐限制最大设备数  | P0  |
| DEV-09 | 支持并发在线限制     | P0  |
| DEV-10 | 支持 TV 端设备码登录 | P2  |

## 10.3 设备限制规则

| 规则        | 说明                       |
|-----------|--------------------------|
| 免费用户设备数限制 | 可配置                      |
| 会员用户设备数限制 | 按套餐配置                    |
| 家庭套餐设备数限制 | 按套餐配置                    |
| 同时在线数量限制  | 可配置                      |
| 同时播放数量限制  | 内容类业务必须支持                |
| 高风险设备登录   | 必须要求额外验证                 |
| 用户移除设备    | 应撤销该设备相关 Token 和 Session |

---

# 11. OAuth2 / OIDC / SSO 需求

## 11.1 OAuth2 授权能力

| 编号       | 需求                           | 优先级 |
|----------|------------------------------|-----|
| OAUTH-01 | 支持 Authorization Code 授权模式   | P0  |
| OAUTH-02 | 支持 PKCE                      | P0  |
| OAUTH-03 | 支持 Client Credentials        | P0  |
| OAUTH-04 | 支持 Refresh Token             | P0  |
| OAUTH-05 | 支持 Token Revocation          | P0  |
| OAUTH-06 | 支持 Token Introspection       | P1  |
| OAUTH-07 | 支持 Device Authorization Flow | P2  |
| OAUTH-08 | 支持 Scope 管理                  | P0  |
| OAUTH-09 | 支持用户授权同意页                    | P0  |
| OAUTH-10 | 支持第三方应用授权撤销                  | P0  |

## 11.2 OIDC 能力

| 编号      | 需求                     | 优先级 |
|---------|------------------------|-----|
| OIDC-01 | 支持 ID Token            | P0  |
| OIDC-02 | 支持 Access Token        | P0  |
| OIDC-03 | 支持 Refresh Token       | P0  |
| OIDC-04 | 支持 UserInfo Endpoint   | P0  |
| OIDC-05 | 支持 Discovery Endpoint  | P0  |
| OIDC-06 | 支持 JWKS Endpoint       | P0  |
| OIDC-07 | 支持 RP-Initiated Logout | P1  |
| OIDC-08 | 支持 nonce 校验            | P0  |
| OIDC-09 | 支持 state 校验            | P0  |
| OIDC-10 | 支持多客户端 SSO             | P0  |

## 11.3 SSO 单点登录

| 编号     | 需求                    | 优先级 |
|--------|-----------------------|-----|
| SSO-01 | 用户登录 UAA 后访问其他系统免登录   | P0  |
| SSO-02 | 支持多个业务系统接入统一登录        | P0  |
| SSO-03 | 支持 Web 应用 SSO         | P0  |
| SSO-04 | 支持 App 应用 SSO         | P1  |
| SSO-05 | 支持管理后台 SSO            | P0  |
| SSO-06 | 支持用户中心 SSO            | P0  |
| SSO-07 | 支持单点退出                | P0  |
| SSO-08 | 支持本应用退出               | P0  |
| SSO-09 | 支持全局退出                | P0  |
| SSO-10 | 支持退出后撤销 Refresh Token | P0  |

## 11.4 OAuth Client 管理

| 编号        | 需求                       | 优先级 |
|-----------|--------------------------|-----|
| CLIENT-01 | 支持创建 OAuth Client        | P0  |
| CLIENT-02 | 支持 Client ID / Secret 管理 | P0  |
| CLIENT-03 | 支持 Secret 轮换             | P0  |
| CLIENT-04 | 支持 Redirect URI 白名单      | P0  |
| CLIENT-05 | 支持 Allowed Origins 配置    | P0  |
| CLIENT-06 | 支持 Grant Type 配置         | P0  |
| CLIENT-07 | 支持 Scope 授权配置            | P0  |
| CLIENT-08 | 支持 Token 有效期配置           | P0  |
| CLIENT-09 | 支持 Client 禁用             | P0  |
| CLIENT-10 | 支持 Client 审计日志           | P0  |

---

# 12. Token 需求

## 12.1 Token 类型

| Token                 | 用途              |
|-----------------------|-----------------|
| Access Token          | 访问资源服务          |
| ID Token              | 表达登录认证结果        |
| Refresh Token         | 刷新 Access Token |
| Authorization Code    | 授权码             |
| Device Code           | 设备登录            |
| API Key               | 开发者接口调用         |
| Service Account Token | 服务账号访问          |
| Temporary Token       | 高危操作临时授权        |

## 12.2 Token 内容要求

Access Token 建议包含：

```json
{
  "iss": "https://uaa.example.com",
  "sub": "acc_10001",
  "profile_id": "prof_20001",
  "client_id": "web-app",
  "aud": [
    "content-service",
    "api-service"
  ],
  "scope": [
    "openid",
    "profile",
    "content.read"
  ],
  "account_type": "consumer",
  "device_id": "dev_30001",
  "session_id": "sess_40001",
  "tenant_id": "tenant_001",
  "auth_version": 12,
  "entitlement_version": 18,
  "risk_level": "LOW",
  "jti": "jwt_50001",
  "iat": 1760000000,
  "exp": 1760001800
}
```

## 12.3 Token 不应包含的内容

| 内容       | 原因               |
|----------|------------------|
| 完整权限列表   | Token 过大，权限变更不实时 |
| 完整订阅信息   | 套餐、退款、欠费状态变化频繁   |
| 完整设备列表   | 设备动态变化           |
| 完整社交关系   | 数据量大，变化频繁        |
| 完整内容访问列表 | 每个内容权限不同，不适合静态写入 |
| 敏感个人信息   | 避免泄露             |

## 12.4 Token 策略

| 编号     | 需求                          | 优先级 |
|--------|-----------------------------|-----|
| TOK-01 | Access Token 短有效期           | P0  |
| TOK-02 | Refresh Token 可配置有效期        | P0  |
| TOK-03 | Refresh Token 轮换            | P0  |
| TOK-04 | 支持 Token 撤销                 | P0  |
| TOK-05 | 支持用户维度撤销全部 Token            | P0  |
| TOK-06 | 支持设备维度撤销 Token              | P0  |
| TOK-07 | 支持 Client 维度撤销 Token        | P0  |
| TOK-08 | 支持权限版本号 auth_version        | P0  |
| TOK-09 | 支持权益版本号 entitlement_version | P0  |
| TOK-10 | 支持密钥轮换                      | P0  |
| TOK-11 | 支持 JWT 和 Opaque Token 策略选择  | P1  |
| TOK-12 | 支持 Token 黑名单                | P0  |

---

# 13. RBAC3 权限需求

## 13.1 RBAC3 模型

RBAC3 包含：

```text
RBAC0：用户、角色、权限、会话
RBAC1：角色继承
RBAC2：约束规则
RBAC3：角色继承 + 约束规则
```

## 13.2 权限对象

| 对象             | 说明   |
|----------------|------|
| User / Account | 权限主体 |
| Role           | 角色   |
| Permission     | 权限   |
| Resource       | 资源   |
| Action         | 操作   |
| Session        | 会话   |
| Constraint     | 约束   |
| Data Scope     | 数据范围 |
| Tenant         | 租户   |
| App            | 应用   |
| Organization   | 组织   |
| Project        | 项目   |
| Channel        | 频道   |

## 13.3 权限类型

| 类型     | 示例                           |
|--------|------------------------------|
| 菜单权限   | `admin:user:menu`            |
| 按钮权限   | `admin:user:create_button`   |
| API 权限 | `uaa:user:create`            |
| 数据权限   | `order:data:dept`            |
| 内容权限   | `content:video:publish`      |
| 频道权限   | `channel:member:invite`      |
| 项目权限   | `project:api_key:create`     |
| 账单权限   | `billing:invoice:read`       |
| 审核权限   | `moderation:content:approve` |

## 13.4 权限编码规范

建议统一格式：

```text
领域:资源:动作
```

示例：

```text
uaa:user:create
uaa:user:update
uaa:role:assign
admin:audit:read
content:video:publish
content:video:delete
channel:member:invite
channel:analytics:read
project:api_key:create
project:usage:read
billing:invoice:read
moderation:comment:delete
```

## 13.5 角色继承

| 编号      | 需求           | 优先级 |
|---------|--------------|-----|
| RBAC-01 | 支持角色父子关系     | P0  |
| RBAC-02 | 支持父角色继承子角色权限 | P0  |
| RBAC-03 | 支持多层角色继承     | P0  |
| RBAC-04 | 支持循环继承检测     | P0  |
| RBAC-05 | 支持角色继承层级限制   | P1  |
| RBAC-06 | 支持继承权限展开计算   | P0  |
| RBAC-07 | 支持继承变更审计     | P0  |

示例：

```text
超级管理员
 └── 系统管理员
      ├── 用户管理员
      └── 权限管理员
```

```text
频道 Owner
 └── 频道 Manager
      ├── Editor
      ├── Moderator
      └── Analyst
```

## 13.6 约束规则

| 约束类型        | 说明              | 优先级 |
|-------------|-----------------|-----|
| 静态职责分离 SSoD | 两个冲突角色不能分配给同一人  | P0  |
| 动态职责分离 DSoD | 同一会话不能同时激活冲突角色  | P1  |
| 角色基数限制      | 某角色最多 / 最少多少人   | P1  |
| 先决角色        | 分配某角色前必须已有另一个角色 | P1  |
| 时间约束        | 临时角色有效期         | P1  |
| 组织约束        | 角色只在某组织内有效      | P0  |
| 项目约束        | 角色只在某项目内有效      | P0  |
| 频道约束        | 角色只在某频道内有效      | P0  |
| 租户约束        | 权限不能跨租户         | P0  |
| 数据范围约束      | 只允许访问指定范围数据     | P0  |

## 13.7 数据权限

| 数据范围                    | 说明         |
|-------------------------|------------|
| SELF                    | 仅本人        |
| PROFILE                 | 当前 Profile |
| DEPARTMENT              | 本部门        |
| DEPARTMENT_AND_CHILDREN | 本部门及下级     |
| ORGANIZATION            | 当前组织       |
| PROJECT                 | 当前项目       |
| CHANNEL                 | 当前频道       |
| TENANT                  | 当前租户       |
| CUSTOM                  | 自定义数据范围    |
| ALL                     | 全部数据       |

## 13.8 RBAC3 应用场景

| 场景     | 使用 RBAC3                |
|--------|-------------------------|
| 内部管理后台 | 是                       |
| 客服后台   | 是                       |
| 运营后台   | 是                       |
| 审核后台   | 是                       |
| 组织成员权限 | 是                       |
| 项目成员权限 | 是                       |
| 频道团队权限 | 是                       |
| 创作者后台  | 是                       |
| 开发者平台  | 是                       |
| 普通会员权益 | 否，使用 Entitlement        |
| 内容观看权限 | 主要使用 Entitlement + ABAC |

---

# 14. 会员与权益需求

## 14.1 套餐模型

| 套餐         | 说明    |
|------------|-------|
| FREE       | 免费用户  |
| BASIC      | 基础会员  |
| PLUS       | 高级会员  |
| PRO        | 专业会员  |
| FAMILY     | 家庭套餐  |
| TEAM       | 团队套餐  |
| ENTERPRISE | 企业套餐  |
| CREATOR    | 创作者套餐 |
| DEVELOPER  | 开发者套餐 |

套餐名称可配置，不应写死。

## 14.2 权益类型

| 权益类型   | 示例               |
|--------|------------------|
| 功能权益   | 是否可使用高级功能        |
| 内容权益   | 是否可观看会员内容        |
| API 权益 | API 调用次数、速率、模型权限 |
| 设备权益   | 最大设备数量           |
| 并发权益   | 同时在线 / 同时播放数量    |
| 存储权益   | 上传空间、文件数量        |
| 创作权益   | 是否可发布长视频、直播、高级内容 |
| 商业权益   | 是否可开通收益、广告、赞助    |
| 数据权益   | 是否可查看高级数据分析      |
| 支持权益   | 普通支持、优先支持、专属支持   |
| 试用权益   | 新用户试用、限时体验       |
| 赠送权益   | 运营补偿、活动赠送        |

## 14.3 权益管理

| 编号     | 需求            | 优先级 |
|--------|---------------|-----|
| ENT-01 | 支持套餐配置        | P0  |
| ENT-02 | 支持套餐绑定权益      | P0  |
| ENT-03 | 支持账号订阅套餐      | P0  |
| ENT-04 | 支持权益生效时间和失效时间 | P0  |
| ENT-05 | 支持权益暂停、恢复、取消  | P0  |
| ENT-06 | 支持套餐升级        | P0  |
| ENT-07 | 支持套餐降级        | P0  |
| ENT-08 | 支持套餐过期        | P0  |
| ENT-09 | 支持欠费后权益冻结     | P0  |
| ENT-10 | 支持运营手动补发权益    | P1  |
| ENT-11 | 支持试用权益        | P1  |
| ENT-12 | 支持权益版本号       | P0  |

## 14.4 额度管理

| 编号       | 需求       | 优先级 |
|----------|----------|-----|
| QUOTA-01 | 支持按天额度   | P0  |
| QUOTA-02 | 支持按月额度   | P0  |
| QUOTA-03 | 支持总量额度   | P0  |
| QUOTA-04 | 支持并发额度   | P0  |
| QUOTA-05 | 支持接口限流额度 | P0  |
| QUOTA-06 | 支持存储额度   | P1  |
| QUOTA-07 | 支持内容上传额度 | P1  |
| QUOTA-08 | 支持额度扣减   | P0  |
| QUOTA-09 | 支持额度回滚   | P1  |
| QUOTA-10 | 支持额度查询   | P0  |

## 14.5 权益判断接口

系统需要提供统一权益判断能力：

```text
用户是否拥有某功能？
用户是否可以访问某内容？
用户是否超过设备限制？
用户是否超过 API 额度？
用户是否可以创建项目？
用户是否可以上传内容？
用户是否可以使用高级模型？
```

返回结果应包含：

```json
{
  "allowed": true,
  "reason": "PLAN_ALLOWED",
  "matched_entitlements": [
    "feature.advanced_search",
    "quota.api.monthly"
  ],
  "remaining_quota": {
    "api_requests": 9500
  }
}
```

---

# 15. 内容访问授权需求

## 15.1 内容访问维度

| 维度         | 说明             |
|------------|----------------|
| 登录状态       | 游客、已登录、已验证     |
| 账号状态       | 正常、冻结、封禁、风控限制  |
| Profile 类型 | 普通、儿童、创作者、品牌   |
| 年龄分级       | 儿童、青少年、成人      |
| 地区限制       | 国家、地区、版权区域     |
| 会员等级       | 免费、会员、高级会员     |
| 内容类型       | 免费、会员、付费、限时、私密 |
| 购买记录       | 单独购买、租赁、订阅     |
| 设备限制       | 最大设备、当前设备状态    |
| 并发限制       | 同时观看数量         |
| 社交关系       | 好友、关注、拉黑、订阅频道  |
| 隐私设置       | 公开、私密、仅链接、仅好友  |
| 风控状态       | 账号风险、设备风险、内容风险 |

## 15.2 内容权限动作

| 动作               | 说明   |
|------------------|------|
| content:view     | 查看内容 |
| content:play     | 播放内容 |
| content:download | 下载内容 |
| content:comment  | 评论内容 |
| content:like     | 点赞   |
| content:share    | 分享   |
| content:save     | 收藏   |
| content:report   | 举报   |
| content:publish  | 发布   |
| content:edit     | 编辑   |
| content:delete   | 删除   |
| content:moderate | 审核   |
| content:monetize | 开启收益 |

## 15.3 内容访问规则

| 编号         | 需求                | 优先级 |
|------------|-------------------|-----|
| CONTENT-01 | 支持内容是否可访问判断       | P0  |
| CONTENT-02 | 支持内容是否可播放判断       | P0  |
| CONTENT-03 | 支持内容是否可下载判断       | P1  |
| CONTENT-04 | 支持内容地区限制          | P0  |
| CONTENT-05 | 支持内容年龄分级          | P0  |
| CONTENT-06 | 支持儿童 Profile 内容限制 | P0  |
| CONTENT-07 | 支持会员内容访问          | P0  |
| CONTENT-08 | 支持单独购买内容访问        | P1  |
| CONTENT-09 | 支持创作者私密内容         | P1  |
| CONTENT-10 | 支持仅粉丝 / 仅订阅者可见    | P1  |
| CONTENT-11 | 支持作者拉黑后禁止互动       | P1  |
| CONTENT-12 | 支持内容下架后禁止访问       | P0  |

---

# 16. 组织、项目与开发者平台需求

## 16.1 组织模型

适用于 OpenAI 类、SaaS 类、团队协作类场景。

```text
Account
 └── Organization
      ├── Member
      ├── Role
      ├── Project
      ├── Billing
      ├── API Key
      └── Service Account
```

## 16.2 组织功能

| 编号     | 需求            | 优先级 |
|--------|---------------|-----|
| ORG-01 | 支持创建组织        | P0  |
| ORG-02 | 支持邀请成员        | P0  |
| ORG-03 | 支持成员加入 / 退出   | P0  |
| ORG-04 | 支持组织角色        | P0  |
| ORG-05 | 支持组织 Owner 转让 | P1  |
| ORG-06 | 支持组织禁用        | P0  |
| ORG-07 | 支持组织账单主体      | P1  |
| ORG-08 | 支持组织审计日志      | P0  |

## 16.3 项目功能

| 编号      | 需求                   | 优先级 |
|---------|----------------------|-----|
| PROJ-01 | 支持组织下创建项目            | P0  |
| PROJ-02 | 支持项目成员               | P0  |
| PROJ-03 | 支持项目角色               | P0  |
| PROJ-04 | 支持项目 API Key         | P0  |
| PROJ-05 | 支持项目 Service Account | P0  |
| PROJ-06 | 支持项目额度               | P0  |
| PROJ-07 | 支持项目限流               | P0  |
| PROJ-08 | 支持项目用量统计             | P1  |
| PROJ-09 | 支持项目禁用               | P0  |
| PROJ-10 | 支持项目审计日志             | P0  |

## 16.4 API Key 需求

| 编号        | 需求                | 优先级 |
|-----------|-------------------|-----|
| APIKEY-01 | 支持创建 API Key      | P0  |
| APIKEY-02 | 支持禁用 API Key      | P0  |
| APIKEY-03 | 支持删除 API Key      | P0  |
| APIKEY-04 | 支持轮换 API Key      | P0  |
| APIKEY-05 | 支持 API Key 作用域    | P0  |
| APIKEY-06 | 支持 API Key 额度限制   | P0  |
| APIKEY-07 | 支持 API Key IP 白名单 | P1  |
| APIKEY-08 | 支持 API Key 最后使用时间 | P0  |
| APIKEY-09 | 支持 API Key 使用审计   | P0  |

## 16.5 Service Account 需求

| 编号    | 需求           | 优先级 |
|-------|--------------|-----|
| SA-01 | 支持创建服务账号     | P0  |
| SA-02 | 支持服务账号绑定项目   | P0  |
| SA-03 | 支持服务账号角色授权   | P0  |
| SA-04 | 支持服务账号密钥管理   | P0  |
| SA-05 | 支持服务账号禁用     | P0  |
| SA-06 | 支持服务账号访问审计   | P0  |
| SA-07 | 支持服务账号最小权限原则 | P0  |

---

# 17. 第三方应用授权需求

## 17.1 开发者应用

| 编号     | 需求                 | 优先级 |
|--------|--------------------|-----|
| APP-01 | 支持开发者创建应用          | P0  |
| APP-02 | 支持应用名称、Logo、描述     | P0  |
| APP-03 | 支持 Redirect URI 配置 | P0  |
| APP-04 | 支持应用 Scope 申请      | P0  |
| APP-05 | 支持敏感 Scope 审核      | P0  |
| APP-06 | 支持用户授权同意页          | P0  |
| APP-07 | 支持用户撤销应用授权         | P0  |
| APP-08 | 支持应用禁用             | P0  |
| APP-09 | 支持应用限流             | P0  |
| APP-10 | 支持应用审计             | P0  |

## 17.2 Scope 示例

```text
profile.read
email.read
phone.read
content.read
content.write
channel.read
channel.manage
comment.moderate
organization.read
organization.member.read
project.read
project.api_key.manage
billing.read
usage.read
```

## 17.3 用户授权同意页

授权页应明确展示：

```text
应用名称
应用开发者
请求访问的权限
权限风险等级
授权有效期
取消按钮
同意按钮
隐私政策链接
```

示例：

```text
应用「ABC Assistant」请求访问：

- 读取你的基础资料
- 读取你的邮箱
- 查看你的频道列表
- 发布内容到你的频道

[拒绝] [同意授权]
```

---

# 18. 社交关系需求

## 18.1 社交关系类型

| 类型           | 说明     |
|--------------|--------|
| FOLLOW       | 关注     |
| FRIEND       | 好友     |
| BLOCK        | 拉黑     |
| MUTE         | 屏蔽     |
| SUBSCRIBE    | 订阅频道   |
| MEMBER       | 成为频道会员 |
| COLLABORATOR | 协作者    |

## 18.2 社交权限规则

| 场景        | 判断方式                     |
|-----------|--------------------------|
| 是否可评论     | 登录状态 + 内容设置 + 是否被拉黑 + 风控 |
| 是否可私信     | 双方关系 + 隐私设置 + 风控         |
| 是否可查看私密内容 | 好友 / 粉丝 / 订阅关系           |
| 是否可关注     | 账号状态 + 对方隐私设置 + 是否被拉黑    |
| 是否可互动     | 账号状态 + 社交关系 + 内容策略       |

## 18.3 社交功能

| 编号     | 需求       | 优先级 |
|--------|----------|-----|
| SOC-01 | 支持关注     | P1  |
| SOC-02 | 支持取消关注   | P1  |
| SOC-03 | 支持拉黑     | P1  |
| SOC-04 | 支持取消拉黑   | P1  |
| SOC-05 | 支持屏蔽     | P2  |
| SOC-06 | 支持好友关系   | P2  |
| SOC-07 | 支持关系判断接口 | P1  |
| SOC-08 | 支持关系变更审计 | P1  |

---

# 19. 创作者与频道需求

## 19.1 频道模型

```text
Channel
 ├── Owner
 ├── Manager
 ├── Editor
 ├── Uploader
 ├── Moderator
 ├── Analyst
 └── Viewer
```

## 19.2 频道角色

| 角色        | 权限             |
|-----------|----------------|
| Owner     | 全部权限、删除频道、转让频道 |
| Manager   | 管理成员、管理内容、管理设置 |
| Editor    | 编辑内容、发布内容      |
| Uploader  | 上传内容草稿         |
| Moderator | 管理评论、处理举报      |
| Analyst   | 查看数据分析         |
| Viewer    | 只读访问           |

## 19.3 频道功能

| 编号    | 需求                | 优先级 |
|-------|-------------------|-----|
| CH-01 | 支持创建频道            | P1  |
| CH-02 | 支持频道资料管理          | P1  |
| CH-03 | 支持邀请频道成员          | P1  |
| CH-04 | 支持频道成员角色          | P1  |
| CH-05 | 支持频道 RBAC3 权限     | P1  |
| CH-06 | 支持频道角色继承          | P1  |
| CH-07 | 支持频道 Owner 至少 1 人 | P1  |
| CH-08 | 支持频道禁用 / 封禁       | P1  |
| CH-09 | 支持创作者认证           | P1  |
| CH-10 | 支持频道审计日志          | P1  |

## 19.4 内容创作权限

| 动作   | 权限                         |
|------|----------------------------|
| 上传内容 | `channel:content:upload`   |
| 编辑内容 | `channel:content:edit`     |
| 发布内容 | `channel:content:publish`  |
| 删除内容 | `channel:content:delete`   |
| 查看数据 | `channel:analytics:read`   |
| 管理评论 | `channel:comment:moderate` |
| 管理成员 | `channel:member:manage`    |
| 管理收益 | `channel:revenue:manage`   |

---

# 20. 风控需求

## 20.1 风控对象

| 对象   | 风险             |
|------|----------------|
| 账号   | 批量注册、撞库、盗号     |
| 登录   | 异地登录、新设备、代理 IP |
| 设备   | 模拟器、批量设备、异常指纹  |
| 内容   | 垃圾内容、违规内容、侵权内容 |
| 社交   | 刷粉、刷赞、骚扰、诈骗    |
| 支付   | 盗刷、退款欺诈、欠费     |
| API  | 爬虫、滥用、撞接口、超限   |
| 订阅   | 账号共享、异常并发      |
| 后台操作 | 越权、异常管理操作      |

## 20.2 风控动作

| 动作                 | 说明          |
|--------------------|-------------|
| ALLOW              | 放行          |
| CHALLENGE          | 要求验证码 / MFA |
| STEP_UP_AUTH       | 高危操作二次认证    |
| LIMIT              | 限流          |
| SHADOW_LIMIT       | 静默限制        |
| SUSPEND            | 临时冻结        |
| BAN                | 永久封禁        |
| REVOKE_TOKEN       | 撤销 Token    |
| FORCE_LOGOUT       | 强制下线        |
| MANUAL_REVIEW      | 人工审核        |
| FREEZE_ENTITLEMENT | 冻结权益        |
| FREEZE_PAYMENT     | 冻结支付 / 提现   |

## 20.3 风控功能

| 编号      | 需求        | 优先级 |
|---------|-----------|-----|
| RISK-01 | 支持登录风险评分  | P0  |
| RISK-02 | 支持新设备风险识别 | P0  |
| RISK-03 | 支持异地登录识别  | P0  |
| RISK-04 | 支持账号注册风控  | P0  |
| RISK-05 | 支持验证码挑战   | P1  |
| RISK-06 | 支持 MFA 挑战 | P1  |
| RISK-07 | 支持账号封禁    | P0  |
| RISK-08 | 支持账号解封    | P0  |
| RISK-09 | 支持 API 限流 | P0  |
| RISK-10 | 支持内容发布风控  | P1  |
| RISK-11 | 支持设备风险标记  | P1  |
| RISK-12 | 支持风控规则配置  | P1  |
| RISK-13 | 支持风险事件审计  | P0  |

---

# 21. 审计日志需求

## 21.1 审计类型

| 类型       | 内容                |
|----------|-------------------|
| 登录审计     | 登录成功、失败、退出、MFA    |
| Token 审计 | 签发、刷新、撤销、过期       |
| OAuth 审计 | 授权、同意、撤销、Scope 变更 |
| 权限审计     | 角色、权限、继承、约束变更     |
| 用户审计     | 注册、修改资料、注销、封禁     |
| 设备审计     | 新设备、踢出设备、可信设备     |
| 权益审计     | 订阅、升级、降级、冻结、恢复    |
| API 审计   | API Key 创建、删除、调用  |
| 管理审计     | 管理员所有敏感操作         |
| 风控审计     | 风险命中、限制、封禁、解封     |
| 内容访问审计   | 关键内容访问、播放、下载      |
| 数据隐私审计   | 数据导出、删除、授权撤销      |

## 21.2 审计字段

每条审计日志至少包含：

```text
事件 ID
事件类型
操作主体
主体类型
目标对象
目标对象类型
操作动作
操作结果
失败原因
操作时间
IP 地址
设备 ID
User-Agent
Client ID
Session ID
Trace ID
变更前内容
变更后内容
风险等级
租户 / 组织 / 项目 / 频道上下文
```

## 21.3 审计要求

| 编号     | 需求              | 优先级 |
|--------|-----------------|-----|
| AUD-01 | 所有登录行为必须审计      | P0  |
| AUD-02 | 所有 Token 行为必须审计 | P0  |
| AUD-03 | 所有权限变更必须审计      | P0  |
| AUD-04 | 所有管理员操作必须审计     | P0  |
| AUD-05 | 所有封禁 / 解封必须审计   | P0  |
| AUD-06 | 所有权益变更必须审计      | P0  |
| AUD-07 | 审计日志支持查询        | P0  |
| AUD-08 | 审计日志支持导出        | P1  |
| AUD-09 | 审计日志不可被普通管理员删除  | P0  |
| AUD-10 | 高危操作必须记录变更前后    | P0  |

---

# 22. 用户自助中心需求

## 22.1 用户自助功能

| 模块         | 功能                  |
|------------|---------------------|
| 个人资料       | 昵称、头像、邮箱、手机、语言、地区   |
| 安全设置       | 密码、MFA、Passkey、备用码  |
| 登录设备       | 查看设备、移除设备、全部退出      |
| 登录历史       | 查看最近登录记录            |
| Profile 管理 | 创建、编辑、删除、切换 Profile |
| 隐私设置       | 公开信息、搜索可见性、授权范围     |
| 订阅账单       | 当前套餐、账单、发票、取消订阅     |
| 权益用量       | 当前权益、剩余额度、使用记录      |
| 第三方授权      | 查看和撤销授权应用           |
| API Key    | 开发者创建、禁用、删除 API Key |
| 数据隐私       | 导出个人数据、注销账号         |
| 通知设置       | 邮件、短信、Push、站内通知     |

---

# 23. 管理后台需求

## 23.1 后台模块

| 模块         | 功能                           |
|------------|------------------------------|
| 用户管理       | 查询用户、冻结、解封、重置 MFA、查看设备       |
| Profile 管理 | 查看 Profile、限制儿童 Profile、处理异常 |
| 权限管理       | 角色、权限、角色继承、互斥角色、数据权限         |
| Client 管理  | OAuth Client、回调地址、Scope、密钥   |
| 组织管理       | 组织、成员、项目、角色                  |
| 开发者管理      | 应用审核、API Key、Service Account |
| 订阅管理       | 套餐、权益、订单、退款、补发权益             |
| 内容授权       | 内容等级、地区规则、会员规则               |
| 创作者管理      | 频道、认证、成员、违规记录                |
| 风控管理       | 黑名单、风险规则、封禁、限流               |
| 审计日志       | 登录、授权、权限、管理操作日志              |
| 系统配置       | 安全策略、Token 策略、通知策略           |
| 运营工具       | 临时授权、批量操作、用户申诉处理             |

## 23.2 后台权限要求

后台必须使用更严格的权限要求：

```text
管理员账号与 C 端账号隔离。
后台登录必须支持 MFA。
高危操作必须二次确认。
超级管理员数量必须受限。
管理员不能给自己提升权限。
权限变更必须审计。
敏感字段必须脱敏。
客服只能查看必要信息。
```

---

# 24. 通知需求

## 24.1 通知类型

| 类型      | 场景                |
|---------|-------------------|
| 邮件通知    | 注册验证、登录提醒、密码重置、账单 |
| 短信通知    | 验证码、高危操作、异常登录     |
| Push 通知 | App 登录提醒、设备变更     |
| 站内通知    | 权益变更、账号状态、审核结果    |
| Webhook | 开发者事件通知           |

## 24.2 通知事件

| 事件              | 优先级 |
|-----------------|-----|
| 新设备登录           | P0  |
| 异地登录            | P0  |
| 密码修改            | P0  |
| MFA 修改          | P0  |
| 账号封禁 / 解封       | P0  |
| Token 大规模撤销     | P0  |
| 订阅成功 / 失败       | P1  |
| 套餐过期            | P1  |
| 权益变更            | P1  |
| API Key 创建 / 删除 | P0  |
| 第三方授权           | P0  |
| 管理员高危操作         | P0  |

---

# 25. 数据隐私与合规需求

## 25.1 数据隐私能力

| 编号      | 需求            | 优先级 |
|---------|---------------|-----|
| PRIV-01 | 支持用户查看个人资料    | P0  |
| PRIV-02 | 支持用户修改个人资料    | P0  |
| PRIV-03 | 支持用户导出个人数据    | P1  |
| PRIV-04 | 支持用户注销账号      | P0  |
| PRIV-05 | 支持第三方授权撤销     | P0  |
| PRIV-06 | 支持隐私设置        | P1  |
| PRIV-07 | 支持敏感字段脱敏      | P0  |
| PRIV-08 | 支持管理员访问敏感信息审计 | P0  |
| PRIV-09 | 支持数据最小化返回     | P0  |
| PRIV-10 | 支持用户授权记录留存    | P0  |

## 25.2 敏感字段

以下字段必须重点保护：

```text
手机号
邮箱
身份证件
支付信息
IP 地址
设备指纹
登录历史
地理位置
API Key
Client Secret
Refresh Token
MFA Secret
Passkey Credential
```

---

# 26. 多租户需求

## 26.1 租户模型

| 对象     | 是否租户隔离 |
|--------|--------|
| 用户     | 可选     |
| 管理员    | 必须     |
| 组织     | 必须     |
| 项目     | 必须     |
| 角色     | 必须     |
| 权限     | 必须     |
| Client | 必须     |
| 订阅     | 必须     |
| 审计日志   | 必须     |
| 风控规则   | 可选     |
| 内容策略   | 可选     |

## 26.2 多租户规则

```text
租户之间数据默认隔离。
管理员只能管理授权租户。
Token 中应包含 tenant_id。
业务服务必须校验 tenant_id。
跨租户访问必须有明确授权。
超级管理员操作跨租户数据必须审计。
```

---

# 27. 业务系统接入需求

## 27.1 接入方式

| 接入类型     | 说明                        |
|----------|---------------------------|
| Web 应用接入 | OIDC 登录                   |
| 后端服务接入   | Resource Server Token 校验  |
| 微服务接入    | JWT / Opaque Token 校验     |
| App 接入   | Authorization Code + PKCE |
| TV 接入    | Device Code               |
| 第三方应用接入  | OAuth2 授权                 |
| API 调用接入 | API Key / Service Account |
| 网关接入     | Gateway 统一鉴权              |
| 管理后台接入   | SSO + RBAC3               |

## 27.2 接入能力

| 编号     | 需求                      | 优先级 |
|--------|-------------------------|-----|
| INT-01 | 提供 OAuth2/OIDC 接入文档     | P0  |
| INT-02 | 提供 Resource Server 接入规范 | P0  |
| INT-03 | 提供统一权限校验接口              | P0  |
| INT-04 | 提供统一权益校验接口              | P0  |
| INT-05 | 提供 Token 校验示例           | P0  |
| INT-06 | 提供 SSO 接入 Demo          | P0  |
| INT-07 | 提供管理后台接入 Demo           | P0  |
| INT-08 | 提供 API Key 使用规范         | P0  |
| INT-09 | 提供错误码规范                 | P0  |
| INT-10 | 提供审计追踪 Trace ID 规范      | P1  |

---

# 28. 统一授权决策需求

系统应提供统一授权决策服务。

## 28.1 决策输入

```json
{
  "subject": {
    "account_id": "acc_10001",
    "profile_id": "prof_20001",
    "roles": [
      "CHANNEL_EDITOR"
    ],
    "plan": "PRO",
    "risk_level": "LOW"
  },
  "resource": {
    "type": "VIDEO",
    "id": "video_90001",
    "owner_channel_id": "ch_80001",
    "maturity_level": "TEEN",
    "region_policy": [
      "SG",
      "US",
      "JP"
    ]
  },
  "action": "content.publish",
  "context": {
    "device_id": "dev_30001",
    "ip_region": "SG",
    "client_id": "web-app",
    "tenant_id": "tenant_001"
  }
}
```

## 28.2 决策输出

```json
{
  "allowed": true,
  "reason": "CHANNEL_ROLE_ALLOWED",
  "matched_policies": [
    "rbac.channel.editor.publish",
    "risk.low.allow"
  ],
  "obligations": [
    "audit.content.publish"
  ]
}
```

## 28.3 决策结果

| 结果             | 说明   |
|----------------|------|
| ALLOW          | 允许   |
| DENY           | 拒绝   |
| CHALLENGE      | 需要验证 |
| LIMIT          | 限制访问 |
| REVIEW         | 需要审核 |
| EXPIRED        | 权益过期 |
| QUOTA_EXCEEDED | 额度不足 |
| DEVICE_LIMITED | 设备限制 |
| REGION_BLOCKED | 地区限制 |
| RISK_BLOCKED   | 风控拒绝 |

---

# 29. 错误码需求

## 29.1 错误码分类

| 分类       | 示例                         |
|----------|----------------------------|
| 认证错误     | `AUTH_INVALID_CREDENTIALS` |
| Token 错误 | `TOKEN_EXPIRED`            |
| 权限错误     | `PERMISSION_DENIED`        |
| 权益错误     | `ENTITLEMENT_EXPIRED`      |
| 额度错误     | `QUOTA_EXCEEDED`           |
| 设备错误     | `DEVICE_LIMIT_EXCEEDED`    |
| 风控错误     | `RISK_CHALLENGE_REQUIRED`  |
| OAuth 错误 | `OAUTH_INVALID_CLIENT`     |
| 内容错误     | `CONTENT_REGION_BLOCKED`   |
| 账号错误     | `ACCOUNT_SUSPENDED`        |

## 29.2 错误返回要求

错误返回应包含：

```json
{
  "code": "PERMISSION_DENIED",
  "message": "You do not have permission to perform this action.",
  "reason": "MISSING_PERMISSION",
  "trace_id": "trace_123456",
  "required": [
    "channel:content:publish"
  ]
}
```

---

# 30. 非功能需求

## 30.1 安全需求

| 编号     | 需求                   | 优先级 |
|--------|----------------------|-----|
| SEC-01 | 全链路 HTTPS            | P0  |
| SEC-02 | 密码不可明文存储             | P0  |
| SEC-03 | Client Secret 不可明文存储 | P0  |
| SEC-04 | API Key 只展示一次        | P0  |
| SEC-05 | 支持密钥轮换               | P0  |
| SEC-06 | 防暴力破解                | P0  |
| SEC-07 | 防 CSRF               | P0  |
| SEC-08 | 防 XSS                | P0  |
| SEC-09 | 防开放重定向               | P0  |
| SEC-10 | 防 Token 重放           | P0  |
| SEC-11 | 高危操作二次认证             | P0  |
| SEC-12 | 敏感操作审计               | P0  |
| SEC-13 | 后台强制 MFA             | P0  |
| SEC-14 | 敏感数据脱敏               | P0  |
| SEC-15 | 最小权限原则               | P0  |

## 30.2 性能需求

| 指标              | 要求            |
|-----------------|---------------|
| 登录接口 P95        | ≤ 500ms       |
| Token 签发 P95    | ≤ 300ms       |
| Token 校验 P95    | ≤ 50ms        |
| 权限判断 P95        | ≤ 100ms       |
| 权益判断 P95        | ≤ 100ms       |
| 内容访问判断 P95      | ≤ 150ms       |
| 管理后台查询 P95      | ≤ 500ms       |
| 支持账号规模          | 首期 100 万，可扩展  |
| 支持 OAuth Client | 首期 1000 个，可扩展 |
| 支持并发登录          | 可按业务规模扩展      |
| 支持 API Key      | 首期 10 万，可扩展   |

## 30.3 可用性需求

| 编号    | 需求               |
|-------|------------------|
| HA-01 | 支持多实例部署          |
| HA-02 | 支持无状态认证服务扩展      |
| HA-03 | 支持 Redis / 缓存高可用 |
| HA-04 | 支持数据库高可用         |
| HA-05 | 支持限流和熔断          |
| HA-06 | 支持灰度发布           |
| HA-07 | 支持配置回滚           |
| HA-08 | 支持 Token 密钥平滑轮换  |
| HA-09 | 支持权限和权益缓存失效      |
| HA-10 | 支持服务降级策略         |

## 30.4 可观测性需求

| 类型        | 指标                        |
|-----------|---------------------------|
| Metrics   | 登录次数、失败次数、Token 签发数、权限拒绝数 |
| Logs      | 登录日志、授权日志、审计日志、风控日志       |
| Traces    | OAuth2 授权链路、SSO 链路、权限判断链路 |
| Alerts    | 异常登录、Token 异常、权限异常、风控异常   |
| Dashboard | 账号概况、登录趋势、授权趋势、风险趋势       |

---

# 31. 数据对象需求

## 31.1 核心数据对象

```text
Account
Credential
Identity
Profile
Device
Session
OAuth Client
Authorization
Consent
Token
Role
Permission
Resource
Role Hierarchy
Role Constraint
Organization
Project
Channel
Subscription
Plan
Entitlement
Quota
Usage
Developer App
API Key
Service Account
Risk Event
Audit Log
Notification Event
```

## 31.2 关键关系

```text
Account 1:N Profile
Account 1:N Device
Account 1:N Session
Account N:N Role
Role N:N Permission
Role N:N Role，通过 Role Hierarchy 实现继承
Role N:N Constraint
Account N:N Organization
Organization 1:N Project
Project 1:N API Key
Project 1:N Service Account
Account 1:N Subscription
Subscription 1:N Entitlement
Account N:N Channel
Channel N:N Role
Developer App N:N Scope
OAuth Client 1:N Authorization
```

---

# 32. 优先级范围

## 32.1 P0 必须实现

```text
账号注册 / 登录 / 找回 / 注销
密码登录 / 验证码登录
账号状态管理
设备登记与远程退出
OAuth2 Authorization Code + PKCE
OIDC 登录
SSO 单点登录
Access Token / Refresh Token
Token 撤销
OAuth Client 管理
RBAC3 基础能力
角色继承
互斥角色
后台权限管理
用户自助中心
管理后台
套餐与权益基础模型
权益判断接口
内容访问判断接口
API Key
Service Account
登录审计
授权审计
权限审计
管理操作审计
基础风控
```

## 32.2 P1 建议实现

```text
MFA
第三方登录
Passkey 预留
儿童 Profile
组织 / 项目高级权限
频道 / 创作者权限
第三方应用 Scope 审核
用户授权同意页增强
设备可信标记
内容分级
地区限制
订阅升级 / 降级
额度扣减
风控规则配置
通知中心
数据导出
```

## 32.3 P2 后续扩展

```text
完整社交关系
好友体系
内容收益权限
创作者商业化
TV 设备码登录
账号合并
高级风控模型
自动化应用审核
多区域合规
复杂家庭账号策略
高级数据隐私管理
```

---

# 33. 首期 MVP 需求范围

首期不要同时完整复制 OpenAI、X、Netflix、YouTube。推荐 MVP 聚焦统一底座，保留扩展口。

## 33.1 MVP 必须交付

```text
1. C 端账号注册、登录、找回、注销。
2. 管理员账号登录和后台权限。
3. OAuth2 / OIDC / SSO。
4. Access Token、Refresh Token、Token 撤销。
5. OAuth Client 管理。
6. Account + Profile 基础模型。
7. 设备管理和远程登出。
8. RBAC3 权限管理。
9. 角色继承和互斥角色。
10. 组织 / 项目基础模型。
11. API Key 和 Service Account。
12. 套餐和权益基础模型。
13. 权益判断接口。
14. 内容访问判断接口。
15. 基础风控。
16. 审计日志。
17. 用户自助中心。
18. 管理后台。
19. 至少一个业务系统完成 SSO 接入。
20. 至少一个资源服务完成 Token 校验和权限判断。
```

## 33.2 MVP 可暂缓

```text
完整社交 Feed
复杂推荐系统
完整视频播放系统
完整支付扣款系统
完整创作者收益结算
高级内容审核工作流
完整家庭账号体系
复杂账号合并
高级 AI 风控模型
```

---

# 34. 验收标准

## 34.1 账号验收

```text
用户可以注册账号。
用户可以通过邮箱、手机号或用户名登录。
用户可以找回密码。
用户可以注销账号。
账号被禁用后无法登录。
账号被封禁后所有 Token 和 Session 失效。
用户可以查看登录设备。
用户可以踢出指定设备。
```

## 34.2 OAuth2 / SSO 验收

```text
业务系统可以通过 OIDC 接入统一登录。
用户登录系统 A 后，访问系统 B 可免登录。
用户全局退出后，相关业务系统登录态失效。
Access Token 可被资源服务校验。
Refresh Token 可刷新 Access Token。
Token 可被主动撤销。
OAuth Client 可配置 Redirect URI、Scope、Token 有效期。
非法 Redirect URI 被拒绝。
```

## 34.3 RBAC3 验收

```text
管理员可以创建角色。
管理员可以创建权限。
管理员可以给角色分配权限。
管理员可以给用户分配角色。
父角色可以继承子角色权限。
系统可以检测并拒绝角色循环继承。
互斥角色不能分配给同一用户。
权限变更后旧权限能够失效。
数据权限可以按本人、组织、项目、频道等范围限制。
所有权限变更都有审计日志。
```

## 34.4 权益验收

```text
管理员可以配置套餐。
管理员可以给套餐绑定权益。
用户可以拥有某个套餐。
系统可以判断用户是否拥有某个功能。
系统可以判断用户是否超过额度。
系统可以判断用户是否可以访问某内容。
套餐过期后相关权益失效。
套餐升级后新权益生效。
套餐降级后旧权益失效。
权益变更有审计日志。
```

## 34.5 内容访问验收

```text
未登录用户访问受限内容会被拒绝。
非会员用户访问会员内容会被拒绝。
会员用户可以访问会员内容。
儿童 Profile 不能访问超出分级的内容。
地区不允许时内容访问被拒绝。
内容下架后不能访问。
设备数超过限制时访问被拒绝。
并发数超过限制时访问被拒绝。
```

## 34.6 开放平台验收

```text
开发者可以创建应用。
应用可以申请 Scope。
用户授权后应用可获取授权范围内数据。
用户可以撤销应用授权。
开发者可以创建 API Key。
API Key 可以被禁用、删除、轮换。
Service Account 可以访问授权资源。
API Key 和 Service Account 操作有审计。
```

## 34.7 风控验收

```text
登录失败多次会触发锁定或验证。
新设备登录会记录并通知。
异地登录会触发风险事件。
账号封禁后无法登录。
高风险操作需要二次验证。
API 调用超限会被限制。
风险事件可在后台查询。
```

## 34.8 审计验收

```text
登录成功和失败都有日志。
Token 签发、刷新、撤销都有日志。
OAuth 授权和撤销都有日志。
权限变更有日志。
权益变更有日志。
管理员高危操作有日志。
审计日志包含操作者、目标对象、时间、IP、设备、结果。
普通管理员不能删除审计日志。
```

---

# 35. 关键设计原则

## 35.1 Account 和 Profile 必须分离

```text
Account = 登录主体、安全主体、订阅主体
Profile = 展示身份、使用身份、内容偏好主体
```

不要把所有东西都塞进一张用户表，否则后面做儿童账号、创作者账号、品牌账号、家庭账号会非常痛苦。

## 35.2 认证、授权、权益、风控必须分离

```text
认证：你是谁？
授权：你能做什么？
权益：你买了什么、拥有多少额度？
风控：你现在是否可信？
```

这四类能力必须分层，不要混成一个权限字段。

## 35.3 RBAC3 不负责所有 ToC 权限

RBAC3 适合：

```text
后台权限
组织权限
项目权限
频道团队权限
客服权限
审核权限
```

不适合直接处理：

```text
会员套餐
设备限制
内容版权
年龄分级
地区限制
API 额度
社交关系
风险状态
```

这些应交给 Entitlement、ABAC、ReBAC 和 Risk Policy。

## 35.4 JWT 只做身份票据

JWT 中只放必要身份和版本信息：

```text
sub
profile_id
client_id
scope
session_id
device_id
auth_version
entitlement_version
risk_level
exp
```

不要把完整权限、完整权益、完整内容列表、完整社交关系塞进 JWT。那不是 Token，是一份迟早会过期的“小作文”。

## 35.5 后台账号和 C 端账号隔离

```text
C 端用户不能天然登录后台。
后台管理员必须有独立权限体系。
后台必须强制 MFA。
后台高危操作必须审计。
```

## 35.6 所有动态权限都要有版本号

至少需要：

```text
auth_version：权限版本
entitlement_version：权益版本
session_version：会话版本
risk_version：风险版本
```

用户权限、套餐、设备、风控状态变化后，业务系统可以通过版本号判断旧 Token 是否仍然可信。

---

# 36. 最终需求结论

本系统最终应建设为：

```text
统一 UAA + CIAM + RBAC3 + Entitlement + OAuth2/OIDC + SSO 平台
```

它既服务内部后台，也服务 C 端用户；既支持普通登录，也支持第三方授权；既支持传统角色权限，也支持会员权益、设备限制、内容访问、组织项目、创作者频道和开发者
API。

最终能力闭环是：

```text
账号注册
→ 登录认证
→ SSO 单点登录
→ Token 签发
→ RBAC3 权限判断
→ 会员权益判断
→ 内容访问判断
→ 设备会话控制
→ 风控拦截
→ 审计追踪
→ 用户自助管理
→ 管理后台治理
```