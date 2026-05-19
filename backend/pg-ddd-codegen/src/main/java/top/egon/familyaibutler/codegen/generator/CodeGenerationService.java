package top.egon.familyaibutler.codegen.generator;

import top.egon.familyaibutler.codegen.config.GeneratorConfig;
import top.egon.familyaibutler.codegen.config.GeneratorConfigLoader;
import top.egon.familyaibutler.codegen.ddl.DdlFileLoader;
import top.egon.familyaibutler.codegen.ddl.PureParseSchemaReader;
import top.egon.familyaibutler.codegen.postgres.PgCatalogSchemaReader;
import top.egon.familyaibutler.codegen.schema.PgEnumModel;
import top.egon.familyaibutler.codegen.schema.PgSchemaModel;
import top.egon.familyaibutler.codegen.schema.PgTableModel;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.generator
 * @ClassName: CodeGenerationService
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: DDD/COLA 代码生成主流程服务
 * @Version: 1.0
 */
public class CodeGenerationService {

    /**
     * 根据 generator.yml 执行代码生成。
     *
     * @param configPath generator.yml 路径
     * @return 生成摘要
     */
    public GenerationSummary generate(Path configPath) {
        GeneratorConfig config = new GeneratorConfigLoader().load(configPath);
        validate(config);
        List<String> ddlTexts = new DdlFileLoader().load(config.getDdl().getInput());
        List<String> warnings = new ArrayList<>();
        PgSchemaModel schemaModel = readSchema(config, ddlTexts, warnings);
        applyIgnoreTables(config, schemaModel);
        applyConfiguredEnums(config, schemaModel, warnings);
        ensureAggregates(config, schemaModel, warnings);
        List<GenerationContext> contexts = buildContexts(config, schemaModel);
        List<GeneratedSourceFile> sourceFiles = new ArrayList<>();
        DddSourceRenderer renderer = new DddSourceRenderer();
        for (GenerationContext context : contexts) {
            sourceFiles.addAll(renderer.render(context));
        }
        sourceFiles.addAll(renderer.renderProjectResources(config));
        List<GeneratedFileRecord> records = new SourceFileWriter().write(config.getProject().getOutputDir(), sourceFiles);
        new GenerationReportWriter().write(config.getProject().getOutputDir(), contexts, warnings);
        new GeneratedIndexWriter().write(config.getProject().getOutputDir(), records);
        return GenerationSummary.builder()
                .generatedFiles(records)
                .warnings(warnings)
                .build();
    }

    /**
     * 校验必要配置。
     *
     * @param config 生成器配置
     */
    private void validate(GeneratorConfig config) {
        if (config.getProject().getBasePackage() == null || config.getProject().getBasePackage().isBlank()) {
            throw new IllegalArgumentException("project.basePackage 不能为空");
        }
        if (config.getProject().getOutputDir() == null) {
            throw new IllegalArgumentException("project.outputDir 不能为空");
        }
        if (config.getDdl().getInput().isEmpty()) {
            throw new IllegalArgumentException("ddl.input 不能为空");
        }
    }

    /**
     * 根据配置读取 Schema。
     *
     * @param config   生成器配置
     * @param ddlTexts DDL 文本列表
     * @param warnings 警告列表
     * @return Schema 元模型
     */
    private PgSchemaModel readSchema(GeneratorConfig config, List<String> ddlTexts, List<String> warnings) {
        String mode = config.getDdl().getMode().toLowerCase(Locale.ROOT);
        if ("catalog".equals(mode)) {
            if (config.getDdl().getPostgres().getJdbcUrl() == null || config.getDdl().getPostgres().getJdbcUrl().isBlank()) {
                warnings.add("Catalog Mode 未配置 ddl.postgres.jdbcUrl，v1 自动回退到 Pure Parse Mode。");
                return new PureParseSchemaReader().read(config.getDdl().getPostgres().getSchema(), ddlTexts);
            }
            return new PgCatalogSchemaReader().read(config.getDdl().getPostgres(), ddlTexts);
        }
        return new PureParseSchemaReader().read(config.getDdl().getPostgres().getSchema(), ddlTexts);
    }

    /**
     * 应用忽略表配置。
     *
     * @param config      生成器配置
     * @param schemaModel Schema 元模型
     */
    private void applyIgnoreTables(GeneratorConfig config, PgSchemaModel schemaModel) {
        schemaModel.getTables().removeIf(table -> config.getIgnore().getTables().contains(table.getTableName()));
    }

    /**
     * 应用 generator.yml 中显式定义的枚举，并把配置的表字段标记为领域枚举。
     *
     * @param config      生成器配置
     * @param schemaModel Schema 元模型
     * @param warnings    警告列表
     */
    private void applyConfiguredEnums(GeneratorConfig config, PgSchemaModel schemaModel, List<String> warnings) {
        for (GeneratorConfig.EnumConfig enumConfig : config.getEnums()) {
            PgEnumModel enumModel = schemaModel.getEnum(enumConfig.getName()).orElseGet(() -> {
                PgEnumModel created = PgEnumModel.builder()
                        .typeName(enumConfig.getName())
                        .javaName(enumConfig.getName())
                        .description(enumConfig.getDescription())
                        .build();
                schemaModel.getEnums().add(created);
                return created;
            });
            enumModel.setJavaName(enumConfig.getName());
            enumModel.setDescription(enumConfig.getDescription());
            enumModel.getValues().clear();
            enumModel.getValueCodes().clear();
            enumModel.getValueDescriptions().clear();
            for (GeneratorConfig.EnumValueConfig value : enumConfig.getValues()) {
                enumModel.getValues().add(value.getName());
                enumModel.getValueCodes().put(value.getName(), value.getCode());
                enumModel.getValueDescriptions().put(value.getName(), value.getDescription());
            }
            markEnumColumns(schemaModel, enumConfig, warnings);
        }
    }

    /**
     * 按自定义枚举配置标记字段。
     *
     * @param schemaModel Schema 元模型
     * @param enumConfig  枚举配置
     * @param warnings    警告列表
     */
    private void markEnumColumns(PgSchemaModel schemaModel, GeneratorConfig.EnumConfig enumConfig, List<String> warnings) {
        for (String columnPath : enumConfig.getColumns()) {
            String[] parts = columnPath.split("\\.", 2);
            if (parts.length == 2) {
                schemaModel.getTable(parts[0]).flatMap(table -> table.getColumn(parts[1])).ifPresentOrElse(column -> {
                    column.setEnumType(true);
                    column.setEnumTypeName(enumConfig.getName());
                    column.setEnumJavaName(enumConfig.getName());
                }, () -> warnings.add("自定义枚举 " + enumConfig.getName() + " 指定的字段不存在: " + columnPath));
            } else {
                schemaModel.getTables().forEach(table -> table.getColumn(columnPath).ifPresent(column -> {
                    column.setEnumType(true);
                    column.setEnumTypeName(enumConfig.getName());
                    column.setEnumJavaName(enumConfig.getName());
                }));
            }
        }
    }

    /**
     * 在未显式配置聚合时，根据 COMMENT hint 或表名兜底生成聚合配置。
     *
     * @param config      生成器配置
     * @param schemaModel Schema 元模型
     * @param warnings    警告列表
     */
    private void ensureAggregates(GeneratorConfig config, PgSchemaModel schemaModel, List<String> warnings) {
        if (!config.getAggregates().isEmpty()) {
            return;
        }
        Naming naming = new Naming(config.getDdl().getNaming().getTablePrefix());
        for (PgTableModel table : schemaModel.getTables()) {
            GeneratorConfig.AggregateConfig aggregate = new GeneratorConfig.AggregateConfig();
            String aggregateName = readHint(table.getTableComment(), "aggregate");
            if (aggregateName == null) {
                aggregateName = naming.tableToClass(table.getTableName());
                warnings.add(table.getTableName() + " 未配置聚合，v1 按表名推断为 " + aggregateName + "。");
            }
            aggregate.setName(aggregateName);
            aggregate.setRootTable(table.getTableName());
            String finalAggregateName = aggregateName;
            table.getPrimaryKeyColumn().ifPresent(column -> aggregate.setIdValueObject(finalAggregateName + "Id"));
            config.getAggregates().add(aggregate);
        }
    }

    /**
     * 构建聚合生成上下文。
     *
     * @param config      生成器配置
     * @param schemaModel Schema 元模型
     * @return 聚合生成上下文列表
     */
    private List<GenerationContext> buildContexts(GeneratorConfig config, PgSchemaModel schemaModel) {
        List<GenerationContext> contexts = new ArrayList<>();
        Naming naming = new Naming(config.getDdl().getNaming().getTablePrefix());
        PgTypeMapper typeMapper = new PgTypeMapper();
        for (GeneratorConfig.AggregateConfig aggregate : config.getAggregates()) {
            PgTableModel rootTable = schemaModel.getTable(aggregate.getRootTable())
                    .orElseThrow(() -> new IllegalArgumentException("聚合根表不存在: " + aggregate.getRootTable()));
            List<PgTableModel> entityTables = new ArrayList<>();
            for (String tableName : aggregate.getEntityTables()) {
                schemaModel.getTable(tableName).ifPresent(entityTables::add);
            }
            contexts.add(GenerationContext.builder()
                    .config(config)
                    .schemaModel(schemaModel)
                    .aggregate(aggregate)
                    .rootTable(rootTable)
                    .entityTables(entityTables)
                    .naming(naming)
                    .typeMapper(typeMapper)
                    .build());
        }
        return contexts;
    }

    /**
     * 读取注释 hint。
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
