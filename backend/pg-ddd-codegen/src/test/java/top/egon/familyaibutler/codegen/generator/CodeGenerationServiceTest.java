package top.egon.familyaibutler.codegen.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.generator
 * @ClassName: CodeGenerationServiceTest
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: DDD/COLA 代码生成主流程测试
 * @Version: 1.0
 */
class CodeGenerationServiceTest {

    /**
     * 根据 DDL 和 generator.yml 生成 DDD/COLA 分层文件、报告和生成索引。
     *
     * @param tempDir JUnit 临时目录
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldGenerateDddColaProjectAndKeepCustomMapperXmlBlock(@TempDir Path tempDir) throws Exception {
        Path migrationDir = Files.createDirectories(tempDir.resolve("db/migration"));
        Path ddl = migrationDir.resolve("V001__init_schema.sql");
        Files.writeString(ddl, """
                CREATE TYPE order_status AS ENUM ('CREATED', 'PAID', 'CANCELED');
                CREATE TABLE trade_order
                (
                    id           bigserial PRIMARY KEY,
                    order_no     varchar(64)    NOT NULL,
                    buyer_id     bigint         NOT NULL,
                    status       order_status   NOT NULL DEFAULT 'CREATED',
                    total_amount numeric(18, 2) NOT NULL,
                    deleted      boolean        NOT NULL DEFAULT false,
                    version      integer        NOT NULL DEFAULT 0,
                    created_at   timestamptz    NOT NULL DEFAULT now(),
                    updated_at   timestamptz    NOT NULL DEFAULT now(),
                    CONSTRAINT uk_trade_order_order_no UNIQUE (order_no)
                );
                COMMENT ON TABLE trade_order IS 'aggregate=Order;module=trade';
                COMMENT ON COLUMN trade_order.id IS '订单ID;vo=OrderId';
                COMMENT ON COLUMN trade_order.order_no IS '订单号;vo=OrderNo;businessKey=true';
                COMMENT ON COLUMN trade_order.buyer_id IS '买家ID;vo=BuyerId';
                COMMENT ON COLUMN trade_order.total_amount IS '订单金额;vo=Money';
                """);
        Path outputDir = tempDir.resolve("generated-src");
        Path config = tempDir.resolve("generator.yml");
        Files.writeString(config, """
                project:
                  basePackage: com.acme.trade
                  moduleName: trade
                  outputDir: %s
                  javaVersion: 21
                ddl:
                  input:
                    - %s
                  mode: parse
                  postgres:
                    schema: public
                  naming:
                    tablePrefix:
                      - trade_
                    columnToCamel: true
                ddd:
                  boundedContext: trade
                aggregates:
                  - name: Order
                    rootTable: trade_order
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
                    queries:
                      - name: PageOrder
                        path: /orders
                        method: GET
                        persistence: mp
                        filters:
                          - buyer_id
                          - status
                          - created_at
                ignore:
                  tables:
                    - flyway_schema_history
                """.formatted(outputDir, ddl));

        GenerationSummary summary = new CodeGenerationService().generate(config);

        Path aggregate = outputDir.resolve("src/main/java/com/acme/trade/domain/order/model/aggregate/Order.java");
        Path valueObject = outputDir.resolve("src/main/java/com/acme/trade/domain/order/model/valueobject/OrderNo.java");
        Path jpaEntity = outputDir.resolve("src/main/java/com/acme/trade/infrastructure/persistence/jpa/entity/OrderJpaEntity.java");
        Path dataObject = outputDir.resolve("src/main/java/com/acme/trade/infrastructure/persistence/mp/dataobject/OrderDO.java");
        Path mapperXml = outputDir.resolve("src/main/resources/mapper/OrderMapper.xml");
        assertThat(summary.getGeneratedFiles()).isNotEmpty();
        assertThat(aggregate).exists();
        assertThat(valueObject).exists();
        assertThat(jpaEntity).exists();
        assertThat(dataObject).exists();
        assertThat(mapperXml).exists();
        assertThat(Files.readString(jpaEntity)).contains("@Version", "@CreatedDate", "@LastModifiedDate", "private String orderNo;");
        assertThat(Files.readString(dataObject)).contains("@TableLogic", "@Version");
        assertThat(Files.readString(outputDir.resolve("src/main/java/com/acme/trade/application/command/CreateOrderCommand.java")))
                .contains("@NotNull", "@Size(max = 64)", "@Digits(integer = 16, fraction = 2)");
        assertThat(Files.readString(outputDir.resolve("generation-report.md"))).contains("Order", "order_status", "total_amount");
        assertThat(Files.readString(outputDir.resolve(".generated/codegen-index.json"))).contains("CREATE_ONLY", "OVERWRITE");

        String customizedXml = Files.readString(mapperXml).replace("<!-- CUSTOM-END -->", "    <select id=\"customQuery\" resultType=\"java.lang.Long\">SELECT 1</select>\n    <!-- CUSTOM-END -->");
        Files.writeString(mapperXml, customizedXml);
        new CodeGenerationService().generate(config);

        assertThat(Files.readString(mapperXml)).contains("customQuery");
    }

    /**
     * 根据 generator.yml 中的枚举配置生成领域枚举，并让 JPA/MP 存储对象使用 String 承接枚举字段。
     *
     * @param tempDir JUnit 临时目录
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldGenerateCustomEnumAndUseStringInPersistenceObjects(@TempDir Path tempDir) throws Exception {
        Path ddl = tempDir.resolve("schema.sql");
        Files.writeString(ddl, """
                CREATE TABLE trade_order
                (
                    id      bigserial PRIMARY KEY,
                    status  varchar(32) NOT NULL,
                    deleted boolean     NOT NULL DEFAULT false
                );
                """);
        Path outputDir = tempDir.resolve("generated-src");
        Path config = tempDir.resolve("generator.yml");
        Files.writeString(config, """
                project:
                  basePackage: com.acme.trade
                  moduleName: trade
                  outputDir: %s
                  javaVersion: 21
                ddl:
                  input:
                    - %s
                  mode: parse
                  postgres:
                    schema: public
                  naming:
                    tablePrefix:
                      - trade_
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
                aggregates:
                  - name: Order
                    rootTable: trade_order
                    idValueObject: OrderId
                """.formatted(outputDir, ddl));

        new CodeGenerationService().generate(config);

        Path enumFile = outputDir.resolve("src/main/java/com/acme/trade/domain/order/model/enums/OrderStatus.java");
        Path aggregateFile = outputDir.resolve("src/main/java/com/acme/trade/domain/order/model/aggregate/Order.java");
        Path jpaEntity = outputDir.resolve("src/main/java/com/acme/trade/infrastructure/persistence/jpa/entity/OrderJpaEntity.java");
        Path dataObject = outputDir.resolve("src/main/java/com/acme/trade/infrastructure/persistence/mp/dataobject/OrderDO.java");
        assertThat(Files.readString(enumFile)).contains("CREATED(\"CREATED\", \"已创建\")", "getByCode");
        assertThat(Files.readString(aggregateFile)).contains("private OrderStatus status;");
        assertThat(Files.readString(jpaEntity)).contains("private String status;");
        assertThat(Files.readString(dataObject)).contains("private String status;");
    }

    /**
     * 生成项目级 JPA 安全配置，避免 Hibernate 自动更新数据库结构。
     *
     * @param tempDir JUnit 临时目录
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldGenerateJpaSafeApplicationYaml(@TempDir Path tempDir) throws Exception {
        Path ddl = tempDir.resolve("schema.sql");
        Files.writeString(ddl, """
                CREATE TABLE trade_order
                (
                    id bigserial PRIMARY KEY
                );
                """);
        Path outputDir = tempDir.resolve("generated-src");
        Path config = tempDir.resolve("generator.yml");
        Files.writeString(config, """
                project:
                  basePackage: com.acme.trade
                  moduleName: trade
                  outputDir: %s
                  javaVersion: 21
                ddl:
                  input:
                    - %s
                  mode: parse
                  postgres:
                    schema: public
                  naming:
                    tablePrefix:
                      - trade_
                ddd:
                  boundedContext: trade
                aggregates:
                  - name: Order
                    rootTable: trade_order
                    idValueObject: OrderId
                """.formatted(outputDir, ddl));

        new CodeGenerationService().generate(config);

        Path applicationYaml = outputDir.resolve("src/main/resources/application.yml");
        assertThat(Files.readString(applicationYaml)).contains("ddl-auto: validate", "generate-ddl: false", "open-in-view: false");
    }

    /**
     * 模板文件应按需求显式放在 resources/templates 下，便于后续业务项目定制。
     */
    @Test
    void shouldProvideVisibleTemplateFiles() {
        assertThat(Path.of("src/main/resources/templates/domain/Enum.ftl")).exists();
        assertThat(Path.of("src/main/resources/templates/application/Result.ftl")).exists();
        assertThat(Path.of("src/main/resources/templates/adapter/WebDto.ftl")).exists();
        assertThat(Path.of("src/main/resources/templates/infrastructure/jpa/JpaEntity.ftl")).exists();
        assertThat(Path.of("src/main/resources/templates/infrastructure/mp/MapperXml.ftl")).exists();
        assertThat(Path.of("src/main/resources/templates/project/ApplicationYml.ftl")).exists();
    }

    /**
     * 核心模板应承载主要分层代码结构，渲染器只负责准备模型数据。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldKeepCoreLayerStructuresInTemplates() throws Exception {
        Path renderer = Path.of("src/main/java/top/egon/familyaibutler/codegen/generator/DddSourceRenderer.java");
        String rendererContent = Files.readString(renderer);
        assertThat(Files.readString(Path.of("src/main/resources/templates/adapter/Controller.ftl")))
                .contains("@RestController", "@RequestMapping", "${manageInterfaceName}", "${webAssemblerName}", "${methods}");
        assertThat(Files.readString(Path.of("src/main/resources/templates/application/ManageImpl.ftl")))
                .contains("@Service", "implements ${interfaceName}", "${executorFields}", "${methods}");
        assertThat(Files.readString(Path.of("src/main/resources/templates/application/Manage.ftl")))
                .contains("public interface ${className}", "${methods}");
        assertThat(Files.readString(Path.of("src/main/resources/templates/application/CmdExe.ftl")))
                .contains("@Component", "${resultClassName} execute(${commandClassName} command)");
        assertThat(Files.readString(Path.of("src/main/resources/templates/domain/Gateway.ftl")))
                .contains("Optional<${aggregateName}> find", "${aggregateName} save");
        assertThat(Files.readString(Path.of("src/main/resources/templates/infrastructure/jpa/JpaEntity.ftl")))
                .contains("@Entity", "@Table(name = \"${tableName}\")", "${fields}");
        assertThat(Files.readString(Path.of("src/main/resources/templates/infrastructure/mp/DataObject.ftl")))
                .contains("@TableName(\"${tableName}\")", "${fields}");
        assertThat(Files.readString(Path.of("src/main/resources/templates/test/ArchitectureTest.ftl")))
                .contains("@AnalyzeClasses", "ArchRule");
        assertThat(rendererContent)
                .contains("TemplateRegistry.ADAPTER_CONTROLLER")
                .contains("TemplateRegistry.APPLICATION_MANAGE_IMPL")
                .contains("TemplateRegistry.APPLICATION_MANAGE")
                .contains("TemplateRegistry.DOMAIN_GATEWAY")
                .contains("TemplateRegistry.INFRA_JPA_ENTITY")
                .contains("TemplateRegistry.TEST_ARCHITECTURE");
    }

    /**
     * 生成完整 DDD/COLA 分层骨架，包含子实体、DomainService、DomainEvent、测试骨架和工程依赖。
     *
     * @param tempDir JUnit 临时目录
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldGenerateFullLayerStructureDependenciesAndAnnotations(@TempDir Path tempDir) throws Exception {
        Path ddl = tempDir.resolve("schema.sql");
        Files.writeString(ddl, """
                CREATE TABLE trade_order
                (
                    id         bigserial PRIMARY KEY,
                    order_no   varchar(64) NOT NULL,
                    created_at timestamptz NOT NULL DEFAULT now()
                );
                CREATE TABLE trade_order_item
                (
                    id       bigserial PRIMARY KEY,
                    order_id bigint      NOT NULL,
                    sku_name varchar(64) NOT NULL,
                    quantity integer     NOT NULL,
                    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES trade_order (id)
                );
                """);
        Path outputDir = tempDir.resolve("generated-src");
        Path config = tempDir.resolve("generator.yml");
        Files.writeString(config, """
                project:
                  basePackage: com.acme.trade
                  moduleName: trade
                  outputDir: %s
                  javaVersion: 21
                ddl:
                  input:
                    - %s
                  mode: parse
                  postgres:
                    schema: public
                  naming:
                    tablePrefix:
                      - trade_
                ddd:
                  boundedContext: trade
                aggregates:
                  - name: Order
                    rootTable: trade_order
                    entityTables:
                      - trade_order_item
                    idValueObject: OrderId
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
                          - order_no
                          - created_at
                """.formatted(outputDir, ddl));

        new CodeGenerationService().generate(config);

        assertThat(outputDir.resolve("pom.xml")).exists();
        assertThat(Files.readString(outputDir.resolve("pom.xml")))
                .contains("spring-boot-starter-data-jpa", "mybatis-plus-spring-boot3-starter", "archunit-junit5");
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/TradeApplication.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/application/manage/OrderManage.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/application/manage/impl/OrderManageImpl.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/application/command/CreateOrderItemCommand.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/application/query/OrderPageQuery.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/application/result/OrderDetailResult.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/application/result/OrderPageResult.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/application/assembler/OrderApplicationAssembler.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/domain/order/gateway/OrderGateway.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/domain/order/service/OrderDomainService.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/domain/order/event/OrderCreatedEvent.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/infrastructure/persistence/jpa/entity/OrderItemJpaEntity.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/infrastructure/persistence/mp/dataobject/OrderItemDO.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/infrastructure/gateway/impl/OrderGatewayImpl.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/infrastructure/gateway/impl/OrderQueryGatewayImpl.java")).exists();
        assertThat(outputDir.resolve("src/test/java/com/acme/trade/domain/OrderDomainTest.java")).exists();
        assertThat(outputDir.resolve("src/test/java/com/acme/trade/application/OrderManageTest.java")).exists();
        assertThat(outputDir.resolve("src/test/java/com/acme/trade/infrastructure/OrderRepositoryJpaIntegrationTest.java")).exists();
        assertThat(outputDir.resolve("src/test/java/com/acme/trade/infrastructure/OrderMapperIntegrationTest.java")).exists();
        assertThat(Files.readString(outputDir.resolve("src/main/java/com/acme/trade/application/manage/impl/OrderManageImpl.java")))
                .contains("@Service", "implements OrderManage")
                .doesNotContain("public Object ");
        assertThat(Files.readString(outputDir.resolve("src/main/java/com/acme/trade/application/executor/command/CreateOrderCmdExe.java")))
                .contains("@Component", "CreateOrderCommand", "CreateOrderResult execute");
        assertThat(Files.readString(outputDir.resolve("src/main/java/com/acme/trade/application/executor/query/PageOrderQryExe.java")))
                .contains("@Component", "OrderPageQuery", "OrderPageResult execute",
                        "OrderPageCriteria criteria = orderApplicationAssembler.toOrderPageCriteria(query);",
                        "pageOrder(criteria)");
        assertThat(Files.readString(outputDir.resolve("src/main/java/com/acme/trade/domain/order/gateway/OrderQueryGateway.java")))
                .contains("import com.acme.trade.domain.order.gateway.query.OrderPageCriteria;",
                        "import com.acme.trade.domain.order.model.aggregate.Order;",
                        "import java.util.List;",
                        "List<Order> pageOrder(OrderPageCriteria criteria);")
                .doesNotContain("application.query");
        assertThat(Files.readString(outputDir.resolve("src/main/java/com/acme/trade/domain/order/gateway/query/OrderPageCriteria.java")))
                .contains("@Data", "private String orderNo;", "private Instant createdAtStart;", "private Instant createdAtEnd;")
                .doesNotContain("application.query");
        assertThat(Files.readString(outputDir.resolve("src/main/java/com/acme/trade/domain/order/service/OrderDomainService.java")))
                .contains("import org.springframework.stereotype.Service;", "@Service");
        assertThat(Files.readString(outputDir.resolve("src/main/java/com/acme/trade/infrastructure/gateway/impl/OrderQueryGatewayImpl.java")))
                .contains("import com.acme.trade.domain.order.gateway.query.OrderPageCriteria;",
                        "public List<Order> pageOrder(OrderPageCriteria criteria)")
                .doesNotContain("application.query");
        assertThat(Files.readString(outputDir.resolve("src/main/java/com/acme/trade/application/assembler/OrderApplicationAssembler.java")))
                .contains("import com.acme.trade.domain.order.gateway.query.OrderPageCriteria;",
                        "OrderPageCriteria toOrderPageCriteria(OrderPageQuery query)");
        assertThat(Files.readString(outputDir.resolve("src/main/java/com/acme/trade/adapter/web/OrderController.java")))
                .contains("OrderManage", "OrderWebAssembler", "CreateOrderRequestDTO", "CreateOrderCommand", "CreateOrderVO", "@PostMapping", "@GetMapping")
                .doesNotContain("application.manage.impl")
                .doesNotContain("com.acme.trade.application.OrderServiceImpl");
        assertThat(Files.readString(outputDir.resolve("src/main/java/com/acme/trade/adapter/web/assembler/OrderWebAssembler.java")))
                .contains("@Component", "toCreateOrderCommand", "toCreateOrderVO", "toOrderDetailVO", "toOrderPageVO")
                .doesNotContain("com.acme.trade.infrastructure");
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/adapter/web/dto/CreateOrderRequestDTO.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/adapter/web/dto/CreateOrderVO.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/adapter/web/dto/OrderDetailVO.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/adapter/web/dto/OrderPageVO.java")).exists();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/client")).doesNotExist();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/app")).doesNotExist();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/application/dto")).doesNotExist();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/application/OrderServiceI.java")).doesNotExist();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/application/OrderServiceImpl.java")).doesNotExist();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/adapter/OrderController.java")).doesNotExist();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/adapter/assembler")).doesNotExist();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/domain/repository")).doesNotExist();
        assertThat(outputDir.resolve("src/main/java/com/acme/trade/infrastructure/persistence/impl")).doesNotExist();
        assertThat(Files.readString(outputDir.resolve("src/main/resources/mapper/OrderMapper.xml"))).contains("OrderPageResult");
        assertThat(Files.readString(outputDir.resolve("src/test/java/com/acme/trade/architecture/ArchitectureTest.java")))
                .contains("adapter.web", "domain.repository", "infrastructure.persistence.impl",
                        "CONTROLLER_SHOULD_ONLY_RESIDE_IN_ADAPTER_WEB",
                        "\"..application..\"",
                        "com.acme.trade.service..", "com.acme.trade.mapper..", "com.acme.trade.po..",
                        "com.acme.trade.do..", "com.acme.trade.repository..", "..adapter.rpc.grpc..");
    }
}
