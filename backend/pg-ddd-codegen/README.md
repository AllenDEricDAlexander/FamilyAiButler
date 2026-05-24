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

每次运行 demo 都会先刷新 demo 目录下的 `generated-src` 子目录，再重新生成示例工程，避免旧生成产物影响注解、注释或模板验收；
`generator.yml` 和 `schema.sql` 缺失时会自动初始化，已存在时保留原文件内容。

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
- adapter 层输出 `adapter.web`、Web DTO/VO 和 WebAssembler，不把 Web DTO 传入 application。
- application 层输出 `manage`、`command`、`query`、`result`、`assembler` 和 executor，不生成旧 `application.dto` /
  `*ServiceI` / `*ServiceImpl`。
- domain 层按聚合小写分包输出 `model`、`gateway`、`service`、`event`，不生成 `domain.repository`。
- infrastructure 网关实现输出到 `infrastructure.gateway.impl`。
- 生成 Mapper XML 自动分区、`generation-report.md`、`.generated/codegen-index.json`。
- 支持 `enums` 显式定义领域枚举，并用 `columns` 绑定数据库字段。
- 生成 `pom.xml`、Spring Boot 启动类、DomainService、DomainEvent、测试骨架和子实体持久化对象。
- Spring Boot 启动类会把 `project.moduleName` 规范化为合法 Java 类名，例如 `demo-order` 生成 `DemoOrderApplication`。
- 生成 `application.yml`，默认 `spring.jpa.hibernate.ddl-auto=validate`，避免 Hibernate 自动更新数据库结构。
- 模板文件位于 `src/main/resources/templates`，v1 使用 Freemarker 渲染 Controller、Manage、Executor、领域对象和持久化对象。
- Web Request DTO 和 Command 会根据 DDL 补充 `@NotNull`、`@Size`、`@Digits` 等 Jakarta Validation 注解。
- Web Request DTO、Web VO、application Command、Query、Result 默认补齐 `@DocModel`、字段 JavaDoc、
  `@DocField(description, required, example)`、`@Data`、`@With`、`@NoArgsConstructor`、`@AllArgsConstructor`、
  `@Accessors(chain = true)`、`@Builder` 和 `@EqualsAndHashCode`；存在继承关系时再改用 `@SuperBuilder`，需要比较父类字段时使用
  `@EqualsAndHashCode(callSuper = true)`。
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

- `domain.order.model.enums.OrderStatus` 使用 Java enum。
- 领域模型、Command、Query、Result 使用 `OrderStatus`。
- JPA Entity 和 MyBatis Plus DO 使用 `String` 承接，避免 PostgreSQL enum / Hibernate 映射带来的数据库刷新和类型兼容问题。

## JPA DDL 策略

当前生成的 `application.yml` 只覆盖 HTTP-only 单模块脚手架的本地基础配置：应用名和 JPA 安全 DDL 策略。生成器暂不生成
Nacos、Dubbo、RPC Provider/Consumer、Sentinel、Seata 等运行时配置；需要接入 RPC 或服务治理时，应由目标业务模块按项目现有配置规范补充。

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
- openapi-debug-console-spring-boot-starter
- Spring Boot Test
- ArchUnit JUnit5

## 项目结构

```shell
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java
    │   │   └── com/acme/trade
    │   │       ├── TradeApplication.java
    │   │       ├── adapter
    │   │       │   └── web
    │   │       │       ├── OrderController.java
    │   │       │       ├── assembler
    │   │       │       │   └── OrderWebAssembler.java
    │   │       │       └── dto
    │   │       │           ├── CreateOrderRequestDTO.java
    │   │       │           ├── CreateOrderVO.java
    │   │       │           ├── PageOrderRequestDTO.java
    │   │       │           └── OrderPageVO.java
    │   │       ├── application
    │   │       │   ├── assembler
    │   │       │   │   └── OrderApplicationAssembler.java
    │   │       │   ├── command
    │   │       │   │   └── CreateOrderCommand.java
    │   │       │   ├── executor
    │   │       │   │   ├── command
    │   │       │   │   │   └── CreateOrderCmdExe.java
    │   │       │   │   └── query
    │   │       │   │       └── PageOrderQryExe.java
    │   │       │   ├── manage
    │   │       │   │   ├── OrderManage.java
    │   │       │   │   └── impl
    │   │       │   │       └── OrderManageImpl.java
    │   │       │   ├── query
    │   │       │   │   └── OrderPageQuery.java
    │   │       │   └── result
    │   │       │       ├── CreateOrderResult.java
    │   │       │       └── OrderPageResult.java
    │   │       ├── domain
    │   │       │   └── order
    │   │       │       ├── event
    │   │       │       ├── gateway
    │   │       │       │   └── query
    │   │       │       ├── model
    │   │       │       └── service
    │   │       └── infrastructure
    │   │           ├── gateway
    │   │           │   └── impl
    │   │           └── persistence
    │   │               ├── jpa
    │   │               └── mp
    │   └── resources
    │       ├── application.yml
    │       └── logback.xml
    └── test
        ├── java
        │   └── com/acme/trade
        │       ├── architecture
        │       ├── application
        │       ├── domain
        │       └── infrastructure
        └── resources
```

## v1 边界

复杂分区表、触发器、存储过程、jsonb 深层结构、数组强类型集合、多租户、Saga 等暂不做自动业务代码生成。遇到 `catalog` 模式没有
JDBC 配置时，当前实现会自动回退到 `parse` 模式并在报告摘要中给出 warning。
