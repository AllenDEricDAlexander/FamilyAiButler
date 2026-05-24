# qwen-ai

`qwen-ai` 是通义千问 AI 服务模块，当前通过 HTTP adapter 暴露图片理解相关接口。

## 接口文档

`ImageController` 已纳入项目自有 doc 注解体系：

- class 级别使用 `@DocService` 描述 AI 服务分组。
- 方法级使用 `@DocOperation` 描述图片生成文本描述接口。
- multipart 请求使用 `@DocRequest`、`@DocBody(contentType = "multipart/form-data")` 和文件参数文档。
- 响应通过 `@DocResponse`、`@DocDataType`、`@DocWrapper` 描述。

该模块不得使用 Springdoc 或 Swagger 注解作为项目自有接口文档来源。

## 验证

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend
mvn -pl family-ai/qwen-ai -am test
mvn -pl family-ai/qwen-ai -am -DskipTests compile
```
