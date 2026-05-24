# uaa-core

`uaa-core` 是用户认证授权服务实现模块，提供账号、登录、授权、OAuth Client、Profile、RBAC、Token、会话和设备相关能力。

## Adapter 文档范围

本模块的 HTTP controller 和 Dubbo adapter 都纳入 `top.egon.openapi.console.annotation` 注解体系。

HTTP adapter：

- `AccountController`
- `AuthController`
- `AuthorizationController`
- `DeviceController`
- `JwkController`
- `OAuthClientController`
- `ProfileController`
- `RbacController`
- `SessionController`

Dubbo adapter：

- `AccountDubboAdapter`
- `AuthDubboAdapter`
- `AuthorizationDubboAdapter`
- `OAuthClientDubboAdapter`
- `ProfileDubboAdapter`
- `RbacDubboAdapter`
- `TokenDubboAdapter`

Adapter class 必须使用 `@DocService`，公开业务方法必须使用 `@DocOperation`。请求体使用 `@DocRequest` 和
`@DocBody`，路径或查询参数使用 `@DocParameter` 或 `@DocParam`，响应使用 `@DocResponse`、`@DocDataType`、
`@DocWrapper`。`List<T>` 返回使用 `DocTypeReference<List<T>>`。

UAA request/response record 字段文档由 `uaa-facade` 提供，字段必须带
`@DocField(description = "...", example = "...")`。

业务接口文档不得使用 Springdoc 或 Swagger 注解作为来源。

## 架构测试

`UaaDddArchitectureTest` 覆盖以下约束：

- UAA adapter 不得依赖 Springdoc 或 Swagger 注解。
- HTTP controller 和 Dubbo adapter 必须使用项目自有 doc 注解。
- Adapter doc data type 和 `DocTypeReference<T>` 指向的 record 字段必须具备 `@DocField` 示例。
- `uaa-facade` DTO record 的 component 数量需要和合规 `@DocField(description, example)` 数量一致。

## 验证

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend
mvn -pl family-uaa/uaa-facade,family-uaa/uaa-core -am -DskipTests compile
mvn -pl family-uaa/uaa-core -Dtest=UaaDddArchitectureTest test
mvn -pl family-uaa/uaa-core -am test
```
