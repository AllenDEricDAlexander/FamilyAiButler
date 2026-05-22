# OpenAPI Doc Migration Follow-up Plan

> **Coordinator authority:** This plan is owned by `multi-agent coordinator`. Parent must follow this split exactly and
> must not reorder, merge, re-scope, or replace agent tasks. Parent may only perform the final README update after Mill,
> Sartre, and Pasteur have returned final outputs.

**Goal:** Finish the follow-up work for the REST OpenAPI starter migration so the starter no longer depends on Springdoc
runtime, still emits standard OpenAPI JSON, preserves the debug console contract, and documents the new annotation
system.

**Architecture:** The implemented REST producer path is expected to stay inside
`backend/openapi-debug-console-spring-boot-starter`. The producer scans Spring MVC mappings, reads the new
`top.egon.openapi.console.annotation` annotations plus Spring MVC/Jackson/Bean Validation metadata, and emits OpenAPI
JSON at `/v3/api-docs`. Gateway aggregation, security, export, tryout, load-test, and frontend parsing remain existing
contracts and must not be redesigned.

**Tech Stack:** Spring Boot 3.x, Spring MVC producer scanning, WebFlux/Gateway console aggregation, Jackson, Jakarta
Validation metadata, Swagger annotations as metadata fallback only, Maven, Node test runner for static console parsing
tests.

---

## Current Context

The current working tree already contains REST migration implementation artifacts:

- `src/main/java/top/egon/openapi/console/annotation/`
- `src/main/java/top/egon/openapi/console/openapi/`
- `src/test/java/top/egon/openapi/console/ApiDocSpringMvcOpenApiGeneratorTest.java`
- Modified `pom.xml`, `ApiDocOpenApiAutoConfiguration.java`, and `README.md`

The source requirement in `/Users/mario/Downloads/doc.md` requires:

- no Springdoc runtime scanner
- standard OpenAPI JSON output
- a minimal custom annotation system
- scanner + schema generator + OpenAPI writer
- REST first; Dubbo/proto/gRPC deferred
- README updated with new annotation usage examples

## Non-negotiable Constraints

- Parent must not implement backend code in this follow-up plan.
- Parent must not assign extra agents or change ownership.
- Mill is read-only.
- Sartre owns implementation changes only after Mill returns final gap analysis.
- Pasteur owns QA/code review gate only after Sartre returns final implementation report.
- Parent owns README only after Pasteur accepts the implementation or explicitly requests docs-only fixes.
- Do not modify unrelated modules.
- Do not reintroduce Springdoc runtime dependencies.
- Do not build Dubbo, proto, gRPC, `/v3/rpc-docs`, `/v3/all-docs`, or `/docs` UI in this phase.
- Do not replace the existing debug console UI, auth, HMAC signing, service discovery, export, tryout, or load-test
  design.
- Do not invent a private API documentation JSON format.
- Do not copy the company-style complex annotation model with `type`, `wapperType`, `modelRef`, `models`, or
  `resultModels`.

## Agent Ownership

### Mill: Current Implementation vs Requirement Gap Analysis

**Mode:** read-only analysis.

**Write scope:** none.

**Objective:** Determine exactly what still differs between the current REST OpenAPI migration implementation and
`/Users/mario/Downloads/doc.md`.

**Read scope:**

- `/Users/mario/Downloads/doc.md`
- `backend/openapi-debug-console-spring-boot-starter/pom.xml`
- `backend/openapi-debug-console-spring-boot-starter/README.md`
- `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/`
- `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/openapi/`
-

`backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/autoconfigure/ApiDocOpenApiAutoConfiguration.java`

- Existing console aggregation/security/frontend parser files only as contract references.

**Required checks:**

- Confirm `org.springdoc:*` runtime dependencies are removed from the starter dependency tree intent.
- Confirm Swagger annotations are fallback metadata only and do not imply Springdoc usage.
- Compare all 6 required custom annotations against the requirement document.
- Check scanner support for `@RestController`, `@Controller + @ResponseBody`, `@RequestMapping` variants,
  path/query/header/cookie/body parameters, ignored elements, and unannotated complex params.
- Check schema support for primitive/simple types, enum, collection, map, DTO, nested generics, `ResponseEntity<T>`,
  `Result<T>`-style wrapper, `PageResult<T>`-style wrapper, Jackson names, hidden fields, and Bean Validation required
  fields.
- Check OpenAPI output compatibility with the existing console frontend parser: `paths`, HTTP method nodes,
  `parameters`, `requestBody`, `responses`, `components.schemas`, `$ref`, `example`, and `required`.
- Check producer config preservation: title, description, version, contact, license, authorization header,
  access-control token.
- Identify whether generated JSON includes invalid OpenAPI fields or missing required OpenAPI fields.
- Identify whether current tests cover only unit generation or also auto-configuration/controller exposure.

**Expected final output contract:**

```markdown
## Mill Final Report

### Confirmed Matches
- ...

### Gaps Blocking MVP
- [severity] exact file/function or behavior

### Non-blocking Follow-ups
- ...

### Recommended Sartre Work Items
- item 1
- item 2

### Evidence
- command/read evidence summary
```

**Wait point:** Sartre must not start implementation until Mill returns this final report.

### Sartre: Development Scope and Boundaries

**Mode:** implementation, bounded by Mill's final blocking gaps.

**Write scope:**

- `backend/openapi-debug-console-spring-boot-starter/pom.xml`
- `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/annotation/`
- `backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/openapi/`
-

`backend/openapi-debug-console-spring-boot-starter/src/main/java/top/egon/openapi/console/autoconfigure/ApiDocOpenApiAutoConfiguration.java`

- `backend/openapi-debug-console-spring-boot-starter/src/test/java/top/egon/openapi/console/`
- `backend/openapi-debug-console-spring-boot-starter/src/test/js/` only if OpenAPI parser compatibility needs a focused
  regression test.

**Forbidden write scope:**

- `backend/openapi-debug-console-spring-boot-starter/README.md`
- Other backend modules
- Frontend app code outside the starter static console tests
- Gateway runtime configuration
- Ops scripts

**Objective:** Close Mill's MVP-blocking implementation gaps while preserving existing console behavior.

**Development boundaries:**

- Keep REST-only MVP.
- Keep `/v3/api-docs` as the producer endpoint.
- Keep output as plain standard OpenAPI JSON.
- Keep `top.egon.openapi.console.annotation` as the annotation namespace.
- Use existing project comment style for file/class/method comments.
- Keep direct readable implementation; do not split methods only for formal length.
- Add or adjust tests before or alongside implementation for every behavior change.
- Preserve existing security and console tests unless Mill identifies a concrete incompatibility.

**Expected implementation areas if Mill confirms gaps:**

- Producer auto-configuration: make sure the new producer endpoint is only active for Servlet MVC producer apps and does
  not collide with WebFlux gateway console configuration.
- Dependency cleanup: ensure Springdoc runtime dependencies are gone while any Swagger annotation dependency is
  metadata-only.
- Annotation semantics: align `@DocService`, `@DocOperation`, `@DocParam`, `@DocModel`, `@DocField`, `@DocIgnore` with
  `/Users/mario/Downloads/doc.md`.
- Scanner behavior: fix missing mapping/parameter edge cases only if they are in the REST MVP.
- Schema behavior: fix generic wrappers, field naming, hidden fields, examples, required fields, recursion, and simple
  type coverage only to MVP depth.
- OpenAPI shape: ensure frontend parser and export renderer can consume generated documents without special private
  format assumptions.

**Required verification commands before Sartre final report:**

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend
mvn -pl openapi-debug-console-spring-boot-starter -Dtest=ApiDocSpringMvcOpenApiGeneratorTest test
```

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend
mvn -pl openapi-debug-console-spring-boot-starter test
```

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend/openapi-debug-console-spring-boot-starter
node --test src/test/js/openapi-console-openapi.test.cjs src/test/js/openapi-console-storage.test.cjs
```

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend
mvn -pl openapi-debug-console-spring-boot-starter dependency:tree -Dincludes=org.springdoc
```

Expected for the dependency check: no `org.springdoc` dependency entries for this starter.

**Expected final output contract:**

```markdown
## Sartre Final Report

### Changes Made
- file: summary

### Mill Gaps Closed
- gap id/name -> resolution

### Tests Added or Updated
- ...

### Verification Results
- command: PASS/FAIL and key output

### Remaining Known Limitations
- ...
```

**Wait point:** Pasteur must not start QA until Sartre returns this final report.

### Pasteur: QA and Code Review Gate

**Mode:** review and validation.

**Write scope:** none by default. Pasteur may not implement fixes. Pasteur may only report required rework.

**Objective:** Decide whether Sartre's implementation can be accepted or must return to Sartre for rework.

**Required review areas:**

- No Springdoc runtime dependency remains.
- New producer endpoint still returns OpenAPI JSON at `/v3/api-docs`.
- Existing console aggregation and security contracts remain intact.
- Generated OpenAPI JSON is structurally valid enough for current static parser and export renderer.
- New annotation usage does not duplicate or override Java method signature truth.
- Tests cover the MVP behaviors, not only happy path generation.
- No broad refactor or unrelated module change slipped in.
- Comments follow existing `@BelongsProject`, `@BelongsPackage`, `@ClassName`, `@Description` style.

**Required verification commands:**

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend
mvn -pl openapi-debug-console-spring-boot-starter test
```

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend/openapi-debug-console-spring-boot-starter
node --test src/test/js/openapi-console-openapi.test.cjs src/test/js/openapi-console-storage.test.cjs
```

```bash
cd /Users/mario/SelfProject/FamilyAiButler/backend
mvn -pl openapi-debug-console-spring-boot-starter dependency:tree -Dincludes=org.springdoc
```

**Acceptance gate:**

Pasteur may accept only if:

- all required verification commands pass or have a clearly documented environment-only blocker
- `dependency:tree -Dincludes=org.springdoc` shows no Springdoc dependency entries
- generated REST OpenAPI JSON includes `openapi`, `info`, `paths`, `components.schemas`, `components.securitySchemes`
- at least one test covers annotations and one test covers generic wrapper schema output
- access-control filter tests still prove missing token rejects and configured token allows `/v3/api-docs`
- static JS parser tests still pass

**Mandatory rework conditions:**

- any Springdoc runtime dependency remains
- `/v3/api-docs` is missing, renamed, or no longer protected by existing internal token flow
- generated JSON uses a private non-OpenAPI structure
- debug console login/signature/catalog/openapi fetch tests regress
- generic DTO wrappers or required field extraction fail existing or newly required MVP tests
- implementation touches unrelated modules
- README changes are made by Sartre instead of parent

**Expected final output contract:**

```markdown
## Pasteur Final Gate Report

### Decision
ACCEPTED or REWORK_REQUIRED

### Verified Commands
- command: PASS/FAIL and key output

### Findings
- severity, file, behavior, required fix

### Rework Required From Sartre
- exact item list, or empty if accepted

### Parent README Readiness
- YES/NO and reason
```

**Wait point:** Parent must not update README until Pasteur returns `Parent README Readiness: YES`.

### Parent: Final README Documentation Only

**Mode:** documentation-only.

**Write scope:**

- `backend/openapi-debug-console-spring-boot-starter/README.md`

**Objective:** Update README to match the final accepted implementation and include new annotation examples.

**Required README content:**

- State clearly that Springdoc is not used for producer scanning.
- State that `/v3/api-docs` remains the REST OpenAPI JSON endpoint.
- Include business module producer config.
- Include all 6 new annotations and intended usage.
- Include Controller example using `@DocService`, `@DocOperation`, `@DocParam`, Spring MVC annotations, and
  `@RequestBody`.
- Include DTO example using `@DocModel`, `@DocField`, `@DocIgnore`, `@JsonProperty`, `@JsonIgnore`, and Bean Validation.
- Explain Swagger annotations are migration fallback metadata only, not Springdoc runtime.
- Preserve existing gateway module config, password config, request signing, internal token, key config table, and
  environment guidance unless Pasteur requires a precise correction.
- Mention REST-only MVP and explicitly defer Dubbo/proto/gRPC.

**Parent must not:**

- modify Java code
- modify POM
- modify tests
- add new subagents
- run implementation beyond docs

**README verification command:**

```bash
cd /Users/mario/SelfProject/FamilyAiButler
rg -n "Springdoc|springdoc|@DocService|@DocOperation|@DocParam|@DocModel|@DocField|@DocIgnore|/v3/api-docs" backend/openapi-debug-console-spring-boot-starter/README.md
```

Expected:

- old claim "通过 Springdoc 生成 `/v3/api-docs`" is gone
- new annotations are documented
- `/v3/api-docs` remains documented as the REST OpenAPI JSON endpoint

## Integration Timeline

1. **Mill analysis phase**
    - Parent dispatches only Mill.
    - Parent waits for Mill final report.
    - No file writes during Mill.

2. **Sartre implementation phase**
    - Parent dispatches only Sartre with Mill's final gap list.
    - Sartre implements only blocking MVP gaps.
    - Parent waits for Sartre final report.

3. **Pasteur QA gate phase**
    - Parent dispatches only Pasteur with Mill and Sartre reports.
    - Pasteur runs review and verification.
    - If `REWORK_REQUIRED`, parent sends exact Pasteur rework items back to Sartre and waits again.
    - If `ACCEPTED`, proceed to parent README phase.

4. **Parent README phase**
    - Parent updates only README.
    - Parent runs README verification command.
    - Parent summarizes final result and all agent outputs.

## Conflict Resolution Strategy

- Mill vs Sartre: If Sartre disagrees with a Mill gap, Sartre must document the disagreement and evidence. Pasteur
  decides whether the gap blocks acceptance.
- Sartre vs Pasteur: Pasteur gate is binding. Any `REWORK_REQUIRED` item returns to Sartre; parent must not fix it.
- README vs implementation: Implementation wins. Parent must document only behavior accepted by Pasteur, not
  aspirational behavior from `/Users/mario/Downloads/doc.md`.
- Requirement vs current console contract: REST OpenAPI producer migration must not break existing console contract. If
  a requirement would break login, signing, catalog, OpenAPI fetch, export, tryout, or load-test, mark it as deferred
  and require explicit user approval.

## Highest Coordination Risk

The highest risk is parent or Sartre expanding the scope into RPC/docs UI or broad console redesign while closing REST
producer gaps.

**Mitigation:** keep the phase gate strict. Mill can identify RPC/proto/gRPC gaps only as deferred follow-ups. Sartre
cannot implement them. Pasteur must reject any unrelated implementation. Parent can only document final accepted REST
behavior.

## Minimum Acceptance Criteria

- REST producer does not use Springdoc runtime.
- `/v3/api-docs` still emits standard OpenAPI JSON.
- New annotation system is available and tested.
- Spring MVC scanner handles REST MVP mapping and parameter sources.
- Schema generator handles REST MVP DTO/generic wrapper fields.
- Existing debug console backend tests pass.
- Existing static OpenAPI parser tests pass.
- Internal OpenAPI token protection still works.
- README accurately explains the new annotation usage after Pasteur acceptance.

