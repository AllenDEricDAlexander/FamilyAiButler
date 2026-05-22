# OpenAPI Annotation Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` or
`superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace all Springdoc/Swagger annotation compatibility with a fully owned annotation contract that can
describe complex REST inputs and responses while still generating standard OpenAPI JSON at `/v3/api-docs`.

**Architecture:** Spring MVC annotations and Java signatures remain the source of routing and type truth. The
`top.egon.openapi.console.annotation` annotations supply documentation metadata, explicit operation-level input/response
descriptions, status codes, media types, wrapper hints, hidden elements, and model descriptions. The generator must not
read `io.swagger.v3.oas.annotations.*`; existing console aggregation, auth, signing, export, tryout, load-test, and
storage behavior remain unchanged.

**Tech Stack:** Spring Boot 3.5.x, Spring MVC, Jackson, Jakarta Validation metadata, Maven, Node test runner for static
console parser tests.

---

## Scope Decisions

### In Scope

- Remove `swagger-annotations-jakarta` from `openapi-debug-console-spring-boot-starter`.
- Remove all imports and fallback logic for `io.swagger.v3.oas.annotations.*` from starter code and tests.
- Remove `example` fields from owned annotations:
    - `DocParam.example()`
    - `DocField.example()`
- Extend `DocOperation` with explicit operation-level input and response properties.
- Add nested metadata annotations that can describe:
    - extra path/query/header/cookie parameters not present in Java method arguments
    - hidden or renamed inferred parameters
    - multiple request body media types
    - multipart/form-data fields and files
    - multiple response status codes
    - response headers
    - wrapper/container hints for erased or ambiguous return types
    - no-content responses
- Keep generated output as standard OpenAPI JSON, not a private document format.
- Migrate project-owned business Swagger annotations to the new annotations where they affect generated docs.
- Update tests and README to remove Swagger fallback language.

### Out of Scope For This Plan

- Dubbo Triple scanner implementation.
- gRPC/proto scanner implementation.
- `/v3/rpc-docs`, `/v3/all-docs`, or new `/docs` UI.
- Replacing the existing debug console frontend design.
- Persisting examples or presets server-side.
- Reintroducing Springdoc or Swagger annotation scanning through another dependency.

---

## Current Implementation Gaps This Plan Closes

- Starter still reads `@Tag`, `@Operation`, `@Parameter`, and `@Schema` as fallback metadata.
- Starter POM still declares `io.swagger.core.v3:swagger-annotations-jakarta`.
- Owned annotations still expose `example`, which the new requirement explicitly removes.
- `DocOperation` currently has only high-level metadata and cannot describe rich request/response contracts.
- Generated responses still default to only `200 OK` unless inferred from the Java return type.
- No operation-level way to document response headers, multiple status codes, no-content responses, multipart inputs, or
  erased generic wrappers.
- Existing business modules still contain Swagger annotations in `family-core` and `family-ai/qwen-ai`.
- `qwen-ai` architecture test still asserts Swagger annotations on `ImageController`.

---

## Proposed Annotation Contract

### `DocOperation`

Modify
`backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocOperation.java`:

```java
public @interface DocOperation {

    String id() default "";

    String summary();

    String description() default "";

    boolean auth() default true;

    int order() default 0;

    String[] tags() default {};

    DocOperationParameter[] parameters() default {};

    DocRequestBody[] requestBodies() default {};

    DocResponse[] responses() default {};
}
```

Rules:

- Java method signatures remain the default source for parameter and response types.
- `parameters` augments or overrides inferred path/query/header/cookie metadata by `(in, name)`.
- `requestBodies` augments inferred request body metadata; `schema.type = Void.class` means infer from Java parameter.
- `responses` replaces the current default response generation when present; `schema.type = Void.class` means infer from
  Java return type for 2xx responses.
- `responses` with `noContent = true` generates a response without `content`.

### `DocOperationParameter`

Create
`backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocOperationParameter.java`:

```java
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocOperationParameter {

    String name();

    DocParamIn in() default DocParamIn.AUTO;

    String description() default "";

    boolean required() default false;

    boolean hidden() default false;

    String source() default "";
}
```

Rules:

- `hidden = true` suppresses an inferred parameter with the same `(in, name)`.
- `source` is a dotted Java field path for flattened query DTO fields, for example `filter.ownerId`.
- No `example` support.

### `DocRequestBody`

Create
`backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocRequestBody.java`:

```java
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocRequestBody {

    String name() default "";

    String description() default "";

    boolean required() default true;

    String[] contentTypes() default {"application/json"};

    DocSchemaRef schema() default @DocSchemaRef;

    DocFormField[] formFields() default {};
}
```

Rules:

- `contentTypes` supports `application/json`, `application/x-www-form-urlencoded`, and `multipart/form-data`.
- For normal JSON body, `schema.type = Void.class` means infer from `@RequestBody` or inferred complex POST/PUT/PATCH
  parameter.
- For multipart, `formFields` describes form fields and files when Java reflection cannot infer enough detail.

### `DocResponse`

Create
`backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocResponse.java`:

```java
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocResponse {

    int status() default 200;

    String description() default "OK";

    String[] contentTypes() default {"application/json"};

    DocSchemaRef schema() default @DocSchemaRef;

    DocResponseHeader[] headers() default {};

    boolean noContent() default false;
}
```

Rules:

- Multiple `DocResponse` entries generate multiple OpenAPI response status codes.
- `noContent = true` suppresses response `content`; use this for `204` or pure command endpoints.
- `schema.type = Void.class` means infer from Java return type.
- `schema` can override erased return types such as `Result<?>`, `ResponseEntity<?>`, `Object`, or `Map`.

### `DocSchemaRef`

Create
`backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocSchemaRef.java`:

```java
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocSchemaRef {

    Class<?> type() default Void.class;

    Class<?>[] wrappers() default {};

    boolean array() default false;

    String dataPath() default "";

    String description() default "";
}
```

Rules:

- `type = Void.class` means infer from Java type.
- `wrappers = {Result.class, PageResult.class}` expresses wrapper shape when Java generic information is erased or
  intentionally hidden.
- `array = true` wraps `type` or inferred type as `array.items`.
- `dataPath` documents wrapper nesting such as `data`, `data.records`, or `payload.items`; the generator should emit
  `x-doc-data-path` while still generating valid OpenAPI schema.
- This does not replace Java signatures when signatures are specific; it only fills reflection gaps.

### `DocFormField`

Create
`backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocFormField.java`:

```java
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocFormField {

    String name();

    String description() default "";

    boolean required() default false;

    boolean file() default false;

    boolean array() default false;

    Class<?> type() default String.class;
}
```

Rules:

- `file = true` maps to OpenAPI `type: string`, `format: binary`.
- `array = true` maps to an array of `type` or binary files.

### `DocResponseHeader`

Create
`backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocResponseHeader.java`:

```java
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocResponseHeader {

    String name();

    String description() default "";

    Class<?> type() default String.class;

    boolean required() default false;
}
```

### `DocParamIn`

Create
`backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocParamIn.java`:

```java
public enum DocParamIn {
    AUTO,
    PATH,
    QUERY,
    HEADER,
    COOKIE,
    BODY,
    FORM,
    PART
}
```

Modify `DocParam` to use `DocParamIn` and remove `example`:

```java
public @interface DocParam {

    String name() default "";

    String description() default "";

    boolean required() default false;

    DocParamIn in() default DocParamIn.AUTO;
}
```

Modify `DocField` to remove `example`:

```java
public @interface DocField {

    String description() default "";

    boolean required() default false;

    boolean hidden() default false;
}
```

---

## Files To Modify

### Starter

- Modify: `backend/openapi-debug-console-spring-boot-starter/pom.xml`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocOperation.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocParam.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocField.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocOperationParameter.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocRequestBody.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocResponse.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocSchemaRef.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocFormField.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocResponseHeader.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocParamIn.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/openapi/ApiDocSpringMvcOpenApiGenerator.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/openapi/ApiDocOpenApiSchemaGenerator.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/test/java/top/egon/openapi/console/ApiDocSpringMvcOpenApiGeneratorTest.java`
- Modify: `backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-openapi.test.cjs` only if a
  parser regression fixture needs to assert new response structures.
- Modify: `backend/openapi-debug-console-spring-boot-starter/README.md`

### Project-Owned Business Modules

- Modify: `backend/family-core/src/main/java/top/egon/familyaibutler/family/adapter/web/CategoryController.java`
- Modify: `backend/family-core/src/main/java/top/egon/familyaibutler/family/adapter/web/PasswordViewController.java`
- Modify DTO/PO files under `backend/family-core/src/main/java/top/egon/familyaibutler/family/application/result/` that
  currently use `@Schema`.
- Modify DTO/PO files under
  `backend/family-core/src/main/java/top/egon/familyaibutler/family/infrastructure/persistence/jpa/dataobject/` only if
  those classes are exposed through controller return/request types.
- Modify:
  `backend/family-core/src/main/java/top/egon/familyaibutler/family/domain/passwordview/model/valueobject/StrengthDTO.java`
- Modify:
  `backend/family-core/src/main/java/top/egon/familyaibutler/family/infrastructure/configuration/SwaggerConfig.java`
- Modify: `backend/family-ai/qwen-ai/src/main/java/top/egon/familyaibutler/ai/qwen/adapter/web/ImageController.java`
- Modify:
  `backend/family-ai/qwen-ai/src/test/java/top/egon/familyaibutler/ai/qwen/architecture/QwenDddStructureTest.java`

---

## Task 1: Remove Springdoc/Swagger Annotation Compatibility From Starter

**Files:**

- Modify: `backend/openapi-debug-console-spring-boot-starter/pom.xml`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/openapi/ApiDocSpringMvcOpenApiGenerator.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/openapi/ApiDocOpenApiSchemaGenerator.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/test/java/top/egon/openapi/console/ApiDocSpringMvcOpenApiGeneratorTest.java`

- [ ] **Step 1: Write a dependency guard check**

Run:

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend
mvn -pl openapi-debug-console-spring-boot-starter dependency:tree -Dincludes=io.swagger.core.v3:swagger-annotations-jakarta
```

Expected before implementation: the dependency appears in the starter tree.

- [ ] **Step 2: Remove Swagger annotation dependency**

Delete this dependency block from `openapi-debug-console-spring-boot-starter/pom.xml`:

```xml
<dependency>
    <groupId>io.swagger.core.v3</groupId>
    <artifactId>swagger-annotations-jakarta</artifactId>
</dependency>
```

- [ ] **Step 3: Remove Swagger annotation imports and fallback reads**

Remove all of these imports from `ApiDocSpringMvcOpenApiGenerator.java`:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
```

Remove all of these imports from `ApiDocOpenApiSchemaGenerator.java`:

```java
import io.swagger.v3.oas.annotations.media.Schema;
```

Replace behavior:

- operation summary/description must come from `@DocOperation` or Java method name.
- tags must come from `@DocService`, `@DocOperation.tags`, or controller simple class name.
- parameter metadata must come from `@DocParam`, `@DocOperation.parameters`, Spring MVC annotations, or Java names.
- model/field metadata must come from `@DocModel`, `@DocField`, Jackson, Bean Validation, or Java names.

- [ ] **Step 4: Remove Swagger fallback tests**

Delete or rewrite the test currently proving `ParameterIn.DEFAULT` fallback behavior. The replacement assertion must
prove that a plain Java parameter without Swagger metadata is inferred by Spring/Java rules.

- [ ] **Step 5: Verify no Swagger annotation usage remains in starter**

Run:

```bash
cd /Users/mario/SelfProject/FamilyAiButler
rg -n "io\\.swagger\\.v3\\.oas\\.annotations|@Operation|@Parameter|@Schema|@Tag|@ApiResponse" backend/openapi-debug-console-spring-boot-starter
```

Expected: no matches.

---

## Task 2: Redesign Owned Annotation Contract

**Files:**

- Modify/Create files under
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/test/java/top/egon/openapi/console/ApiDocSpringMvcOpenApiGeneratorTest.java`

- [ ] **Step 1: Update annotation compile tests**

Add a test method to `ApiDocSpringMvcOpenApiGeneratorTest` with a fixture method using the new `DocOperation` shape:

```java
@PostMapping("/complex")
@DocOperation(
        id = "doc.user.complex",
        summary = "复杂创建用户",
        parameters = {
                @DocOperationParameter(name = "X-Tenant-Id", in = DocParamIn.HEADER, description = "租户 ID", required = true),
                @DocOperationParameter(name = "debug", in = DocParamIn.QUERY, hidden = true)
        },
        requestBodies = {
                @DocRequestBody(description = "创建用户请求", contentTypes = {"application/json"})
        },
        responses = {
                @DocResponse(status = 201, description = "创建成功",
                        schema = @DocSchemaRef(type = UserDocResponse.class, wrappers = {ResultDoc.class}, dataPath = "data")),
                @DocResponse(status = 400, description = "参数错误",
                        schema = @DocSchemaRef(type = ErrorDocResponse.class, wrappers = {ResultDoc.class}, dataPath = "data")),
                @DocResponse(status = 204, description = "无返回内容", noContent = true)
        }
)
ResultDoc<UserDocResponse> complex(@RequestBody UserCreateDocRequest request) {
    return new ResultDoc<>();
}
```

Expected before implementation: compile fails because annotation types and members do not exist.

- [ ] **Step 2: Create new annotation types**

Create the files listed in the Proposed Annotation Contract section with existing project file/class comment style.

- [ ] **Step 3: Remove `example` from owned annotations**

Update:

```java
String example() default "";
```

Remove it from:

- `DocParam`
- `DocField`

- [ ] **Step 4: Update existing tests to stop setting examples**

Replace assertions that read `example` from generated schema with assertions for `description`, `required`, `headers`,
`responses`, or schema `$ref`.

Run:

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend
mvn -pl openapi-debug-console-spring-boot-starter -Dtest=ApiDocSpringMvcOpenApiGeneratorTest test
```

Expected after implementation: test compiles and any new failing assertions point to generator work in Task 3.

---

## Task 3: Implement Operation-Level Input And Response Metadata

**Files:**

- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/openapi/ApiDocSpringMvcOpenApiGenerator.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/openapi/ApiDocOpenApiSchemaGenerator.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/test/java/top/egon/openapi/console/ApiDocSpringMvcOpenApiGeneratorTest.java`

- [ ] **Step 1: Merge operation parameters**

Implementation rule:

- Build inferred parameters first.
- Build explicit `DocOperation.parameters`.
- For each explicit parameter:
    - if `hidden = true`, remove the inferred parameter matching `(in, name)`.
    - otherwise merge by `(in, name)`, with explicit description/required overriding inferred metadata.
    - if no inferred match exists, add it as a synthetic OpenAPI parameter.

Expected test assertions:

```java
Assertions.assertTrue(parameters.stream().anyMatch(parameter ->
        "X-Tenant-Id".equals(parameter.get("name"))
                && "header".equals(parameter.get("in"))
                && Boolean.TRUE.equals(parameter.get("required"))));
Assertions.assertFalse(parameters.stream().anyMatch(parameter -> "debug".equals(parameter.get("name"))));
```

- [ ] **Step 2: Merge operation request bodies**

Implementation rule:

- If `DocOperation.requestBodies` is empty, keep current inferred request body behavior.
- If present:
    - use `contentTypes` for OpenAPI content keys.
    - if `schema.type = Void.class`, infer schema from Java request body parameter.
    - if `schema.type != Void.class`, generate schema from `schema.type`, `schema.array`, and `schema.wrappers`.
    - if `contentTypes` contains `multipart/form-data`, generate object schema from `formFields`.

Expected assertions:

```java
Map<String, Object> requestBody = (Map<String, Object>) post.get("requestBody");
Assertions.assertEquals("创建用户请求", requestBody.get("description"));
Assertions.assertTrue(((Map<String, Object>) requestBody.get("content")).containsKey("application/json"));
```

- [ ] **Step 3: Generate multiple responses**

Implementation rule:

- If `DocOperation.responses` is empty, keep current default `200` response from method return type.
- If present:
    - generate one response per `status`.
    - write `description`.
    - write `headers` if present.
    - if `noContent = true`, do not write `content`.
    - if `schema.type = Void.class`, infer schema from Java return type for 2xx responses.
    - if `schema.type != Void.class`, use `DocSchemaRef`.

Expected assertions:

```java
Map<String, Object> responses = (Map<String, Object>) post.get("responses");
Assertions.assertTrue(responses.containsKey("201"));
Assertions.assertTrue(responses.containsKey("400"));
Assertions.assertTrue(responses.containsKey("204"));
Assertions.assertFalse(((Map<String, Object>) responses.get("204")).containsKey("content"));
```

- [ ] **Step 4: Generate wrapper schema hints without private document shape**

Implementation rule:

- `DocSchemaRef.wrappers` affects schema generation only.
- OpenAPI output remains standard `schema`, `$ref`, `type`, `items`, `properties`.
- Add `x-doc-data-path` only when `dataPath` is not blank.

Expected assertion:

```java
Map<String, Object> schema = jsonSchema((Map<String, Object>) responses.get("201"));
Assertions.assertEquals("#/components/schemas/ResultDocUserDocResponse", schema.get("$ref"));
Assertions.assertEquals("data", schema.get("x-doc-data-path"));
```

---

## Task 4: Strengthen Schema Generator Without Examples

**Files:**

- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/openapi/ApiDocOpenApiSchemaGenerator.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/test/java/top/egon/openapi/console/ApiDocSpringMvcOpenApiGeneratorTest.java`

- [ ] **Step 1: Remove all `example` output from annotation metadata**

Delete all schema writes based on `DocField.example` and `DocParam.example`.

Run:

```bash
cd /Users/mario/SelfProject/FamilyAiButler
rg -n "example\\(\\)|put\\(\"example\"|\\.example" backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console
```

Expected: no matches from owned annotation code.

- [ ] **Step 2: Add Bean Validation constraints**

Support these annotations by name to avoid requiring a validator provider:

- `jakarta.validation.constraints.Size` -> `minLength`, `maxLength`, `minItems`, `maxItems`
- `jakarta.validation.constraints.Min` -> `minimum`
- `jakarta.validation.constraints.Max` -> `maximum`
- `jakarta.validation.constraints.Pattern` -> `pattern`

Expected DTO fixture:

```java
@Size(min = 2, max = 32)
@DocField(description = "用户名")
private String fullName;
```

Expected assertion:

```java
Map<String, Object> fullName = (Map<String, Object>) propertiesNode.get("full_name");
Assertions.assertEquals(2, fullName.get("minLength"));
Assertions.assertEquals(32, fullName.get("maxLength"));
```

- [ ] **Step 3: Add `Optional<T>` support**

Implementation rule:

- `Optional<T>` should generate schema for `T`.
- `Optional<T>` fields should not be required unless explicitly annotated with `@DocField(required = true)`.

- [ ] **Step 4: Add file and multipart schema support**

Implementation rule:

- `MultipartFile` -> `type: string`, `format: binary`.
- `MultipartFile[]` / `List<MultipartFile>` -> `type: array`, `items.format: binary`.

Expected assertion:

```java
Assertions.assertEquals("binary", ((Map<String, Object>) fileSchema).get("format"));
```

---

## Task 5: Remove Swagger Annotations From Project-Owned Business Code

**Files:**

- Modify files listed under Project-Owned Business Modules.
- Modify module tests that assert Swagger annotations.

- [ ] **Step 1: Audit current Swagger annotations**

Run:

```bash
cd /Users/mario/SelfProject/FamilyAiButler
rg -n "io\\.swagger\\.v3\\.oas\\.annotations|@Operation|@Parameter|@Schema|@Tag|@ApiResponse|@SecurityRequirement" backend/family-core backend/family-ai backend/family-uaa backend/family-gateway
```

Expected before migration: matches in `family-core` and `family-ai/qwen-ai`.

- [ ] **Step 2: Migrate controllers to owned annotations**

Mapping rules:

- `@Tag(name = "...")` -> class-level `@DocService(name = "...", protocol = DocService.Protocol.HTTP)`
- `@Operation(summary = "...", description = "...")` -> method-level
  `@DocOperation(summary = "...", description = "...")`
- `@Parameter(...)` -> method parameter `@DocParam(...)` or operation-level `@DocOperation(parameters = {...})`
- `@ApiResponse(...)` -> `@DocOperation(responses = {...})`
- `@SecurityRequirement` -> use existing `auth = true` or `auth = false`

- [ ] **Step 3: Migrate DTO field annotations**

Mapping rules:

- `@Schema(title = "...")` -> `@DocField(description = "...")`
- `@Schema(description = "...")` -> `@DocField(description = "...")`
- `@Schema(name = "...")` on class -> `@DocModel(name = "...")`
- `@Schema(title = "...")` on class -> `@DocModel(description = "...")`
- Do not migrate `example` or `defaultValue` values into owned annotations.

- [ ] **Step 4: Remove obsolete Swagger configuration**

Review
`backend/family-core/src/main/java/top/egon/familyaibutler/family/infrastructure/configuration/SwaggerConfig.java`.

Implementation rule:

- If it only configures Springdoc/OpenAPI beans for Swagger annotations, delete it.
- If any config value is still needed, move it into `egon.openapi.console.producer` yml/Nacos config.

- [ ] **Step 5: Update qwen architecture test**

Modify `backend/family-ai/qwen-ai/src/test/java/top/egon/familyaibutler/ai/qwen/architecture/QwenDddStructureTest.java`
so it asserts owned annotations:

```java
Assertions.assertThat(source)
        .contains("@DocService")
        .contains("@DocOperation")
        .contains("@DocParam")
        .doesNotContain("@Tag")
        .doesNotContain("@Operation")
        .doesNotContain("@Parameter")
        .doesNotContain("@ApiResponse");
```

- [ ] **Step 6: Verify project-owned Swagger annotations are gone**

Run:

```bash
cd /Users/mario/SelfProject/FamilyAiButler
rg -n "io\\.swagger\\.v3\\.oas\\.annotations|@Operation|@Parameter|@Schema|@Tag|@ApiResponse|@SecurityRequirement" backend/family-core backend/family-ai backend/family-uaa backend/family-gateway
```

Expected after migration: no matches for Swagger annotations in project-owned modules.

---

## Task 6: Console Parser And Export Regression

**Files:**

- Modify: `backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-openapi.test.cjs`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/test/java/top/egon/openapi/console/ApiDocConsoleClientTest.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/test/java/top/egon/openapi/console/ApiDocConsolePayloadsTest.java`
  only if payload fixture needs new OpenAPI response structures.

- [ ] **Step 1: Add JS fixture with multiple responses and no examples**

Add a fixture operation containing:

- `201` response with schema `$ref`
- `400` response with schema `$ref`
- `204` response with no content
- requestBody with `multipart/form-data`
- response headers
- no `example` fields

Expected parser behavior:

- Interface list still renders the operation.
- Sample data generator still uses schema type, enum, required, and object structure; it does not require `example`.

- [ ] **Step 2: Verify document renderer handles multiple responses**

Run existing renderer/export tests:

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend
mvn -pl openapi-debug-console-spring-boot-starter -Dtest=ApiDocConsoleClientTest,ApiDocConsolePayloadsTest test
```

Expected: existing Markdown/PDF preparation still passes.

---

## Task 7: README And Migration Guide

**Files:**

- Modify: `backend/openapi-debug-console-spring-boot-starter/README.md`
- Modify: `backend/openapi-debug-console-spring-boot-starter/OPENAPI_DOC_MIGRATION_PLAN.md` only if it must be marked
  superseded by this plan.

- [ ] **Step 1: Remove Swagger fallback language**

Delete README sections that say old Swagger annotations are fallback metadata.

- [ ] **Step 2: Document new `DocOperation` contract**

Add examples for:

- request parameters
- request bodies
- multipart form fields
- multiple responses
- response headers
- no-content response
- wrapper/dataPath hints

- [ ] **Step 3: Document no-example policy**

Add a short note:

```markdown
注解不再提供 `example` 字段。控制台测试数据由前端根据 OpenAPI schema 类型、required、enum、format 和字段名自动生成；接口文档只描述结构和语义，不保存示例值。
```

- [ ] **Step 4: Document migration commands**

Add:

```bash
rg -n "io\\.swagger\\.v3\\.oas\\.annotations|@Operation|@Parameter|@Schema|@Tag|@ApiResponse|@SecurityRequirement" backend
```

Expected after full migration: no Swagger annotation matches in project-owned source.

---

## Required Verification Commands

Run from `/Users/mario/SelfProject/FamilyAiButler` unless command says otherwise:

```bash
mvn -f backend/pom.xml -pl openapi-debug-console-spring-boot-starter -am test
```

Expected: starter Java tests pass.

```bash
node --test backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-openapi.test.cjs backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-storage.test.cjs
```

Expected: static console parser/storage tests pass.

```bash
mvn -f backend/pom.xml -pl openapi-debug-console-spring-boot-starter -am dependency:tree -Dincludes=io.swagger.core.v3:swagger-annotations-jakarta
```

Expected: no `swagger-annotations-jakarta` dependency entries.

```bash
rg -n "io\\.swagger\\.v3\\.oas\\.annotations|@Operation|@Parameter|@Schema|@Tag|@ApiResponse|@SecurityRequirement" backend/openapi-debug-console-spring-boot-starter backend/family-core backend/family-ai backend/family-uaa backend/family-gateway
```

Expected: no Swagger annotation matches in starter or project-owned modules.

```bash
mvn -f backend/pom.xml -pl family-core,family-ai/qwen-ai -am -DskipTests compile
```

Expected: migrated business modules compile.

```bash
git diff --check
```

Expected: no whitespace errors.

---

## Acceptance Criteria

- Starter has no `swagger-annotations-jakarta` dependency.
- Starter source has no `io.swagger.v3.oas.annotations.*` imports.
- Project-owned business source has no Swagger annotation imports or annotations.
- `DocParam` and `DocField` no longer expose `example`.
- Generated OpenAPI no longer emits `example` from owned annotations.
- `DocOperation` supports explicit parameters, request bodies, and responses.
- Multiple response status codes are generated correctly.
- No-content responses generate no response content.
- Multipart inputs can be described without Swagger annotations.
- Existing console auth, signing, catalog, OpenAPI fetch, tryout, load-test, export, and storage tests still pass.
- `/v3/api-docs` remains the REST OpenAPI endpoint.
- Output remains standard OpenAPI JSON with optional `x-doc-*` extensions only for metadata that OpenAPI cannot
  represent directly.

---

## Implementation Order

1. Starter annotation contract and Swagger compatibility removal.
2. Generator support for new `DocOperation` parameters/requestBodies/responses.
3. Schema enhancements and no-example cleanup.
4. Starter Java and JS regression tests.
5. Business module Swagger annotation migration.
6. README update.
7. Full verification commands.

This order keeps the new contract compile-ready before touching business modules and avoids leaving the project in a
state where controllers have no usable documentation annotations.
