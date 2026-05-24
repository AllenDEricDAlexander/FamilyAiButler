# family-core

`family-core` 是家庭核心业务服务，当前提供分类管理和账号密码管理等 HTTP 接口。

## 接口文档

`adapter.web` 下的 controller 使用 `top.egon.openapi.console.annotation` 自有注解生成接口文档：

- `CategoryController`：分类和分类类型接口。
- `PasswordViewController`：账号密码查询、维护、随机密码生成和密码强度检查接口。

Controller class 需要使用 `@DocService` 描述服务分组，公开接口方法需要使用 `@DocOperation` 描述请求和响应。
请求参数使用 `@DocRequest`、`@DocParameter` 或 `@DocParam`，响应使用 `@DocResponse`、`@DocDataType` 和
`@DocWrapper`。复杂泛型使用 `DocTypeReference<T>`。

对外 DTO、VO、value object 字段需要使用 `@DocField(description = "...", example = "...")` 提供字段示例。

业务接口文档不得使用 Springdoc 或 Swagger 注解作为来源。

## 验证

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend
mvn -pl family-core -am test
mvn -pl family-core -am -DskipTests compile
```
