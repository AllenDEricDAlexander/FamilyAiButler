package top.egon.familyaibutler.codegen.generator;

import top.egon.familyaibutler.codegen.schema.PgColumnModel;

import java.util.Locale;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.generator
 * @ClassName: PgTypeMapper
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: PostgreSQL 类型到 Java 类型的映射器
 * @Version: 1.0
 */
public class PgTypeMapper {

    /**
     * 获取字段对应的 Java 类型。
     *
     * @param column 字段模型
     * @param naming 命名工具
     * @return Java 类型
     */
    public String toJavaType(PgColumnModel column, Naming naming) {
        if (column.isEnumType()) {
            return toEnumJavaType(column, naming);
        }
        return toScalarJavaType(column);
    }

    /**
     * 获取持久层字段对应的 Java 类型。
     *
     * @param column 字段模型
     * @return Java 类型
     */
    public String toPersistenceJavaType(PgColumnModel column) {
        if (column.isEnumType()) {
            return "String";
        }
        return toScalarJavaType(column);
    }

    /**
     * 获取枚举对应的 Java 类型。
     *
     * @param column 字段模型
     * @param naming 命名工具
     * @return Java 类型
     */
    private String toEnumJavaType(PgColumnModel column, Naming naming) {
        if (column.getEnumJavaName() != null && !column.getEnumJavaName().isBlank()) {
            return column.getEnumJavaName();
        }
        if (column.getEnumTypeName() == null || column.getEnumTypeName().isBlank()) {
            return "String";
        }
        return naming.upperCamel(column.getEnumTypeName());
    }

    /**
     * 获取普通字段对应的 Java 类型。
     *
     * @param column 字段模型
     * @return Java 类型
     */
    private String toScalarJavaType(PgColumnModel column) {
        String type = column.getPgType().toLowerCase(Locale.ROOT);
        if (type.contains("bigserial") || type.equals("bigint") || type.equals("int8")) {
            return "Long";
        }
        if (type.contains("serial") || type.equals("integer") || type.equals("int4")) {
            return "Integer";
        }
        if (type.equals("smallint") || type.equals("int2")) {
            return "Short";
        }
        if (type.startsWith("numeric") || type.startsWith("decimal") || type.equals("money")) {
            return "BigDecimal";
        }
        if (type.startsWith("varchar") || type.startsWith("character varying") || type.equals("text") || type.equals("inet") || type.equals("cidr")) {
            return "String";
        }
        if (type.equals("boolean") || type.equals("bool")) {
            return "Boolean";
        }
        if (type.equals("date")) {
            return "LocalDate";
        }
        if (type.startsWith("timestamp with time zone") || type.equals("timestamptz")) {
            return "Instant";
        }
        if (type.startsWith("timestamp")) {
            return "LocalDateTime";
        }
        if (type.equals("uuid")) {
            return "UUID";
        }
        if (type.equals("json") || type.equals("jsonb")) {
            return "String";
        }
        if (type.equals("bytea")) {
            return "byte[]";
        }
        return "String";
    }

    /**
     * 获取值对象内部承载类型。
     *
     * @param column 字段模型
     * @return Java 类型
     */
    public String toValueObjectType(PgColumnModel column) {
        String type = column.getPgType().toLowerCase(Locale.ROOT);
        if (type.contains("bigserial") || type.equals("bigint") || type.equals("int8")) {
            return "Long";
        }
        if (type.startsWith("numeric") || type.startsWith("decimal") || type.equals("money")) {
            return "BigDecimal";
        }
        return toScalarJavaType(column);
    }
}
