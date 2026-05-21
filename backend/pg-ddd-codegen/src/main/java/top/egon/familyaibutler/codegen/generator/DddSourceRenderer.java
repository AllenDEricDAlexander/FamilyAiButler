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
import java.util.Locale;
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
        files.addAll(renderApplicationContract(context));
        files.addAll(renderApplicationImplementation(context));
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
        String applicationClassName = applicationClassName(config.getProject().getModuleName());
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
        return renderTemplate(TemplateRegistry.PROJECT_APPLICATION, packageName, className, config.getProject().getModuleName() + " 启动类", Map.of());
    }

    /**
     * 根据模块名生成合法的 Spring Boot 启动类名。
     *
     * @param moduleName 模块名
     * @return 启动类名
     */
    private String applicationClassName(String moduleName) {
        String normalized = moduleName == null ? "" : moduleName.replaceAll("[^A-Za-z0-9]+", "_");
        String className = new Naming(List.of()).upperCamel(normalized);
        if (className.isBlank()) {
            className = "Generated";
        }
        if (!Character.isJavaIdentifierStart(className.charAt(0))) {
            className = "App" + className;
        }
        return className + "Application";
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
        String domainPath = domainPackagePath(context);
        files.add(javaFile(context, domainPath + "/model/aggregate/" + aggregateName + ".java", renderPojo(
                context, domainPackage(context) + ".model.aggregate", aggregateName, context.getRootTable(), WritePolicy.CREATE_ONLY), WritePolicy.CREATE_ONLY));
        for (PgTableModel entityTable : context.getEntityTables()) {
            String entityName = context.getNaming().tableToClass(entityTable.getTableName());
            files.add(javaFile(context, domainPath + "/model/entity/" + entityName + ".java", renderPojo(
                    context, domainPackage(context) + ".model.entity", entityName, entityTable, WritePolicy.CREATE_ONLY), WritePolicy.CREATE_ONLY));
        }
        for (Map.Entry<String, PgColumnModel> entry : valueObjectColumns(context).entrySet()) {
            files.add(javaFile(context, domainPath + "/model/valueobject/" + entry.getKey() + ".java", renderValueObject(context, entry.getKey(), entry.getValue()), WritePolicy.OVERWRITE));
        }
        for (PgEnumModel enumModel : context.getSchemaModel().getEnums()) {
            String enumName = enumModel.getJavaName() == null ? context.getNaming().upperCamel(enumModel.getTypeName()) : enumModel.getJavaName();
            files.add(javaFile(context, domainPath + "/model/enums/" + enumName + ".java", renderEnum(context, enumName, enumModel), WritePolicy.OVERWRITE));
        }
        files.add(javaFile(context, domainPath + "/gateway/" + aggregateName + "Gateway.java", renderGateway(context), WritePolicy.CREATE_ONLY));
        files.add(javaFile(context, domainPath + "/gateway/" + aggregateName + "QueryGateway.java", renderQueryGateway(context), WritePolicy.CREATE_ONLY));
        for (GeneratorConfig.QueryConfig query : context.getAggregate().getQueries()) {
            files.add(javaFile(context, domainPath + "/gateway/query/" + criteriaClassName(query) + ".java", renderQueryCriteria(context, query), WritePolicy.OVERWRITE));
        }
        files.add(javaFile(context, domainPath + "/service/" + aggregateName + "DomainService.java", renderSimpleComponent(context, domainPackage(context) + ".service", aggregateName + "DomainService", aggregateName + " 领域服务"), WritePolicy.CREATE_ONLY));
        files.add(javaFile(context, domainPath + "/event/" + aggregateName + "CreatedEvent.java", renderDomainEvent(context), WritePolicy.CREATE_ONLY));
        return files;
    }

    /**
     * 渲染 application 接口和用例入出参文件。
     *
     * @param context 生成上下文
     * @return 生成文件列表
     */
    private List<GeneratedSourceFile> renderApplicationContract(GenerationContext context) {
        List<GeneratedSourceFile> files = new ArrayList<>();
        String aggregateName = context.getAggregate().getName();
        files.add(javaFile(context, "application/manage/" + aggregateName + "Manage.java", renderApplicationManage(context), WritePolicy.OVERWRITE));
        files.add(javaFile(context, "application/assembler/" + aggregateName + "ApplicationAssembler.java", renderApplicationAssembler(context), WritePolicy.CREATE_ONLY));
        for (GeneratorConfig.UseCaseConfig command : context.getAggregate().getCommands()) {
            files.add(javaFile(context, "application/command/" + command.getName() + "Command.java", renderCommand(context, command), WritePolicy.OVERWRITE));
            files.add(javaFile(context, "application/result/" + commandResultClassName(command) + ".java", renderResult(context, commandResultClassName(command)), WritePolicy.OVERWRITE));
            files.addAll(renderEntityCommands(context, command));
        }
        for (GeneratorConfig.QueryConfig query : context.getAggregate().getQueries()) {
            String queryClass = queryClassName(query);
            String resultClass = resultClassName(query);
            files.add(javaFile(context, "application/query/" + queryClass + ".java", renderQuery(context, query, queryClass), WritePolicy.OVERWRITE));
            files.add(javaFile(context, "application/result/" + resultClass + ".java", renderResult(context, resultClass), WritePolicy.OVERWRITE));
        }
        return files;
    }

    /**
     * 渲染 application 实现和执行器文件。
     *
     * @param context 生成上下文
     * @return 生成文件列表
     */
    private List<GeneratedSourceFile> renderApplicationImplementation(GenerationContext context) {
        List<GeneratedSourceFile> files = new ArrayList<>();
        String aggregateName = context.getAggregate().getName();
        files.add(javaFile(context, "application/manage/impl/" + aggregateName + "ManageImpl.java", renderApplicationManageImpl(context), WritePolicy.CREATE_ONLY));
        for (GeneratorConfig.UseCaseConfig command : context.getAggregate().getCommands()) {
            files.add(javaFile(context, "application/executor/command/" + command.getName() + "CmdExe.java", renderCommandExecutor(context, command), WritePolicy.CREATE_ONLY));
        }
        for (GeneratorConfig.QueryConfig query : context.getAggregate().getQueries()) {
            files.add(javaFile(context, "application/executor/query/" + query.getName() + "QryExe.java", renderQueryExecutor(context, query), WritePolicy.CREATE_ONLY));
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
        files.add(javaFile(context, "adapter/web/" + aggregateName + "Controller.java", renderController(context), WritePolicy.CREATE_ONLY));
        files.add(javaFile(context, "adapter/web/assembler/" + aggregateName + "WebAssembler.java", renderAssembler(context), WritePolicy.CREATE_ONLY));
        for (GeneratorConfig.UseCaseConfig command : context.getAggregate().getCommands()) {
            files.add(javaFile(context, "adapter/web/dto/" + requestDtoClassName(command.getName()) + ".java", renderWebRequestDto(context, command.getName(), requestDtoClassName(command.getName())), WritePolicy.OVERWRITE));
            files.add(javaFile(context, "adapter/web/dto/" + voClassName(command.getName()) + ".java", renderWebVo(context, voClassName(command.getName())), WritePolicy.OVERWRITE));
        }
        for (GeneratorConfig.QueryConfig query : context.getAggregate().getQueries()) {
            files.add(javaFile(context, "adapter/web/dto/" + requestDtoClassName(query.getName()) + ".java", renderWebQueryRequestDto(context, query, requestDtoClassName(query.getName())), WritePolicy.OVERWRITE));
            files.add(javaFile(context, "adapter/web/dto/" + queryVoClassName(query) + ".java", renderWebVo(context, queryVoClassName(query)), WritePolicy.OVERWRITE));
        }
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
        files.add(javaFile(context, "infrastructure/gateway/impl/" + aggregateName + "GatewayImpl.java", renderJpaGatewayImpl(context), WritePolicy.CREATE_ONLY));
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
        files.add(javaFile(context, "infrastructure/gateway/impl/" + aggregateName + "QueryGatewayImpl.java", renderQueryGatewayImpl(context), WritePolicy.CREATE_ONLY));
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
        String templateName = subPackage.endsWith("aggregate") ? TemplateRegistry.DOMAIN_AGGREGATE : TemplateRegistry.DOMAIN_ENTITY;
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importsFor(table, context, false), "lombok.Data"));
        model.put("fields", fields(context, table, false));
        return renderTemplate(templateName, packageName, className, table.getTableName() + " 领域模型，写入策略 " + policy, model);
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
        String packageName = context.getConfig().getProject().getBasePackage() + "." + domainPackage(context) + ".model.valueobject";
        String valueType = context.getTypeMapper().toValueObjectType(column);
        Set<String> importSet = importsForType(valueType);
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importSet));
        model.put("valueType", valueType);
        return renderTemplate(TemplateRegistry.DOMAIN_VALUE_OBJECT, packageName, className, column.getColumnName() + " 值对象", model);
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
        String packageName = context.getConfig().getProject().getBasePackage() + "." + domainPackage(context) + ".model.enums";
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
        String packageName = context.getConfig().getProject().getBasePackage() + "." + domainPackage(context) + ".event";
        return renderTemplate(TemplateRegistry.DOMAIN_DOMAIN_EVENT, packageName, className, aggregateName + " 创建领域事件", Map.of());
    }

    /**
     * 渲染领域写模型网关接口。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderGateway(GenerationContext context) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String aggregateName = context.getAggregate().getName();
        String idType = context.getAggregate().getIdValueObject() == null ? "Long" : context.getAggregate().getIdValueObject();
        String packageName = basePackage + "." + domainPackage(context) + ".gateway";
        Set<String> importSet = new LinkedHashSet<>();
        importSet.add(basePackage + "." + domainPackage(context) + ".model.aggregate." + aggregateName);
        if (!idType.equals("Long")) {
            importSet.add(basePackage + "." + domainPackage(context) + ".model.valueobject." + idType);
        }
        importSet.add("java.util.Optional");
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importSet));
        model.put("aggregateName", aggregateName);
        model.put("idType", idType);
        return renderTemplate(TemplateRegistry.DOMAIN_GATEWAY, packageName, aggregateName + "Gateway", aggregateName + " 写模型网关接口", model);
    }

    /**
     * 渲染查询网关接口。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderQueryGateway(GenerationContext context) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String packageName = basePackage + "." + domainPackage(context) + ".gateway";
        String aggregateName = context.getAggregate().getName();
        Set<String> importSet = new LinkedHashSet<>();
        importSet.add(basePackage + "." + domainPackage(context) + ".model.aggregate." + aggregateName);
        importSet.add("java.util.List");
        StringBuilder methods = new StringBuilder();
        for (GeneratorConfig.QueryConfig query : context.getAggregate().getQueries()) {
            String criteriaClassName = criteriaClassName(query);
            importSet.add(basePackage + "." + domainPackage(context) + ".gateway.query." + criteriaClassName);
            methods.append("    /**\n")
                    .append("     * 执行 ").append(query.getName()).append(" 查询。\n")
                    .append("     *\n")
                    .append("     * @param criteria 领域查询条件\n")
                    .append("     * @return 查询结果\n")
                    .append("     */\n")
                    .append("    List<").append(aggregateName).append("> ").append(queryGatewayMethodName(query))
                    .append("(").append(criteriaClassName).append(" criteria);\n\n");
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importSet));
        model.put("methods", methods.toString());
        return renderTemplate(TemplateRegistry.DOMAIN_QUERY_GATEWAY, packageName, aggregateName + "QueryGateway", aggregateName + " 复杂查询网关", model);
    }

    /**
     * 渲染领域查询条件。
     *
     * @param context 生成上下文
     * @param query   Query 配置
     * @return Java 源码
     */
    private String renderQueryCriteria(GenerationContext context, GeneratorConfig.QueryConfig query) {
        String packageName = context.getConfig().getProject().getBasePackage() + "." + domainPackage(context) + ".gateway.query";
        StringBuilder fields = new StringBuilder();
        for (String filter : query.getFilters()) {
            context.getRootTable().getColumn(filter).ifPresent(column -> appendQueryClassField(context, fields, column));
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importsFor(context.getRootTable(), context, false)));
        model.put("fields", fields.toString());
        return renderTemplate(TemplateRegistry.DOMAIN_QUERY_CRITERIA, packageName, criteriaClassName(query), query.getName() + " 领域查询条件", model);
    }

    /**
     * 渲染 COLA Manage 接口。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderApplicationManage(GenerationContext context) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String aggregateName = context.getAggregate().getName();
        String packageName = basePackage + ".application.manage";
        Set<String> importSet = new LinkedHashSet<>();
        StringBuilder methods = new StringBuilder();
        for (GeneratorConfig.UseCaseConfig command : context.getAggregate().getCommands()) {
            String commandClassName = command.getName() + "Command";
            String resultClassName = commandResultClassName(command);
            importSet.add(basePackage + ".application.command." + commandClassName);
            importSet.add(basePackage + ".application.result." + resultClassName);
            appendManageMethod(context, methods, command.getName(), commandClassName, resultClassName, "command", "命令对象");
        }
        for (GeneratorConfig.QueryConfig query : context.getAggregate().getQueries()) {
            String queryClassName = queryClassName(query);
            String resultClassName = resultClassName(query);
            importSet.add(basePackage + ".application.query." + queryClassName);
            importSet.add(basePackage + ".application.result." + resultClassName);
            appendManageMethod(context, methods, query.getName(), queryClassName, resultClassName, "query", "查询对象");
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importSet));
        model.put("methods", methods.toString());
        return renderTemplate(TemplateRegistry.APPLICATION_MANAGE, packageName, aggregateName + "Manage", aggregateName + " 应用服务接口", model);
    }

    /**
     * 渲染 Command 类。
     *
     * @param context 生成上下文
     * @param command Command 配置
     * @return Java 源码
     */
    private String renderCommand(GenerationContext context, GeneratorConfig.UseCaseConfig command) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".application.command";
        StringBuilder fields = new StringBuilder();
        for (PgColumnModel column : businessColumns(context.getRootTable())) {
            appendValidatedClassField(context, fields, column);
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importsFor(context.getRootTable(), context, false), "jakarta.validation.constraints.*"));
        model.put("fields", fields.toString());
        return renderTemplate(TemplateRegistry.APPLICATION_COMMAND, packageName, command.getName() + "Command", command.getName() + " 命令对象", model);
    }

    /**
     * 渲染 Query 类。
     *
     * @param context   生成上下文
     * @param query     Query 配置
     * @param className 类名
     * @return Java 源码
     */
    private String renderQuery(GenerationContext context, GeneratorConfig.QueryConfig query, String className) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".application.query";
        StringBuilder fields = new StringBuilder();
        for (String filter : query.getFilters()) {
            context.getRootTable().getColumn(filter).ifPresent(column -> appendQueryClassField(context, fields, column));
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importsFor(context.getRootTable(), context, false)));
        model.put("fields", fields.toString());
        return renderTemplate(TemplateRegistry.APPLICATION_QUERY, packageName, className, query.getName() + " 查询对象", model);
    }

    /**
     * 渲染 Result 类。
     *
     * @param context   生成上下文
     * @param className 类名
     * @return Java 源码
     */
    private String renderResult(GenerationContext context, String className) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".application.result";
        StringBuilder fields = new StringBuilder();
        for (PgColumnModel column : context.getRootTable().getColumns()) {
            appendClassField(context, fields, column);
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importsFor(context.getRootTable(), context, false)));
        model.put("fields", fields.toString());
        return renderTemplate(TemplateRegistry.APPLICATION_RESULT, packageName, className, className + " 应用结果", model);
    }

    /**
     * 渲染应用层转换器。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderApplicationAssembler(GenerationContext context) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String aggregateName = context.getAggregate().getName();
        String packageName = basePackage + ".application.assembler";
        Set<String> importSet = new LinkedHashSet<>();
        importSet.add(basePackage + "." + domainPackage(context) + ".model.aggregate." + aggregateName);
        StringBuilder methods = new StringBuilder();
        for (GeneratorConfig.UseCaseConfig command : context.getAggregate().getCommands()) {
            String resultClassName = commandResultClassName(command);
            importSet.add(basePackage + ".application.result." + resultClassName);
            appendApplicationAssemblerMethod(methods, command.getName(), aggregateName, resultClassName);
        }
        for (GeneratorConfig.QueryConfig query : context.getAggregate().getQueries()) {
            String queryClassName = queryClassName(query);
            String criteriaClassName = criteriaClassName(query);
            String resultClassName = resultClassName(query);
            importSet.add(basePackage + ".application.query." + queryClassName);
            importSet.add(basePackage + ".application.result." + resultClassName);
            importSet.add(basePackage + "." + domainPackage(context) + ".gateway.query." + criteriaClassName);
            importSet.add("java.util.List");
            appendQueryCriteriaAssemblerMethod(context, methods, query, queryClassName, criteriaClassName);
            appendQueryApplicationAssemblerMethod(methods, query.getName(), aggregateName, resultClassName);
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importSet));
        model.put("methods", methods.toString());
        return renderTemplate(TemplateRegistry.APPLICATION_ASSEMBLER, packageName, aggregateName + "ApplicationAssembler", aggregateName + " 应用层对象转换器", model);
    }

    /**
     * 渲染 Web 请求 DTO。
     *
     * @param context   生成上下文
     * @param useCase   用例名称
     * @param className 类名
     * @return Java 源码
     */
    private String renderWebRequestDto(GenerationContext context, String useCase, String className) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".adapter.web.dto";
        StringBuilder fields = new StringBuilder();
        for (PgColumnModel column : businessColumns(context.getRootTable())) {
            appendValidatedClassField(context, fields, column);
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importsFor(context.getRootTable(), context, false), "jakarta.validation.constraints.*"));
        model.put("fields", fields.toString());
        return renderTemplate(TemplateRegistry.ADAPTER_WEB_DTO, packageName, className, useCase + " Web 请求", model);
    }

    /**
     * 渲染 Web 查询请求 DTO。
     *
     * @param context   生成上下文
     * @param query     查询配置
     * @param className 类名
     * @return Java 源码
     */
    private String renderWebQueryRequestDto(GenerationContext context, GeneratorConfig.QueryConfig query, String className) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".adapter.web.dto";
        StringBuilder fields = new StringBuilder();
        for (String filter : query.getFilters()) {
            context.getRootTable().getColumn(filter).ifPresent(column -> appendQueryClassField(context, fields, column));
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importsFor(context.getRootTable(), context, false)));
        model.put("fields", fields.toString());
        return renderTemplate(TemplateRegistry.ADAPTER_WEB_DTO, packageName, className, query.getName() + " Web 查询请求", model);
    }

    /**
     * 渲染 Web 视图对象。
     *
     * @param context   生成上下文
     * @param className 类名
     * @return Java 源码
     */
    private String renderWebVo(GenerationContext context, String className) {
        String packageName = context.getConfig().getProject().getBasePackage() + ".adapter.web.dto";
        StringBuilder fields = new StringBuilder();
        for (PgColumnModel column : context.getRootTable().getColumns()) {
            appendClassField(context, fields, column);
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importsFor(context.getRootTable(), context, false)));
        model.put("fields", fields.toString());
        return renderTemplate(TemplateRegistry.ADAPTER_WEB_DTO, packageName, className, className + " Web 视图对象", model);
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
            files.add(javaFile(context, "application/command/" + className + ".java", renderEntityCommand(context, className, entityTable), WritePolicy.OVERWRITE));
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
        String packageName = context.getConfig().getProject().getBasePackage() + ".application.command";
        StringBuilder fields = new StringBuilder();
        for (PgColumnModel column : businessColumns(table)) {
            appendValidatedClassField(context, fields, column);
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importsFor(table, context, false), "jakarta.validation.constraints.*"));
        model.put("fields", fields.toString());
        return renderTemplate(TemplateRegistry.APPLICATION_COMMAND, packageName, className, className + " 子实体命令对象", model);
    }

    /**
     * 渲染 COLA 应用服务。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderApplicationManageImpl(GenerationContext context) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String aggregateName = context.getAggregate().getName();
        String packageName = basePackage + ".application.manage.impl";
        Set<String> importSet = new LinkedHashSet<>();
        importSet.add(basePackage + ".application.manage." + aggregateName + "Manage");
        importSet.add("lombok.RequiredArgsConstructor");
        importSet.add("org.springframework.stereotype.Service");
        importSet.add("org.springframework.transaction.annotation.Transactional");
        StringBuilder executorFields = new StringBuilder();
        StringBuilder methods = new StringBuilder();
        for (GeneratorConfig.UseCaseConfig command : context.getAggregate().getCommands()) {
            String commandClassName = command.getName() + "Command";
            String resultClassName = commandResultClassName(command);
            String executorClassName = command.getName() + "CmdExe";
            String executorFieldName = context.getNaming().classToField(executorClassName);
            importSet.add(basePackage + ".application.command." + commandClassName);
            importSet.add(basePackage + ".application.result." + resultClassName);
            importSet.add(basePackage + ".application.executor.command." + executorClassName);
            executorFields.append("    private final ").append(executorClassName).append(" ").append(executorFieldName).append(";\n");
            appendManageImplMethod(context, methods, command.getName(), commandClassName, resultClassName, "command", executorFieldName, true);
        }
        for (GeneratorConfig.QueryConfig query : context.getAggregate().getQueries()) {
            String queryClassName = queryClassName(query);
            String resultClassName = resultClassName(query);
            String executorClassName = query.getName() + "QryExe";
            String executorFieldName = context.getNaming().classToField(executorClassName);
            importSet.add(basePackage + ".application.query." + queryClassName);
            importSet.add(basePackage + ".application.result." + resultClassName);
            importSet.add(basePackage + ".application.executor.query." + executorClassName);
            executorFields.append("    private final ").append(executorClassName).append(" ").append(executorFieldName).append(";\n");
            appendManageImplMethod(context, methods, query.getName(), queryClassName, resultClassName, "query", executorFieldName, false);
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importSet));
        model.put("interfaceName", aggregateName + "Manage");
        model.put("executorFields", executorFields.toString());
        model.put("methods", methods.toString());
        return renderTemplate(TemplateRegistry.APPLICATION_MANAGE_IMPL, packageName, aggregateName + "ManageImpl", aggregateName + " 应用服务实现", model);
    }

    /**
     * 渲染命令执行器骨架。
     *
     * @param context 生成上下文
     * @param command 命令配置
     * @return Java 源码
     */
    private String renderCommandExecutor(GenerationContext context, GeneratorConfig.UseCaseConfig command) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String aggregateName = context.getAggregate().getName();
        String packageName = basePackage + ".application.executor.command";
        String commandClassName = command.getName() + "Command";
        String resultClassName = commandResultClassName(command);
        String applicationAssemblerName = aggregateName + "ApplicationAssembler";
        Set<String> importSet = new LinkedHashSet<>();
        importSet.add(basePackage + ".application.assembler." + applicationAssemblerName);
        importSet.add(basePackage + ".application.command." + commandClassName);
        importSet.add(basePackage + ".application.result." + resultClassName);
        importSet.add(basePackage + "." + domainPackage(context) + ".gateway." + aggregateName + "Gateway");
        importSet.add(basePackage + "." + domainPackage(context) + ".service." + aggregateName + "DomainService");
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importSet));
        model.put("commandClassName", commandClassName);
        model.put("resultClassName", resultClassName);
        model.put("fields", "    private final " + aggregateName + "Gateway " + context.getNaming().classToField(aggregateName + "Gateway") + ";\n"
                + "    private final " + aggregateName + "DomainService " + context.getNaming().classToField(aggregateName + "DomainService") + ";\n"
                + "    private final " + applicationAssemblerName + " " + context.getNaming().classToField(applicationAssemblerName) + ";\n");
        model.put("applicationAssemblerFieldName", context.getNaming().classToField(applicationAssemblerName));
        model.put("assemblerMethodName", applicationAssemblerMethodName(command.getName()));
        return renderTemplate(TemplateRegistry.APPLICATION_CMD_EXE, packageName, command.getName() + "CmdExe", command.getName() + " 命令执行器", model);
    }

    /**
     * 渲染查询执行器骨架。
     *
     * @param context 生成上下文
     * @param query   查询配置
     * @return Java 源码
     */
    private String renderQueryExecutor(GenerationContext context, GeneratorConfig.QueryConfig query) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String aggregateName = context.getAggregate().getName();
        String packageName = basePackage + ".application.executor.query";
        String queryClassName = queryClassName(query);
        String resultClassName = resultClassName(query);
        String criteriaClassName = criteriaClassName(query);
        String applicationAssemblerName = aggregateName + "ApplicationAssembler";
        Set<String> importSet = new LinkedHashSet<>();
        importSet.add(basePackage + ".application.assembler." + applicationAssemblerName);
        importSet.add(basePackage + ".application.query." + queryClassName);
        importSet.add(basePackage + ".application.result." + resultClassName);
        importSet.add(basePackage + "." + domainPackage(context) + ".gateway." + aggregateName + "QueryGateway");
        importSet.add(basePackage + "." + domainPackage(context) + ".gateway.query." + criteriaClassName);
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importSet));
        model.put("queryClassName", queryClassName);
        model.put("resultClassName", resultClassName);
        model.put("criteriaClassName", criteriaClassName);
        model.put("fields", "    private final " + aggregateName + "QueryGateway " + context.getNaming().classToField(aggregateName + "QueryGateway") + ";\n"
                + "    private final " + applicationAssemblerName + " " + context.getNaming().classToField(applicationAssemblerName) + ";\n");
        model.put("queryGatewayFieldName", context.getNaming().classToField(aggregateName + "QueryGateway"));
        model.put("queryGatewayMethodName", queryGatewayMethodName(query));
        model.put("applicationAssemblerFieldName", context.getNaming().classToField(applicationAssemblerName));
        model.put("assemblerMethodName", applicationAssemblerMethodName(query.getName()));
        model.put("criteriaAssemblerMethodName", criteriaAssemblerMethodName(query));
        return renderTemplate(TemplateRegistry.APPLICATION_QRY_EXE, packageName, query.getName() + "QryExe", query.getName() + " 查询执行器", model);
    }

    /**
     * 渲染 Controller 骨架。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderController(GenerationContext context) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String packageName = basePackage + ".adapter.web";
        String aggregateName = context.getAggregate().getName();
        String manageInterfaceName = aggregateName + "Manage";
        String webAssemblerName = aggregateName + "WebAssembler";
        String requestMapping = controllerBasePath(context);
        Set<String> importSet = new LinkedHashSet<>();
        importSet.add(basePackage + ".adapter.web.assembler." + webAssemblerName);
        importSet.add(basePackage + ".application.manage." + manageInterfaceName);
        StringBuilder methods = new StringBuilder();
        for (GeneratorConfig.UseCaseConfig command : context.getAggregate().getCommands()) {
            String requestClassName = requestDtoClassName(command.getName());
            String commandClassName = command.getName() + "Command";
            String resultClassName = commandResultClassName(command);
            String voClassName = voClassName(command.getName());
            importSet.add(basePackage + ".adapter.web.dto." + requestClassName);
            importSet.add(basePackage + ".adapter.web.dto." + voClassName);
            importSet.add(basePackage + ".application.command." + commandClassName);
            importSet.add(basePackage + ".application.result." + resultClassName);
            methods.append("\n")
                    .append("    /**\n")
                    .append("     * 执行 ").append(command.getName()).append(" 命令。\n")
                    .append("     *\n")
                    .append("     * @param request Web 请求\n")
                    .append("     * @return 执行结果\n")
                    .append("     */\n")
                    .append("    @").append(mappingAnnotation(command.getMethod(), "PostMapping"))
                    .append("(\"").append(relativeUseCasePath(requestMapping, command.getPath())).append("\")\n")
                    .append("    public ResponseEntity<").append(voClassName).append("> ").append(context.getNaming().classToField(command.getName()))
                    .append("(@Valid @RequestBody ").append(requestClassName).append(" request) {\n")
                    .append("        ").append(commandClassName).append(" command = ").append(context.getNaming().classToField(webAssemblerName))
                    .append(".").append(webToCommandMethodName(command.getName())).append("(request);\n")
                    .append("        ").append(resultClassName).append(" result = ").append(context.getNaming().classToField(manageInterfaceName))
                    .append(".").append(context.getNaming().classToField(command.getName())).append("(command);\n")
                    .append("        return ResponseEntity.ok(").append(context.getNaming().classToField(webAssemblerName))
                    .append(".").append(webToVoMethodName(command.getName())).append("(result));\n")
                    .append("    }\n");
        }
        for (GeneratorConfig.QueryConfig query : context.getAggregate().getQueries()) {
            String requestClassName = requestDtoClassName(query.getName());
            String queryClassName = queryClassName(query);
            String resultClassName = resultClassName(query);
            String voClassName = queryVoClassName(query);
            importSet.add(basePackage + ".adapter.web.dto." + requestClassName);
            importSet.add(basePackage + ".adapter.web.dto." + voClassName);
            importSet.add(basePackage + ".application.query." + queryClassName);
            importSet.add(basePackage + ".application.result." + resultClassName);
            methods.append("\n")
                    .append("    /**\n")
                    .append("     * 执行 ").append(query.getName()).append(" 查询。\n")
                    .append("     *\n")
                    .append("     * @param request Web 请求\n")
                    .append("     * @return 查询结果\n")
                    .append("     */\n")
                    .append("    @").append(mappingAnnotation(query.getMethod(), "GetMapping"))
                    .append("(\"").append(relativeUseCasePath(requestMapping, query.getPath())).append("\")\n")
                    .append("    public ResponseEntity<").append(voClassName).append("> ").append(context.getNaming().classToField(query.getName()))
                    .append("(@Valid ").append(requestClassName).append(" request) {\n")
                    .append("        ").append(queryClassName).append(" query = ").append(context.getNaming().classToField(webAssemblerName))
                    .append(".").append(webToQueryMethodName(query.getName())).append("(request);\n")
                    .append("        ").append(resultClassName).append(" result = ").append(context.getNaming().classToField(manageInterfaceName))
                    .append(".").append(context.getNaming().classToField(query.getName())).append("(query);\n")
                    .append("        return ResponseEntity.ok(").append(context.getNaming().classToField(webAssemblerName))
                    .append(".").append(webToVoMethodName(query.getName())).append("(result));\n")
                    .append("    }\n");
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importSet));
        model.put("requestMapping", requestMapping);
        model.put("manageInterfaceName", manageInterfaceName);
        model.put("manageFieldName", context.getNaming().classToField(manageInterfaceName));
        model.put("webAssemblerName", webAssemblerName);
        model.put("webAssemblerFieldName", context.getNaming().classToField(webAssemblerName));
        model.put("methods", methods.toString());
        return renderTemplate(TemplateRegistry.ADAPTER_CONTROLLER, packageName, aggregateName + "Controller", aggregateName + " Web 控制层", model);
    }

    /**
     * 渲染 Web Assembler 骨架。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderAssembler(GenerationContext context) {
        String aggregateName = context.getAggregate().getName();
        String basePackage = context.getConfig().getProject().getBasePackage();
        String packageName = basePackage + ".adapter.web.assembler";
        Set<String> importSet = new LinkedHashSet<>();
        StringBuilder methods = new StringBuilder();
        for (GeneratorConfig.UseCaseConfig command : context.getAggregate().getCommands()) {
            String requestClassName = requestDtoClassName(command.getName());
            String commandClassName = command.getName() + "Command";
            String resultClassName = commandResultClassName(command);
            String voClassName = voClassName(command.getName());
            importSet.add(basePackage + ".adapter.web.dto." + requestClassName);
            importSet.add(basePackage + ".adapter.web.dto." + voClassName);
            importSet.add(basePackage + ".application.command." + commandClassName);
            importSet.add(basePackage + ".application.result." + resultClassName);
            appendWebAssemblerCommandMethod(context, methods, command.getName(), requestClassName, commandClassName);
            appendWebAssemblerVoMethod(context, methods, command.getName(), resultClassName, voClassName);
        }
        for (GeneratorConfig.QueryConfig query : context.getAggregate().getQueries()) {
            String requestClassName = requestDtoClassName(query.getName());
            String queryClassName = queryClassName(query);
            String resultClassName = resultClassName(query);
            String voClassName = queryVoClassName(query);
            importSet.add(basePackage + ".adapter.web.dto." + requestClassName);
            importSet.add(basePackage + ".adapter.web.dto." + voClassName);
            importSet.add(basePackage + ".application.query." + queryClassName);
            importSet.add(basePackage + ".application.result." + resultClassName);
            appendWebAssemblerQueryMethod(context, methods, query, requestClassName, queryClassName);
            appendWebAssemblerVoMethod(context, methods, query.getName(), resultClassName, voClassName);
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importSet));
        model.put("methods", methods.toString());
        return renderTemplate(TemplateRegistry.ADAPTER_WEB_ASSEMBLER, packageName, aggregateName + "WebAssembler", aggregateName + " Web 装配器", model);
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
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(imports, "jakarta.persistence.*", "lombok.Data"));
        model.put("entityListenersAnnotation", hasAuditFields(table) ? "@EntityListeners(AuditingEntityListener.class)\n" : "");
        model.put("tableName", table.getTableName());
        model.put("fields", jpaFields(context, table));
        return renderTemplate(TemplateRegistry.INFRA_JPA_ENTITY, packageName, className, table.getTableName() + " JPA 实体", model);
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
        Set<String> importSet = new LinkedHashSet<>();
        importSet.add(context.getConfig().getProject().getBasePackage() + ".infrastructure.persistence.jpa.entity." + aggregateName + "JpaEntity");
        importSet.add("org.springframework.data.jpa.repository.JpaRepository");
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importSet));
        model.put("entityClass", aggregateName + "JpaEntity");
        model.put("idType", pkType);
        return renderTemplate(TemplateRegistry.INFRA_JPA_REPOSITORY, packageName, aggregateName + "JpaRepository", aggregateName + " JPA Repository", model);
    }

    /**
     * 渲染 JPA 写模型网关实现骨架。
     *
     * @param context 生成上下文
     * @return Java 源码
     */
    private String renderJpaGatewayImpl(GenerationContext context) {
        String basePackage = context.getConfig().getProject().getBasePackage();
        String aggregateName = context.getAggregate().getName();
        String idType = context.getAggregate().getIdValueObject() == null ? "Long" : context.getAggregate().getIdValueObject();
        String packageName = basePackage + ".infrastructure.gateway.impl";
        Set<String> importSet = new LinkedHashSet<>();
        importSet.add(basePackage + "." + domainPackage(context) + ".model.aggregate." + aggregateName);
        if (!idType.equals("Long")) {
            importSet.add(basePackage + "." + domainPackage(context) + ".model.valueobject." + idType);
        }
        importSet.add(basePackage + "." + domainPackage(context) + ".gateway." + aggregateName + "Gateway");
        importSet.add("lombok.RequiredArgsConstructor");
        importSet.add("org.springframework.stereotype.Repository");
        importSet.add("java.util.Optional");
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importSet));
        model.put("aggregateName", aggregateName);
        model.put("idType", idType);
        return renderTemplate(TemplateRegistry.INFRA_JPA_GATEWAY_IMPL, packageName, aggregateName + "GatewayImpl", aggregateName + " JPA 写模型网关实现", model);
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
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importsFor(table, context, true), "com.baomidou.mybatisplus.annotation.*", "lombok.Data"));
        model.put("tableName", table.getTableName());
        model.put("fields", mpFields(context, table));
        return renderTemplate(TemplateRegistry.INFRA_MP_DATA_OBJECT, packageName, className, table.getTableName() + " MyBatis Plus 数据对象", model);
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
        Set<String> importSet = new LinkedHashSet<>();
        importSet.add(basePackage + ".infrastructure.persistence.mp.dataobject." + aggregateName + "DO");
        importSet.add("com.baomidou.mybatisplus.core.mapper.BaseMapper");
        importSet.add("org.apache.ibatis.annotations.Mapper");
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importSet));
        model.put("dataObjectClass", aggregateName + "DO");
        return renderTemplate(TemplateRegistry.INFRA_MP_MAPPER, packageName, aggregateName + "Mapper", aggregateName + " MyBatis Plus Mapper", model);
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
                .map(this::resultClassName)
                .orElse(aggregateName + "PageResult");
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("mapperNamespace", basePackage + ".infrastructure.persistence.mp.mapper." + aggregateName + "Mapper");
        model.put("baseColumns", columns);
        model.put("pageQueryId", "page" + aggregateName + "s");
        model.put("resultType", basePackage + ".application.result." + responseClass);
        model.put("tableName", context.getRootTable().getTableName());
        model.put("logicDeleteSql", logicDeleteSql(context));
        return templateEngine.render(TemplateRegistry.INFRA_MP_MAPPER_XML, model);
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
        String packageName = basePackage + ".infrastructure.gateway.impl";
        Set<String> importSet = new LinkedHashSet<>();
        importSet.add(basePackage + "." + domainPackage(context) + ".gateway." + aggregateName + "QueryGateway");
        importSet.add(basePackage + "." + domainPackage(context) + ".model.aggregate." + aggregateName);
        importSet.add("lombok.RequiredArgsConstructor");
        importSet.add("org.springframework.stereotype.Repository");
        importSet.add("java.util.List");
        StringBuilder methods = new StringBuilder();
        for (GeneratorConfig.QueryConfig query : context.getAggregate().getQueries()) {
            String criteriaClassName = criteriaClassName(query);
            importSet.add(basePackage + "." + domainPackage(context) + ".gateway.query." + criteriaClassName);
            methods.append("\n")
                    .append("    /**\n")
                    .append("     * 执行 ").append(query.getName()).append(" 查询。\n")
                    .append("     *\n")
                    .append("     * @param criteria 领域查询条件\n")
                    .append("     * @return 查询结果\n")
                    .append("     */\n")
                    .append("    @Override\n")
                    .append("    public List<").append(aggregateName).append("> ").append(queryGatewayMethodName(query))
                    .append("(").append(criteriaClassName).append(" criteria) {\n")
                    .append("        return List.of();\n")
                    .append("    }\n");
        }
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("imports", imports(importSet));
        model.put("aggregateName", aggregateName);
        model.put("methods", methods.toString());
        return renderTemplate(TemplateRegistry.INFRA_MP_QUERY_GATEWAY_IMPL, packageName, aggregateName + "QueryGatewayImpl", aggregateName + " MP 查询网关实现", model);
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
        files.add(javaTestFile(context, "application/" + aggregateName + "ManageTest.java", renderTestClass(context, "application", aggregateName + "ManageTest", aggregateName + " 应用服务测试"), WritePolicy.CREATE_ONLY));
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
        String templateName = TemplateRegistry.TEST_DOMAIN;
        if (className.endsWith("RepositoryJpaIntegrationTest")) {
            templateName = TemplateRegistry.TEST_JPA_REPOSITORY_INTEGRATION;
        } else if (className.endsWith("MapperIntegrationTest")) {
            templateName = TemplateRegistry.TEST_MP_MAPPER_INTEGRATION;
        }
        return renderTemplate(templateName, packageName, className, description, Map.of());
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
        return renderTemplate(TemplateRegistry.DOMAIN_DOMAIN_SERVICE, packageName, className, description, Map.of());
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
        String templateName = subPackage.contains(".mp.") ? TemplateRegistry.INFRA_MP_CONVERTER : TemplateRegistry.INFRA_JPA_CONVERTER;
        return renderTemplate(templateName, packageName, className, description, Map.of());
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
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("basePackage", basePackage);
        String content = renderTemplate(TemplateRegistry.TEST_ARCHITECTURE, packageName, "ArchitectureTest", "DDD/COLA 架构约束测试", model);
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
     * 追加 Manage 接口方法。
     *
     * @param context       生成上下文
     * @param methods       方法源码
     * @param useCaseName   用例名称
     * @param requestType   请求类型
     * @param resultType    结果类型
     * @param parameterName 参数名称
     * @param parameterDesc 参数描述
     */
    private void appendManageMethod(GenerationContext context, StringBuilder methods, String useCaseName, String requestType, String resultType, String parameterName, String parameterDesc) {
        methods.append("    /**\n")
                .append("     * 执行 ").append(useCaseName).append(" 用例。\n")
                .append("     *\n")
                .append("     * @param ").append(parameterName).append(" ").append(parameterDesc).append("\n")
                .append("     * @return 执行结果\n")
                .append("     */\n")
                .append("    ").append(resultType).append(" ").append(context.getNaming().classToField(useCaseName))
                .append("(").append(requestType).append(" ").append(parameterName).append(");\n\n");
    }

    /**
     * 追加 Manage 实现方法。
     *
     * @param context       生成上下文
     * @param methods       方法源码
     * @param useCaseName   用例名称
     * @param requestType   请求类型
     * @param resultType    结果类型
     * @param parameterName 参数名称
     * @param executorField 执行器字段
     * @param transactional 是否事务方法
     */
    private void appendManageImplMethod(GenerationContext context, StringBuilder methods, String useCaseName, String requestType, String resultType,
                                        String parameterName, String executorField, boolean transactional) {
        methods.append("\n")
                .append("    /**\n")
                .append("     * 执行 ").append(useCaseName).append(" 用例。\n")
                .append("     *\n")
                .append("     * @param ").append(parameterName).append(" 用例入参\n")
                .append("     * @return 执行结果\n")
                .append("     */\n")
                .append("    @Override\n");
        if (transactional) {
            methods.append("    @Transactional(rollbackFor = Exception.class)\n");
        }
        methods.append("    public ").append(resultType).append(" ").append(context.getNaming().classToField(useCaseName))
                .append("(").append(requestType).append(" ").append(parameterName).append(") {\n")
                .append("        return ").append(executorField).append(".execute(").append(parameterName).append(");\n")
                .append("    }\n");
    }

    /**
     * 追加应用层转换方法。
     *
     * @param methods     方法源码
     * @param useCaseName 用例名称
     * @param aggregate   聚合类型
     * @param resultType  结果类型
     */
    private void appendApplicationAssemblerMethod(StringBuilder methods, String useCaseName, String aggregate, String resultType) {
        methods.append("\n")
                .append("    /**\n")
                .append("     * 将领域模型转换为 ").append(resultType).append("。\n")
                .append("     *\n")
                .append("     * @param aggregate 领域模型\n")
                .append("     * @return 应用层结果\n")
                .append("     */\n")
                .append("    public ").append(resultType).append(" ").append(applicationAssemblerMethodName(useCaseName))
                .append("(").append(aggregate).append(" aggregate) {\n")
                .append("        return new ").append(resultType).append("();\n")
                .append("    }\n");
    }

    /**
     * 追加查询应用层转换方法。
     *
     * @param methods     方法源码
     * @param useCaseName 用例名称
     * @param aggregate   聚合类型
     * @param resultType  结果类型
     */
    private void appendQueryApplicationAssemblerMethod(StringBuilder methods, String useCaseName, String aggregate, String resultType) {
        methods.append("\n")
                .append("    /**\n")
                .append("     * 将领域模型列表转换为 ").append(resultType).append("。\n")
                .append("     *\n")
                .append("     * @param aggregates 领域模型列表\n")
                .append("     * @return 应用层结果\n")
                .append("     */\n")
                .append("    public ").append(resultType).append(" ").append(applicationAssemblerMethodName(useCaseName))
                .append("(List<").append(aggregate).append("> aggregates) {\n")
                .append("        return new ").append(resultType).append("();\n")
                .append("    }\n");
    }

    /**
     * 追加应用层 Query 到领域 Criteria 的转换方法。
     *
     * @param context           生成上下文
     * @param methods           方法源码
     * @param query             Query 配置
     * @param queryClassName    应用层 Query 类名
     * @param criteriaClassName 领域 Criteria 类名
     */
    private void appendQueryCriteriaAssemblerMethod(GenerationContext context, StringBuilder methods, GeneratorConfig.QueryConfig query,
                                                    String queryClassName, String criteriaClassName) {
        List<PgColumnModel> fields = query.getFilters().stream()
                .flatMap(filter -> context.getRootTable().getColumn(filter).stream())
                .toList();
        methods.append("\n")
                .append("    /**\n")
                .append("     * 将应用层查询对象转换为领域查询条件。\n")
                .append("     *\n")
                .append("     * @param query 应用层查询对象\n")
                .append("     * @return 领域查询条件\n")
                .append("     */\n")
                .append("    public ").append(criteriaClassName).append(" ").append(criteriaAssemblerMethodName(query))
                .append("(").append(queryClassName).append(" query) {\n")
                .append("        ").append(criteriaClassName).append(" criteria = new ").append(criteriaClassName).append("();\n")
                .append(copyQueryAssignments(context, "criteria", "query", fields))
                .append("        return criteria;\n")
                .append("    }\n");
    }

    /**
     * 追加 Web 请求到 Command 的转换方法。
     *
     * @param context          生成上下文
     * @param methods          方法源码
     * @param useCaseName      用例名称
     * @param requestClassName 请求类名
     * @param commandClassName 命令类名
     */
    private void appendWebAssemblerCommandMethod(GenerationContext context, StringBuilder methods, String useCaseName, String requestClassName, String commandClassName) {
        methods.append("\n")
                .append("    /**\n")
                .append("     * 将 Web 请求转换为应用层命令。\n")
                .append("     *\n")
                .append("     * @param request Web 请求\n")
                .append("     * @return 应用层命令\n")
                .append("     */\n")
                .append("    public ").append(commandClassName).append(" ").append(webToCommandMethodName(useCaseName))
                .append("(").append(requestClassName).append(" request) {\n")
                .append("        ").append(commandClassName).append(" command = new ").append(commandClassName).append("();\n")
                .append(copyAssignments(context, "command", "request", businessColumns(context.getRootTable())))
                .append("        return command;\n")
                .append("    }\n");
    }

    /**
     * 追加 Web 请求到 Query 的转换方法。
     *
     * @param context        生成上下文
     * @param methods        方法源码
     * @param query          查询配置
     * @param requestClass   请求类名
     * @param queryClassName 查询类名
     */
    private void appendWebAssemblerQueryMethod(GenerationContext context, StringBuilder methods, GeneratorConfig.QueryConfig query, String requestClass, String queryClassName) {
        List<PgColumnModel> fields = query.getFilters().stream()
                .flatMap(filter -> context.getRootTable().getColumn(filter).stream())
                .toList();
        methods.append("\n")
                .append("    /**\n")
                .append("     * 将 Web 请求转换为应用层查询。\n")
                .append("     *\n")
                .append("     * @param request Web 请求\n")
                .append("     * @return 应用层查询\n")
                .append("     */\n")
                .append("    public ").append(queryClassName).append(" ").append(webToQueryMethodName(query.getName()))
                .append("(").append(requestClass).append(" request) {\n")
                .append("        ").append(queryClassName).append(" query = new ").append(queryClassName).append("();\n")
                .append(copyQueryAssignments(context, "query", "request", fields))
                .append("        return query;\n")
                .append("    }\n");
    }

    /**
     * 追加应用层 Result 到 Web VO 的转换方法。
     *
     * @param context     生成上下文
     * @param methods     方法源码
     * @param useCaseName 用例名称
     * @param resultClass 应用结果类名
     * @param voClass     Web VO 类名
     */
    private void appendWebAssemblerVoMethod(GenerationContext context, StringBuilder methods, String useCaseName, String resultClass, String voClass) {
        methods.append("\n")
                .append("    /**\n")
                .append("     * 将应用层结果转换为 Web 视图对象。\n")
                .append("     *\n")
                .append("     * @param result 应用层结果\n")
                .append("     * @return Web 视图对象\n")
                .append("     */\n")
                .append("    public ").append(voClass).append(" ").append(webToVoMethodName(useCaseName))
                .append("(").append(resultClass).append(" result) {\n")
                .append("        ").append(voClass).append(" vo = new ").append(voClass).append("();\n")
                .append(copyAssignments(context, "vo", "result", context.getRootTable().getColumns()))
                .append("        return vo;\n")
                .append("    }\n");
    }

    /**
     * 追加带 Jakarta Validation 注解的类字段。
     *
     * @param context 生成上下文
     * @param fields  字段源码
     * @param column  字段模型
     */
    private void appendValidatedClassField(GenerationContext context, StringBuilder fields, PgColumnModel column) {
        String javaType = context.getTypeMapper().toJavaType(column, context.getNaming());
        fields.append("    /**\n")
                .append("     * ").append(fieldComment(column)).append("\n")
                .append("     */\n");
        if (!column.isNullable()) {
            fields.append("    @NotNull\n");
        }
        if ("String".equals(javaType) && column.getLength() != null) {
            fields.append("    @Size(max = ").append(column.getLength()).append(")\n");
        }
        if ("BigDecimal".equals(javaType) && column.getPrecision() != null && column.getScale() != null) {
            fields.append("    @Digits(integer = ").append(column.getPrecision() - column.getScale())
                    .append(", fraction = ").append(column.getScale()).append(")\n");
        }
        fields.append("    private ").append(javaType).append(" ")
                .append(context.getNaming().columnToField(column.getColumnName())).append(";\n\n");
    }

    /**
     * 追加普通类字段。
     *
     * @param context 生成上下文
     * @param fields  字段源码
     * @param column  字段模型
     */
    private void appendClassField(GenerationContext context, StringBuilder fields, PgColumnModel column) {
        fields.append("    /**\n")
                .append("     * ").append(fieldComment(column)).append("\n")
                .append("     */\n")
                .append("    private ").append(context.getTypeMapper().toJavaType(column, context.getNaming()))
                .append(" ").append(context.getNaming().columnToField(column.getColumnName())).append(";\n\n");
    }

    /**
     * 追加查询类字段。
     *
     * @param context 生成上下文
     * @param fields  字段源码
     * @param column  字段模型
     */
    private void appendQueryClassField(GenerationContext context, StringBuilder fields, PgColumnModel column) {
        String javaType = context.getTypeMapper().toJavaType(column, context.getNaming());
        String fieldName = context.getNaming().columnToField(column.getColumnName());
        if (List.of("Instant", "LocalDateTime", "LocalDate").contains(javaType)) {
            fields.append("    /**\n")
                    .append("     * ").append(fieldComment(column)).append("开始。\n")
                    .append("     */\n")
                    .append("    private ").append(javaType).append(" ").append(fieldName).append("Start;\n\n")
                    .append("    /**\n")
                    .append("     * ").append(fieldComment(column)).append("结束。\n")
                    .append("     */\n")
                    .append("    private ").append(javaType).append(" ").append(fieldName).append("End;\n\n");
        } else {
            appendClassField(context, fields, column);
        }
    }

    /**
     * 复制同名字段。
     *
     * @param context 生成上下文
     * @param target  目标变量
     * @param source  来源变量
     * @param columns 字段列表
     * @return 赋值源码
     */
    private String copyAssignments(GenerationContext context, String target, String source, List<PgColumnModel> columns) {
        StringBuilder builder = new StringBuilder();
        for (PgColumnModel column : columns) {
            String fieldName = context.getNaming().columnToField(column.getColumnName());
            appendSetterAssignment(builder, target, source, fieldName);
        }
        return builder.toString();
    }

    /**
     * 复制查询字段。
     *
     * @param context 生成上下文
     * @param target  目标变量
     * @param source  来源变量
     * @param columns 字段列表
     * @return 赋值源码
     */
    private String copyQueryAssignments(GenerationContext context, String target, String source, List<PgColumnModel> columns) {
        StringBuilder builder = new StringBuilder();
        for (PgColumnModel column : columns) {
            String javaType = context.getTypeMapper().toJavaType(column, context.getNaming());
            String fieldName = context.getNaming().columnToField(column.getColumnName());
            if (List.of("Instant", "LocalDateTime", "LocalDate").contains(javaType)) {
                appendSetterAssignment(builder, target, source, fieldName + "Start");
                appendSetterAssignment(builder, target, source, fieldName + "End");
            } else {
                appendSetterAssignment(builder, target, source, fieldName);
            }
        }
        return builder.toString();
    }

    /**
     * 追加 setter 赋值语句。
     *
     * @param builder   源码
     * @param target    目标变量
     * @param source    来源变量
     * @param fieldName 字段名
     */
    private void appendSetterAssignment(StringBuilder builder, String target, String source, String fieldName) {
        String methodSuffix = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        builder.append("        ").append(target).append(".set").append(methodSuffix)
                .append("(").append(source).append(".get").append(methodSuffix).append("());\n");
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
     * 根据 Query 配置生成结果对象类名。
     *
     * @param query Query 配置
     * @return 结果对象类名
     */
    private String resultClassName(GeneratorConfig.QueryConfig query) {
        if (query.getName().startsWith("Page")) {
            return query.getName().substring("Page".length()) + "PageResult";
        }
        if (query.getName().startsWith("Get")) {
            return query.getName().substring("Get".length()) + "Result";
        }
        if (query.getName().startsWith("List")) {
            return query.getName().substring("List".length()) + "Result";
        }
        return query.getName() + "Result";
    }

    /**
     * 根据 Query 配置生成领域查询条件类名。
     *
     * @param query Query 配置
     * @return 领域查询条件类名
     */
    private String criteriaClassName(GeneratorConfig.QueryConfig query) {
        if (query.getName().startsWith("Page")) {
            return query.getName().substring("Page".length()) + "PageCriteria";
        }
        if (query.getName().startsWith("Get")) {
            return query.getName().substring("Get".length()) + "Criteria";
        }
        if (query.getName().startsWith("List")) {
            return query.getName().substring("List".length()) + "Criteria";
        }
        return query.getName() + "Criteria";
    }

    /**
     * 根据 Command 配置生成结果对象类名。
     *
     * @param command Command 配置
     * @return 结果对象类名
     */
    private String commandResultClassName(GeneratorConfig.UseCaseConfig command) {
        return command.getName() + "Result";
    }

    /**
     * 生成 Web 请求 DTO 类名。
     *
     * @param useCaseName 用例名称
     * @return Web 请求 DTO 类名
     */
    private String requestDtoClassName(String useCaseName) {
        return useCaseName + "RequestDTO";
    }

    /**
     * 生成命令 Web VO 类名。
     *
     * @param useCaseName 用例名称
     * @return Web VO 类名
     */
    private String voClassName(String useCaseName) {
        return useCaseName + "VO";
    }

    /**
     * 生成查询 Web VO 类名。
     *
     * @param query Query 配置
     * @return Web VO 类名
     */
    private String queryVoClassName(GeneratorConfig.QueryConfig query) {
        if (query.getName().startsWith("Page")) {
            return query.getName().substring("Page".length()) + "PageVO";
        }
        if (query.getName().startsWith("Get")) {
            return query.getName().substring("Get".length()) + "VO";
        }
        if (query.getName().startsWith("List")) {
            return query.getName().substring("List".length()) + "VO";
        }
        return query.getName() + "VO";
    }

    /**
     * 生成 Web 请求转命令方法名。
     *
     * @param useCaseName 用例名称
     * @return 方法名
     */
    private String webToCommandMethodName(String useCaseName) {
        return "to" + useCaseName + "Command";
    }

    /**
     * 生成 Web 请求转查询方法名。
     *
     * @param useCaseName 用例名称
     * @return 方法名
     */
    private String webToQueryMethodName(String useCaseName) {
        return "to" + useCaseName + "Query";
    }

    /**
     * 生成 Web VO 转换方法名。
     *
     * @param useCaseName 用例名称
     * @return 方法名
     */
    private String webToVoMethodName(String useCaseName) {
        if (useCaseName.startsWith("Page")) {
            return "to" + useCaseName.substring("Page".length()) + "PageVO";
        }
        if (useCaseName.startsWith("Get")) {
            return "to" + useCaseName.substring("Get".length()) + "VO";
        }
        if (useCaseName.startsWith("List")) {
            return "to" + useCaseName.substring("List".length()) + "VO";
        }
        return "to" + voClassName(useCaseName);
    }

    /**
     * 生成应用层转换方法名。
     *
     * @param useCaseName 用例名称
     * @return 方法名
     */
    private String applicationAssemblerMethodName(String useCaseName) {
        return "to" + useCaseName + "Result";
    }

    /**
     * 生成应用层 Query 到领域 Criteria 的转换方法名。
     *
     * @param query Query 配置
     * @return 方法名
     */
    private String criteriaAssemblerMethodName(GeneratorConfig.QueryConfig query) {
        return "to" + criteriaClassName(query);
    }

    /**
     * 生成查询网关方法名。
     *
     * @param query Query 配置
     * @return 方法名
     */
    private String queryGatewayMethodName(GeneratorConfig.QueryConfig query) {
        return contextFreeFieldName(query.getName());
    }

    /**
     * 生成领域分包名。
     *
     * @param context 生成上下文
     * @return domain 子包名
     */
    private String domainPackage(GenerationContext context) {
        return "domain." + context.getAggregate().getName().toLowerCase(Locale.ROOT);
    }

    /**
     * 生成领域分包路径。
     *
     * @param context 生成上下文
     * @return domain 子包路径
     */
    private String domainPackagePath(GenerationContext context) {
        return domainPackage(context).replace('.', '/');
    }

    /**
     * 获取字段注释。
     *
     * @param column 字段模型
     * @return 字段注释
     */
    private String fieldComment(PgColumnModel column) {
        if (column.getColumnComment() == null || column.getColumnComment().isBlank()) {
            return column.getColumnName() + "。";
        }
        String comment = column.getColumnComment().split(";", 2)[0].trim();
        return comment.endsWith("。") ? comment : comment + "。";
    }

    /**
     * 获取字段方法名。
     *
     * @param className 类名
     * @return 字段方法名
     */
    private String contextFreeFieldName(String className) {
        if (className == null || className.isBlank()) {
            return className;
        }
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    /**
     * 获取 Controller 根路径。
     *
     * @param context 生成上下文
     * @return Controller 根路径
     */
    private String controllerBasePath(GenerationContext context) {
        return context.getAggregate().getCommands().stream()
                .map(GeneratorConfig.UseCaseConfig::getPath)
                .filter(path -> path != null && !path.isBlank())
                .findFirst()
                .or(() -> context.getAggregate().getQueries().stream()
                        .map(GeneratorConfig.QueryConfig::getPath)
                        .filter(path -> path != null && !path.isBlank())
                        .findFirst())
                .map(this::firstPathSegment)
                .orElse("/" + context.getNaming().classToField(context.getAggregate().getName()) + "s");
    }

    /**
     * 截取用例路径的第一段作为 Controller 根路径。
     *
     * @param path 用例路径
     * @return 第一段路径
     */
    private String firstPathSegment(String path) {
        String normalized = path.startsWith("/") ? path : "/" + path;
        int nextSlash = normalized.indexOf('/', 1);
        return nextSlash < 0 ? normalized : normalized.substring(0, nextSlash);
    }

    /**
     * 获取方法级路径。
     *
     * @param basePath    Controller 根路径
     * @param useCasePath 用例完整路径
     * @return 方法级路径
     */
    private String relativeUseCasePath(String basePath, String useCasePath) {
        if (useCasePath == null || useCasePath.isBlank()) {
            return "";
        }
        String normalized = useCasePath.startsWith("/") ? useCasePath : "/" + useCasePath;
        if (normalized.equals(basePath)) {
            return "";
        }
        if (normalized.startsWith(basePath + "/")) {
            return normalized.substring(basePath.length());
        }
        return normalized;
    }

    /**
     * 获取 Spring Mapping 注解名称。
     *
     * @param method            HTTP 方法
     * @param defaultAnnotation 默认注解
     * @return Mapping 注解名称
     */
    private String mappingAnnotation(String method, String defaultAnnotation) {
        if (method == null) {
            return defaultAnnotation;
        }
        return switch (method.toUpperCase(Locale.ROOT)) {
            case "GET" -> "GetMapping";
            case "POST" -> "PostMapping";
            default -> defaultAnnotation;
        };
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
     * 追加通用模板参数并渲染 Freemarker 模板。
     *
     * @param templateName 模板名称
     * @param packageName  包名
     * @param className    类名
     * @param description  类描述
     * @param model        扩展模板参数
     * @return 渲染后的源码
     */
    private String renderTemplate(String templateName, String packageName, String className, String description, Map<String, Object> model) {
        Map<String, Object> templateModel = new LinkedHashMap<>(model);
        templateModel.put("packageName", packageName);
        templateModel.put("className", className);
        templateModel.put("classComment", classComment(packageName, className, description));
        return templateEngine.render(templateName, templateModel);
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
                imports.add(context.getConfig().getProject().getBasePackage() + "." + domainPackage(context) + ".model.enums." + javaType);
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
