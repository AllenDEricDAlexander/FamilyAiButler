package top.egon.familyaibutler.codegen.postgres;

import top.egon.familyaibutler.codegen.config.GeneratorConfig;
import top.egon.familyaibutler.codegen.ddl.DdlStatementSplitter;
import top.egon.familyaibutler.codegen.schema.ConstraintType;
import top.egon.familyaibutler.codegen.schema.PgColumnModel;
import top.egon.familyaibutler.codegen.schema.PgConstraintModel;
import top.egon.familyaibutler.codegen.schema.PgEnumModel;
import top.egon.familyaibutler.codegen.schema.PgIndexModel;
import top.egon.familyaibutler.codegen.schema.PgSchemaModel;
import top.egon.familyaibutler.codegen.schema.PgTableModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.postgres
 * @ClassName: PgCatalogSchemaReader
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: Shadow PostgreSQL Catalog Mode Schema Reader
 * @Version: 1.0
 */
public class PgCatalogSchemaReader {

    /**
     * 执行 DDL 并从 information_schema 与 pg_catalog 读取元数据。
     *
     * @param postgres PostgreSQL 配置
     * @param ddlTexts DDL 文本列表
     * @return Schema 元模型
     */
    public PgSchemaModel read(GeneratorConfig.PostgresConfig postgres, List<String> ddlTexts) {
        if (postgres.getJdbcUrl() == null || postgres.getJdbcUrl().isBlank()) {
            throw new IllegalArgumentException("Catalog Mode 需要配置 ddl.postgres.jdbcUrl");
        }
        try (Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            executeDdl(connection, ddlTexts);
            PgSchemaModel schemaModel = PgSchemaModel.builder().schemaName(postgres.getSchema()).build();
            readTables(connection, schemaModel);
            readColumns(connection, schemaModel);
            readConstraints(connection, schemaModel);
            readIndexes(connection, schemaModel);
            readEnums(connection, schemaModel);
            return schemaModel;
        } catch (SQLException e) {
            throw new IllegalStateException("Catalog Mode 读取 PostgreSQL 元数据失败", e);
        }
    }

    /**
     * 在 shadow database 中执行 DDL。
     *
     * @param connection 数据库连接
     * @param ddlTexts   DDL 文本列表
     * @throws SQLException SQL 执行异常
     */
    private void executeDdl(Connection connection, List<String> ddlTexts) throws SQLException {
        DdlStatementSplitter splitter = new DdlStatementSplitter();
        try (Statement statement = connection.createStatement()) {
            for (String ddlText : ddlTexts) {
                for (String sql : splitter.split(ddlText)) {
                    statement.execute(sql);
                }
            }
        }
    }

    /**
     * 读取表和表注释。
     *
     * @param connection  数据库连接
     * @param schemaModel Schema 元模型
     * @throws SQLException SQL 查询异常
     */
    private void readTables(Connection connection, PgSchemaModel schemaModel) throws SQLException {
        String sql = """
                SELECT c.relname AS table_name,
                       obj_description(c.oid) AS table_comment,
                       c.relkind = 'p' AS partitioned,
                       c.relkind = 'v' AS view
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname = ?
                  AND c.relkind IN ('r', 'p', 'v')
                ORDER BY c.relname
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schemaModel.getSchemaName());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    schemaModel.getTables().add(PgTableModel.builder()
                            .schemaName(schemaModel.getSchemaName())
                            .tableName(resultSet.getString("table_name"))
                            .tableComment(resultSet.getString("table_comment"))
                            .partitioned(resultSet.getBoolean("partitioned"))
                            .view(resultSet.getBoolean("view"))
                            .build());
                }
            }
        }
    }

    /**
     * 读取字段、字段注释和字段形状。
     *
     * @param connection  数据库连接
     * @param schemaModel Schema 元模型
     * @throws SQLException SQL 查询异常
     */
    private void readColumns(Connection connection, PgSchemaModel schemaModel) throws SQLException {
        String sql = """
                SELECT t.relname AS table_name,
                       a.attname AS column_name,
                       col_description(t.oid, a.attnum) AS column_comment,
                       format_type(a.atttypid, a.atttypmod) AS pg_type,
                       NOT a.attnotnull AS nullable,
                       pg_get_expr(d.adbin, d.adrelid) AS default_expression,
                       a.attidentity <> '' AS identity
                FROM pg_attribute a
                JOIN pg_class t ON t.oid = a.attrelid
                JOIN pg_namespace n ON n.oid = t.relnamespace
                LEFT JOIN pg_attrdef d ON d.adrelid = a.attrelid AND d.adnum = a.attnum
                WHERE n.nspname = ?
                  AND a.attnum > 0
                  AND NOT a.attisdropped
                ORDER BY t.relname, a.attnum
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schemaModel.getSchemaName());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    PgColumnModel column = PgColumnModel.builder()
                            .columnName(resultSet.getString("column_name"))
                            .columnComment(resultSet.getString("column_comment"))
                            .pgType(resultSet.getString("pg_type"))
                            .nullable(resultSet.getBoolean("nullable"))
                            .identity(resultSet.getBoolean("identity"))
                            .serial(defaultLooksSerial(resultSet.getString("default_expression")))
                            .defaultExpression(resultSet.getString("default_expression"))
                            .build();
                    schemaModel.getTable(resultSet.getString("table_name"))
                            .ifPresent(table -> table.getColumns().add(column));
                }
            }
        }
    }

    /**
     * 读取约束信息并同步字段标记。
     *
     * @param connection  数据库连接
     * @param schemaModel Schema 元模型
     * @throws SQLException SQL 查询异常
     */
    private void readConstraints(Connection connection, PgSchemaModel schemaModel) throws SQLException {
        String sql = """
                SELECT tc.table_name,
                       tc.constraint_name,
                       tc.constraint_type,
                       kcu.column_name,
                       ccu.table_name AS referenced_table,
                       ccu.column_name AS referenced_column,
                       cc.check_clause
                FROM information_schema.table_constraints tc
                LEFT JOIN information_schema.key_column_usage kcu
                       ON kcu.constraint_schema = tc.constraint_schema
                      AND kcu.constraint_name = tc.constraint_name
                      AND kcu.table_name = tc.table_name
                LEFT JOIN information_schema.constraint_column_usage ccu
                       ON ccu.constraint_schema = tc.constraint_schema
                      AND ccu.constraint_name = tc.constraint_name
                LEFT JOIN information_schema.check_constraints cc
                       ON cc.constraint_schema = tc.constraint_schema
                      AND cc.constraint_name = tc.constraint_name
                WHERE tc.table_schema = ?
                ORDER BY tc.table_name, tc.constraint_name, kcu.ordinal_position
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schemaModel.getSchemaName());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    addConstraintRow(schemaModel, resultSet);
                }
            }
        }
    }

    /**
     * 读取索引信息。
     *
     * @param connection  数据库连接
     * @param schemaModel Schema 元模型
     * @throws SQLException SQL 查询异常
     */
    private void readIndexes(Connection connection, PgSchemaModel schemaModel) throws SQLException {
        String sql = """
                SELECT schemaname, tablename, indexname, indexdef
                FROM pg_indexes
                WHERE schemaname = ?
                ORDER BY tablename, indexname
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schemaModel.getSchemaName());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String indexDef = resultSet.getString("indexdef");
                    PgIndexModel index = PgIndexModel.builder()
                            .indexName(resultSet.getString("indexname"))
                            .unique(indexDef.toUpperCase().contains("CREATE UNIQUE INDEX"))
                            .partial(indexDef.toUpperCase().contains(" WHERE "))
                            .expressionIndex(indexDef.contains("(("))
                            .indexDefinition(indexDef)
                            .columns(readIndexColumns(indexDef))
                            .build();
                    schemaModel.getTable(resultSet.getString("tablename"))
                            .ifPresent(table -> table.getIndexes().add(index));
                }
            }
        }
    }

    /**
     * 读取 PostgreSQL enum 信息。
     *
     * @param connection  数据库连接
     * @param schemaModel Schema 元模型
     * @throws SQLException SQL 查询异常
     */
    private void readEnums(Connection connection, PgSchemaModel schemaModel) throws SQLException {
        String sql = """
                SELECT t.typname, e.enumlabel
                FROM pg_type t
                JOIN pg_enum e ON e.enumtypid = t.oid
                JOIN pg_namespace n ON n.oid = t.typnamespace
                WHERE n.nspname = ?
                ORDER BY t.typname, e.enumsortorder
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, schemaModel.getSchemaName());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String typeName = resultSet.getString("typname");
                    PgEnumModel enumModel = schemaModel.getEnum(typeName).orElseGet(() -> {
                        PgEnumModel created = PgEnumModel.builder().typeName(typeName).build();
                        schemaModel.getEnums().add(created);
                        return created;
                    });
                    enumModel.getValues().add(resultSet.getString("enumlabel"));
                }
            }
        }
    }

    /**
     * 将约束查询行合并到表模型。
     *
     * @param schemaModel Schema 元模型
     * @param resultSet   结果集
     * @throws SQLException 结果集读取异常
     */
    private void addConstraintRow(PgSchemaModel schemaModel, ResultSet resultSet) throws SQLException {
        PgTableModel table = schemaModel.getTable(resultSet.getString("table_name")).orElse(null);
        if (table == null) {
            return;
        }
        String constraintName = resultSet.getString("constraint_name");
        PgConstraintModel constraint = table.getConstraints().stream()
                .filter(item -> item.getConstraintName().equals(constraintName))
                .findFirst()
                .orElseGet(() -> {
                    PgConstraintModel created = PgConstraintModel.builder()
                            .constraintName(constraintName)
                            .type(toConstraintType(getUnchecked(resultSet, "constraint_type")))
                            .checkExpression(getUnchecked(resultSet, "check_clause"))
                            .referencedTable(getUnchecked(resultSet, "referenced_table"))
                            .build();
                    table.getConstraints().add(created);
                    return created;
                });
        String columnName = resultSet.getString("column_name");
        if (columnName != null && !constraint.getColumns().contains(columnName)) {
            constraint.getColumns().add(columnName);
            table.getColumn(columnName).ifPresent(column -> {
                column.setPrimaryKey(constraint.getType() == ConstraintType.PRIMARY_KEY || column.isPrimaryKey());
                column.setUnique(constraint.getType() == ConstraintType.UNIQUE || column.isUnique());
            });
        }
        String referencedColumn = resultSet.getString("referenced_column");
        if (referencedColumn != null && !constraint.getReferencedColumns().contains(referencedColumn)) {
            constraint.getReferencedColumns().add(referencedColumn);
        }
    }

    /**
     * 转换 information_schema 约束类型。
     *
     * @param typeName 约束类型名称
     * @return 约束类型枚举
     */
    private ConstraintType toConstraintType(String typeName) {
        return switch (typeName) {
            case "PRIMARY KEY" -> ConstraintType.PRIMARY_KEY;
            case "UNIQUE" -> ConstraintType.UNIQUE;
            case "FOREIGN KEY" -> ConstraintType.FOREIGN_KEY;
            default -> ConstraintType.CHECK;
        };
    }

    /**
     * 从索引定义中提取字段列表。
     *
     * @param indexDef 索引定义
     * @return 字段列表
     */
    private List<String> readIndexColumns(String indexDef) {
        int start = indexDef.lastIndexOf('(');
        int end = indexDef.indexOf(')', start);
        if (start < 0 || end < 0) {
            return List.of();
        }
        List<String> columns = new ArrayList<>();
        for (String column : indexDef.substring(start + 1, end).split(",")) {
            columns.add(column.trim().replace("\"", ""));
        }
        return columns;
    }

    /**
     * 判断默认值是否来自 serial 序列。
     *
     * @param defaultExpression 默认值表达式
     * @return 是否为 serial
     */
    private boolean defaultLooksSerial(String defaultExpression) {
        return defaultExpression != null && defaultExpression.contains("nextval(");
    }

    /**
     * 包装 ResultSet 字段读取。
     *
     * @param resultSet 结果集
     * @param column    字段名
     * @return 字段值
     */
    private String getUnchecked(ResultSet resultSet, String column) {
        try {
            return resultSet.getString(column);
        } catch (SQLException e) {
            throw new IllegalStateException("读取元数据字段失败: " + column, e);
        }
    }
}
