# 1. 总体定位

这个生成器不是普通的“表转 Entity + Mapper”工具，而是：

```text
通过 PostgreSQL DDL 识别数据库结构，
通过规则和少量配置识别 DDD 边界，
最终生成符合 DDD/COLA 分层的 Java 代码。
```

生成目标仍然保持上一版原则：

```text
简单写入、简单查询：Spring Data JPA
复杂查询、分页、统计、报表、导出：MyBatis Plus
领域层：不感知 JPA，不感知 MP，不感知数据库
```

Spring Data JPA 本身提供基于 Jakarta Persistence API 的 Repository 支持，适合做聚合持久化和常规 Repository 模型；MyBatis
Plus 提供 Mapper、Service、分页、查询等持久层能力，更适合复杂 SQL、动态条件和查询模型。([Home][1])

---

# 2. 推荐架构：DDL Catalog Mode 优先

PostgreSQL DDL 语法比较丰富，`CREATE TABLE`、`ALTER TABLE`、`COMMENT ON`、`CREATE TYPE`、`CREATE INDEX`、`CHECK`、`IDENTITY`
、分区表、表达式索引、部分索引都可能出现。PostgreSQL
官方文档里，主键、唯一、外键、检查约束等都可以通过表级或列级约束表达；主键还会隐含唯一和非空约束。([PostgreSQL][2])

所以第一版我建议不要一上来硬写完整 PostgreSQL DDL Parser。更稳的方式是：

## 2.1 主模式：Catalog Mode

把 DDL 执行到一个临时 PostgreSQL 实例或隔离 schema 里，然后查询 PostgreSQL 的元数据。

```text
schema.sql
  ↓
启动临时 PostgreSQL / 使用本地 shadow database
  ↓
执行 DDL
  ↓
查询 information_schema + pg_catalog
  ↓
得到 SchemaModel
```

PostgreSQL 的 `information_schema` 是标准化元数据视图，适合读取表、字段、部分约束等通用信息；但它不包含很多 PostgreSQL
特有信息，所以 PostgreSQL 官方也说明，涉及 PostgreSQL-specific features 时需要查询系统目录或特定视图。([PostgreSQL][3])

因此：

```text
information_schema：读通用表结构、字段、约束
pg_catalog：读注释、枚举、索引、表达式索引、部分索引、序列、identity、pg 类型
```

这个模式最可靠。

---

## 2.2 辅助模式：Pure Parse Mode

为了支持离线、CI 轻量运行，也可以提供纯解析模式：

```text
schema.sql
  ↓
DDL Lexer / Parser
  ↓
SchemaModel
```

但 Pure Parse Mode 只支持常用 DDL 子集：

```text
CREATE SCHEMA
CREATE TYPE ... AS ENUM
CREATE TABLE
ALTER TABLE ADD COLUMN
ALTER TABLE ADD CONSTRAINT
COMMENT ON TABLE
COMMENT ON COLUMN
CREATE INDEX
CREATE UNIQUE INDEX
```

复杂能力，比如分区、继承、表达式索引、复杂 CHECK、函数默认值、扩展类型，第一版不强行完整解析。否则生成器还没开始生成代码，就先把自己写成
PostgreSQL 解释器了，得不偿失。

---

# 3. 生成器输入

## 3.1 主输入：PostgreSQL DDL

支持两种形式：

```text
schema.sql
```

或者 Flyway 风格：

```text
db/migration
├── V001__init_schema.sql
├── V002__create_order.sql
├── V003__create_payment.sql
└── V004__add_order_index.sql
```

生成器按版本顺序执行或解析。

---

## 3.2 辅助输入：generator.yml

DDL 只能表达数据库结构，不能完整表达领域语义。比如下面这些信息，DDL 很难 100% 自动推断：

```text
哪个表是聚合根？
哪个表是聚合内实体？
哪个字段应该生成值对象？
哪些接口是 Command？
哪些接口是 Query？
哪些查询属于复杂查询？
哪些表只是字典表？
哪些表不需要生成代码？
```

所以需要一个轻量配置。

```yaml
project:
  basePackage: com.acme.trade
  moduleName: trade
  outputDir: ./generated-src
  javaVersion: 17

ddl:
  input:
    - db/migration/V001__init_schema.sql
    - db/migration/V002__create_order.sql
  mode: catalog
  postgres:
    version: 16
    schema: public
  naming:
    tablePrefix:
      - t_
      - trade_
    columnToCamel: true

ddd:
  boundedContext: trade

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
      - name: CANCELED
        code: CANCELED
        description: 已取消

aggregates:
  - name: Order
    rootTable: trade_order
    entityTables:
      - trade_order_item
    repository: jpa
    queryGateway: mp
    idValueObject: OrderId
    businessKeys:
      - order_no
    valueObjects:
      order_no: OrderNo
      buyer_id: BuyerId
      total_amount: Money
    commands:
      - name: CreateOrder
        path: /orders
        method: POST
      - name: CancelOrder
        path: /orders/{orderId}/cancel
        method: POST
    queries:
      - name: GetOrderDetail
        path: /orders/{orderId}
        method: GET
        persistence: jpa
      - name: PageOrder
        path: /orders
        method: GET
        persistence: mp
        filters:
          - buyer_id
          - status
          - created_at
          - keyword

ignore:
  tables:
    - flyway_schema_history
    - sys_config
```

我的建议是：**DDL 负责结构，YAML 负责业务语义。**
不要试图从 DDL 自动推导所有 DDD 设计。数据库会沉默，沉默不代表它懂业务。

补充约定：

```text
如果数据库字段是 varchar / smallint 等普通类型，但业务上要生成领域枚举，
使用 generator.yml 的 enums 节点显式定义枚举项，并通过 columns 绑定字段。

领域模型、Command、Query、Response 使用 Java enum。
JPA Entity / MyBatis Plus DO 默认使用 String 承接枚举字段。
```

这样做的原因是：领域层可以表达业务语义；持久层仍然保持简单稳定，避免 PostgreSQL enum、Hibernate enum mapping、MP TypeHandler
在 v1 阶段引入额外不确定性。

---

# 4. DDL 约定设计

为了让生成器更聪明，可以在 DDL 注释里加轻量 hint。

例如：

```sql
CREATE TYPE order_status AS ENUM ('CREATED', 'PAID', 'CANCELED');

CREATE TABLE trade_order
(
    id           bigserial PRIMARY KEY,
    order_no     varchar(64)    NOT NULL,
    buyer_id     bigint         NOT NULL,
    status       order_status   NOT NULL DEFAULT 'CREATED',
    total_amount numeric(18, 2) NOT NULL,
    remark       text,
    version      integer        NOT NULL DEFAULT 0,
    deleted      boolean        NOT NULL DEFAULT false,
    created_at   timestamptz    NOT NULL DEFAULT now(),
    updated_at   timestamptz    NOT NULL DEFAULT now(),

    CONSTRAINT uk_trade_order_order_no UNIQUE (order_no)
);

COMMENT ON TABLE trade_order IS 'aggregate=Order;module=trade';
COMMENT ON COLUMN trade_order.id IS '订单ID;vo=OrderId';
COMMENT ON COLUMN trade_order.order_no IS '订单号;vo=OrderNo;businessKey=true';
COMMENT ON COLUMN trade_order.buyer_id IS '买家ID;vo=BuyerId';
COMMENT ON COLUMN trade_order.status IS '订单状态';

CREATE INDEX idx_trade_order_buyer_status
    ON trade_order (buyer_id, status);

CREATE INDEX idx_trade_order_created_at
    ON trade_order (created_at);
```

子表：

```sql
CREATE TABLE trade_order_item
(
    id         bigserial PRIMARY KEY,
    order_id   bigint         NOT NULL,
    sku_id     bigint         NOT NULL,
    sku_name   varchar(128)   NOT NULL,
    quantity   integer        NOT NULL,
    sale_price numeric(18, 2) NOT NULL,

    CONSTRAINT fk_order_item_order
        FOREIGN KEY (order_id) REFERENCES trade_order (id)
);

COMMENT ON TABLE trade_order_item IS 'aggregateEntity=OrderItem;root=Order';
COMMENT ON COLUMN trade_order_item.order_id IS '订单ID;vo=OrderId';
```

生成器读取后，可以推导：

```text
trade_order      -> Order 聚合根
trade_order_item -> OrderItem 聚合内实体
order_status     -> OrderStatus Java enum
order_no         -> OrderNo 值对象
buyer_id         -> BuyerId 值对象
numeric(18,2)    -> BigDecimal / Money
version          -> 乐观锁字段
deleted          -> 逻辑删除字段
created_at       -> 创建时间
updated_at       -> 更新时间
```

---

# 5. 生成流程

推荐完整流程如下：

```text
1. 加载 generator.yml
2. 收集 DDL 文件
3. 按顺序执行 DDL 到 shadow PostgreSQL
4. 查询 information_schema
5. 查询 pg_catalog
6. 构建 PgSchemaModel
7. 根据规则和 YAML 构建 DddGenerationModel
8. 决定每个用例走 JPA 还是 MP
9. 生成 JavaClassModel
10. 渲染模板
11. 写入文件
12. 生成 ArchUnit 架构约束测试
13. 生成 generation-report.md
14. 保存 generated-index.json
```

流程图：

```text
┌──────────────┐
│ PostgreSQL   │
│ DDL Files    │
└──────┬───────┘
       │
       v
┌──────────────┐
│ DDL Loader   │
└──────┬───────┘
       │
       v
┌────────────────────┐
│ Shadow PG Executor │
└──────┬─────────────┘
       │
       v
┌─────────────────────────────┐
│ information_schema/pg_catalog│
└──────┬──────────────────────┘
       │
       v
┌──────────────┐
│ SchemaModel  │
└──────┬───────┘
       │
       v
┌──────────────┐
│ DDD Model    │
└──────┬───────┘
       │
       v
┌──────────────┐
│ Templates    │
└──────┬───────┘
       │
       v
┌──────────────┐
│ Java Code    │
└──────────────┘
```

---

# 6. 生成器模块规划

```text
pg-ddd-codegen
├── bootstrap
│   └── CodegenApplication.java
│
├── config
│   ├── GeneratorConfig.java
│   ├── DddConfig.java
│   ├── AggregateConfig.java
│   └── PersistenceRouteConfig.java
│
├── ddl
│   ├── DdlFileLoader.java
│   ├── DdlStatementSplitter.java
│   ├── DdlPreprocessor.java
│   └── DdlExecutionPlan.java
│
├── postgres
│   ├── PgShadowDatabase.java
│   ├── PgDdlExecutor.java
│   ├── PgCatalogReader.java
│   ├── PgInformationSchemaReader.java
│   └── PgMetadataQuery.java
│
├── schema
│   ├── PgSchemaModel.java
│   ├── PgTableModel.java
│   ├── PgColumnModel.java
│   ├── PgConstraintModel.java
│   ├── PgIndexModel.java
│   ├── PgEnumModel.java
│   └── PgRelationModel.java
│
├── analyzer
│   ├── AggregateAnalyzer.java
│   ├── TableRoleAnalyzer.java
│   ├── ValueObjectAnalyzer.java
│   ├── EnumAnalyzer.java
│   ├── AuditFieldAnalyzer.java
│   ├── LogicDeleteAnalyzer.java
│   ├── OptimisticLockAnalyzer.java
│   └── PersistenceRouteAnalyzer.java
│
├── ddd
│   ├── DddGenerationModel.java
│   ├── AggregateGenerationModel.java
│   ├── CommandGenerationModel.java
│   ├── QueryGenerationModel.java
│   └── RepositoryGenerationModel.java
│
├── java
│   ├── JavaClassModel.java
│   ├── JavaFieldModel.java
│   ├── JavaMethodModel.java
│   └── JavaAnnotationModel.java
│
├── template
│   ├── TemplateEngine.java
│   ├── FreemarkerTemplateEngine.java
│   ├── TemplateRegistry.java
│   └── TemplateContextFactory.java
│
├── writer
│   ├── SourceFileWriter.java
│   ├── GeneratedFileIndex.java
│   ├── MergeStrategy.java
│   └── WritePolicy.java
│
└── report
    ├── GenerationReport.java
    └── MarkdownReportWriter.java
```

---

# 7. Schema 元模型设计

DDL 或 PostgreSQL Catalog 读取后，先不要直接生成 Java。先统一成元模型。

## 7.1 PgSchemaModel

```java
public class PgSchemaModel {

    private String schemaName;

    private List<PgTableModel> tables;

    private List<PgEnumModel> enums;

    private List<PgRelationModel> relations;
}
```

## 7.2 PgTableModel

```java
public class PgTableModel {

    private String schemaName;
    private String tableName;
    private String tableComment;

    private List<PgColumnModel> columns;
    private List<PgConstraintModel> constraints;
    private List<PgIndexModel> indexes;

    private boolean partitioned;
    private boolean view;
}
```

## 7.3 PgColumnModel

```java
public class PgColumnModel {

    private String columnName;
    private String columnComment;

    private String pgType;
    private Integer length;
    private Integer precision;
    private Integer scale;

    private boolean nullable;
    private boolean primaryKey;
    private boolean unique;
    private boolean identity;
    private boolean serial;
    private boolean generated;
    private boolean enumType;

    private String defaultExpression;
    private String enumTypeName;
}
```

## 7.4 PgConstraintModel

```java
public class PgConstraintModel {

    private String constraintName;
    private ConstraintType type;

    private List<String> columns;

    private String checkExpression;

    private String referencedTable;
    private List<String> referencedColumns;
}
```

## 7.5 PgIndexModel

```java
public class PgIndexModel {

    private String indexName;

    private boolean unique;
    private boolean partial;
    private boolean expressionIndex;

    private List<String> columns;

    private String whereExpression;
    private String indexDefinition;
}
```

---

# 8. PostgreSQL 类型映射

第一版必须把 PostgreSQL 类型映射规则定死，否则生成出来的代码会很飘。

| PostgreSQL 类型       |                           Java 类型 | Domain 建议     | JPA 建议                             | MP 建议                          |
|---------------------|----------------------------------:|---------------|------------------------------------|--------------------------------|
| `bigint` / `int8`   |                            `Long` | ID 可包装成 VO    | `Long`                             | `Long`                         |
| `bigserial`         |                            `Long` | ID VO         | `@GeneratedValue`                  | `@TableId(type = IdType.AUTO)` |
| `integer` / `int4`  |                         `Integer` | 普通数值          | `Integer`                          | `Integer`                      |
| `smallint` / `int2` |                           `Short` | 枚举码可转 enum    | `Short`                            | `Short`                        |
| `numeric(p,s)`      |                      `BigDecimal` | 金额建议 Money VO | `BigDecimal`                       | `BigDecimal`                   |
| `decimal(p,s)`      |                      `BigDecimal` | 金额建议 Money VO | `BigDecimal`                       | `BigDecimal`                   |
| `varchar(n)`        |                          `String` | 可包装 VO        | `String`                           | `String`                       |
| `text`              |                          `String` | 长文本           | `String`                           | `String`                       |
| `boolean`           |                         `Boolean` | 布尔值           | `Boolean`                          | `Boolean`                      |
| `date`              |                       `LocalDate` | 日期            | `LocalDate`                        | `LocalDate`                    |
| `timestamp`         |                   `LocalDateTime` | 本地时间          | `LocalDateTime`                    | `LocalDateTime`                |
| `timestamptz`       |                         `Instant` | 时间点           | `Instant`                          | `Instant`                      |
| `uuid`              |                            `UUID` | ID 可包装 VO     | `UUID`                             | `UUID`                         |
| `json`              | `JsonNode` / `Map<String,Object>` | 不建议进入核心领域     | Converter                          | TypeHandler                    |
| `jsonb`             | `JsonNode` / `Map<String,Object>` | 不建议进入核心领域     | Converter                          | TypeHandler                    |
| `bytea`             |                          `byte[]` | 文件内容不建议进聚合    | `byte[]`                           | `byte[]`                       |
| PostgreSQL enum     |                         Java enum | 领域 enum       | Converter / Hibernate enum mapping | TypeHandler                    |
| `inet` / `cidr`     |                          `String` | 可包装 VO        | `String`                           | `String`                       |
| array 类型            |                         `List<T>` | v0.1 谨慎支持     | Converter                          | TypeHandler                    |

MyBatis Plus 官方生成器文档也提醒，使用元数据查询时，部分 PostgreSQL 类型如 `json`、`jsonb`、`uuid`、`xml`、`money`
可能处理不理想，建议通过自定义类型或 TypeHandler 处理；所以我们这个生成器应该自己维护 PostgreSQL 类型映射，不要完全依赖 MP
Generator 的默认类型转换。([MyBatis-Plus][4])

---

# 9. 字段规则识别

## 9.1 主键识别

DDL：

```sql
id bigserial PRIMARY KEY
```

生成：

```text
Domain:
OrderId

JPA:
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)

MP:
@TableId(value = "id", type = IdType.AUTO)
```

领域层：

```java
public record OrderId(Long value) {

    public OrderId {
        if (value == null || value <= 0) {
            throw new DomainException("订单ID不合法");
        }
    }
}
```

---

## 9.2 唯一约束识别

DDL：

```sql
CONSTRAINT uk_trade_order_order_no UNIQUE (order_no)
```

生成：

```text
OrderNo 值对象
OrderRepository.existsByOrderNo(OrderNo orderNo)
CreateOrder 前置校验
JPA Repository existsByOrderNo
```

示例：

```java
public interface OrderRepository {

    Optional<Order> find(OrderId orderId);

    Order save(Order order);

    boolean exists(OrderNo orderNo);
}
```

---

## 9.3 非空识别

DDL：

```sql
buyer_id bigint NOT NULL
```

生成：

```text
CreateOrderCommand.buyerId 添加 @NotNull
JPA Entity 添加 nullable = false
领域 create 方法校验 buyerId
```

示例：

```java
public record CreateOrderCommand(
        @NotNull Long buyerId,
        @NotNull List<CreateOrderItemCommand> items
) {
}
```

---

## 9.4 长度识别

DDL：

```sql
order_no varchar(64) NOT NULL
```

生成：

```java

@Size(max = 64)
private String orderNo;
```

JPA：

```java

@Column(name = "order_no", nullable = false, length = 64)
private String orderNo;
```

---

## 9.5 数值精度识别

DDL：

```sql
total_amount numeric(18, 2) NOT NULL
```

生成：

```java

@Digits(integer = 16, fraction = 2)
private BigDecimal totalAmount;
```

也可以根据配置生成 Money 值对象：

```java
public record Money(BigDecimal value) {

    public Money {
        if (value == null) {
            throw new DomainException("金额不能为空");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("金额不能小于0");
        }
    }
}
```

---

## 9.6 枚举识别

两种来源。

### PostgreSQL enum

```sql
CREATE TYPE order_status AS ENUM ('CREATED', 'PAID', 'CANCELED');

status order_status NOT NULL DEFAULT 'CREATED'
```

生成：

```java
public enum OrderStatus {
    CREATED,
    PAID,
    CANCELED
}
```

### CHECK 约束

```sql
status varchar(32) NOT NULL,
CONSTRAINT ck_order_status CHECK (status IN ('CREATED', 'PAID', 'CANCELED'))
```

也生成：

```java
public enum OrderStatus {
    CREATED,
    PAID,
    CANCELED
}
```

第一版建议：**Domain 使用 Java enum，JPA Entity / MP DO 可以先用 String 承接，再在 Converter 里转换。**
这样最稳，不会被 PostgreSQL enum JDBC 映射坑住。追求精致可以后面加 Hibernate enum mapping 和 MP TypeHandler。

如果枚举不来自 PostgreSQL enum 或 CHECK，而是业务侧定义，可以在 `generator.yml` 中写：

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

生成：

```text
domain.model.enums.OrderStatus
trade_order.status 在领域模型中使用 OrderStatus
OrderJpaEntity.status 使用 String
OrderDO.status 使用 String
```

---

## 9.7 逻辑删除识别

DDL：

```sql
deleted boolean NOT NULL DEFAULT false
```

或：

```sql
is_deleted boolean NOT NULL DEFAULT false
```

生成：

JPA Entity：

```java

@Column(name = "deleted", nullable = false)
private Boolean deleted;
```

MP DO：

```java

@TableLogic
@TableField("deleted")
private Boolean deleted;
```

复杂查询默认追加：

```sql
WHERE deleted = false
```

---

## 9.8 乐观锁识别

DDL：

```sql
version integer NOT NULL DEFAULT 0
```

生成：

JPA：

```java

@Version
@Column(name = "version", nullable = false)
private Integer version;
```

MP：

```java

@Version
@TableField("version")
private Integer version;
```

---

## 9.9 审计字段识别

DDL：

```sql
created_at timestamptz NOT NULL DEFAULT now(),
updated_at timestamptz NOT NULL DEFAULT now()
```

生成：

```java
private Instant createdAt;
private Instant updatedAt;
```

JPA 可以生成：

```java

@CreatedDate
@Column(name = "created_at", nullable = false, updatable = false)
private Instant createdAt;

@LastModifiedDate
@Column(name = "updated_at", nullable = false)
private Instant updatedAt;
```

MP 可以生成：

```java

@TableField(value = "created_at", fill = FieldFill.INSERT)
private Instant createdAt;

@TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
private Instant updatedAt;
```

---

# 10. DDD/COLA 分层生成结构

生成包结构建议如下：

```text
com.acme.trade
├── TradeApplication.java
│
├── adapter
│   └── web
│       ├── controller
│       │   └── OrderController.java
│       └── assembler
│           └── OrderWebAssembler.java
│
├── client
│   ├── command
│   │   ├── CreateOrderCommand.java
│   │   └── CancelOrderCommand.java
│   ├── query
│   │   └── OrderPageQuery.java
│   └── response
│       ├── OrderDetailResponse.java
│       └── OrderPageResponse.java
│
├── app
│   ├── service
│   │   ├── OrderCommandService.java
│   │   └── OrderQueryService.java
│   └── executor
│       ├── command
│       │   ├── CreateOrderCmdExe.java
│       │   └── CancelOrderCmdExe.java
│       └── query
│           └── OrderPageQryExe.java
│
├── domain
│   ├── model
│   │   ├── aggregate
│   │   │   └── Order.java
│   │   ├── entity
│   │   │   └── OrderItem.java
│   │   ├── valueobject
│   │   │   ├── OrderId.java
│   │   │   ├── OrderNo.java
│   │   │   ├── BuyerId.java
│   │   │   └── Money.java
│   │   └── enums
│   │       └── OrderStatus.java
│   ├── repository
│   │   └── OrderRepository.java
│   └── gateway
│       └── OrderQueryGateway.java
│
└── infrastructure
    ├── persistence
    │   ├── jpa
    │   │   ├── entity
    │   │   │   ├── OrderJpaEntity.java
    │   │   │   └── OrderItemJpaEntity.java
    │   │   ├── repository
    │   │   │   └── OrderJpaRepository.java
    │   │   └── converter
    │   │       └── OrderJpaConverter.java
    │   ├── mp
    │   │   ├── dataobject
    │   │   │   ├── OrderDO.java
    │   │   │   └── OrderItemDO.java
    │   │   ├── mapper
    │   │   │   └── OrderMapper.java
    │   │   ├── xml
    │   │   │   └── OrderMapper.xml
    │   │   └── converter
    │   │       └── OrderMpConverter.java
    │   └── impl
    │       └── OrderRepositoryJpaImpl.java
    └── gatewayimpl
        └── OrderQueryGatewayImpl.java
```

---

# 11. JPA 和 MP 的分工

## 11.1 写模型：JPA

写操作走领域模型：

```text
Controller
  -> CommandService
    -> CmdExe
      -> Domain Aggregate
        -> Domain Repository
          -> JPA Repository Impl
            -> Spring Data JPA
```

生成：

```java
public interface OrderRepository {

    Optional<Order> find(OrderId orderId);

    Order save(Order order);

    boolean exists(OrderNo orderNo);
}
```

JPA 实现：

```java

@Repository
@RequiredArgsConstructor
public class OrderRepositoryJpaImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderJpaConverter orderJpaConverter;

    @Override
    public Optional<Order> find(OrderId orderId) {
        return orderJpaRepository.findById(orderId.value())
                .map(orderJpaConverter::toDomain);
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = orderJpaConverter.toEntity(order);
        OrderJpaEntity saved = orderJpaRepository.save(entity);
        return orderJpaConverter.toDomain(saved);
    }

    @Override
    public boolean exists(OrderNo orderNo) {
        return orderJpaRepository.existsByOrderNo(orderNo.value());
    }
}
```

---

## 11.2 复杂读模型：MyBatis Plus

复杂查询走 QueryGateway：

```text
Controller
  -> QueryService
    -> QryExe
      -> Domain Gateway Interface
        -> MP QueryGatewayImpl
          -> Mapper XML
```

领域层只定义接口：

```java
public interface OrderQueryGateway {

    PageResponse<OrderPageResponse> page(OrderPageQuery query);
}
```

基础设施层实现：

```java

@Repository
@RequiredArgsConstructor
public class OrderQueryGatewayImpl implements OrderQueryGateway {

    private final OrderMapper orderMapper;

    @Override
    public PageResponse<OrderPageResponse> page(OrderPageQuery query) {
        Page<OrderPageResponse> page = new Page<>(query.pageNo(), query.pageSize());
        IPage<OrderPageResponse> result = orderMapper.pageOrders(page, query);
        return PageResponse.of(result.getRecords(), result.getTotal());
    }
}
```

Mapper：

```java

@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {

    IPage<OrderPageResponse> pageOrders(
            Page<?> page,
            @Param("query") OrderPageQuery query
    );
}
```

XML：

```xml

<select id="pageOrders" resultType="com.acme.trade.client.response.OrderPageResponse">
    SELECT
    o.id,
    o.order_no,
    o.buyer_id,
    o.status,
    o.total_amount,
    o.created_at
    FROM trade_order o
    WHERE o.deleted = false

    <if test="query.buyerId != null">
        AND o.buyer_id = #{query.buyerId}
    </if>

    <if test="query.status != null">
        AND o.status = #{query.status}
    </if>

    <if test="query.createdAtStart != null">
        AND o.created_at &gt;= #{query.createdAtStart}
    </if>

    <if test="query.createdAtEnd != null">
        AND o.created_at &lt; #{query.createdAtEnd}
    </if>

    ORDER BY o.created_at DESC
</select>
```

---

# 12. JPA / MP 路由规则

## 12.1 默认路由

```text
Command 写操作：JPA
按 ID 查询详情：JPA
简单 exists / findByBusinessKey：JPA
复杂分页：MP
多表 join：MP
报表统计：MP
批量更新：MP
批量导出：MP
```

## 12.2 查询复杂度评分

如果 YAML 没显式配置，可以自动评分：

| 条件             | 分值 |
|----------------|---:|
| 单表按 ID 查询      |  0 |
| 单表 1-2 个等值条件   |  1 |
| 有分页            | +1 |
| 有时间范围          | +1 |
| 有 keyword 模糊查询 | +2 |
| 有 3 个以上动态条件    | +2 |
| 有 join         | +3 |
| 有 group by     | +3 |
| 有 having       | +3 |
| 有 union        | +4 |
| 有窗口函数          | +4 |
| 有子查询           | +3 |
| 有批量更新/删除       | +4 |

规则：

```text
score <= 2：JPA
score > 2：MP
```

但最终仍然以配置优先：

```yaml
queries:
  - name: PageOrder
    persistence: mp
```

---

# 13. 聚合识别规则

DDL 无法完美识别聚合边界，所以采用“三层识别”。

## 13.1 第一优先级：generator.yml

```yaml
aggregates:
  - name: Order
    rootTable: trade_order
    entityTables:
      - trade_order_item
```

这是最可靠的。

---

## 13.2 第二优先级：COMMENT hint

```sql
COMMENT ON TABLE trade_order IS 'aggregate=Order';
COMMENT ON TABLE trade_order_item IS 'aggregateEntity=OrderItem;root=Order';
```

生成器可以解析 comment 中的键值。

---

## 13.3 第三优先级：命名 + 外键推断

```text
trade_order
trade_order_item
trade_order_payment
```

再结合：

```sql
FOREIGN KEY (order_id) REFERENCES trade_order(id)
```

可以推断：

```text
trade_order 是 root 候选
trade_order_item 是 Order 聚合内实体候选
```

但这里必须给生成报告提示：

```text
[Warning] trade_order_item 被推断为 Order 聚合内实体，请确认。
```

因为外键只说明数据库关联，不代表 DDD 聚合归属。外键很诚实，但它不懂限界上下文。

---

# 14. 生成文件清单

以 `Order` 聚合为例。

## 14.1 client

```text
CreateOrderCommand.java
CreateOrderItemCommand.java
CancelOrderCommand.java
OrderPageQuery.java
OrderDetailResponse.java
OrderPageResponse.java
```

## 14.2 adapter

```text
OrderController.java
OrderWebAssembler.java
```

## 14.3 app

```text
OrderCommandService.java
OrderQueryService.java
CreateOrderCmdExe.java
CancelOrderCmdExe.java
GetOrderDetailQryExe.java
OrderPageQryExe.java
```

## 14.4 domain

```text
Order.java
OrderItem.java
OrderId.java
OrderNo.java
BuyerId.java
Money.java
OrderStatus.java
OrderRepository.java
OrderQueryGateway.java
OrderDomainService.java
OrderCreatedEvent.java
```

## 14.5 infrastructure / JPA

```text
OrderJpaEntity.java
OrderItemJpaEntity.java
OrderJpaRepository.java
OrderJpaConverter.java
OrderRepositoryJpaImpl.java
```

## 14.6 infrastructure / MP

```text
OrderDO.java
OrderItemDO.java
OrderMapper.java
OrderMapper.xml
OrderMpConverter.java
OrderQueryGatewayImpl.java
```

## 14.7 test

```text
ArchitectureTest.java
OrderDomainTest.java
OrderCommandServiceTest.java
OrderRepositoryJpaIntegrationTest.java
OrderMapperIntegrationTest.java
```

补充：

```text
生成工程根目录还需要输出 pom.xml。
src/main/java/com/acme/trade 下需要输出 TradeApplication.java。
如果 aggregate 配置了 entityTables，子实体表也要生成对应的 JpaEntity 和 DO。
Command 对象需要按 DDL 字段补充 Jakarta Validation 注解。
JPA Entity 需要按审计字段补充 @CreatedDate / @LastModifiedDate。
```

---

# 15. 模板规划

```text
src/main/resources/templates
├── adapter
│   ├── Controller.ftl
│   └── WebAssembler.ftl
│
├── client
│   ├── Command.ftl
│   ├── Query.ftl
│   ├── Response.ftl
│   └── PageResponse.ftl
│
├── app
│   ├── CommandService.ftl
│   ├── QueryService.ftl
│   ├── CmdExe.ftl
│   └── QryExe.ftl
│
├── domain
│   ├── Aggregate.ftl
│   ├── Entity.ftl
│   ├── ValueObject.ftl
│   ├── Enum.ftl
│   ├── Repository.ftl
│   ├── QueryGateway.ftl
│   ├── DomainService.ftl
│   └── DomainEvent.ftl
│
├── infrastructure
│   ├── jpa
│   │   ├── JpaEntity.ftl
│   │   ├── JpaRepository.ftl
│   │   ├── JpaConverter.ftl
│   │   └── JpaRepositoryImpl.ftl
│   └── mp
│       ├── DataObject.ftl
│       ├── Mapper.ftl
│       ├── MapperXml.ftl
│       ├── MpConverter.ftl
│       └── QueryGatewayImpl.ftl
│
└── test
    ├── ArchitectureTest.ftl
    ├── DomainTest.ftl
    ├── JpaRepositoryIntegrationTest.ftl
    └── MpMapperIntegrationTest.ftl
```

模板引擎建议 Freemarker。MyBatis Plus 官方生成器也支持 Freemarker、Velocity、Beetl、Enjoy 等模板引擎；但这里不建议直接让 MP
Generator 主导工程生成，因为我们的主目标是 DDD/COLA 分层，不只是生成 MP 的 Entity、Mapper、Service。([MyBatis-Plus][4])

v1 模板必须真实落在生成器工程里，不能只把模板写死在 Java 字符串中。当前模板位置固定为：

```text
backend/pg-ddd-codegen/src/main/resources/templates
```

项目级模板：

```text
backend/pg-ddd-codegen/src/main/resources/templates/project
├── Pom.ftl
└── ApplicationYml.ftl
```

生成器通过 Freemarker 读取模板。允许少量过渡期的 Java 组装逻辑，但新增模板应优先放入该目录，便于后续业务项目复制和定制。

---

# 15.1 Spring Data JPA DDL 安全配置

因为这个生成器是 DDL-first，数据库结构应该由 DDL / Flyway / Liquibase 管理，不能让 Spring Data JPA / Hibernate
在启动时自动刷新数据库结构。

生成项目必须默认输出：

```yaml
spring:
  jpa:
    generate-ddl: false
    open-in-view: false
    hibernate:
      ddl-auto: validate
```

说明：

```text
validate：只校验实体和数据库结构是否匹配，不创建、不更新、不删除表结构。
generate-ddl=false：关闭 JPA 生成 DDL。
open-in-view=false：避免 Web 层拖着持久化上下文。
```

如果业务项目已有自己的 `application.yml`，生成器只能 `CREATE_ONLY` 生成，不应覆盖已有配置；报告中要提醒用户确认等效配置。

---

# 16. 代码规范约束

生成器必须生成架构测试，不然规范会慢慢变成“项目启动会上讲过的那个东西”。

```text
src/test/java/com/acme/trade/architecture/ArchitectureTest.java
```

核心规则：

```java

@AnalyzeClasses(packages = "com.acme.trade")
public class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_outer_layers =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "..adapter..",
                            "..app..",
                            "..infrastructure.."
                    );

    @ArchTest
    static final ArchRule domain_should_not_use_jpa_or_mp =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "jakarta.persistence..",
                            "org.springframework.data.jpa..",
                            "com.baomidou.mybatisplus.."
                    );

    @ArchTest
    static final ArchRule controller_should_not_access_mapper =
            noClasses()
                    .that().resideInAPackage("..adapter.web.controller..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure.persistence.mp.mapper..");

    @ArchTest
    static final ArchRule app_should_not_depend_on_adapter =
            noClasses()
                    .that().resideInAPackage("..app..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..adapter..");

    @ArchTest
    static final ArchRule mapper_should_only_exist_in_mp_package =
            classes()
                    .that().areAssignableTo("com.baomidou.mybatisplus.core.mapper.BaseMapper")
                    .should().resideInAPackage("..infrastructure.persistence.mp.mapper..");
}
```

---

# 17. 覆盖策略

代码生成器必须处理“二次生成”。

## 17.1 可覆盖文件

这些文件由 DDL 直接决定，可以覆盖：

```text
JpaEntity
JpaRepository
DO
Mapper
Mapper.xml 的基础片段
基础 Converter
Command / Query / Response
枚举
ArchUnit Test
```

## 17.2 只生成一次

这些文件开发会改，不建议覆盖：

```text
Aggregate
DomainService
CmdExe
QryExe
Controller
CommandService
QueryService
QueryGatewayImpl 自定义 SQL 逻辑
```

## 17.3 文件索引

生成：

```text
.generated/codegen-index.json
```

示例：

```json
{
  "ddlHash": "9e1f2a",
  "generatedAt": "2026-05-19T00:00:00+08:00",
  "aggregates": [
    {
      "name": "Order",
      "rootTable": "trade_order",
      "files": [
        {
          "path": "domain/model/aggregate/Order.java",
          "policy": "CREATE_ONLY",
          "checksum": "abc"
        },
        {
          "path": "infrastructure/persistence/jpa/entity/OrderJpaEntity.java",
          "policy": "OVERWRITE",
          "checksum": "def"
        },
        {
          "path": "infrastructure/persistence/mp/mapper/xml/OrderMapper.xml",
          "policy": "MERGE",
          "checksum": "ghi"
        }
      ]
    }
  ]
}
```

---

# 18. Mapper XML 合并策略

`Mapper.xml` 很容易被开发改，所以不能粗暴覆盖。

建议分区：

```xml

<mapper namespace="com.acme.trade.infrastructure.persistence.mp.mapper.OrderMapper">

    <!-- AUTO-GENERATED-START: base-columns -->
    <sql id="Base_Column_List">
        id, order_no, buyer_id, status, total_amount, created_at, updated_at
    </sql>
    <!-- AUTO-GENERATED-END: base-columns -->

    <!-- AUTO-GENERATED-START: page-query -->
    <select id="pageOrders" resultType="com.acme.trade.client.response.OrderPageResponse">
        ...
    </select>
    <!-- AUTO-GENERATED-END: page-query -->

    <!-- CUSTOM-START -->
    <!-- 开发自己写的 SQL 放这里 -->
    <!-- CUSTOM-END -->

</mapper>
```

二次生成时只替换：

```text
AUTO-GENERATED-START / AUTO-GENERATED-END
```

不碰：

```text
CUSTOM-START / CUSTOM-END
```

---

# 19. 生成报告

每次生成后输出：

```text
generation-report.md
```

示例：

```text
# Code Generation Report

## DDL Input

- db/migration/V001__init_schema.sql
- db/migration/V002__create_order.sql

## Aggregates

### Order

Root Table:
- trade_order

Entity Tables:
- trade_order_item

Generated Domain Types:
- Order
- OrderItem
- OrderId
- OrderNo
- BuyerId
- Money
- OrderStatus

Persistence Route:
- Command: JPA
- Detail Query: JPA
- Page Query: MP

Detected Fields:
- id: bigserial -> Long -> OrderId
- order_no: varchar(64) -> String -> OrderNo
- status: order_status -> OrderStatus
- total_amount: numeric(18,2) -> BigDecimal -> Money
- version: optimistic lock
- deleted: logic delete
- created_at: audit created time
- updated_at: audit updated time

Warnings:
- trade_order_item was inferred as aggregate entity by FK and naming. Please confirm.
- jsonb fields require TypeHandler if enabled.
- PostgreSQL enum order_status generated as Java enum OrderStatus.
```

---

# 20. v0.1 支持范围

第一版建议支持这些：

```text
1. PostgreSQL DDL 文件输入
2. Flyway migration SQL 顺序输入
3. Shadow PostgreSQL Catalog Mode
4. CREATE TYPE ... AS ENUM
5. CREATE TABLE
6. ALTER TABLE ADD COLUMN
7. ALTER TABLE ADD CONSTRAINT
8. COMMENT ON TABLE
9. COMMENT ON COLUMN
10. CREATE INDEX
11. CREATE UNIQUE INDEX
12. 主键、唯一、外键、非空、CHECK 识别
13. bigserial / identity 主键识别
14. PostgreSQL 常见类型到 Java 类型映射
15. 审计字段识别
16. 逻辑删除字段识别
17. 乐观锁字段识别
18. DDD/COLA 包结构生成
19. JPA Repository 生成
20. MP Mapper + XML 生成
21. ArchUnit 架构测试生成
22. 生成报告
23. 二次生成索引和覆盖策略
24. 生成项目 pom.xml 和启动类
25. Command 参数校验注解
26. JPA 审计注解和安全 DDL 配置
27. 子实体表 JpaEntity / DO 生成
```

---

# 21. v0.1 暂不支持或只弱支持

这些先别硬吃：

```text
1. 分区表完整代码生成
2. 表继承
3. 物化视图生成
4. 复杂表达式索引生成查询逻辑
5. 复杂 CHECK 自动转业务规则
6. 触发器自动转 Java 逻辑
7. 存储过程自动转服务
8. jsonb 深层结构自动生成 Java 类型
9. PostgreSQL array 自动生成强类型集合
10. 多租户规则自动推断
11. 多数据源
12. Saga / 分布式事务
13. 领域事件自动发布
```

这些可以进 v0.2 / v0.3。第一版目标是“稳定生成可维护代码”，不是“一口吃成数据库神谕”。

---

# 22. 推荐技术栈

```text
Java 21+
Spring Boot 3.5.X
Spring Data JPA
MyBatis Plus
PostgreSQL JDBC Driver
Freemarker
ArchUnit
Jackson
Jakarta Validation
Testcontainers 可选
MapStruct 可选
Lombok 可选
```

Catalog Mode 可以有两种实现：

```text
本地 shadow database：适合开发机
Testcontainers PostgreSQL：适合 CI/CD
```

---

# 23. 最终架构图

```text
                 ┌──────────────────────┐
                 │ PostgreSQL DDL Files  │
                 └──────────┬───────────┘
                            │
                            v
                 ┌──────────────────────┐
                 │ DDL Loader/Splitter   │
                 └──────────┬───────────┘
                            │
                            v
                 ┌──────────────────────┐
                 │ Shadow PostgreSQL     │
                 └──────────┬───────────┘
                            │
                            v
        ┌────────────────────────────────────┐
        │ information_schema + pg_catalog     │
        └────────────────┬───────────────────┘
                         │
                         v
                 ┌──────────────────────┐
                 │ PgSchemaModel         │
                 └──────────┬───────────┘
                            │
                            v
                 ┌──────────────────────┐
                 │ DddGenerationModel    │
                 └──────────┬───────────┘
                            │
             ┌──────────────┴──────────────┐
             │                             │
             v                             v
   ┌──────────────────┐          ┌──────────────────┐
   │ JPA Write Model  │          │ MP Query Model    │
   └────────┬─────────┘          └────────┬─────────┘
            │                             │
            v                             v
   Repository Impl                QueryGateway Impl
   JpaEntity                      Mapper
   JpaRepository                  Mapper.xml
            │                             │
            └──────────────┬──────────────┘
                           v
                  ┌─────────────────┐
                  │ Java Source Code │
                  └─────────────────┘
```
