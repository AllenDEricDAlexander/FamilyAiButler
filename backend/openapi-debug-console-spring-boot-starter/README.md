# OpenAPI Debug Console Spring Boot Starter

一个通用的 Spring Boot Starter，用于在业务服务自建扫描生成 OpenAPI JSON，并在 Spring Cloud Gateway / Spring WebFlux
中聚合为内部接口调试控制台。

## 功能边界

- 业务模块：不使用 Springdoc 生成 `/v3/api-docs`；Starter 自建 Spring MVC REST 扫描器，读取 Spring MVC 注解、新文档注解、Jackson
  注解和 Bean Validation 注解后输出标准 OpenAPI JSON。
- 网关模块：聚合多个业务模块的 OpenAPI JSON，提供登录、接口浏览、自动测试数据、代理调试、轻量压力测试、Markdown/PDF 导出。
- 服务发现：网关侧可使用 `http://service-name` 形式配置文档和调试地址，Starter 会在存在 `ReactiveDiscoveryClient` 或
  `DiscoveryClient` 时解析到实际服务实例。
- 状态管理：账号、密码、服务列表、环境开关、签名、导出、压测均来自配置文件，不落库。
- 控制台交互：登录使用一次性 challenge + HMAC proof，前端不提交明文密码；登录后的控制台 API 请求默认带 HMAC 签名。
- 环境保护：`mode=auto` 时，`prod` 默认只读，其他环境默认可调试。
- 默认保护：控制台默认关闭，业务模块只生成 OpenAPI JSON，不会暴露调试页面；只有显式配置 `egon.openapi.console.enabled=true`
  的网关模块才开放页面。
- 导出策略：Markdown/PDF 由请求实时生成并直接返回，不在服务端留存。

## 业务模块配置

业务模块只负责生产 OpenAPI JSON，不需要开放调试页面。默认 `producer.enabled=true`，Servlet MVC 应用会暴露
`GET /v3/api-docs`；如果某个业务模块不希望生产 OpenAPI JSON，可以显式配置 `producer.enabled=false`。

```yaml
egon:
  openapi:
    console:
      producer:
        enabled: true
        title: Demo Service API
        description: Demo service interfaces
        version: v1
        contract-policy: WARN
        example-policy: WARN
        contact-name: Demo Team
        contact-url: https://example.com/support
        contact-email: demo@example.com
        license-name: Internal
        license-url: https://example.com/license
        authorization-header: Authorization
        access-control:
          enabled: true
          header-name: X-OpenAPI-Console-Token
          token: <internal-openapi-doc-token>
```

## REST 文档生成

新版文档生产端不依赖 Springdoc runtime，也不会注册 Springdoc 的 `/v3/api-docs`。Starter 通过
`RequestMappingHandlerMapping` 扫描以下 REST 接口：

- `@RestController`
- `@Controller` 类或方法上存在 `@ResponseBody`
- `@RequestMapping`、`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`、`@PatchMapping`

当前版本只生成 REST OpenAPI JSON。Dubbo Triple、gRPC、proto 文档暂缓，但文档生成器和 Schema 生成器已独立放在
`top.egon.openapi.console.openapi` 包下，后续 RPC 扫描器可以复用同一套 OpenAPI 输出结构。

### 新文档注解

Starter 的自有注解包名为 `top.egon.openapi.console.annotation`。这些注解只表达接口文档语义，不依赖
Springdoc / Swagger runtime。

| 注解/类型                 | 使用位置                                          | 说明                                                       |
|-----------------------|-----------------------------------------------|----------------------------------------------------------|
| `@DocService`         | Controller / Dubbo service interface          | 定义一级分组、二级服务、协议、版本和排序                                     |
| `@DocOperation`       | Method                                        | 定义 operationId、summary、description、鉴权、请求契约和唯一主响应契约       |
| `@DocRequest`         | `@DocOperation.request`                       | 定义请求参数和请求体                                               |
| `@DocParameter`       | `@DocRequest.params` / `@DocResponse.headers` | 定义 path/query/header/cookie/form/file 参数                 |
| `@DocParam`           | Method parameter                              | 方法参数上的便捷写法，字段与 `@DocParameter` 保持一致                      |
| `@DocBody`            | `@DocRequest.body`                            | 定义 request body 的 content type、数据类型和示例                   |
| `@DocResponse`        | `@DocOperation.response`                      | 定义唯一主响应：`dataType + wrapper`                             |
| `@DocDataType`        | 嵌套注解                                          | 定义对象、数组、Map、文件和复杂泛型数据类型                                  |
| `@DocWrapper`         | 嵌套注解                                          | 定义一个最外层响应包装，例如 `Result<T>`                               |
| `DocTypeReference<T>` | Class                                         | 捕获复杂泛型，例如 `PageResult<UserDTO>`                          |
| `@DocModel`           | DTO / VO Class                                | 定义模型名称和描述                                                |
| `@DocField`           | Field / Getter / Record Component             | 定义字段描述、required、hidden、nullable、deprecated、类型补充和 example |
| `@DocIgnore`          | Class / Method / Field / Parameter            | 从文档中忽略对应元素                                               |

核心规则：

- 接口文档必须显式描述 request 和 response；方法签名用于补全和一致性校验。
- `@DocOperation` 只有一个 `response`，生成的 OpenAPI JSON 仍按 OpenAPI 标准输出 response map。
- `@DocResponse` 固定表达为 `dataType + wrapper`。
- `wrapper` 只能有一个，用于最外层统一响应包装，例如 `Result<T>`。
- 分页、列表、Map 和复杂泛型属于 `dataType`，通过 `DocDataType` 或 `DocTypeReference<T>` 表达。
- DTO / VO 字段通过 `@DocField(example = "...")` 提供示例，生成器会合成 schema example、request example 和 response
  example。

### 服务分组

`@DocService` 支持一级分组和二级服务，前端控制台按这个结构展示接口。

```java
@RestController
@RequestMapping("/category")
@DocService(
        groupId = "core",
        groupName = "家庭核心服务",
        groupOrder = 10,
        serviceId = "family-core-category",
        serviceName = "分类服务",
        serviceDescription = "分类和分类类型管理接口",
        serviceOrder = 20,
        protocol = DocProtocol.HTTP,
        version = "1.0.0"
)
public class CategoryController {
}
```

Dubbo service 可以使用同一套注解：

```java
@DocService(
        groupId = "uaa",
        groupName = "用户认证服务",
        serviceId = "uaa-user-rpc",
        serviceName = "用户 RPC 服务",
        serviceDescription = "用户认证与账号 RPC 接口",
        protocol = DocProtocol.DUBBO_TRIPLE,
        serviceInterface = UserRpcService.class,
        version = "1.0.0"
)
public interface UserRpcService {
}
```

### 请求与响应

普通对象返回：

```java
@GetMapping("/{id}")
@DocOperation(
        id = "category.get",
        summary = "获取指定分类",
        request = @DocRequest(params = {
                @DocParameter(name = "id", in = DocParamIn.PATH, required = true,
                        description = "分类 ID", dataType = @DocDataType(kind = DocDataKind.LONG), example = "1")
        }),
        response = @DocResponse(
                description = "查询成功",
                dataType = @DocDataType(kind = DocDataKind.OBJECT, type = CategoryDTO.class),
                wrapper = @DocWrapper(type = Result.class, dataPath = "data")
        )
)
public Result<CategoryDTO> get(@PathVariable Long id) {
    return Result.success(new CategoryDTO());
}
```

数组返回：

```java
@GetMapping("/list")
@DocOperation(
        id = "category.list",
        summary = "查询分类列表",
        response = @DocResponse(
                description = "查询成功",
                dataType = @DocDataType(kind = DocDataKind.ARRAY, itemType = CategoryDTO.class),
                wrapper = @DocWrapper(type = Result.class, dataPath = "data")
        )
)
public Result<List<CategoryDTO>> list() {
    return Result.success(List.of());
}
```

复杂泛型返回使用 `DocTypeReference<T>`：

```java
public static final class CategoryPageDataType extends DocTypeReference<PageResult<CategoryDTO>> {
}

@GetMapping
@DocOperation(
        id = "category.page",
        summary = "分页查询分类",
        request = @DocRequest(params = {
                @DocParameter(name = "pageNum", in = DocParamIn.QUERY, required = true,
                        description = "页码", dataType = @DocDataType(kind = DocDataKind.INTEGER), example = "1"),
                @DocParameter(name = "pageSize", in = DocParamIn.QUERY, required = true,
                        description = "页大小", dataType = @DocDataType(kind = DocDataKind.INTEGER), example = "10")
        }),
        response = @DocResponse(
                description = "查询成功",
                dataType = @DocDataType(kind = DocDataKind.GENERIC, ref = CategoryPageDataType.class),
                wrapper = @DocWrapper(type = Result.class, dataPath = "data")
        )
)
public Result<PageResult<CategoryDTO>> page(CategoryQuery query) {
    return Result.success(PageResult.empty());
}
```

这里的语义是：

```text
wrapper = Result<T>
dataType = PageResult<CategoryDTO>
final response = Result<PageResult<CategoryDTO>>
```

multipart 上传：

```java
@PostMapping("/avatar")
@DocOperation(
        id = "user.avatar.upload",
        summary = "上传头像",
        request = @DocRequest(
                body = @DocBody(enabled = true, contentType = "multipart/form-data"),
                params = {
                        @DocParameter(name = "file", in = DocParamIn.FILE, required = true,
                                description = "头像文件", dataType = @DocDataType(kind = DocDataKind.FILE)),
                        @DocParameter(name = "userId", in = DocParamIn.FORM, required = true,
                                description = "用户 ID", dataType = @DocDataType(kind = DocDataKind.LONG), example = "10001")
                }
        ),
        response = @DocResponse(
                description = "上传成功",
                dataType = @DocDataType(kind = DocDataKind.OBJECT, type = UploadResultDTO.class),
                wrapper = @DocWrapper(type = Result.class, dataPath = "data")
        )
)
public Result<UploadResultDTO> upload(MultipartFile file, Long userId) {
    return Result.success(new UploadResultDTO());
}
```

`DocParamIn.AUTO` 不会作为最终 OpenAPI `in` 输出。匹配到已有参数时沿用 Spring MVC 推断位置；无法匹配时按 query 参数处理。
`source` 用于覆盖 GET 复杂对象展开后的字段，例如 `source = "filter.keyword"`。

示例 DTO：

```java
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocExampleMode;
import top.egon.openapi.console.annotation.DocIgnore;
import top.egon.openapi.console.annotation.DocModel;

import java.util.List;

@DocModel(description = "创建用户请求")
public class UserCreateRequest {

    @NotBlank
    @JsonProperty("user_name")
    @DocField(description = "用户名", required = true, example = "mario")
    private String username;

    @DocField(description = "手机号", example = "13800000000")
    private String mobile;

    @DocField(description = "标签", example = "[\"admin\",\"owner\"]", exampleMode = DocExampleMode.JSON)
    private List<String> tags;

    @DocIgnore
    private String internalTraceId;

    @JsonIgnore
    private String rawPassword;
}
```

统一响应包装类型也建议加模型注解和字段示例：

```java
@DocModel(description = "统一响应")
public class Result<T> {

    @DocField(description = "业务状态码", required = true, example = "0")
    private Integer code;

    @DocField(description = "提示信息", required = true, example = "success")
    private String message;

    @DocField(description = "响应数据")
    private T data;
}

@DocModel(description = "分页响应")
public class PageResult<T> {

    @DocField(description = "总数", required = true, example = "100")
    private Long total;

    @DocField(description = "当前页数据")
    private List<T> records;
}
```

`Result<PageResult<CategoryDTO>>` 会生成类似示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 100,
    "records": [
      {
        "id": 1,
        "name": "生活",
        "description": "生活分类"
      }
    ]
  }
}
```

参数位置推断：

- `@PathVariable` -> `path`
- `@RequestParam` -> `query`
- `@RequestHeader` -> `header`
- `@CookieValue` -> `cookie`
- `@RequestBody` -> `requestBody`
- `DocParamIn.FILE` -> `multipart/form-data` 文件字段
- `DocParamIn.FORM` -> 表单字段
- 无注解简单类型 -> `query`
- 无注解复杂类型 + GET -> 展开为 query 参数
- 无注解复杂类型 + POST / PUT / PATCH -> `requestBody`

Schema MVP 支持：

- 基础类型：`String`、数字类型、`Boolean`、`BigDecimal`
- 时间类型：`LocalDate`、`LocalDateTime`、`Date`
- 结构类型：`Enum`、`Collection`、`Map`、DTO
- 特殊类型：`Optional<T>` 会展开为 `T`，`MultipartFile` 会生成 `type=string, format=binary`
- 泛型包装：`ResponseEntity<T>` 自动展开；`Result<T>` 通过单个 `DocWrapper` 绑定；`PageResult<T>` 作为 `dataType` 表达
- 复杂泛型：通过 `DocTypeReference<T>` 捕获并生成 schema
- 元数据来源：`@DocField`、`@DocModel`、`@DocIgnore`、`@JsonProperty`、`@JsonIgnore`
- 必填推断：`@DocField(required = true)`、`@DocParam(required = true)`、`@NotNull`、`@NotBlank`、`@NotEmpty`
- 约束推断：`@Size`、`@Min`、`@Max`、`@Pattern` 会尽量映射到 OpenAPI schema 约束
- 响应语义：`@DocResponse(noBody = true)` 不生成 content；默认按 `dataType + wrapper` 生成唯一主响应
- 示例生成：字段级 `example` 写入 schema property，并合成 request body example 和 response example

### 迁移建议

1. 保留原有 Spring MVC mapping、Jackson 注解和 Bean Validation 注解。
2. Controller / Dubbo service 上补 `@DocService`，明确 `groupId/groupName/serviceId/serviceName/protocol`。
3. 方法上补 `@DocOperation`，显式声明 `request` 和唯一主 `response`。
4. 常规参数用 `@DocRequest.params` 或参数级 `@DocParam` 补描述、required、位置、类型和 example。
5. 请求体用 `@DocRequest.body` 声明 `contentType` 和 `dataType`。
6. 返回值统一使用 `@DocResponse(dataType = ..., wrapper = ...)`。
7. `Result<List<X>>` 使用 `DocDataKind.ARRAY + itemType`；`Result<PageResult<X>>` 使用 `DocTypeReference<PageResult<X>>`。
8. DTO / VO / wrapper 上用 `@DocModel`、`@DocField` 补充模型、字段说明和 example。
9. 需要从文档隐藏的内部接口、字段或参数使用 `@DocIgnore`。
10. 移除业务模块中的 Springdoc / Swagger 注解依赖和配置；Starter 不再读取这些注解作为文档元数据来源。

### 生产端校验策略

`producer.contract-policy` 用于校验接口注解和真实方法签名是否一致；`producer.example-policy` 用于校验 DTO / VO 字段示例是否完整。

```yaml
egon:
  openapi:
    console:
      producer:
        contract-policy: WARN # OFF / WARN / FAIL
        example-policy: WARN  # OFF / WARN / FAIL
```

- `OFF`：不校验。
- `WARN`：不中断文档生成。
- `FAIL`：发现缺失响应契约、返回包装冲突、泛型冲突、参数缺失或非法 JSON 示例时阻断生成。

## 网关模块配置

网关模块负责聚合服务、登录控制台、代理调试请求和导出文档。账号密码等敏感配置建议放在 `application-dev.yml`、Nacos
对应环境配置或部署平台密钥中，不要放在公共 `application.yml`。

```yaml
egon:
  openapi:
    console:
      enabled: true
      mode: auto
      base-path: /openapi-console
      title: Demo API Debug Console
      environment: dev
      auth:
        username: admin
        password: "{sha256}<password-sha256-hex>"
        session-secret: <high-entropy-session-secret>
        secure-cookie: false
        ttl: 8h
        challenge-ttl: 2m
        request-signature-ttl: 5m
        request-signing-enabled: true
      producer:
        enabled: false
      client:
        engine: VIRTUAL_THREAD
        max-connections: 200
      services:
        - id: demo-service
          name: Demo Service
          group: demo
          open-api-url: http://demo-service/v3/api-docs
          open-api-access-header: X-OpenAPI-Console-Token
          open-api-access-token: <internal-openapi-doc-token>
          base-url: http://demo-service
          order: 10
```

默认页面地址：`/openapi-console/index.html`。

## 密码配置

`egon.openapi.console.auth.password` 支持三种格式：

```yaml
egon:
  openapi:
    console:
      auth:
        # 推荐：保存密码 SHA-256 摘要，服务端不会反解密码
        password: "{sha256}<password-sha256-hex>"

        # 仅建议本地或 dev 临时使用
        # password: "{plain}<raw-password>"

        # 兼容旧配置，未加前缀时按明文处理
        # password: "<raw-password>"
```

生成 SHA-256 摘要时不要带换行。macOS / Linux 示例：

```bash
printf '%s' 'OpenApiDev@2026' | shasum -a 256
```

然后填写：

```yaml
password: "{sha256}<上面命令输出的第一段 hex>"
```

当前实现不会也不应该“解密 password”。`{sha256}` 是单向摘要，登录校验流程是：

1. Spring Boot 通过 `@ConfigurationProperties(prefix = "egon.openapi.console")` 把 yml / Nacos 配置绑定到
   `ApiDocConsoleProperties.Auth.password`。
2. 前端请求 `/login-challenge` 获取一次性 `challengeId` 和 `nonce`。
3. 前端只在浏览器内使用用户输入的明文密码计算 `SHA-256(password)`，再用这个摘要对 challenge 载荷做 HMAC proof。
4. 后端在 `ApiDocConsoleSessionService#configuredPasswordDigest()` 中读取配置：
    - `{sha256}`：把配置中的 hex 摘要转成字节。
    - `{plain}`：先对配置明文做 SHA-256 摘要。
    - 无前缀：兼容旧明文配置，也先做 SHA-256 摘要。
5. 后端使用配置得到的密码摘要重新计算 HMAC proof，并与前端提交的 proof 做常量时间比较。

因此，配置文件里填 `{sha256}` 后，服务端不需要明文密码，也没有解密步骤。

## 控制台请求签名

登录成功后，控制台会生成一段本次会话使用的请求签名密钥。后续控制台自身 API 都会带以下请求头：

```http
X-OpenAPI-Console-Timestamp: <timestamp>
X-OpenAPI-Console-Nonce: <nonce>
X-OpenAPI-Console-Signature: <hmac-sha256>
```

后端校验签名、时间窗口和 nonce，降低控制台 API 被重放或伪造调用的风险。会话 Cookie 使用 `session-secret` 派生 AES-GCM
密钥加密载荷，再做 HMAC 完整性保护。`session-secret` 必须使用高强度随机值，且生产环境不要使用示例值。

控制台签名只保护控制台前后端交互。点击“发送请求”或执行压测时，Starter 代理到具体业务接口的请求不会自动添加这套控制台签名；业务接口自己的鉴权仍由请求
Headers、Bearer Token、Basic Auth、API Key 等调试参数决定。如果确实需要给代理到业务接口的请求追加独立签名头，可以单独开启
`signing.enabled` 并配置对应 header 和密钥。

## OpenAPI 文档内部 Token

`open-api-access-header` / `open-api-access-token` 是服务端拉取 `/v3/api-docs` 时使用的内部访问凭据，只配置在 gateway /
Nacos / yml 中，不会下发给前端页面，也不需要在浏览器页面中录入。前端访问 `/openapi-console/api/openapi/{serviceId}`
时只携带控制台登录会话和控制台 API 签名，Starter 后端会在聚合 OpenAPI JSON 时自动把内部 header 注入到请求里。

业务接口调试所需的 JWT、Basic Auth 或 API Key 仍然在页面的 `Auth` 页签配置，这类认证会进入“发送请求”和“压力测试”的业务请求，不用于读取
OpenAPI 文档 JSON。

## 关键配置项

| 配置项                                 | 默认值                       | 说明                                               |
|-------------------------------------|---------------------------|--------------------------------------------------|
| `enabled`                           | `false`                   | 是否开放控制台页面                                        |
| `mode`                              | `AUTO`                    | `AUTO` / `OFF` / `READ_ONLY` / `FULL`            |
| `base-path`                         | `/openapi-console`        | 控制台访问路径                                          |
| `request-timeout`                   | `30s`                     | 控制台代理单次请求超时时间                                    |
| `max-response-size`                 | `3MB`                     | 控制台代理响应最大读取大小                                    |
| `auth.username`                     | `admin`                   | 控制台登录用户名                                         |
| `auth.password`                     | `OpenApi@123456`          | 控制台登录密码配置，开启控制台时禁止使用默认值                          |
| `auth.session-secret`               | 示例值                       | 会话签名和加密密钥，开启控制台时必须替换                             |
| `auth.ttl`                          | `8h`                      | 登录会话有效期                                          |
| `auth.challenge-ttl`                | `2m`                      | 登录 challenge 有效期                                 |
| `auth.request-signature-ttl`        | `5m`                      | 控制台 API 请求签名时间窗口                                 |
| `auth.request-signing-enabled`      | `true`                    | 是否校验控制台 API 请求签名                                 |
| `auth.reject-default-credentials`   | `true`                    | 开启控制台时是否拒绝默认账号密码和默认 session-secret               |
| `auth.max-login-failures`           | `5`                       | 单个运行实例内连续登录失败锁定阈值                                |
| `auth.login-lock-duration`          | `10m`                     | 登录失败锁定时长                                         |
| `signing.enabled`                   | `false`                   | 是否给代理到业务接口的调试请求追加签名头                             |
| `signing.algorithm`                 | `HmacSHA256`              | 代理业务请求签名算法                                       |
| `client.engine`                     | `AUTO`                    | HTTP 客户端引擎，`AUTO` 默认选择虚拟线程客户端；可显式设置 `REACTIVE`   |
| `client.max-connections`            | `200`                     | Reactive 客户端连接池上限                                |
| `client.response-timeout`           | `30s`                     | 客户端响应超时时间                                        |
| `services[].open-api-access-header` | `X-OpenAPI-Console-Token` | 后端聚合 OpenAPI JSON 时注入的内部 header 名称               |
| `services[].open-api-access-token`  | 空                         | 后端聚合 OpenAPI JSON 时注入的内部 token                   |
| `producer.contract-policy`          | `WARN`                    | 接口契约和方法签名一致性校验策略，`OFF` / `WARN` / `FAIL`         |
| `producer.example-policy`           | `WARN`                    | DTO / VO 字段 example 校验策略，`OFF` / `WARN` / `FAIL` |
| `load-test.max-requests`            | `200`                     | 单次压测最大请求数                                        |
| `load-test.max-concurrency`         | `20`                      | 单次压测最大并发数                                        |
| `load-test.max-active-runs`         | `2`                       | 当前 JVM 同时运行的压测任务数                                |
| `load-test.max-active-concurrency`  | `50`                      | 当前 JVM 压测全局并发上限                                  |
| `export.enabled`                    | `true`                    | 是否允许导出文档                                         |

## 环境建议

- dev：可以 `mode=auto` 或 `FULL`，可临时使用 `{plain}`，但建议尽早改成 `{sha256}`。
- test/staging：建议使用 `{sha256}`，开启控制台 API 签名。
- prod：建议 `mode=OFF`；如必须开放，至少使用 `READ_ONLY`、HTTPS、强密码、强 `session-secret`、`secure-cookie=true`。
