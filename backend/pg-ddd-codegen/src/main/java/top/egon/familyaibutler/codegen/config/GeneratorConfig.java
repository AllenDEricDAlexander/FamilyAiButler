package top.egon.familyaibutler.codegen.config;

import lombok.Data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.config
 * @ClassName: GeneratorConfig
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: 代码生成器配置模型
 * @Version: 1.0
 */
@Data
public class GeneratorConfig {
    private Path configPath;
    private ProjectConfig project = new ProjectConfig();
    private DdlConfig ddl = new DdlConfig();
    private DddConfig ddd = new DddConfig();
    private List<EnumConfig> enums = new ArrayList<>();
    private List<AggregateConfig> aggregates = new ArrayList<>();
    private IgnoreConfig ignore = new IgnoreConfig();

    /**
     * 项目基础配置。
     */
    @Data
    public static class ProjectConfig {
        private String basePackage;
        private String moduleName;
        private Path outputDir;
        private Integer javaVersion = 21;
    }

    /**
     * DDL 输入与解析配置。
     */
    @Data
    public static class DdlConfig {
        private List<Path> input = new ArrayList<>();
        private String mode = "parse";
        private PostgresConfig postgres = new PostgresConfig();
        private NamingConfig naming = new NamingConfig();
    }

    /**
     * PostgreSQL 连接配置。
     */
    @Data
    public static class PostgresConfig {
        private String version = "16";
        private String schema = "public";
        private String jdbcUrl;
        private String username;
        private String password;
    }

    /**
     * 数据库命名转 Java 命名配置。
     */
    @Data
    public static class NamingConfig {
        private List<String> tablePrefix = new ArrayList<>();
        private boolean columnToCamel = true;
    }

    /**
     * DDD 限界上下文配置。
     */
    @Data
    public static class DddConfig {
        private String boundedContext;
    }

    /**
     * 聚合配置。
     */
    @Data
    public static class AggregateConfig {
        private String name;
        private String rootTable;
        private List<String> entityTables = new ArrayList<>();
        private String repository = "jpa";
        private String queryGateway = "mp";
        private String idValueObject;
        private List<String> businessKeys = new ArrayList<>();
        private Map<String, String> valueObjects = new LinkedHashMap<>();
        private List<UseCaseConfig> commands = new ArrayList<>();
        private List<QueryConfig> queries = new ArrayList<>();
    }

    /**
     * Command 用例配置。
     */
    @Data
    public static class UseCaseConfig {
        private String name;
        private String path;
        private String method;
    }

    /**
     * Query 用例配置。
     */
    @Data
    public static class QueryConfig {
        private String name;
        private String path;
        private String method;
        private String persistence;
        private List<String> filters = new ArrayList<>();
    }

    /**
     * 自定义枚举配置。
     */
    @Data
    public static class EnumConfig {
        private String name;
        private String description;
        private List<String> columns = new ArrayList<>();
        private List<EnumValueConfig> values = new ArrayList<>();
    }

    /**
     * 自定义枚举项配置。
     */
    @Data
    public static class EnumValueConfig {
        private String name;
        private String code;
        private String description;
    }

    /**
     * 忽略配置。
     */
    @Data
    public static class IgnoreConfig {
        private List<String> tables = new ArrayList<>();
    }
}
