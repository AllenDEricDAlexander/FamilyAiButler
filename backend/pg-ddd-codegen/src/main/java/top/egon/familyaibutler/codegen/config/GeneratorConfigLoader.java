package top.egon.familyaibutler.codegen.config;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.config
 * @ClassName: GeneratorConfigLoader
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: generator.yml 配置加载器
 * @Version: 1.0
 */
public class GeneratorConfigLoader {

    /**
     * 读取并转换 generator.yml。
     *
     * @param configPath 配置文件路径
     * @return 生成器配置
     */
    public GeneratorConfig load(Path configPath) {
        try (InputStream inputStream = Files.newInputStream(configPath)) {
            Map<String, Object> yamlMap = new Yaml().load(inputStream);
            GeneratorConfig config = new GeneratorConfig();
            config.setConfigPath(configPath.toAbsolutePath().normalize());
            readProject(config, map(yamlMap.get("project")));
            readDdl(config, map(yamlMap.get("ddl")));
            readDdd(config, map(yamlMap.get("ddd")));
            readEnums(config, list(yamlMap.get("enums")));
            readAggregates(config, list(yamlMap.get("aggregates")));
            readIgnore(config, map(yamlMap.get("ignore")));
            return config;
        } catch (IOException e) {
            throw new IllegalArgumentException("读取 generator.yml 失败: " + configPath, e);
        }
    }

    /**
     * 读取项目配置。
     *
     * @param config 目标配置
     * @param source YAML 节点
     */
    private void readProject(GeneratorConfig config, Map<String, Object> source) {
        GeneratorConfig.ProjectConfig project = config.getProject();
        project.setBasePackage(string(source.get("basePackage")));
        project.setModuleName(string(source.get("moduleName")));
        project.setJavaVersion(integer(source.get("javaVersion"), 21));
        String outputDir = string(source.get("outputDir"));
        if (outputDir != null) {
            Path outputPath = Path.of(outputDir);
            if (!outputPath.isAbsolute()) {
                outputPath = config.getConfigPath().getParent().resolve(outputPath);
            }
            project.setOutputDir(outputPath.normalize());
        }
    }

    /**
     * 读取 DDL 配置。
     *
     * @param config 目标配置
     * @param source YAML 节点
     */
    private void readDdl(GeneratorConfig config, Map<String, Object> source) {
        GeneratorConfig.DdlConfig ddl = config.getDdl();
        ddl.setMode(Objects.requireNonNullElse(string(source.get("mode")), "parse"));
        List<Path> ddlInputs = new ArrayList<>();
        for (Object input : list(source.get("input"))) {
            Path inputPath = Path.of(String.valueOf(input));
            if (!inputPath.isAbsolute()) {
                inputPath = config.getConfigPath().getParent().resolve(inputPath);
            }
            ddlInputs.add(inputPath.normalize());
        }
        ddl.setInput(ddlInputs);
        Map<String, Object> postgres = map(source.get("postgres"));
        ddl.getPostgres().setVersion(Objects.requireNonNullElse(string(postgres.get("version")), "16"));
        ddl.getPostgres().setSchema(Objects.requireNonNullElse(string(postgres.get("schema")), "public"));
        ddl.getPostgres().setJdbcUrl(string(postgres.get("jdbcUrl")));
        ddl.getPostgres().setUsername(string(postgres.get("username")));
        ddl.getPostgres().setPassword(string(postgres.get("password")));
        Map<String, Object> naming = map(source.get("naming"));
        ddl.getNaming().setTablePrefix(strings(naming.get("tablePrefix")));
        ddl.getNaming().setColumnToCamel(booleanValue(naming.get("columnToCamel"), true));
    }

    /**
     * 读取 DDD 配置。
     *
     * @param config 目标配置
     * @param source YAML 节点
     */
    private void readDdd(GeneratorConfig config, Map<String, Object> source) {
        config.getDdd().setBoundedContext(string(source.get("boundedContext")));
    }

    /**
     * 读取聚合配置。
     *
     * @param config 目标配置
     * @param source YAML 节点
     */
    private void readAggregates(GeneratorConfig config, List<Object> source) {
        List<GeneratorConfig.AggregateConfig> aggregates = new ArrayList<>();
        for (Object item : source) {
            Map<String, Object> map = map(item);
            GeneratorConfig.AggregateConfig aggregate = new GeneratorConfig.AggregateConfig();
            aggregate.setName(string(map.get("name")));
            aggregate.setRootTable(string(map.get("rootTable")));
            aggregate.setEntityTables(strings(map.get("entityTables")));
            aggregate.setRepository(Objects.requireNonNullElse(string(map.get("repository")), "jpa"));
            aggregate.setQueryGateway(Objects.requireNonNullElse(string(map.get("queryGateway")), "mp"));
            aggregate.setIdValueObject(string(map.get("idValueObject")));
            aggregate.setBusinessKeys(strings(map.get("businessKeys")));
            aggregate.setValueObjects(stringMap(map.get("valueObjects")));
            aggregate.setCommands(useCases(map.get("commands")));
            aggregate.setQueries(queries(map.get("queries")));
            aggregates.add(aggregate);
        }
        config.setAggregates(aggregates);
    }

    /**
     * 读取自定义枚举配置。
     *
     * @param config 目标配置
     * @param source YAML 节点
     */
    private void readEnums(GeneratorConfig config, List<Object> source) {
        List<GeneratorConfig.EnumConfig> enums = new ArrayList<>();
        for (Object item : source) {
            Map<String, Object> map = map(item);
            GeneratorConfig.EnumConfig enumConfig = new GeneratorConfig.EnumConfig();
            enumConfig.setName(string(map.get("name")));
            enumConfig.setDescription(string(map.get("description")));
            enumConfig.setColumns(strings(map.get("columns")));
            enumConfig.setValues(enumValues(map.get("values")));
            enums.add(enumConfig);
        }
        config.setEnums(enums);
    }

    /**
     * 读取自定义枚举项配置。
     *
     * @param source YAML 节点
     * @return 枚举项配置列表
     */
    private List<GeneratorConfig.EnumValueConfig> enumValues(Object source) {
        List<GeneratorConfig.EnumValueConfig> result = new ArrayList<>();
        for (Object item : list(source)) {
            GeneratorConfig.EnumValueConfig enumValue = new GeneratorConfig.EnumValueConfig();
            if (item instanceof Map<?, ?>) {
                Map<String, Object> map = map(item);
                enumValue.setName(string(map.get("name")));
                enumValue.setCode(Objects.requireNonNullElse(string(map.get("code")), enumValue.getName()));
                enumValue.setDescription(string(map.get("description")));
            } else {
                enumValue.setName(String.valueOf(item));
                enumValue.setCode(String.valueOf(item));
            }
            result.add(enumValue);
        }
        return result;
    }

    /**
     * 读取忽略配置。
     *
     * @param config 目标配置
     * @param source YAML 节点
     */
    private void readIgnore(GeneratorConfig config, Map<String, Object> source) {
        config.getIgnore().setTables(strings(source.get("tables")));
    }

    /**
     * 转换 Command 配置列表。
     *
     * @param source YAML 节点
     * @return Command 配置列表
     */
    private List<GeneratorConfig.UseCaseConfig> useCases(Object source) {
        List<GeneratorConfig.UseCaseConfig> result = new ArrayList<>();
        for (Object item : list(source)) {
            Map<String, Object> map = map(item);
            GeneratorConfig.UseCaseConfig useCase = new GeneratorConfig.UseCaseConfig();
            useCase.setName(string(map.get("name")));
            useCase.setPath(string(map.get("path")));
            useCase.setMethod(string(map.get("method")));
            result.add(useCase);
        }
        return result;
    }

    /**
     * 转换 Query 配置列表。
     *
     * @param source YAML 节点
     * @return Query 配置列表
     */
    private List<GeneratorConfig.QueryConfig> queries(Object source) {
        List<GeneratorConfig.QueryConfig> result = new ArrayList<>();
        for (Object item : list(source)) {
            Map<String, Object> map = map(item);
            GeneratorConfig.QueryConfig query = new GeneratorConfig.QueryConfig();
            query.setName(string(map.get("name")));
            query.setPath(string(map.get("path")));
            query.setMethod(string(map.get("method")));
            query.setPersistence(string(map.get("persistence")));
            query.setFilters(strings(map.get("filters")));
            result.add(query);
        }
        return result;
    }

    /**
     * 转换字符串 Map。
     *
     * @param source YAML 节点
     * @return 字符串 Map
     */
    private Map<String, String> stringMap(Object source) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map(source).entrySet()) {
            result.put(entry.getKey(), string(entry.getValue()));
        }
        return result;
    }

    /**
     * 转换字符串列表。
     *
     * @param source YAML 节点
     * @return 字符串列表
     */
    private List<String> strings(Object source) {
        List<String> result = new ArrayList<>();
        for (Object item : list(source)) {
            result.add(String.valueOf(item));
        }
        return result;
    }

    /**
     * 转换 Map 节点。
     *
     * @param source YAML 节点
     * @return Map 节点
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object source) {
        if (source instanceof Map<?, ?> sourceMap) {
            return (Map<String, Object>) sourceMap;
        }
        return new LinkedHashMap<>();
    }

    /**
     * 转换 List 节点。
     *
     * @param source YAML 节点
     * @return List 节点
     */
    @SuppressWarnings("unchecked")
    private List<Object> list(Object source) {
        if (source instanceof List<?> sourceList) {
            return (List<Object>) sourceList;
        }
        return List.of();
    }

    /**
     * 转换字符串。
     *
     * @param source YAML 节点
     * @return 字符串
     */
    private String string(Object source) {
        return source == null ? null : String.valueOf(source);
    }

    /**
     * 转换整数。
     *
     * @param source       YAML 节点
     * @param defaultValue 默认值
     * @return 整数
     */
    private Integer integer(Object source, Integer defaultValue) {
        return source == null ? defaultValue : Integer.parseInt(String.valueOf(source));
    }

    /**
     * 转换布尔值。
     *
     * @param source       YAML 节点
     * @param defaultValue 默认值
     * @return 布尔值
     */
    private boolean booleanValue(Object source, boolean defaultValue) {
        return source == null ? defaultValue : Boolean.parseBoolean(String.valueOf(source));
    }
}
