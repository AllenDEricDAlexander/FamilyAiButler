# pg-ddd-codegen

PostgreSQL DDL 到 DDD/COLA 分层 Java 代码的 v1 生成器。

## 使用方式

### IDEA 直接运行

在 IDEA 里新增普通 Java Application：

```text
Main class: top.egon.familyaibutler.codegen.bootstrap.CodegenApplication
Module classpath: pg-ddd-codegen
Working directory: backend/pg-ddd-codegen
Program arguments: 留空
```

参数留空时会自动运行内置 demo：

```text
target/codegen-demo
├── generator.yml
├── schema.sql
└── generated-src
```

也可以显式指定 demo 目录：

```text
Program arguments: --demo target/codegen-demo
```

命令行运行同一个 demo：

```bash
cd backend
mvn -pl pg-ddd-codegen -DskipTests compile exec:java
```

### 初始化 YAML 模板

如果要复制一份可改的模板：

```bash
cd backend
mvn -pl pg-ddd-codegen -DskipTests -Dexec.args="--init pg-ddd-codegen/codegen-starter" compile exec:java
```

内置模板文件也可以直接查看：

```text
src/main/resources/examples/generator-example.yml
src/main/resources/examples/schema-example.sql
```

### 使用自己的 DDL 生成

```bash
cd backend
mvn -pl pg-ddd-codegen -DskipTests -Dexec.args="--config /path/to/generator.yml" compile exec:java
```

`generator.yml` 可以从 `src/main/resources/examples/generator-example.yml` 复制后修改；完整结构参考仓库根目录的
`codeGen.md`。v1 支持：

- PostgreSQL DDL 文件或 Flyway migration 目录输入。
- `parse` 离线模式解析常见 DDL 子集。
- `catalog` 模式在配置 `ddl.postgres.jdbcUrl` 时执行 DDL 并读取 PostgreSQL 元数据。
- 按聚合配置生成 COLA 风格的 adapter、application、domain、infrastructure 分层代码。
- application 层输出 `*ServiceI`、`*ServiceImpl`、`dto/*` 和 executor，adapter 只依赖 application 接口，不直接依赖实现类。
- 生成 Mapper XML 自动分区、`generation-report.md`、`.generated/codegen-index.json`。
- 支持 `enums` 显式定义领域枚举，并用 `columns` 绑定数据库字段。
- 生成 `pom.xml`、Spring Boot 启动类、DomainService、DomainEvent、测试骨架和子实体持久化对象。
- 生成 `application.yml`，默认 `spring.jpa.hibernate.ddl-auto=validate`，避免 Hibernate 自动更新数据库结构。
- 模板文件位于 `src/main/resources/templates`，v1 使用 Freemarker 渲染 Controller、Application
  Service、Executor、领域对象和持久化对象。
- Command record 会根据 DDL 补充 `@NotNull`、`@Size`、`@Digits` 等 Jakarta Validation 注解。
- JPA Entity 会根据 `created_at` / `updated_at` 补充 `@CreatedDate` / `@LastModifiedDate` 和 `@EntityListeners`。

## 自定义枚举

当数据库字段是 `varchar` / `smallint` 等普通类型，但业务上需要生成领域枚举时，可以在 `generator.yml` 中显式定义：

```yaml
enums:
  - name: OrderStatus
    description: 订单状态
    columns:
      - trade_order.status
    values:
      - name: CREATED
        code: CREATED
        description: 已创建
      - name: PAID
        code: PAID
        description: 已支付
```

生成规则：

- `domain.model.enums.OrderStatus` 使用 Java enum。
- 领域模型、Command、Query、Response 使用 `OrderStatus`。
- JPA Entity 和 MyBatis Plus DO 使用 `String` 承接，避免 PostgreSQL enum / Hibernate 映射带来的数据库刷新和类型兼容问题。

## JPA DDL 策略

生成项目默认输出：

```yaml
spring:
  jpa:
    generate-ddl: false
    open-in-view: false
    hibernate:
      ddl-auto: validate
```

`validate` 只校验实体和数据库结构是否匹配，不会创建、更新、删除表结构。如果目标工程已有 `application.yml`，生成器按
`CREATE_ONLY` 策略不覆盖，需要业务工程自己确认等效配置。

## 生成工程依赖

生成项目的 `pom.xml` 默认包含：

- Spring Boot Web
- Jakarta Validation
- Spring Data JPA
- MyBatis Plus Spring Boot 3 Starter
- PostgreSQL JDBC
- Lombok
- Spring Boot Test
- ArchUnit JUnit5

## 项目结构

```shell
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── huawei
    │   │           └── charging
    │   │               ├── adapter
    │   │               │   └── ChargeController.java
    │   │               ├── application
    │   │               │   ├── ChargeServiceI.java
    │   │               │   ├── ChargeServiceImpl.java
    │   │               │   └── dto
    │   │               │       ├── BeginSessionRequest.java
    │   │               │       ├── ChargeRecordDto.java
    │   │               │       ├── ChargeRequest.java
    │   │               │       ├── EndSessionRequest.java
    │   │               │       ├── MultiResponse.java
    │   │               │       ├── Response.java
    │   │               │       └── SingleResponse.java
    │   │               ├── Application.java
    │   │               ├── domain
    │   │               │   ├── account
    │   │               │   │   ├── Account.java
    │   │               │   │   └── AccountDomainService.java
    │   │               │   ├── ApplicationContextHelper.java
    │   │               │   ├── BizException.java
    │   │               │   ├── charge
    │   │               │   │   ├── CallType.java
    │   │               │   │   ├── ChargeContext.java
    │   │               │   │   ├── chargeplan
    │   │               │   │   │   ├── BasicChargePlan.java
    │   │               │   │   │   ├── ChargePlan.java
    │   │               │   │   │   ├── ChargePlanType.java
    │   │               │   │   │   ├── FamilyChargePlan.java
    │   │               │   │   │   ├── FixedTimeChangePlan.java
    │   │               │   │   │   └── Resource.java
    │   │               │   │   ├── ChargeRecord.java
    │   │               │   │   ├── chargerule
    │   │               │   │   │   ├── AbstractChargeRule.java
    │   │               │   │   │   ├── BasicChargeRule.java
    │   │               │   │   │   ├── ChargeRule.java
    │   │               │   │   │   ├── ChargeRuleFactory.java
    │   │               │   │   │   ├── CompositeChargeRule.java
    │   │               │   │   │   ├── FamilyChargeRule.java
    │   │               │   │   │   └── FixedTimeChargeRule.java
    │   │               │   │   ├── Money.java
    │   │               │   │   ├── MoneyConverter.java
    │   │               │   │   └── Session.java
    │   │               │   ├── DomainFactory.java
    │   │               │   ├── Entity.java
    │   │               │   └── gateway
    │   │               │       ├── AccountGateway.java
    │   │               │       ├── ChargeGateway.java
    │   │               │       └── SessionGateway.java
    │   │               └── infrastructure
    │   │                   ├── AccountGatewayImpl.java
    │   │                   ├── RestClientBean.java
    │   │                   └── SessionGatewayImpl.java
    │   └── resources
    │       ├── application.yml
    │       └── logback.xml
    └── test
        ├── charge.http
        ├── java
        │   └── com
        │       └── huawei
        │           └── charging
        │               ├── application
        │               │   └── ChargeServiceTest.java
        │               ├── CleanArchTest.java
        │               ├── domain
        │               │   ├── ChargeRecordPlanTest.java
        │               │   ├── ChargeRecordRuleTest.java
        │               │   └── CompositeChargeRuleTestRecord.java
        │               ├── infrastructure
        │               │   ├── AccountGatewayTest.java
        │               │   ├── ChargeRecordRepoTest.java
        │               │   ├── FixtureLoader.java
        │               │   ├── JSONTest.java
        │               │   ├── SpingBootConfTest.java
        │               │   ├── WireMockBasicTest.java
        │               │   └── WireMockRegister.java
        │               └── TestsContainerBoot.java
        └── resources
            ├── application-test.yml
            ├── application.yml
            ├── fixture
            │   └── wiremock
            │       ├── stub_account.json
            │       ├── stub_insufficient_account.json
            │       └── stub_wire_mock_basic.json
            └── logback-test.xml
```

## v1 边界

复杂分区表、触发器、存储过程、jsonb 深层结构、数组强类型集合、多租户、Saga 等暂不做自动业务代码生成。遇到 `catalog` 模式没有
JDBC 配置时，当前实现会自动回退到 `parse` 模式并在报告摘要中给出 warning。
