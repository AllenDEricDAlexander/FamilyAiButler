# family-gateway

`family-gateway` 是 Spring Cloud Gateway 模块，负责统一入口、路由转发、鉴权和 OpenAPI debug console 聚合。

## OpenAPI Console 边界

网关负责聚合业务模块生产的 OpenAPI JSON，并提供控制台页面、登录、接口调试、测试数据生成、压测和文档导出能力。

`family-gateway` 下的 `GlobalExceptionHandler` 是网关异常处理边界，不是业务接口文档 adapter，因此不作为业务 doc service
输出。业务接口文档由 `family-core`、`family-uaa/uaa-core`、`family-ai/qwen-ai` 等业务模块的 HTTP controller 或 Dubbo
adapter 负责。

网关自身不得使用 Springdoc 或 Swagger 注解作为项目自有接口文档来源。

## 验证

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend
mvn -pl family-gateway -am test
mvn -pl family-gateway -am -DskipTests compile
```
