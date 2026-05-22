# OpenAPI Annotation Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or the coordinator-assigned
> subagent workflow to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the current weak annotation contract with the `ANNOTATION.md` design: explicit request, single
response, single wrapper, generic data type support, DTO/VO examples, generated OpenAPI examples, and migrated business
modules.

**Architecture:** The new contract treats annotations as the documentation source of truth while method signatures
remain validation input. `DocOperation` owns one `DocRequest` and one `DocResponse`; `DocResponse` is always
`dataType + wrapper`; complex generic `dataType` is represented by `DocTypeReference<T>`. The generator must emit
standard OpenAPI JSON, including schema examples and request/response examples derived from DTO/VO
`@DocField(example=...)`.

**Tech Stack:** Spring Boot 3.5.13, Spring MVC, Jackson, Jakarta Bean Validation, Maven reactor, Node test runner,
OpenAPI 3 JSON.

---

## Coordinator Rules

- Dalton owns agent coordination.
- Hegel owns implementation and business migration.
- Poincare owns code review and quality gate.
- James owns runtime/test verification gate.
- dependency-manager runs only after Hegel, Poincare, and James pass.
- Parent updates README only after dependency-manager and follow-up review/QA pass.
- Parent must not edit implementation files while subagents are active.

## Source Design

Implementation must follow:

- `backend/openapi-debug-console-spring-boot-starter/ANNOTATION.md`

Do not implement the older `OPENAPI_ANNOTATION_REDESIGN_PLAN.md` contract where it conflicts with `ANNOTATION.md`.

## Blocking Standards

The task is not complete if any item remains:

- Project-owned Java source imports or uses Swagger annotations.
- Starter reads Swagger/Springdoc annotations as fallback metadata.
- Starter or business source still uses old contract types or old semantics:
    - `DocSchemaRef`
    - `DocOperationParameter`
    - `DocRequestBody.formFields`
    - `DocResponse[] responses`
    - `wrappers[]`
- `DocOperation` is not `request + single response`.
- `DocResponse` supports multiple main responses.
- `DocWrapper` supports multiple wrappers.
- `DocField` does not support `example`.
- OpenAPI generation does not emit schema/request/response examples.
- `DocTypeReference<T>` cannot resolve complex generic data types.
- Business modules fail to compile.
- Required Maven/Node/rg/diff checks fail.
- README later still documents old design or Swagger fallback.

---

## File Map

### Starter Annotation Contract

- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocService.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocOperation.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocParam.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocField.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocModel.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocIgnore.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocProtocol.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocRequest.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocParameter.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocBody.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocDataType.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocDataKind.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocWrapper.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocExampleMode.java`
- Create:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/DocTypeReference.java`
- Delete or replace: `DocSchemaRef.java`
- Delete or replace: `DocOperationParameter.java`
- Delete or replace: `DocRequestBody.java`
- Delete or replace: `DocFormField.java`
- Replace old `DocResponse.java` with the single-response contract from `ANNOTATION.md`.

### Starter Generation

- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/openapi/ApiDocSpringMvcOpenApiGenerator.java`
- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/openapi/ApiDocOpenApiSchemaGenerator.java`
- Create helper files if needed under
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/openapi/**`, keeping
  responsibilities focused:
    - type resolution
    - wrapper binding
    - example generation
    - contract validation

### Starter Configuration

- Modify only if needed:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/ApiDocConsoleProperties.java`
- Add producer properties:
    - `contractPolicy`
    - `examplePolicy`

### Starter Tests

- Modify/create tests under `backend/openapi-debug-console-spring-boot-starter/src/test/java/**`
- Modify JS parser tests:
    - `backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-openapi.test.cjs`
    - `backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-storage.test.cjs`, only if storage
      behavior is affected

### Business Migration

- Migrate owned annotation usage under:
    - `backend/family-core/src/main/java/**`
    - `backend/family-ai/qwen-ai/src/main/java/**`
    - `backend/family-framework/family-common/src/main/java/**`
    - `backend/family-ai/ai-common/src/main/java/**`, only if affected by qwen shared DTOs/wrappers
- Update architecture tests only where they assert the old annotation contract:
    - `backend/family-ai/qwen-ai/src/test/**`
    - `backend/family-core/src/test/**`, if present

### Dependencies

Hegel may only make dependency changes required for compilation. Broad dependency optimization is reserved for
dependency-manager after quality gates pass.

---

## Task 1: Replace Annotation Contract

**Files:**

- Modify/create/delete files in
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/**`
- Test: `backend/openapi-debug-console-spring-boot-starter/src/test/java/**`

- [ ] **Step 1: Add failing annotation contract tests**

Add or update a focused Java test that asserts:

- `DocOperation` exposes `request()` and single `response()`.
- `DocOperation` does not expose `responses()`.
- `DocResponse` exposes `dataType()` and single `wrapper()`.
- `DocResponse` does not expose wrapper arrays.
- `DocField` exposes `example()` and `exampleMode()`.
- `DocTypeReference<T>` can be subclassed with `PageResult<UserDTO>`.

Run:

```bash
mvn -f backend/pom.xml -pl openapi-debug-console-spring-boot-starter -Dtest=ApiDocSpringMvcOpenApiGeneratorTest test
```

Expected before implementation: FAIL or compilation failure showing old annotation contract.

- [ ] **Step 2: Implement the new annotation types**

Implement the contract from `ANNOTATION.md`:

- `DocProtocol`
- `DocRequest`
- `DocParameter`
- `DocBody`
- `DocDataType`
- `DocDataKind`
- `DocWrapper`
- `DocExampleMode`
- `DocTypeReference<T>`

Follow existing project JavaDoc/file-header style.

- [ ] **Step 3: Update existing annotation types**

Update:

- `DocService` to support
  `groupId/groupName/groupOrder/serviceId/serviceName/serviceDescription/serviceOrder/version/protocol/serviceInterface/enabled`.
- `DocOperation` to support `request()` and single `response()`.
- `DocParam` or replace it with `DocParameter` usage while preserving method-parameter ergonomics if needed.
- `DocField` to support `name`, `nullable`, `deprecated`, `dataType`, `example`, `exampleMode`.

- [ ] **Step 4: Remove old contract types**

Remove or fully replace:

- `DocSchemaRef`
- `DocOperationParameter`
- `DocRequestBody`
- `DocFormField`

No project-owned source should import these after migration.

- [ ] **Step 5: Run annotation contract verification**

Run:

```bash
rg -n "DocSchemaRef|DocOperationParameter|DocRequestBody|DocFormField|responses\\s*\\(|wrappers\\s*\\(" backend/openapi-debug-console-spring-boot-starter/src/main/java
mvn -f backend/pom.xml -pl openapi-debug-console-spring-boot-starter -am test
```

Expected:

- `rg` has no matches in main Java source.
- Maven starter tests pass or only fail on generator behavior that will be addressed by later tasks.

---

## Task 2: Implement Type Resolution and Single Wrapper Binding

**Files:**

- Modify/create under
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/openapi/**`
- Test:
  `backend/openapi-debug-console-spring-boot-starter/src/test/java/top/egon/openapi/console/ApiDocSpringMvcOpenApiGeneratorTest.java`

- [ ] **Step 1: Add failing tests for data type resolution**

Cover:

- `DocDataType(kind = OBJECT, type = UserDTO.class)`
- `DocDataType(kind = ARRAY, itemType = UserDTO.class)`
- `DocDataType(kind = MAP, keyType = String.class, valueType = UserDTO.class)`
- `DocDataType(kind = GENERIC, ref = UserPageDataType.class)` where
  `UserPageDataType extends DocTypeReference<PageResult<UserDTO>>`

- [ ] **Step 2: Implement `DocTypeReference<T>` generic extraction**

Resolve the concrete generic parameter from subclasses such as:

```java
public final class UserPageDataType extends DocTypeReference<PageResult<UserDTO>> {
}
```

The resolver must fail fast when the generic type cannot be resolved.

- [ ] **Step 3: Implement `DocDataType` schema generation**

Map:

- primitive/simple kinds to OpenAPI scalar schemas
- `OBJECT` to component schema
- `ARRAY` to `items`
- `MAP` to `additionalProperties`
- `FILE` to `type=string, format=binary`
- `GENERIC` to the resolved `DocTypeReference<T>` schema

- [ ] **Step 4: Implement single `DocWrapper` binding**

For:

```text
wrapper = Result.class
dataType = PageResult<UserDTO>
```

generate the schema for:

```text
Result<PageResult<UserDTO>>
```

Use `DocWrapper.dataPath` and `genericIndex` to bind the data schema into the wrapper generic slot.

- [ ] **Step 5: Reject old multi-wrapper semantics**

Tests must prove there is no generator path that accepts `wrappers[]`.

Run:

```bash
mvn -f backend/pom.xml -pl openapi-debug-console-spring-boot-starter -Dtest=ApiDocSpringMvcOpenApiGeneratorTest test
```

Expected: PASS for type/wrapper cases.

---

## Task 3: Implement Explicit Request and Single Response Generation

**Files:**

- Modify: `ApiDocSpringMvcOpenApiGenerator.java`
- Modify/create generator helpers under `openapi/**`
- Test: `ApiDocSpringMvcOpenApiGeneratorTest.java`

- [ ] **Step 1: Add failing tests for explicit request**

Cover:

- path/query/header/cookie parameters from `DocOperation.request.params`
- multipart form using `DocParamIn.FILE` and `DocParamIn.FORM`
- request body using `DocRequest.body`
- hidden parameter is not emitted
- `source` can map a flattened query DTO field

- [ ] **Step 2: Add failing tests for single response**

Cover:

- one OpenAPI response generated from `DocOperation.response`
- `response.status`
- `response.contentType`
- `response.noBody`
- `response.headers`
- `response.dataType + response.wrapper`

- [ ] **Step 3: Implement request generation**

Generation order:

1. Start from explicit `DocRequest`.
2. Fill missing location/type information from Spring MVC method signature.
3. Validate conflicts according to `contractPolicy`.
4. Emit OpenAPI `parameters` and `requestBody`.

- [ ] **Step 4: Implement single response generation**

Generation order:

1. Start from explicit `DocResponse`.
2. If `dataType` is `AUTO`, infer data type from method return value.
3. If `wrapper` is empty, emit data type directly.
4. If `wrapper` is present, emit wrapper-bound schema.
5. Validate method return type consistency according to `contractPolicy`.

- [ ] **Step 5: Remove old response-array generation**

No code path should read `DocResponse[] responses`.

Run:

```bash
rg -n "responses\\s*\\(|DocResponse\\[\\]|DocOperationParameter|DocSchemaRef" backend/openapi-debug-console-spring-boot-starter/src/main/java
mvn -f backend/pom.xml -pl openapi-debug-console-spring-boot-starter -am test
```

Expected:

- `rg` has no old-contract matches.
- Starter tests pass.

---

## Task 4: Implement DTO/VO Examples and OpenAPI Example Generation

**Files:**

- Modify: `ApiDocOpenApiSchemaGenerator.java`
- Modify/create example helper under `openapi/**`
- Test: `ApiDocSpringMvcOpenApiGeneratorTest.java`
- Test: `backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-openapi.test.cjs`

- [ ] **Step 1: Add failing schema example tests**

Cover:

- `@DocField(example = "10001")` on `Long` emits numeric example.
- `@DocField(example = "true")` on `Boolean` emits boolean example.
- `@DocField(exampleMode = JSON, example = "[\"admin\"]")` emits JSON array.
- `@DocField(exampleMode = JSON, example = "{\"source\":\"web\"}")` emits JSON object.
- hidden fields do not appear in schema or examples.

- [ ] **Step 2: Add failing request/response example tests**

Cover:

- request body example generated from request DTO.
- response example generated from `DocWrapper + DocDataType`.
- `Result<PageResult<UserDTO>>` example includes wrapper fields and nested DTO example.

- [ ] **Step 3: Implement field-level example parsing**

Rules:

- `AUTO`: convert by resolved Java type.
- `STRING`: keep as string.
- `JSON`: parse as JSON and fail if invalid.

- [ ] **Step 4: Implement recursive example generation**

Generate examples for:

- object
- array/list
- map
- generic wrapper
- nested DTO

- [ ] **Step 5: Emit OpenAPI examples**

Emit:

- `components.schemas.*.properties.*.example`
- `requestBody.content.<contentType>.example`
- `responses.<status>.content.<contentType>.example`

Run:

```bash
mvn -f backend/pom.xml -pl openapi-debug-console-spring-boot-starter -am test
node --test backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-openapi.test.cjs backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-storage.test.cjs
```

Expected: PASS.

---

## Task 5: Implement Contract and Example Policies

**Files:**

- Modify:
  `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/ApiDocConsoleProperties.java`
- Modify/create validation helper under `openapi/**`
- Test: `ApiDocSpringMvcOpenApiGeneratorTest.java`

- [ ] **Step 1: Add producer policy properties**

Add:

```yaml
egon.openapi.console.producer.contract-policy
egon.openapi.console.producer.example-policy
```

Enum values:

```text
OFF
WARN
FAIL
```

- [ ] **Step 2: Add failing tests for contract policy**

Cover:

- missing `DocOperation.response` when method has non-void return
- `DocResponse.wrapper` conflict with method return outer type
- `DocResponse.dataType` conflict with method return generic data type
- declared `DocParameter` not found in mapping/signature

- [ ] **Step 3: Add failing tests for example policy**

Cover:

- missing `DocField.example`
- invalid JSON example
- `FAIL` blocks generation
- `WARN` logs but still generates
- `OFF` does not validate examples

- [ ] **Step 4: Implement validation**

Validation must be deterministic and testable. Avoid logging-only behavior for `FAIL`.

Run:

```bash
mvn -f backend/pom.xml -pl openapi-debug-console-spring-boot-starter -am test
```

Expected: PASS.

---

## Task 6: Migrate Common Wrappers and Business DTO/VO Models

**Files:**

- Modify: `backend/family-framework/family-common/src/main/java/**`
- Modify: `backend/family-core/src/main/java/**`
- Modify: `backend/family-ai/qwen-ai/src/main/java/**`
- Modify: `backend/family-ai/ai-common/src/main/java/**`, only if affected

- [ ] **Step 1: Migrate common wrapper models**

Add `@DocModel` and `@DocField(example=...)` to wrappers such as:

- `Result`
- `ResultRecord`
- `PageResult`
- `PageResultRecord`
- `CacheMessage`, if exposed in API docs

Each exposed field needs description and example where meaningful.

- [ ] **Step 2: Migrate family-core DTO/VO/PO used by controllers**

For each exposed DTO/VO/PO:

- add `@DocModel`
- add `@DocField(description, example)`
- use `exampleMode = JSON` for array/object examples
- keep Jackson and Bean Validation annotations as type/constraint sources

- [ ] **Step 3: Migrate qwen-ai request/response models**

For image/text related API models:

- add `@DocModel`
- add `@DocField(example=...)`
- use `DocDataKind.FILE` for upload fields through operation request params

- [ ] **Step 4: Verify no project-owned Swagger annotations**

Run:

```bash
rg -n "io\\.swagger\\.v3\\.oas\\.annotations|@Operation|@Parameter|@Schema|@Tag|@ApiResponse|@SecurityRequirement" backend/family-framework/family-common backend/family-core backend/family-ai backend/family-uaa backend/family-gateway
```

Expected:

- No Java source matches for project-owned Swagger annotations.
- Markdown/history matches, if any, must be explicitly classified before review.

---

## Task 7: Migrate Controllers and Dubbo Service Contracts

**Files:**

- Modify: `backend/family-core/src/main/java/**/adapter/web/**`
- Modify: `backend/family-ai/qwen-ai/src/main/java/**/adapter/web/**`
- Modify: Dubbo service interfaces under business modules if present
- Modify tests under `backend/family-ai/qwen-ai/src/test/**` or `backend/family-core/src/test/**` where they assert
  annotations

- [ ] **Step 1: Migrate `DocService` usage**

Every documented controller/service must declare:

- `groupId`
- `groupName`
- `serviceId`
- `serviceName`
- `protocol`
- optional order/version/description

- [ ] **Step 2: Migrate operation requests**

Each public documented endpoint must declare:

- `DocOperation.request.params` for path/query/header/cookie/form/file params
- `DocOperation.request.body` for request body
- examples for simple params
- `source` when overriding flattened query DTO fields

- [ ] **Step 3: Migrate operation responses**

Each public documented endpoint must declare one main response:

```text
DocOperation.response = DocResponse(dataType + wrapper)
```

For `Result<List<X>>`:

```text
dataType = ARRAY itemType X
wrapper = Result
```

For `Result<PageResult<X>>`:

```text
dataType = GENERIC ref XPageDataType
wrapper = Result
```

For raw `String`:

```text
dataType = STRING
wrapper = empty
```

- [ ] **Step 4: Add type reference classes**

Create small final classes near the controller or in a local doc/type package when complex generics are needed:

```java
public final class CategoryPageDataType extends DocTypeReference<PageResult<CategoryDTO>> {
}
```

Do not create type references for simple object/list cases where `DocDataType` can express the type directly.

- [ ] **Step 5: Compile migrated business modules**

Run:

```bash
mvn -f backend/pom.xml -pl family-core,family-ai/qwen-ai -am -DskipTests compile
```

Expected: PASS.

---

## Task 8: Remove Old Contract Residue and Update Parser Tests

**Files:**

- Modify: `backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-openapi.test.cjs`
- Modify: `backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-storage.test.cjs`, only if
  needed
- Modify: starter Java tests

- [ ] **Step 1: Remove old-contract test fixtures**

No fixture should rely on:

- multiple main responses
- `wrappers[]`
- `DocSchemaRef`
- `DocOperationParameter`
- missing examples on DTO/VO fields

- [ ] **Step 2: Add parser fixture for generated examples**

JS parser tests should assert the front end parser accepts:

- schema property examples
- request body example
- response example
- `Result<PageResult<UserDTO>>` style schema
- array response data
- multipart request schema

- [ ] **Step 3: Run JS tests**

Run:

```bash
node --test backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-openapi.test.cjs backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-storage.test.cjs
```

Expected: PASS.

---

## Task 9: Mandatory Hegel Verification

Hegel must run and report:

```bash
mvn -f backend/pom.xml -pl openapi-debug-console-spring-boot-starter -am test
node --test backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-openapi.test.cjs backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-storage.test.cjs
mvn -f backend/pom.xml -pl family-core,family-ai/qwen-ai -am -DskipTests compile
rg -n "io\\.swagger\\.v3\\.oas\\.annotations|@Operation|@Parameter|@Schema|@Tag|@ApiResponse|@SecurityRequirement" backend/openapi-debug-console-spring-boot-starter backend/family-framework/family-common backend/family-core backend/family-ai backend/family-uaa backend/family-gateway
rg -n "DocSchemaRef|DocOperationParameter|DocRequestBody|responses\\s*\\(|wrappers\\s*\\(" backend/openapi-debug-console-spring-boot-starter/src/main/java backend/family-core/src/main/java backend/family-ai/qwen-ai/src/main/java backend/family-framework/family-common/src/main/java
git diff --check
```

Expected:

- Maven starter tests pass.
- Node tests pass.
- business compile passes.
- no project-owned Swagger Java annotation usage.
- no old annotation contract residue in main Java source.
- diff check passes.

---

## Task 10: Poincare Code Review Gate

Poincare must review after Hegel reports completion.

- [ ] **Step 1: Check implementation against `ANNOTATION.md`**

Reject if implementation follows older design instead of:

```text
DocOperation(request + single response)
DocResponse(dataType + single wrapper)
DocDataType(ref = DocTypeReference<T>)
DocField(example)
```

- [ ] **Step 2: Check scope**

Reject:

- README edits by Hegel
- `annotation-plan.md` edits by Hegel
- frontend edits
- unrelated module edits
- broad dependency optimization

- [ ] **Step 3: Check tests**

Tests must cover:

- request params/body
- single response
- single wrapper
- generic data type reference
- DTO examples
- request/response examples
- policies
- business migration compile

- [ ] **Step 4: Return result**

Output:

```text
STATUS: PASS | FAIL | BLOCKED
Blocking issues:
Non-blocking issues:
Scope violations:
Required fixes for Hegel:
```

If FAIL, return to Hegel. Parent must not fix implementation directly.

---

## Task 11: James QA Gate

James runs after Poincare PASS.

- [ ] **Step 1: Run mandatory commands**

```bash
mvn -f backend/pom.xml -pl openapi-debug-console-spring-boot-starter -am test
node --test backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-openapi.test.cjs backend/openapi-debug-console-spring-boot-starter/src/test/js/openapi-console-storage.test.cjs
mvn -f backend/pom.xml -pl family-core,family-ai/qwen-ai -am -DskipTests compile
rg -n "io\\.swagger\\.v3\\.oas\\.annotations|@Operation|@Parameter|@Schema|@Tag|@ApiResponse|@SecurityRequirement" backend/openapi-debug-console-spring-boot-starter backend/family-framework/family-common backend/family-core backend/family-ai backend/family-uaa backend/family-gateway
rg -n "DocSchemaRef|DocOperationParameter|DocRequestBody|responses\\s*\\(|wrappers\\s*\\(" backend/openapi-debug-console-spring-boot-starter/src/main/java backend/family-core/src/main/java backend/family-ai/qwen-ai/src/main/java backend/family-framework/family-common/src/main/java
git diff --check
```

- [ ] **Step 2: Verify generated OpenAPI behavior**

If practical from tests or local endpoints, verify generated OpenAPI contains:

- service group/service metadata extensions or equivalent grouping data
- explicit request params/body
- one main response
- wrapper-bound response schema
- schema field examples
- request example
- response example

- [ ] **Step 3: Return result**

Output:

```text
STATUS: PASS | FAIL | BLOCKED
Commands run and results:
Blocking failures:
Non-blocking warnings:
Recommended next action:
```

If FAIL, return to Hegel, then Poincare, then James again.

---

## Task 12: Dependency Manager Optimization

Runs only after Hegel completion, Poincare PASS, and James PASS.

**Assigned agent:** dependency-manager.

**Allowed files:**

- `backend/pom.xml`
- `backend/openapi-debug-console-spring-boot-starter/pom.xml`
- `backend/family-core/pom.xml`
- `backend/family-framework/family-common/pom.xml`
- `backend/family-ai/ai-common/pom.xml`
- `backend/family-ai/qwen-ai/pom.xml`

- [ ] **Step 1: Remove unnecessary direct Swagger/Springdoc dependencies**

Remove project-owned direct dependencies no longer needed by implementation.

- [ ] **Step 2: Keep third-party internals safe**

Do not exclude Swagger annotations from pure third-party chains such as Spring AI unless there is proof it is safe and
Dalton approves the scope.

- [ ] **Step 3: Preserve project dependency style**

Use existing dependencyManagement and module POM style. Do not reorder unrelated dependency blocks.

- [ ] **Step 4: Run dependency verification**

```bash
mvn -f backend/pom.xml -pl openapi-debug-console-spring-boot-starter -am dependency:tree -Dincludes=org.springdoc,io.swagger.core.v3
mvn -f backend/pom.xml -pl family-core -am dependency:tree -Dincludes=org.springdoc,io.swagger.core.v3
mvn -f backend/pom.xml -pl family-ai/qwen-ai -am dependency:tree -Dincludes=org.springdoc,io.swagger.core.v3
mvn -f backend/pom.xml -pl family-core,family-ai/qwen-ai -am -DskipTests compile
git diff --check
```

Expected:

- no project-owned direct Swagger/Springdoc dependency remains.
- pure third-party transitive dependencies are documented as warnings, not blocking, unless Dalton changes scope.
- compile passes.

---

## Task 13: Dependency Review and QA

After dependency-manager completes:

- [ ] **Step 1: Poincare reviews dependency changes**

Check:

- no Java source changes by dependency-manager
- dependency cleanup is minimal
- no unsafe third-party exclusions
- project-owned direct Swagger/Springdoc dependencies are gone

- [ ] **Step 2: James re-runs dependency verification**

Run dependency-manager verification commands again.

Expected: PASS.

If either gate fails, return to dependency-manager, then Poincare, then James.

---

## Task 14: Parent README Update

Runs only after:

- Hegel DONE
- Poincare PASS
- James PASS
- dependency-manager DONE
- dependency review PASS
- dependency QA PASS

**Allowed file:**

- `backend/openapi-debug-console-spring-boot-starter/README.md`

- [ ] **Step 1: Update README annotation section**

Document:

- `DocService` group/service
- `DocOperation.request`
- `DocOperation.response`
- `DocRequest`
- `DocParameter`
- `DocBody`
- `DocResponse`
- `DocDataType`
- `DocWrapper`
- `DocTypeReference<T>`
- `DocField(example)`

- [ ] **Step 2: Update examples**

README must include:

- HTTP Controller example
- Dubbo service example if implemented
- `Result<List<T>>`
- `Result<PageResult<T>>`
- multipart upload
- DTO/VO examples
- generated OpenAPI request/response example behavior

- [ ] **Step 3: Remove old design docs from README**

README must not describe:

- `DocSchemaRef`
- `DocOperationParameter`
- `DocRequestBody.formFields`
- `responses[]`
- `wrappers[]`
- Swagger fallback
- Springdoc fallback

- [ ] **Step 4: README verification**

Run:

```bash
git diff --check -- backend/openapi-debug-console-spring-boot-starter/README.md
rg -n "DocSchemaRef|DocOperationParameter|DocRequestBody|responses\\[\\]|wrappers\\[\\]|Swagger fallback|Springdoc fallback" backend/openapi-debug-console-spring-boot-starter/README.md
```

Expected:

- diff check passes.
- rg has no matches.

---

## Final Acceptance

The implementation is accepted only when:

- Hegel implementation is complete.
- Poincare final code review is PASS.
- James final QA is PASS.
- dependency-manager cleanup is complete.
- dependency cleanup review and QA are PASS.
- Parent README update is complete.
- Final verification commands all pass.
- No blocking standard listed in this plan remains.
