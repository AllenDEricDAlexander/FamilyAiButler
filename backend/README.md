# FamilyAiButler Backend

后端 Maven 聚合工程位于本目录，父 POM 为 `backend/pom.xml`。运行服务包括 `family-core`、`family-uaa/uaa-core`、
`family-ai/qwen-ai` 和 `family-gateway`。

## 模块总览

| 模块                                          | 职责                                                 |
|---------------------------------------------|----------------------------------------------------|
| `family-core`                               | 家庭核心业务服务，提供分类、账号密码等 HTTP 接口                        |
| `family-uaa/uaa-facade`                     | UAA 对外契约模块，承载 request/response record 和字段文档示例      |
| `family-uaa/uaa-core`                       | 用户认证授权服务，提供 HTTP controller 和 Dubbo adapter        |
| `family-ai/qwen-ai`                         | 通义千问 AI 服务，提供图片理解等 AI HTTP 接口                      |
| `family-gateway`                            | Spring Cloud Gateway，负责路由、鉴权和 OpenAPI console 聚合入口 |
| `openapi-debug-console-spring-boot-starter` | 项目自有 OpenAPI 文档和调试控制台 starter                      |

本轮 README 覆盖 `family-core`、`family-uaa/uaa-core`、`family-uaa/uaa-facade`、`family-ai/qwen-ai` 和
`family-gateway`。`family-framework`、公共 starter、codegen 等模块没有在本轮文档收尾中展开。

## 接口文档注解规范

业务 adapter 层接口文档统一使用 `top.egon.openapi.console.annotation` 下的项目自有注解：

```text
@DocService
@DocOperation
@DocRequest
@DocParameter / @DocParam
@DocBody
@DocResponse
@DocDataType
@DocWrapper
DocTypeReference<T>
@DocField
```

HTTP controller 和 Dubbo adapter 都纳入该注解体系。DTO、VO、record 字段需要使用
`@DocField(description = "...", example = "...")` 提供字段说明和示例。`Result<T>`、`PageResult<T>`、
`List<T>` 等泛型返回需要通过 `@DocResponse`、`@DocDataType`、`@DocWrapper` 和 `DocTypeReference<T>` 明确表达。

业务模块不得使用 Springdoc 或 Swagger 注解作为项目自有接口文档来源。

## 本轮迁移范围

- `family-core`：HTTP controller 已使用 doc 注解，补齐 `StrengthDTO` 字段示例约束。
- `family-ai/qwen-ai`：HTTP controller 已使用 doc 注解。
- `family-uaa/uaa-core`：HTTP controller 和 Dubbo adapter 均已纳入 doc 注解体系。
- `family-uaa/uaa-facade`：UAA request/response records 已补齐 `@DocField(description, example)`。
- `family-gateway`：作为网关和 OpenAPI console 聚合入口；异常 handler 是运行边界，不作为业务 doc adapter 输出。

## 验证命令

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend
mvn -DskipTests compile
mvn test
mvn -pl family-uaa/uaa-facade,family-uaa/uaa-core -am -DskipTests compile
mvn -pl family-uaa/uaa-core -Dtest=UaaDddArchitectureTest test

cd /Users/mario/SelfProject/FamilyAiButler
git diff --check
rg -n --glob 'backend/**/src/main/java/**/*.java' 'io\.swagger|springdoc|springfox|org\.springdoc|@Tag|@Operation|@Parameter|@Schema|@ApiResponse|@ApiResponses'
rg -n --glob 'backend/**/src/main/java/**/adapter/**/*.java' '@Doc(Service|Operation|Request|Parameter|Param|Body|Response|DataType|Wrapper)|DocTypeReference'
```
