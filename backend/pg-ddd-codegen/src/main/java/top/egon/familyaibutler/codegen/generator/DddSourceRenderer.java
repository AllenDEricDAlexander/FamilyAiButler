package top.egon.familyaibutler.codegen.generator;

import top.egon.familyaibutler.codegen.config.GeneratorConfig;
import top.egon.familyaibutler.codegen.schema.PgColumnModel;
import top.egon.familyaibutler.codegen.schema.PgEnumModel;
import top.egon.familyaibutler.codegen.schema.PgTableModel;
import top.egon.familyaibutler.codegen.template.FreemarkerTemplateEngine;
import top.egon.familyaibutler.codegen.template.TemplateEngine;
import top.egon.familyaibutler.codegen.template.TemplateRegistry;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.generator
 * @ClassName: DddSourceRenderer
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: DDD/COLA 分层 Java 源码渲染器
 * @Version: 1.0
 */
public class DddSourceRenderer {
    private final TemplateEngine templateEngine = new FreemarkerTemplateEngine();

    /**
     * 渲染单聚合的全部 v1 文件。
     *
     * @param context 生成上下文
     * @return 生成文件列表
     */
    public List<GeneratedSourceFile> render(GenerationContext context) {
        List<GeneratedSourceFile> files = new ArrayList<>();
        files.addAll(renderDomain(context));
        files.addAll(renderClient(context));
        files.addAll(renderApp(context));
        files.addAll(renderAdapter(context));
        files.addAll(renderJpa(context));
        files.addAll(renderMp(context));
        files.add(renderArchitectureTest(context));
        return files;
    }

    /**
     * 渲染项目级资源文件。
     *
     * @param config 生成器配置
     * @return 生成文件列表
     */
    public List<GeneratedSourceFile> renderProjectResources(GeneratorConfig config) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("applicationName", config.getProject().getModuleName());
        model.put("artifactId", config.getProject().getModuleName());
        model.put("javaVersion", config.getProject().getJavaVersion());
        model.put("basePackage", config.getProject().getBasePackage());
        String applicationClassName = new Naming(List.of()).upperCamel(config.getProject().getModuleName()) + "Application";
        return List.of(
                resourceFile("pom.xml", templateEngine.render(TemplateRegistry.PROJECT_POM, model), WritePolicy.CREATE_ONLY),
                resourceFile("src/main/resources/application.yml", templateEngine.render(TemplateRegistry.PROJECT_APPLICATION_YML, model), WritePolicy.CREATE_ONLY),
                GeneratedSourceFile.builder()
                        .relativePath(Path.of("src/main/java").resolve(config.getProject().getBasePackage().replace('.', '/')).resolve(applicationClassName + ".java"))
                        .content(renderApplication(config, applicationClassName))
                        .policy(WritePolicy.CREATE_ONLY)
                        .build()
        );
    }

    /**
     * 渲染 Spring Boot 启动类。
     *
     * @param config    生成器配置
     * @param className 类名
     * @return Java 源码
     */
    private String renderApplication(GeneratorConfig config, String className) {
        String packageName = config.getProject().getBasePackage();
        return packageLine(packageName)
                + "import org.springframework.boot.SpringApplication;\n"
                + "import org.springframework.boot.autoconfigure.SpringBootApplication;\n\n"
                + classComment(packageName, className, config.getProject().getModuleName() + " 启动类")
                + "@SpringBootApplication\n"
                + "public class " + className + " {\n\n"
                + "    /**\n"
                + "     * 应用启动入口。\n"
                + "     *\n"
                + "     * @param args 启动参数\n"
                + "     */\n"
                + "    public static void main(String[] args) {\n"
                + "        SpringApplication.run(" + className + ".class, args);\n"
                + "    }\n"
                + "}\n";
    }

    /**
     * 渲染领域层文件。
     *
     * @param context 生成上下文
     * @return 生成文件列表
     */
    private List<GeneratedSourceFile> renderDomain(GenerationContext context) {
        List<GeneratedSourceFile> files = new ArrayList<>();
        String aggregateName = context.getAggregate().getName();
        files.add(javaFile(context, "domain/model/aggregate/" + aggregateName + ".java", renderPojo(
                context, "domain.model.aggregate", aggregateName, context.getRootTable(), WritePolicy.CREATE_ONLY), WritePolicy.CREATE_ONLY));
        for (PgTableModel entityTable : context.getEntityTables()) {
            String entityName = context.getNaming().tableToClass(entityTable.getTableName());
            files.add(javaFile(context, "domain/model/entity/" + entityName + ".java", renderPojo(
                    context, "domain.model.entity", entityName, entityTable, WritePolicy.CREATE_ONLY), WritePolicy.CREATE_ONLY));
        }
        for (Map.Entry<String, PgColumnModel> entry : valueObjectColumns(context).entrySet()) {
            files.add(javaFile(context, "domain/model/valueobject/" + entry.getKey() + ".java", renderValueObject(context, entry.getKey(), entry.getValue()), WritePolicy.OVERWRITE));
        }
        for (PgEnumModel enumModel : context.getSchemaModel().getEnums()) {
            String enumName = enumModel.getJavaName() == null ? context.getNaming().upperCamel(enumModel.getTypeName()) : enumModel.getJavaName();
            files.add(javaFile(context, "domain/model/enums/" + enumName + ".java", renderEnum(context, enumName, enumModel), WritePolicy.OVERWRITE));
        }
        files.add(javaFile(context, "domain/repository/" + aggregateName + "Repository.java", renderRepository(context), WritePolicy.CREATE_ONLY));
        files.add(javaFile(context, "domain/gateway/" + aggregateName + "QueryGateway.java", renderQueryGateway(context), WritePolicy.CREATE_ONLY));
        files.add(javaFile(context, "domain/service/" + aggregateName + "DomainService.java", renderSimpleComponent(context, "domain.service", aggregateName + "DomainService", aggregateName + " 领域服务"), WritePolicy.CREATE_ONLY));
        files.add(javaFile(context, "domain/event/" + aggregateName + "CreatedEvent.java", renderDomainEvent(context), WritePolicy.CREATE_ONLY));
        return files;
    }

    /**
     * 渲染 client 层文件。
     *
     * @param context 生成上下文
     * @return 生成文件列表
     */
    private List<GeneratedSourceFile> renderClient(GenerationContext context) {
        List<GeneratedSourceFile> files = new ArrayList<>();
        for (GeneratorConfig.UseCaseConfig command : context.getAggregate().getCommands()) {
            files.add(javaFile(context, "client/command/" + command.getName() + "Command.java", renderCommand(context, command), WritePolicy.OVERWRITE));
            files.addAll(renderEntityCommands(context, command));
        }
        for (GeneratorConfig.QueryConfig query : context.getAggregate().getQueries()) {
            String queryClass = queryClassName(query);
            String responseClass = responseClassName(query);
            files.add(javaFile(context, "client/query/" + queryClass + ".java", renderQuery(context, query, queryClass), WritePolicy.OVERWRITE));
            files.add(javaFile(context, "client/response/" + responseClass + ".java", renderResponse(context, responseClass), WritePolicy.OVERWRITE));
        }
        return files;
    }

    /**
     * 渲染应用层文件。
     *
     * @param context 生成上下文
     * @return 生成文件列表
     */
    private List<GeneratedSourceFile> renderApp(GenerationContext context) {
        List<GeneratedSourceFile> files = new ArrayList<>();
        String aggregateName = context.getAggregate().getName();
        files.add(javaFile(context, "app/service/" + aggregateName + "CommandService.java", renderAppService(context, aggregateName + "CommandService"), WritePolicy.CREATE_ONLY));
        files.add(javaFile(context, "app/service/" + aggregateName + "QueryService.java", renderAppService(context, aggregateName + "QueryService"), WritePolicy.CREATE_ONLY));
        for (GeneratorConfig.UseCaseConfig command : context.getAggregate().getCommands()) {
            files.add(javaFile(context, "app/executor/command/" + command.getName() + "CmdExe.java", renderExecutor(context, "command", command.getName() + "CmdExe"), WritePolicy.CREATE_ONLY));
        }
        for (GeneratorConfig.QueryConfig query : context.getAggregate().getQueries()) {
            files.add(javaFile(context, "app/executor/query/" + query.getName() + "QryExe.java", renderExecutor(context, "query", query.getName() + "QryExe"), WritePolicy.CREATE_ONLY));
        }
        return files;
    }

    /**
     * 渲染 adapter 层文件。
     *
     * @param context 生成上下文
     * @return 生成文件列表
     */
    private List<GeneratedSourceFile> renderAdapter(GenerationContext context) {
        String aggregateName = context.getAggregate().getName();
        List<GeneratedSourceFile> files = new ArrayList<>();
        files.add(javaFile(context, "adapter/web/controller/" + aggregateName + "Controller.java", renderController(context), WritePolicy.CREATE_ONLY));
        files.add(javaFile(context, "adapter/web/assembler/" + aggregateName + "WebAssembler.java", renderAssembler(context), WritePolicy.CREATE_ONLY));
        return files;
    }

    /**
     * 渲染 JPA 持久层文件。
     *
     * @param context 生成上下文
     * @return 生成文件列表
     */
    private List<GeneratedSourceFile> renderJpa(GenerationContext context) {
        String aggregateName = context.getAggregate().getName();
        List<GeneratedSourceFile> files = new ArrayList<>();
        files.add(javaFile(context, "infrastructure/persistence/jpa/entity/" + aggregateName + "JpaEntity.java", renderJpaEntity(context), WritePolicy.OVERWRITE));
        for (PgTableModel entityTable : context.getEntityTables()) {
            String entityName = context.getNaming().tableToClass(entityTable.getTableName());
            files.add(javaFile(context, "infrastructure/persistence/jpa/entity/" + entityName + "JpaEntity.java", renderJpaEntity(context, entityName + "JpaEntity", entityTable), WritePolicy.OVERWRITE));
        }
        files.add(javaFile(context, "infrastructure/persistence/jpa/repository/" + aggregateName + "JpaRepository.java", renderJpaRepository(context), WritePolicy.OVERWRITE));
        files.add(javaFile(context, "infrastructure/persistence/jpa/converter/" + aggregateName + "JpaConverter.java", renderComponent(context, "infrastructure.persistence.jpa.converter", aggregateName + "JpaConverter", "JPA Entity 与领域模型转换器"), WritePolicy.OVERWRITE));
        files.add(javaFile(context, "infrastructure/persistence/impl/" + aggregateName + "RepositoryJpaImpl.java", renderJpaRepositoryImpl(context), WritePolicy.CREATE_ONLY));
        return files;
    }

    /**
     * 渲染 MyBatis Plus 持久层文件。
     *
     * @param context 生成上下文
     * @return 生成文件列表
     */
    private List<GeneratedSourceFile> renderMp(GenerationContext context) {
        String aggregateName = context.getAggregate().getName();
        List<GeneratedSourceFile> files = new ArrayList<>();
        files.add(javaFile(context, "infrastructure/persistence/mp/dataobject/" + aggregateName + "DO.java", renderMpDo(context), WritePolicy.OVERWRITE));
        for (PgTableModel entityTable : context.getEntityTables()) {
            String entityName = context.getNaming().tableToClass(entityTable.getTableName());
            files.add(javaFile(context, "infrastructure/persistence/mp/dataobject/" + entityName + "DO.java", renderMpDo(context, entityName + "DO", entityTable), WritePolicy.OVERWRITE));
        }
        files.add(javaFile(context, "infrastructure/persistence/mp/mapper/" + aggregateName + "Mapper.java", renderMpMapper(context), WritePolicy.OVERWRITE));
        files.add(resourceFile("src/main/resources/mapper/" + aggregateName + "Mapper.xml", renderMapperXml(context), WritePolicy.MERGE));
        files.add(javaFile(context, "infrastructure/persistence/mp/converter/" + aggregateName + "MpConverter.java", renderComponent(context, "infrastructure.persistence.mp.converter", aggregateName + "MpConverter", "MyBatis Plus DO 与响应模型转换器"), WritePolicy.OVERWRITE));
        files.add(javaFile(context, "infrastructure/gatewayimpl/" + aggregateName + "QueryGatewayImpl.java", renderQueryGatewayImpl(context), WritePolicy.CREATE_ONLY));
        files.addAll(renderTests(context));
        return files;
    }

    /**
     * 渲染普通 Lombok POJO。
     *
     * @param context    生成上下文
     * @param subPackage 子包名
     * @param className  类名
     * @param table      表模型
     * @param policy     写入策略
     * @return Java 源码
     */
    private String renderPojo(GenerationContext context, String subPackage, String className, PgTableModel table, WritePolicy policy) {
        String packageName = context.getConfig().getProject().getBasePackage() + "." + subPackage;
        return packageLine(packageName)
                + imports(importsFor(table, context, false), "lombok.Data")
                + classComment(packageName, className, table.getTableName() + " 领域模型，写入策略 " + policy)
                + "@Data\n"
                + "public class " + className + " {\n"
                + fields(context, table, false)
                + "}\n";
    }

    /**
     * 渲染值对象 record。
     *
     * @param context   生成上下文
     * @param className 类名
     * @param column    字段模型
     * @return Java 源码
     */
    private String renderValueObject(GenerationContext context, String className, PgColumnModel column) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".domain.model.valueobject";
        String valueType = context.getTypeMapper().toValueObjectType(column);
        Set<String> importSet = importsForType(valueType);
        return packageLine(packageName)
                + imports(importSet)
                + classComment(packageName, className, column.getColumnName() + " 值对象")
                + "public record " + className + "(" + valueType + " value) {\n\n"
                + "    /**\n"
                + "     * 校验值对象基础合法性。\n"
                + "     */\n"
                + "    public " + className + " {\n"
                + "        if (value == null) {\n"
                + "            throw new IllegalArgumentException(\"" + className + "不能为空\");\n"
                + "        }\n"
                + "    }\n"
                + "}\n";
    }

    /**
     * 渲染 Java enum。
     *
     * @param context   生成上下文
     * @param className 类名
     * @param enumModel 枚举模型
     * @return Java 源码
     */
    private String renderEnum(GenerationContext context, String className, PgEnumModel enumModel) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".domain.model.enums";
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("packageName", packageName);
        model.put("className", className);
        model.put("classComment", classComment(packageName, className, enumModel.getTypeName() + " 领域枚举"));
        model.put("detailed", !enumModel.getValueCodes().isEmpty() || !enumModel.getValueDescriptions().isEmpty());
        model.put("values", enumValues(enumModel));
        return templateEngine.render(TemplateRegistry.DOMAIN_ENUM, model);
    }

    /**
     * 渲染领域事件。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderDomainEvent(GenerationContext context) {
        String aggregateName = context.getAggregate().getName();
        String className = aggregateName + "CreatedEvent";
        String packageName = context.getConfig().getProject().getBasePackage() + ".domain.event";
        return packageLine(packageName)
                + classComment(packageName, className, aggregateName + " 创建领域事件")
                + "public record " + className + "() {\n"
                + "}\n";
    }

    /**
     * 渲染领域 Repository 接口。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderRepository(GenerationContext context) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String aggregateName = context.getAggregate().getName();
        String idType = context.getAggregate().getIdValueObject() == null ? "Long" : context.getAggregate().getIdValueObject();
        String packageName = basePackage + ".domain.repository";
        return packageLine(packageName)
                + "import " + basePackage + ".domain.model.aggregate." + aggregateName + ";\n"
                + (idType.equals("Long") ? "" : "import " + basePackage + ".domain.model.valueobject." + idType + ";\n")
                + "import java.util.Optional;\n\n"
                + classComment(packageName, aggregateName + "Repository", aggregateName + " 领域仓储接口")
                + "public interface " + aggregateName + "Repository {\n\n"
                + "    /**\n"
                + "     * 按主键查找聚合。\n"
                + "     *\n"
                + "     * @param id 聚合主键\n"
                + "     * @return 聚合对象\n"
                + "     */\n"
                + "    Optional<" + aggregateName + "> find(" + idType + " id);\n\n"
                + "    /**\n"
                + "     * 保存聚合。\n"
                + "     *\n"
                + "     * @param aggregate 聚合对象\n"
                + "     * @return 保存后的聚合\n"
                + "     */\n"
                + "    " + aggregateName + " save(" + aggregateName + " aggregate);\n"
                + "}\n";
    }

    /**
     * 渲染查询网关接口。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderQueryGateway(GenerationContext context) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".domain.gateway";
        String aggregateName = context.getAggregate().getName();
        return packageLine(packageName)
                + "import java.util.List;\n\n"
                + classComment(packageName, aggregateName + "QueryGateway", aggregateName + " 复杂查询网关")
                + "public interface " + aggregateName + "QueryGateway {\n\n"
                + "    /**\n"
                + "     * 执行复杂查询。\n"
                + "     *\n"
                + "     * @param query 查询对象\n"
                + "     * @return 查询结果\n"
                + "     */\n"
                + "    List<?> query(Object query);\n"
                + "}\n";
    }

    /**
     * 渲染 Command record。
     *
     * @param context 生成上下文
     * @param command Command 配置
     * @return Java 源码
     */
    private String renderCommand(GenerationContext context, GeneratorConfig.UseCaseConfig command) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".client.command";
        StringBuilder fields = new StringBuilder();
        for (PgColumnModel column : businessColumns(context.getRootTable())) {
            appendValidatedRecordField(context, fields, column);
        }
        String body = trimRecordFields(fields);
        return packageLine(packageName)
                + imports(importsFor(context.getRootTable(), context, false), "jakarta.validation.constraints.*")
                + classComment(packageName, command.getName() + "Command", command.getName() + " 命令对象")
                + "public record " + command.getName() + "Command(\n" + body + "\n) {\n"
                + "}\n";
    }

    /**
     * 渲染 Query record。
     *
     * @param context   生成上下文
     * @param query     Query 配置
     * @param className 类名
     * @return Java 源码
     */
    private String renderQuery(GenerationContext context, GeneratorConfig.QueryConfig query, String className) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".client.query";
        StringBuilder fields = new StringBuilder();
        for (String filter : query.getFilters()) {
            context.getRootTable().getColumn(filter).ifPresent(column -> appendQueryField(context, fields, column));
        }
        String body = trimRecordFields(fields);
        return packageLine(packageName)
                + imports(importsFor(context.getRootTable(), context, false))
                + classComment(packageName, className, query.getName() + " 查询对象")
                + "public record " + className + "(\n" + body + "\n) {\n"
                + "}\n";
    }

    /**
     * 渲染 Response record。
     *
     * @param context   生成上下文
     * @param className 类名
     * @return Java 源码
     */
    private String renderResponse(GenerationContext context, String className) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".client.response";
        StringBuilder fields = new StringBuilder();
        for (PgColumnModel column : context.getRootTable().getColumns()) {
            fields.append("        ").append(context.getTypeMapper().toJavaType(column, context.getNaming()))
                    .append(" ").append(context.getNaming().columnToField(column.getColumnName())).append(",\n");
        }
        return packageLine(packageName)
                + imports(importsFor(context.getRootTable(), context, false))
                + classComment(packageName, className, className + " 响应对象")
                + "public record " + className + "(\n" + trimRecordFields(fields) + "\n) {\n"
                + "}\n";
    }

    /**
     * 渲染创建聚合时的子实体 Command。
     *
     * @param context 生成上下文
     * @param command Command 配置
     * @return 子实体 Command 文件
     */
    private List<GeneratedSourceFile> renderEntityCommands(GenerationContext context, GeneratorConfig.UseCaseConfig command) {
        if (!command.getName().startsWith("Create")) {
            return List.of();
        }
        List<GeneratedSourceFile> files = new ArrayList<>();
        for (PgTableModel entityTable : context.getEntityTables()) {
            String entityName = context.getNaming().tableToClass(entityTable.getTableName());
            String className = "Create" + entityName + "Command";
            files.add(javaFile(context, "client/command/" + className + ".java", renderEntityCommand(context, className, entityTable), WritePolicy.OVERWRITE));
        }
        return files;
    }

    /**
     * 渲染子实体 Command record。
     *
     * @param context   生成上下文
     * @param className 类名
     * @param table     表模型
     * @return Java 源码
     */
    private String renderEntityCommand(GenerationContext context, String className, PgTableModel table) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".client.command";
        StringBuilder fields = new StringBuilder();
        for (PgColumnModel column : businessColumns(table)) {
            appendValidatedRecordField(context, fields, column);
        }
        return packageLine(packageName)
                + imports(importsFor(table, context, false), "jakarta.validation.constraints.*")
                + classComment(packageName, className, className + " 子实体命令对象")
                + "public record " + className + "(\n" + trimRecordFields(fields) + "\n) {\n"
                + "}\n";
    }

    /**
     * 渲染应用服务骨架。
     *
     * @param context   生成上下文
     * @param className 类名
     * @return Java 源码
     */
    private String renderAppService(GenerationContext context, String className) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".app.service";
        return packageLine(packageName)
                + "import org.springframework.stereotype.Service;\n\n"
                + classComment(packageName, className, className + " 应用服务")
                + "@Service\n"
                + "public class " + className + " {\n"
                + "}\n";
    }

    /**
     * 渲染执行器骨架。
     *
     * @param context   生成上下文
     * @param type      执行器类型
     * @param className 类名
     * @return Java 源码
     */
    private String renderExecutor(GenerationContext context, String type, String className) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".app.executor." + type;
        return packageLine(packageName)
                + "import org.springframework.stereotype.Component;\n\n"
                + classComment(packageName, className, className + " 用例执行器")
                + "@Component\n"
                + "public class " + className + " {\n"
                + "}\n";
    }

    /**
     * 渲染 Controller 骨架。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderController(GenerationContext context) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".adapter.web.controller";
        String aggregateName = context.getAggregate().getName();
        return packageLine(packageName)
                + "import org.springframework.web.bind.annotation.RequestMapping;\n"
                + "import org.springframework.web.bind.annotation.RestController;\n\n"
                + classComment(packageName, aggregateName + "Controller", aggregateName + " Web 控制层")
                + "@RestController\n"
                + "@RequestMapping(\"/" + context.getNaming().classToField(aggregateName) + "s\")\n"
                + "public class " + aggregateName + "Controller {\n"
                + "}\n";
    }

    /**
     * 渲染 Web Assembler 骨架。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderAssembler(GenerationContext context) {
        String aggregateName = context.getAggregate().getName();
        return renderComponent(context, "adapter.web.assembler", aggregateName + "WebAssembler", aggregateName + " Web 装配器");
    }

    /**
     * 渲染 JPA Entity。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderJpaEntity(GenerationContext context) {
        return renderJpaEntity(context, context.getAggregate().getName() + "JpaEntity", context.getRootTable());
    }

    /**
     * 渲染指定表的 JPA Entity。
     *
     * @param context   生成上下文
     * @param className 类名
     * @param table     表模型
     * @return Java 源码
     */
    private String renderJpaEntity(GenerationContext context, String className, PgTableModel table) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".infrastructure.persistence.jpa.entity";
        List<String> auditImports = hasAuditFields(table)
                ? List.of("org.springframework.data.annotation.CreatedDate", "org.springframework.data.annotation.LastModifiedDate", "org.springframework.data.jpa.domain.support.AuditingEntityListener")
                : List.of();
        Set<String> imports = importsFor(table, context, true);
        imports.addAll(auditImports);
        return packageLine(packageName)
                + imports(imports, "jakarta.persistence.*", "lombok.Data")
                + classComment(packageName, className, table.getTableName() + " JPA 实体")
                + "@Data\n"
                + "@Entity\n"
                + (hasAuditFields(table) ? "@EntityListeners(AuditingEntityListener.class)\n" : "")
                + "@Table(name = \"" + table.getTableName() + "\")\n"
                + "public class " + className + " {\n"
                + jpaFields(context, table)
                + "}\n";
    }

    /**
     * 渲染 Spring Data JPA Repository。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderJpaRepository(GenerationContext context) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".infrastructure.persistence.jpa.repository";
        String aggregateName = context.getAggregate().getName();
        String pkType = context.getRootTable().getPrimaryKeyColumn()
                .map(column -> context.getTypeMapper().toJavaType(column, context.getNaming()))
                .orElse("Long");
        return packageLine(packageName)
                + "import " + context.getConfig().getProject().getBasePackage() + ".infrastructure.persistence.jpa.entity." + aggregateName + "JpaEntity;\n"
                + "import org.springframework.data.jpa.repository.JpaRepository;\n\n"
                + classComment(packageName, aggregateName + "JpaRepository", aggregateName + " JPA Repository")
                + "public interface " + aggregateName + "JpaRepository extends JpaRepository<" + aggregateName + "JpaEntity, " + pkType + "> {\n"
                + "}\n";
    }

    /**
     * 渲染 JPA Repository 实现骨架。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderJpaRepositoryImpl(GenerationContext context) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String aggregateName = context.getAggregate().getName();
        String idType = context.getAggregate().getIdValueObject() == null ? "Long" : context.getAggregate().getIdValueObject();
        String packageName = basePackage + ".infrastructure.persistence.impl";
        return packageLine(packageName)
                + "import " + basePackage + ".domain.model.aggregate." + aggregateName + ";\n"
                + (idType.equals("Long") ? "" : "import " + basePackage + ".domain.model.valueobject." + idType + ";\n")
                + "import " + basePackage + ".domain.repository." + aggregateName + "Repository;\n"
                + "import lombok.RequiredArgsConstructor;\n"
                + "import org.springframework.stereotype.Repository;\n"
                + "import java.util.Optional;\n\n"
                + classComment(packageName, aggregateName + "RepositoryJpaImpl", aggregateName + " JPA 仓储实现")
                + "@Repository\n"
                + "@RequiredArgsConstructor\n"
                + "public class " + aggregateName + "RepositoryJpaImpl implements " + aggregateName + "Repository {\n\n"
                + "    /**\n"
                + "     * 按主键查找聚合。\n"
                + "     *\n"
                + "     * @param id 聚合主键\n"
                + "     * @return 聚合对象\n"
                + "     */\n"
                + "    @Override\n"
                + "    public Optional<" + aggregateName + "> find(" + idType + " id) {\n"
                + "        return Optional.empty();\n"
                + "    }\n\n"
                + "    /**\n"
                + "     * 保存聚合。\n"
                + "     *\n"
                + "     * @param aggregate 聚合对象\n"
                + "     * @return 保存后的聚合\n"
                + "     */\n"
                + "    @Override\n"
                + "    public " + aggregateName + " save(" + aggregateName + " aggregate) {\n"
                + "        return aggregate;\n"
                + "    }\n"
                + "}\n";
    }

    /**
     * 渲染 MP DO。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderMpDo(GenerationContext context) {
        return renderMpDo(context, context.getAggregate().getName() + "DO", context.getRootTable());
    }

    /**
     * 渲染指定表的 MP DO。
     *
     * @param context   生成上下文
     * @param className 类名
     * @param table     表模型
     * @return Java 源码
     */
    private String renderMpDo(GenerationContext context, String className, PgTableModel table) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".infrastructure.persistence.mp.dataobject";
        return packageLine(packageName)
                + imports(importsFor(table, context, true), "com.baomidou.mybatisplus.annotation.*", "lombok.Data")
                + classComment(packageName, className, table.getTableName() + " MyBatis Plus 数据对象")
                + "@Data\n"
                + "@TableName(\"" + table.getTableName() + "\")\n"
                + "public class " + className + " {\n"
                + mpFields(context, table)
                + "}\n";
    }

    /**
     * 渲染 MP Mapper。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderMpMapper(GenerationContext context) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String aggregateName = context.getAggregate().getName();
        String packageName = basePackage + ".infrastructure.persistence.mp.mapper";
        return packageLine(packageName)
                + "import " + basePackage + ".infrastructure.persistence.mp.dataobject." + aggregateName + "DO;\n"
                + "import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n"
                + "import org.apache.ibatis.annotations.Mapper;\n\n"
                + classComment(packageName, aggregateName + "Mapper", aggregateName + " MyBatis Plus Mapper")
                + "@Mapper\n"
                + "public interface " + aggregateName + "Mapper extends BaseMapper<" + aggregateName + "DO> {\n"
                + "}\n";
    }

    /**
     * 渲染 Mapper XML。
     *
     * @param context 生成上下文
     * @return Mapper XML
     */
    private String renderMapperXml(GenerationContext context) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String aggregateName = context.getAggregate().getName();
        String columns = String.join(", ", context.getRootTable().getColumns().stream().map(PgColumnModel::getColumnName).toList());
        String responseClass = context.getAggregate().getQueries().stream()
                .filter(query -> query.getName().startsWith("Page"))
                .findFirst()
                .map(this::responseClassName)
                .orElse(aggregateName + "PageResponse");
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
                + "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n"
                + "<mapper namespace=\"" + basePackage + ".infrastructure.persistence.mp.mapper." + aggregateName + "Mapper\">\n\n"
                + "    <!-- AUTO-GENERATED-START: base-columns -->\n"
                + "    <sql id=\"Base_Column_List\">\n"
                + "        " + columns + "\n"
                + "    </sql>\n"
                + "    <!-- AUTO-GENERATED-END: base-columns -->\n\n"
                + "    <!-- AUTO-GENERATED-START: page-query -->\n"
                + "    <select id=\"page" + aggregateName + "s\" resultType=\"" + basePackage + ".client.response." + responseClass + "\">\n"
                + "        SELECT\n"
                + "        <include refid=\"Base_Column_List\" />\n"
                + "        FROM " + context.getRootTable().getTableName() + "\n"
                + "        WHERE 1 = 1\n"
                + logicDeleteSql(context)
                + "    </select>\n"
                + "    <!-- AUTO-GENERATED-END: page-query -->\n\n"
                + "    <!-- CUSTOM-START -->\n"
                + "    <!-- 开发自己写的 SQL 放这里 -->\n"
                + "    <!-- CUSTOM-END -->\n\n"
                + "</mapper>\n";
    }

    /**
     * 渲染查询网关实现骨架。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderQueryGatewayImpl(GenerationContext context) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String aggregateName = context.getAggregate().getName();
        String packageName = basePackage + ".infrastructure.gatewayimpl";
        return packageLine(packageName)
                + "import " + basePackage + ".domain.gateway." + aggregateName + "QueryGateway;\n"
                + "import lombok.RequiredArgsConstructor;\n"
                + "import org.springframework.stereotype.Repository;\n"
                + "import java.util.List;\n\n"
                + classComment(packageName, aggregateName + "QueryGatewayImpl", aggregateName + " MP 查询网关实现")
                + "@Repository\n"
                + "@RequiredArgsConstructor\n"
                + "public class " + aggregateName + "QueryGatewayImpl implements " + aggregateName + "QueryGateway {\n\n"
                + "    /**\n"
                + "     * 执行复杂查询。\n"
                + "     *\n"
                + "     * @param query 查询对象\n"
                + "     * @return 查询结果\n"
                + "     */\n"
                + "    @Override\n"
                + "    public List<?> query(Object query) {\n"
                + "        return List.of();\n"
                + "    }\n"
                + "}\n";
    }

    /**
     * 渲染聚合测试骨架。
     *
     * @param context 生成上下文
     * @return 测试文件列表
     */
    private List<GeneratedSourceFile> renderTests(GenerationContext context) {
        String aggregateName = context.getAggregate().getName();
        List<GeneratedSourceFile> files = new ArrayList<>();
        files.add(javaTestFile(context, "domain/" + aggregateName + "DomainTest.java", renderTestClass(context, "domain", aggregateName + "DomainTest", aggregateName + " 领域模型测试"), WritePolicy.CREATE_ONLY));
        files.add(javaTestFile(context, "app/" + aggregateName + "CommandServiceTest.java", renderTestClass(context, "app", aggregateName + "CommandServiceTest", aggregateName + " 命令服务测试"), WritePolicy.CREATE_ONLY));
        files.add(javaTestFile(context, "infrastructure/" + aggregateName + "RepositoryJpaIntegrationTest.java", renderTestClass(context, "infrastructure", aggregateName + "RepositoryJpaIntegrationTest", aggregateName + " JPA 仓储集成测试"), WritePolicy.CREATE_ONLY));
        files.add(javaTestFile(context, "infrastructure/" + aggregateName + "MapperIntegrationTest.java", renderTestClass(context, "infrastructure", aggregateName + "MapperIntegrationTest", aggregateName + " Mapper 集成测试"), WritePolicy.CREATE_ONLY));
        return files;
    }

    /**
     * 渲染 JUnit 测试类骨架。
     *
     * @param context     生成上下文
     * @param subPackage  子包
     * @param className   类名
     * @param description 描述
     * @return Java 源码
     */
    private String renderTestClass(GenerationContext context, String subPackage, String className, String description) {
        String packageName = context.getConfig().getProject().getBasePackage() + "." + subPackage;
        return packageLine(packageName)
                + "import org.junit.jupiter.api.Test;\n\n"
                + classComment(packageName, className, description)
                + "class " + className + " {\n\n"
                + "    /**\n"
                + "     * 保留基础测试入口，业务逻辑落地后补充具体断言。\n"
                + "     */\n"
                + "    @Test\n"
                + "    void contextLoads() {\n"
                + "    }\n"
                + "}\n";
    }

    /**
     * 渲染简单组件骨架。
     *
     * @param context     生成上下文
     * @param subPackage  子包
     * @param className   类名
     * @param description 描述
     * @return Java 源码
     */
    private String renderSimpleComponent(GenerationContext context, String subPackage, String className, String description) {
        String packageName = context.getConfig().getProject().getBasePackage() + "." + subPackage;
        return packageLine(packageName)
                + classComment(packageName, className, description)
                + "public class " + className + " {\n"
                + "}\n";
    }

    /**
     * 渲染 Spring Component 骨架。
     *
     * @param context     生成上下文
     * @param subPackage  子包
     * @param className   类名
     * @param description 描述
     * @return Java 源码
     */
    private String renderComponent(GenerationContext context, String subPackage, String className, String description) {
        String packageName = context.getConfig().getProject().getBasePackage() + "." + subPackage;
        return packageLine(packageName)
                + "import org.springframework.stereotype.Component;\n\n"
                + classComment(packageName, className, description)
                + "@Component\n"
                + "public class " + className + " {\n"
                + "}\n";
    }

    /**
     * 渲染架构约束测试。
     *
     * @param context 生成上下文
     * @return 生成文件
     */
    private GeneratedSourceFile renderArchitectureTest(GenerationContext context) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String packageName = basePackage + ".architecture";
        String content = packageLine(packageName)
                + "import com.tngtech.archunit.junit.AnalyzeClasses;\n"
                + "import com.tngtech.archunit.junit.ArchTest;\n"
                + "import com.tngtech.archunit.lang.ArchRule;\n\n"
                + "import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;\n"
                + "import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;\n\n"
                + classComment(packageName, "ArchitectureTest", "DDD/COLA 架构约束测试")
                + "@AnalyzeClasses(packages = \"" + basePackage + "\")\n"
                + "public class ArchitectureTest {\n\n"
                + "    /**\n"
                + "     * 校验领域层不依赖外层。\n"
                + "     */\n"
                + "    @ArchTest\n"
                + "    static final ArchRule domainShouldNotDependOnOuterLayers = noClasses()\n"
                + "            .that().resideInAPackage(\"..domain..\")\n"
                + "            .should().dependOnClassesThat()\n"
                + "            .resideInAnyPackage(\"..adapter..\", \"..app..\", \"..infrastructure..\");\n\n"
                + "    /**\n"
                + "     * 校验领域层不依赖 JPA 或 MyBatis Plus。\n"
                + "     */\n"
                + "    @ArchTest\n"
                + "    static final ArchRule domainShouldNotUseJpaOrMp = noClasses()\n"
                + "            .that().resideInAPackage(\"..domain..\")\n"
                + "            .should().dependOnClassesThat()\n"
                + "            .resideInAnyPackage(\"jakarta.persistence..\", \"org.springframework.data.jpa..\", \"com.baomidou.mybatisplus..\");\n\n"
                + "    /**\n"
                + "     * 校验 Controller 不直接访问 Mapper。\n"
                + "     */\n"
                + "    @ArchTest\n"
                + "    static final ArchRule controllerShouldNotAccessMapper = noClasses()\n"
                + "            .that().resideInAPackage(\"..adapter.web.controller..\")\n"
                + "            .should().dependOnClassesThat()\n"
                + "            .resideInAnyPackage(\"..infrastructure.persistence.mp.mapper..\");\n\n"
                + "    /**\n"
                + "     * 校验 Mapper 只能存在于 MP mapper 包。\n"
                + "     */\n"
                + "    @ArchTest\n"
                + "    static final ArchRule mapperShouldOnlyExistInMpPackage = classes()\n"
                + "            .that().areAssignableTo(\"com.baomidou.mybatisplus.core.mapper.BaseMapper\")\n"
                + "            .should().resideInAPackage(\"..infrastructure.persistence.mp.mapper..\");\n"
                + "}\n";
        return javaTestFile(context, "architecture/ArchitectureTest.java", content, WritePolicy.OVERWRITE);
    }

    /**
     * 渲染普通字段。
     *
     * @param context           生成上下文
     * @param table             表模型
     * @param withColumnComment 是否输出字段注释
     * @return 字段源码
     */
    private String fields(GenerationContext context, PgTableModel table, boolean withColumnComment) {
        StringBuilder builder = new StringBuilder();
        for (PgColumnModel column : table.getColumns()) {
            if (withColumnComment && column.getColumnComment() != null) {
                builder.append("    /**\n     * ").append(column.getColumnComment()).append("\n     */\n");
            }
            builder.append("    private ").append(context.getTypeMapper().toJavaType(column, context.getNaming()))
                    .append(" ").append(context.getNaming().columnToField(column.getColumnName())).append(";\n\n");
        }
        return builder.toString();
    }

    /**
     * 渲染 JPA 字段。
     *
     * @param context 生成上下文
     * @return 字段源码
     */
    private String jpaFields(GenerationContext context, PgTableModel table) {
        StringBuilder builder = new StringBuilder();
        for (PgColumnModel column : table.getColumns()) {
            if (column.isPrimaryKey()) {
                builder.append("    @Id\n");
                if (column.isSerial() || column.isIdentity()) {
                    builder.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
                }
            }
            if ("version".equals(column.getColumnName())) {
                builder.append("    @Version\n");
            }
            if ("created_at".equals(column.getColumnName())) {
                builder.append("    @CreatedDate\n");
            } else if ("updated_at".equals(column.getColumnName())) {
                builder.append("    @LastModifiedDate\n");
            }
            builder.append("    @Column(name = \"").append(column.getColumnName()).append("\", nullable = ").append(column.isNullable());
            if (column.getLength() != null) {
                builder.append(", length = ").append(column.getLength());
            }
            if ("created_at".equals(column.getColumnName())) {
                builder.append(", updatable = false");
            }
            builder.append(")\n")
                    .append("    private ").append(context.getTypeMapper().toPersistenceJavaType(column))
                    .append(" ").append(context.getNaming().columnToField(column.getColumnName())).append(";\n\n");
        }
        return builder.toString();
    }

    /**
     * 渲染 MP 字段。
     *
     * @param context 生成上下文
     * @return 字段源码
     */
    private String mpFields(GenerationContext context, PgTableModel table) {
        StringBuilder builder = new StringBuilder();
        for (PgColumnModel column : table.getColumns()) {
            if (column.isPrimaryKey()) {
                builder.append("    @TableId(value = \"").append(column.getColumnName()).append("\", type = IdType.AUTO)\n");
            } else if ("deleted".equals(column.getColumnName()) || "is_deleted".equals(column.getColumnName())) {
                builder.append("    @TableLogic\n    @TableField(\"").append(column.getColumnName()).append("\")\n");
            } else if ("version".equals(column.getColumnName())) {
                builder.append("    @Version\n    @TableField(\"version\")\n");
            } else if ("created_at".equals(column.getColumnName())) {
                builder.append("    @TableField(value = \"created_at\", fill = FieldFill.INSERT)\n");
            } else if ("updated_at".equals(column.getColumnName())) {
                builder.append("    @TableField(value = \"updated_at\", fill = FieldFill.INSERT_UPDATE)\n");
            } else {
                builder.append("    @TableField(\"").append(column.getColumnName()).append("\")\n");
            }
            builder.append("    private ").append(context.getTypeMapper().toPersistenceJavaType(column))
                    .append(" ").append(context.getNaming().columnToField(column.getColumnName())).append(";\n\n");
        }
        return builder.toString();
    }

    /**
     * 提取值对象对应字段。
     *
     * @param context 生成上下文
     * @return 值对象字段映射
     */
    private Map<String, PgColumnModel> valueObjectColumns(GenerationContext context) {
        Map<String, PgColumnModel> result = new LinkedHashMap<>();
        context.getRootTable().getPrimaryKeyColumn().ifPresent(column -> {
            if (context.getAggregate().getIdValueObject() != null) {
                result.put(context.getAggregate().getIdValueObject(), column);
            }
        });
        for (Map.Entry<String, String> entry : context.getAggregate().getValueObjects().entrySet()) {
            context.getRootTable().getColumn(entry.getKey()).ifPresent(column -> result.put(entry.getValue(), column));
        }
        for (PgColumnModel column : context.getRootTable().getColumns()) {
            String voName = readHint(column.getColumnComment(), "vo");
            if (voName != null) {
                result.putIfAbsent(voName, column);
            }
        }
        return result;
    }

    /**
     * 获取业务字段。
     *
     * @param table 表模型
     * @return 业务字段列表
     */
    private List<PgColumnModel> businessColumns(PgTableModel table) {
        return table.getColumns().stream()
                .filter(column -> !column.isPrimaryKey())
                .filter(column -> !List.of("deleted", "is_deleted", "version", "created_at", "updated_at").contains(column.getColumnName()))
                .toList();
    }

    /**
     * 转换枚举模板参数。
     *
     * @param enumModel 枚举模型
     * @return 模板枚举项列表
     */
    private List<Map<String, String>> enumValues(PgEnumModel enumModel) {
        List<Map<String, String>> values = new ArrayList<>();
        for (String value : enumModel.getValues()) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("name", value);
            item.put("code", enumModel.getValueCodes().getOrDefault(value, value));
            item.put("description", enumModel.getValueDescriptions().getOrDefault(value, value));
            values.add(item);
        }
        return values;
    }

    /**
     * 追加带 Jakarta Validation 注解的 record 字段。
     *
     * @param context 生成上下文
     * @param fields  字段源码
     * @param column  字段模型
     */
    private void appendValidatedRecordField(GenerationContext context, StringBuilder fields, PgColumnModel column) {
        String javaType = context.getTypeMapper().toJavaType(column, context.getNaming());
        if (!column.isNullable()) {
            fields.append("        @NotNull\n");
        }
        if ("String".equals(javaType) && column.getLength() != null) {
            fields.append("        @Size(max = ").append(column.getLength()).append(")\n");
        }
        if ("BigDecimal".equals(javaType) && column.getPrecision() != null && column.getScale() != null) {
            fields.append("        @Digits(integer = ").append(column.getPrecision() - column.getScale())
                    .append(", fraction = ").append(column.getScale()).append(")\n");
        }
        fields.append("        ").append(javaType)
                .append(" ").append(context.getNaming().columnToField(column.getColumnName())).append(",\n");
    }

    /**
     * 判断表是否包含审计字段。
     *
     * @param table 表模型
     * @return 是否包含审计字段
     */
    private boolean hasAuditFields(PgTableModel table) {
        return table.getColumn("created_at").isPresent() || table.getColumn("updated_at").isPresent();
    }

    /**
     * 根据 Query 配置生成查询对象类名。
     *
     * @param query Query 配置
     * @return 查询对象类名
     */
    private String queryClassName(GeneratorConfig.QueryConfig query) {
        if (query.getName().startsWith("Page")) {
            return query.getName().substring("Page".length()) + "PageQuery";
        }
        if (query.getName().endsWith("Query")) {
            return query.getName();
        }
        return query.getName() + "Query";
    }

    /**
     * 根据 Query 配置生成响应对象类名。
     *
     * @param query Query 配置
     * @return 响应对象类名
     */
    private String responseClassName(GeneratorConfig.QueryConfig query) {
        if (query.getName().startsWith("Page")) {
            return query.getName().substring("Page".length()) + "PageResponse";
        }
        if (query.getName().startsWith("Get")) {
            return query.getName().substring("Get".length()) + "Response";
        }
        if (query.getName().startsWith("List")) {
            return query.getName().substring("List".length()) + "Response";
        }
        return query.getName() + "Response";
    }

    /**
     * 给 Query 追加字段。
     *
     * @param context 生成上下文
     * @param fields  字段源码
     * @param column  字段模型
     */
    private void appendQueryField(GenerationContext context, StringBuilder fields, PgColumnModel column) {
        String javaType = context.getTypeMapper().toJavaType(column, context.getNaming());
        String fieldName = context.getNaming().columnToField(column.getColumnName());
        if (List.of("Instant", "LocalDateTime", "LocalDate").contains(javaType)) {
            fields.append("        ").append(javaType).append(" ").append(fieldName).append("Start,\n")
                    .append("        ").append(javaType).append(" ").append(fieldName).append("End,\n");
        } else {
            fields.append("        ").append(javaType).append(" ").append(fieldName).append(",\n");
        }
    }

    /**
     * 生成逻辑删除 SQL。
     *
     * @param context 生成上下文
     * @return SQL 片段
     */
    private String logicDeleteSql(GenerationContext context) {
        return context.getRootTable().getColumns().stream()
                .filter(column -> "deleted".equals(column.getColumnName()) || "is_deleted".equals(column.getColumnName()))
                .findFirst()
                .map(column -> "        AND " + column.getColumnName() + " = false\n")
                .orElse("");
    }

    /**
     * 根据字段类型生成 import。
     *
     * @param table       表模型
     * @param context     生成上下文
     * @param persistence 是否为持久层
     * @return import 集合
     */
    private Set<String> importsFor(PgTableModel table, GenerationContext context, boolean persistence) {
        Set<String> imports = new LinkedHashSet<>();
        for (PgColumnModel column : table.getColumns()) {
            String javaType = persistence ? context.getTypeMapper().toPersistenceJavaType(column) : context.getTypeMapper().toJavaType(column, context.getNaming());
            imports.addAll(importsForType(javaType));
            if (column.isEnumType() && !persistence) {
                imports.add(context.getConfig().getProject().getBasePackage() + ".domain.model.enums." + javaType);
            }
        }
        return imports;
    }

    /**
     * 根据 Java 类型生成 import。
     *
     * @param javaType Java 类型
     * @return import 集合
     */
    private Set<String> importsForType(String javaType) {
        Set<String> imports = new LinkedHashSet<>();
        if ("BigDecimal".equals(javaType)) {
            imports.add("java.math.BigDecimal");
        } else if ("Instant".equals(javaType)) {
            imports.add("java.time.Instant");
        } else if ("LocalDate".equals(javaType)) {
            imports.add("java.time.LocalDate");
        } else if ("LocalDateTime".equals(javaType)) {
            imports.add("java.time.LocalDateTime");
        } else if ("UUID".equals(javaType)) {
            imports.add("java.util.UUID");
        }
        return imports;
    }

    /**
     * 合并 import 并输出。
     *
     * @param imports 动态 import
     * @param fixed   固定 import
     * @return import 源码
     */
    private String imports(Set<String> imports, String... fixed) {
        Set<String> all = new LinkedHashSet<>(imports);
        all.addAll(List.of(fixed));
        if (all.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String item : all) {
            builder.append("import ").append(item).append(";\n");
        }
        return builder.append("\n").toString();
    }

    /**
     * 输出 package 行。
     *
     * @param packageName 包名
     * @return package 源码
     */
    private String packageLine(String packageName) {
        return "package " + packageName + ";\n\n";
    }

    /**
     * 输出类注释。
     *
     * @param packageName 包名
     * @param className   类名
     * @param description 描述
     * @return 注释源码
     */
    private String classComment(String packageName, String className, String description) {
        return "/**\n"
                + " * @BelongsProject: familyaibutler\n"
                + " * @BelongsPackage: " + packageName + "\n"
                + " * @ClassName: " + className + "\n"
                + " * @Author: atluofu\n"
                + " * @CreateTime: 2026-05-19 00:00\n"
                + " * @Description: " + description + "\n"
                + " * @Version: 1.0\n"
                + " */\n";
    }

    /**
     * 创建 Java 主源码文件。
     *
     * @param context  生成上下文
     * @param relative 相对路径
     * @param content  文件内容
     * @param policy   写入策略
     * @return 生成文件
     */
    private GeneratedSourceFile javaFile(GenerationContext context, String relative, String content, WritePolicy policy) {
        return GeneratedSourceFile.builder()
                .relativePath(Path.of("src/main/java").resolve(context.basePackagePath()).resolve(relative))
                .content(content)
                .policy(policy)
                .build();
    }

    /**
     * 创建 Java 测试源码文件。
     *
     * @param context  生成上下文
     * @param relative 相对路径
     * @param content  文件内容
     * @param policy   写入策略
     * @return 生成文件
     */
    private GeneratedSourceFile javaTestFile(GenerationContext context, String relative, String content, WritePolicy policy) {
        return GeneratedSourceFile.builder()
                .relativePath(Path.of("src/test/java").resolve(context.basePackagePath()).resolve(relative))
                .content(content)
                .policy(policy)
                .build();
    }

    /**
     * 创建资源文件。
     *
     * @param relative 相对路径
     * @param content  文件内容
     * @param policy   写入策略
     * @return 生成文件
     */
    private GeneratedSourceFile resourceFile(String relative, String content, WritePolicy policy) {
        return GeneratedSourceFile.builder()
                .relativePath(Path.of(relative))
                .content(content)
                .policy(policy)
                .build();
    }

    /**
     * 修剪 record 字段末尾逗号。
     *
     * @param fields 字段源码
     * @return 修剪后源码
     */
    private String trimRecordFields(StringBuilder fields) {
        if (fields.isEmpty()) {
            return "";
        }
        int comma = fields.lastIndexOf(",");
        if (comma >= 0) {
            fields.deleteCharAt(comma);
        }
        return fields.toString();
    }

    /**
     * 读取 comment hint。
     *
     * @param comment 注释
     * @param key     key 名称
     * @return hint 值
     */
    private String readHint(String comment, String key) {
        if (comment == null) {
            return null;
        }
        for (String part : comment.split(";")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && key.equals(pair[0].trim())) {
                return pair[1].trim();
            }
        }
        return null;
    }
}
