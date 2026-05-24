# uaa-facade

`uaa-facade` 是 UAA 对外契约模块，承载账号、认证、授权、OAuth Client、Profile、RBAC 和 Token 的
request/response records。

## 文档契约

本模块的 request/response record 会被 `uaa-core` 的 HTTP controller 和 Dubbo adapter 用作 OpenAPI doc data type。
因此每个对外 record component 都需要使用：

```java
@DocField(description = "...", example = "...")
```

字段示例应匹配真实类型，不写真实敏感数据。record 字段名、顺序、构造参数和 JSON 结构属于契约，不应为了文档迁移而改变。

本模块依赖 `openapi-debug-console-spring-boot-starter`，仅用于复用项目自有 doc annotation。不得引入 Springdoc 或 Swagger
注解作为接口文档来源。

## 验证

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend
mvn -pl family-uaa/uaa-facade -am -DskipTests compile
mvn -pl family-uaa/uaa-core -Dtest=UaaDddArchitectureTest test
```
