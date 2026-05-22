# OpenAPI Console Annotation Redesign

本文档定义下一版 `openapi-debug-console-spring-boot-starter` 的自有文档注解方案。目标是替换当前 `DocSchemaRef`、
`DocOperationParameter`、`responses[]`、`wrappers[]` 这套表达不清的设计。

## 1. 设计结论

新的注解体系遵循下面几个硬规则：

1. 接口文档必须显式描述入参和返回值，不能只依赖方法签名推断。
2. 方法签名仍然参与校验，用来发现注解和真实代码不一致的问题。
3. DTO / VO 字段必须支持 `example`，生成 OpenAPI 时需要自动合成 request / response 示例。
4. 一个接口只有一个主返回描述：`response = dataType + wrapper`。
5. `wrapper` 只能有一个，表示最外层统一响应包装，例如 `Result<T>`。
6. 分页、列表、Map、复杂泛型都属于 `dataType`，不属于 `wrapper` 数组。
7. 复杂泛型通过 `DocTypeReference<T>` 指定具体类型，不用字符串猜，不用多个 wrapper 硬拼。
8. `DocService` 同时支持一级分组 `group` 和二级服务 `service`，用于前端二级导航。

核心表达模型：

```text
DocService(group + service)
  -> DocOperation(request + response)
      -> request(params + body)
      -> response(dataType + wrapper)
          -> DTO/VO @DocField(example)
```

## 2. 注解清单

建议保留和新增以下注解：

| 注解 / 类型               | 作用                                                               |
|-----------------------|------------------------------------------------------------------|
| `@DocService`         | 标在 Controller 或 Dubbo service interface 上，定义一级 group 和二级 service |
| `@DocOperation`       | 标在接口方法上，定义接口基础信息、请求和唯一主响应                                        |
| `@DocRequest`         | 描述接口请求，包括 path/query/header/cookie/form 参数和 body                 |
| `@DocParameter`       | 描述单个入参                                                           |
| `@DocBody`            | 描述 request body 或 response body                                  |
| `@DocResponse`        | 描述唯一主返回，结构固定为 `dataType + wrapper`                               |
| `@DocDataType`        | 描述对象、数组、Map、文件、泛型引用等数据类型                                         |
| `@DocWrapper`         | 描述单个最外层响应包装                                                      |
| `@DocModel`           | 标在 DTO / VO 类型上，描述模型                                             |
| `@DocField`           | 标在 DTO / VO 字段或 getter 上，描述字段和 example                           |
| `@DocIgnore`          | 忽略类、方法、字段或参数                                                     |
| `DocTypeReference<T>` | 用 Java 泛型类型捕获复杂 `dataType`                                       |

## 3. 服务层注解

`@DocService` 标在 HTTP Controller、Dubbo service interface 或未来的 RPC service 上。它必须支持二级分类：

- `group`：一级分组，例如 `core`、`uaa`、`ai`。
- `service`：二级服务，例如 `family-core`、`family-uaa`、`qwen-ai`。

建议定义：

```java
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocService {

    /**
     * 一级分组 ID。
     */
    String groupId() default "default";

    /**
     * 一级分组名称。
     */
    String groupName() default "默认分组";

    /**
     * 一级分组排序。
     */
    int groupOrder() default 0;

    /**
     * 二级服务 ID。
     */
    String serviceId() default "";

    /**
     * 二级服务名称。
     */
    String serviceName();

    /**
     * 二级服务描述。
     */
    String serviceDescription() default "";

    /**
     * 二级服务排序。
     */
    int serviceOrder() default 0;

    /**
     * 服务版本。
     */
    String version() default "";

    /**
     * 协议类型。
     */
    DocProtocol protocol() default DocProtocol.AUTO;

    /**
     * Dubbo / RPC 场景下的接口类型。
     */
    Class<?> serviceInterface() default Void.class;

    /**
     * 是否生成文档。
     */
    boolean enabled() default true;
}
```

协议枚举：

```java
public enum DocProtocol {
    AUTO,
    HTTP,
    DUBBO_TRIPLE,
    GRPC
}
```

HTTP Controller 示例：

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

Dubbo service 示例：

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

前端导航使用规则：

```text
groupId/groupName      -> 一级分组
serviceId/serviceName  -> 二级服务
DocOperation           -> 当前二级服务下的接口列表
```

## 4. 接口层注解

`@DocOperation` 只描述一个接口，不再放多个 response，也不再通过 `wrappers[]` 表示嵌套。

建议定义：

```java
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocOperation {

    /**
     * 稳定操作 ID。
     */
    String id() default "";

    /**
     * 一句话标题。
     */
    String summary();

    /**
     * 详细说明。
     */
    String description() default "";

    /**
     * 是否需要登录。
     */
    boolean auth() default true;

    /**
     * 排序值。
     */
    int order() default 0;

    /**
     * 额外 tag。
     */
    String[] tags() default {};

    /**
     * 请求契约。
     */
    DocRequest request() default @DocRequest;

    /**
     * 唯一主响应契约。
     */
    DocResponse response() default @DocResponse;
}
```

说明：

- `response()` 是单数。
- 如果业务统一用 `Result<T>`，HTTP 错误、业务错误通过 `Result.code/message` 表达，不为每个错误码维护一套 OpenAPI response。
- 如果未来确实需要多个 HTTP status 的响应文档，可以单独新增 `@DocErrorResponse`，但不进入本轮设计。

## 5. 请求注解

`@DocRequest` 描述完整请求。多个参数是合理的，所以 `params` 是数组；请求体只能有一个。

```java
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocRequest {

    /**
     * path/query/header/cookie/form 参数。
     */
    DocParameter[] params() default {};

    /**
     * 请求体。
     */
    DocBody body() default @DocBody(enabled = false);
}
```

`@DocParameter`：

```java
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocParameter {

    /**
     * 参数名称。
     */
    String name() default "";

    /**
     * 参数位置。
     */
    DocParamIn in() default DocParamIn.AUTO;

    /**
     * 参数描述。
     */
    String description() default "";

    /**
     * 是否必填。
     */
    boolean required() default false;

    /**
     * 是否隐藏。
     */
    boolean hidden() default false;

    /**
     * 对应复杂对象展开后的来源字段路径。
     */
    String source() default "";

    /**
     * 参数类型。
     */
    DocDataType dataType() default @DocDataType;

    /**
     * 参数示例。
     */
    String example() default "";

    /**
     * 示例解析方式。
     */
    DocExampleMode exampleMode() default DocExampleMode.AUTO;
}
```

参数位置：

```java
public enum DocParamIn {
    AUTO,
    PATH,
    QUERY,
    HEADER,
    COOKIE,
    BODY,
    FORM,
    FILE
}
```

`FORM` 和 `FILE` 用于 `multipart/form-data` 或 `application/x-www-form-urlencoded`，不再单独设计 `DocFormField`。

## 6. Body 注解

`@DocBody` 同时用于 request body 和 response body。

```java
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocBody {

    /**
     * 是否启用 body。
     */
    boolean enabled() default true;

    /**
     * body 描述。
     */
    String description() default "";

    /**
     * 内容类型。
     */
    String contentType() default "application/json";

    /**
     * 是否必填。
     */
    boolean required() default true;

    /**
     * body 数据类型。
     */
    DocDataType dataType() default @DocDataType;

    /**
     * body 最外层包装。
     */
    DocWrapper wrapper() default @DocWrapper;

    /**
     * body 级别示例。为空时由 DTO / VO 字段 example 自动生成。
     */
    String example() default "";

    /**
     * body 示例解析方式。
     */
    DocExampleMode exampleMode() default DocExampleMode.JSON;
}
```

request body 通常不需要 wrapper；response body 通常需要 wrapper。

## 7. 返回值注解

`@DocResponse` 只有一个，且核心结构固定为：

```text
response = dataType + wrapper
```

建议定义：

```java
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocResponse {

    /**
     * HTTP 状态码。默认 200。
     */
    int status() default 200;

    /**
     * 响应描述。
     */
    String description() default "OK";

    /**
     * 是否无响应体。
     */
    boolean noBody() default false;

    /**
     * 响应内容类型。
     */
    String contentType() default "application/json";

    /**
     * 业务数据类型。
     */
    DocDataType dataType() default @DocDataType;

    /**
     * 最外层包装。只能一个。
     */
    DocWrapper wrapper() default @DocWrapper;

    /**
     * 响应头。
     */
    DocParameter[] headers() default {};

    /**
     * 响应级示例。为空时由 wrapper + dataType + DTO 字段 example 自动生成。
     */
    String example() default "";

    /**
     * 响应级示例解析方式。
     */
    DocExampleMode exampleMode() default DocExampleMode.JSON;
}
```

关键规则：

1. `dataType` 描述真实业务数据。
2. `wrapper` 描述最外层统一响应包装。
3. `wrapper` 只能一个，不能是数组。
4. `Result<PageResult<UserDTO>>` 的表达不是 `wrappers = {Result.class, PageResult.class}`。
5. 正确表达是：

```text
wrapper = Result.class
dataType = PageResult<UserDTO>
```

## 8. 数据类型注解

`@DocDataType` 只描述数据类型。它可以表达简单类型、对象、数组、Map、文件，也可以通过 `DocTypeReference<T>` 表达复杂泛型。

```java
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocDataType {

    /**
     * 类型种类。
     */
    DocDataKind kind() default DocDataKind.AUTO;

    /**
     * 普通对象类型。
     */
    Class<?> type() default Void.class;

    /**
     * 复杂泛型引用。
     */
    Class<? extends DocTypeReference<?>> ref() default DocTypeReference.None.class;

    /**
     * 简单数组 / List 的元素类型。
     */
    Class<?> itemType() default Void.class;

    /**
     * 简单 Map 的 key 类型。
     */
    Class<?> keyType() default String.class;

    /**
     * 简单 Map 的 value 类型。
     */
    Class<?> valueType() default Void.class;

    /**
     * 数据路径。只用于标记主要业务数据路径，不用于表达 wrapper 链。
     */
    String dataPath() default "";
}
```

类型种类：

```java
public enum DocDataKind {
    AUTO,
    VOID,
    BOOLEAN,
    INTEGER,
    LONG,
    DECIMAL,
    STRING,
    DATE,
    DATETIME,
    ENUM,
    OBJECT,
    ARRAY,
    MAP,
    FILE,
    BINARY,
    GENERIC
}
```

`DocTypeReference<T>`：

```java
public abstract class DocTypeReference<T> {
    public static final class None extends DocTypeReference<Void> {
    }
}
```

复杂泛型示例：

```java
public final class UserPageDataType extends DocTypeReference<PageResult<UserDTO>> {
}

public final class UserListDataType extends DocTypeReference<List<UserDTO>> {
}

public final class UserMapDataType extends DocTypeReference<Map<String, List<UserDTO>>> {
}
```

使用规则：

```java
// UserDTO
@DocDataType(kind = DocDataKind.OBJECT, type = UserDTO.class)

// List<UserDTO>
@DocDataType(kind = DocDataKind.ARRAY, itemType = UserDTO.class)

// Map<String, UserDTO>
@DocDataType(kind = DocDataKind.MAP, keyType = String.class, valueType = UserDTO.class)

// PageResult<UserDTO>
@DocDataType(kind = DocDataKind.GENERIC, ref = UserPageDataType.class)

// Map<String, List<UserDTO>>
@DocDataType(kind = DocDataKind.GENERIC, ref = UserMapDataType.class)
```

## 9. Wrapper 注解

`@DocWrapper` 只表示最外层包装，不能表达多层包装数组。

```java
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocWrapper {

    /**
     * 包装类型。Void.class 表示没有 wrapper。
     */
    Class<?> type() default Void.class;

    /**
     * wrapper 中承载 dataType 的字段路径。
     */
    String dataPath() default "data";

    /**
     * wrapper 泛型参数索引。默认把 dataType 绑定到第一个泛型参数。
     */
    int genericIndex() default 0;
}
```

统一响应：

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
```

分页数据：

```java
@DocModel(description = "分页数据")
public class PageResult<T> {

    @DocField(description = "总数", required = true, example = "100")
    private Long total;

    @DocField(description = "当前页数据")
    private List<T> records;
}
```

`Result<PageResult<UserDTO>>` 的最终写法：

```java
public final class UserPageDataType extends DocTypeReference<PageResult<UserDTO>> {
}

@DocResponse(
        description = "查询成功",
        dataType = @DocDataType(kind = DocDataKind.GENERIC, ref = UserPageDataType.class),
        wrapper = @DocWrapper(type = Result.class, dataPath = "data")
)
```

语义：

```text
最外层 wrapper: Result<T>
T 的具体类型: PageResult<UserDTO>
最终响应类型: Result<PageResult<UserDTO>>
```

## 10. DTO / VO 注解和 example

DTO / VO 必须能写字段描述和 example。生成器基于这些字段自动生成 schema example 和 request / response example。

```java
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocModel {

    String name() default "";

    String description() default "";
}
```

```java
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocField {

    /**
     * 字段名。为空时使用 Java 字段名或 Jackson 字段名。
     */
    String name() default "";

    /**
     * 字段描述。
     */
    String description() default "";

    /**
     * 是否必填。
     */
    boolean required() default false;

    /**
     * 是否隐藏。
     */
    boolean hidden() default false;

    /**
     * 是否可为空。
     */
    boolean nullable() default false;

    /**
     * 是否废弃。
     */
    boolean deprecated() default false;

    /**
     * 字段类型补充。
     */
    DocDataType dataType() default @DocDataType;

    /**
     * 字段示例。
     */
    String example() default "";

    /**
     * 示例解析方式。
     */
    DocExampleMode exampleMode() default DocExampleMode.AUTO;
}
```

示例解析方式：

```java
public enum DocExampleMode {
    AUTO,
    STRING,
    JSON
}
```

DTO 示例：

```java
@DocModel(name = "UserDTO", description = "用户信息")
public class UserDTO {

    @DocField(description = "用户 ID", required = true, example = "10001")
    private Long userId;

    @DocField(description = "用户名", required = true, example = "mario")
    private String username;

    @DocField(description = "手机号", example = "13800000000")
    private String mobile;

    @DocField(description = "标签", example = "[\"admin\",\"owner\"]", exampleMode = DocExampleMode.JSON)
    private List<String> tags;

    @DocField(description = "扩展信息", example = "{\"source\":\"web\"}", exampleMode = DocExampleMode.JSON)
    private Map<String, Object> extra;
}
```

`exampleMode` 规则：

- `AUTO`：按 Java 类型转换，Long 输出数字，Boolean 输出布尔，String 输出字符串。
- `STRING`：强制输出字符串。
- `JSON`：把 example 当 JSON 解析，适合数组、对象、Map。

## 11. HTTP Controller 示例

分页查询：

```java
public final class CategoryPageDataType extends DocTypeReference<PageResult<CategoryDTO>> {
}

@RestController
@RequestMapping("/category")
@DocService(
        groupId = "core",
        groupName = "家庭核心服务",
        serviceId = "family-core-category",
        serviceName = "分类服务",
        protocol = DocProtocol.HTTP
)
public class CategoryController {

    @GetMapping
    @DocOperation(
            id = "category.page",
            summary = "分页查询分类",
            request = @DocRequest(params = {
                    @DocParameter(name = "pageNum", in = DocParamIn.QUERY, required = true,
                            description = "页码", dataType = @DocDataType(kind = DocDataKind.INTEGER), example = "1"),
                    @DocParameter(name = "pageSize", in = DocParamIn.QUERY, required = true,
                            description = "页大小", dataType = @DocDataType(kind = DocDataKind.INTEGER), example = "10"),
                    @DocParameter(name = "name", in = DocParamIn.QUERY,
                            description = "分类名称", dataType = @DocDataType(kind = DocDataKind.STRING), example = "生活")
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

## 12. Dubbo Service 示例

Dubbo interface：

```java
public final class UserRpcPageDataType extends DocTypeReference<PageResult<UserDTO>> {
}

@DocService(
        groupId = "uaa",
        groupName = "用户认证服务",
        serviceId = "uaa-user-rpc",
        serviceName = "用户 RPC 服务",
        serviceDescription = "用户 RPC 查询接口",
        protocol = DocProtocol.DUBBO_TRIPLE,
        serviceInterface = UserRpcService.class
)
public interface UserRpcService {

    @DocOperation(
            id = "user.rpc.page",
            summary = "RPC 分页查询用户",
            request = @DocRequest(body = @DocBody(
                    enabled = true,
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = UserQuery.class)
            )),
            response = @DocResponse(
                    description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.GENERIC, ref = UserRpcPageDataType.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")
            )
    )
    Result<PageResult<UserDTO>> page(UserQuery query);
}
```

Dubbo 文档生成规则：

- `DocProtocol.DUBBO_TRIPLE` 时，接口路径按 Triple 规则生成：`/{serviceFullName}/{methodName}`。
- request body 默认使用方法参数。如果参数只有一个对象，直接生成该对象 schema。
- response 仍按 `dataType + wrapper` 生成。
- `serviceInterface` 优先使用注解指定值；为空时从当前 interface 或实现类推断。

## 13. OpenAPI example 生成规则

生成器需要输出：

```text
components.schemas.*.properties.*.example
requestBody.content.<contentType>.example
responses.<status>.content.<contentType>.example
```

生成步骤：

1. 根据 `DocResponse.wrapper` 生成最外层对象。
2. 根据 `DocWrapper.dataPath` 找到承载业务数据的位置。
3. 根据 `DocResponse.dataType` 生成业务数据 schema。
4. 如果 `dataType.ref` 是 `DocTypeReference<T>`，解析真实泛型 `T`。
5. 递归读取 DTO / VO 的 `@DocField.example`。
6. `exampleMode=AUTO` 时按字段类型转换。
7. `exampleMode=JSON` 时解析 JSON 后写入对象或数组。
8. 如果字段没有 example，按 `example-policy` 处理。

`Result<PageResult<UserDTO>>` 示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 100,
    "records": [
      {
        "userId": 10001,
        "username": "mario",
        "mobile": "13800000000",
        "tags": ["admin", "owner"]
      }
    ]
  }
}
```

## 14. 校验策略

新增 producer 配置：

```yaml
egon:
  openapi:
    console:
      producer:
        contract-policy: WARN # OFF / WARN / FAIL
        example-policy: WARN  # OFF / WARN / FAIL
```

建议默认：

- dev：`WARN`
- test/staging：`WARN`
- prod 构建或发布前：`FAIL`

校验点：

1. `@DocOperation.response` 未声明且方法返回值不是 `void`：按 policy 处理。
2. `@DocResponse.dataType` 与方法返回值泛型冲突：按 policy 处理。
3. `@DocResponse.wrapper` 与方法返回值最外层类型冲突：按 policy 处理。
4. `@DocParameter` 声明的参数在 Spring MVC mapping 或方法签名中找不到：按 policy 处理。
5. DTO / VO 字段缺少 `@DocField.example`：按 example-policy 处理。
6. `exampleMode=JSON` 但 example 不是合法 JSON：直接失败。
7. `DocTypeReference<T>` 无法解析泛型实参：直接失败。

## 15. 和当前实现的替换关系

废弃当前设计：

```text
DocOperationParameter
DocSchemaRef
DocRequestBody.formFields
DocResponse.schema
DocResponse[] responses
Class<?>[] wrappers
```

替换为：

```text
DocOperation.request
DocOperation.response
DocRequest.params
DocRequest.body
DocResponse.dataType
DocResponse.wrapper
DocDataType.ref
DocField.example
```

迁移原则：

1. 原 `DocSchemaRef(type = X.class)` -> `DocDataType(type = X.class)`。
2. 原 `DocSchemaRef(array = true, type = X.class)` -> `DocDataType(kind = ARRAY, itemType = X.class)`。
3. 原 `wrappers = {Result.class, PageResult.class}` 不再允许。
4. `Result<PageResult<X>>` 改为：

```java
dataType = @DocDataType(kind = DocDataKind.GENERIC, ref = XPageDataType.class)
wrapper = @DocWrapper(type = Result.class, dataPath = "data")
```

其中：

```java
public final class XPageDataType extends DocTypeReference<PageResult<X>> {
}
```

## 16. 最终判断

这套方案能明确支持：

- Controller / Dubbo service 的一级 group 和二级 service 分类。
- 完整入参文档。
- 唯一主响应文档。
- `response = dataType + wrapper`。
- 单 wrapper，不再使用 wrapper 数组。
- 数组、对象、Map、文件。
- `Result<T>`、`Result<PageResult<T>>`、`Map<String, List<T>>` 等复杂泛型。
- DTO / VO 字段 example。
- 自动生成 request / response example。
- 注解契约和真实方法签名的一致性校验。

这个版本可以作为下一轮实现 plan 的基础。
