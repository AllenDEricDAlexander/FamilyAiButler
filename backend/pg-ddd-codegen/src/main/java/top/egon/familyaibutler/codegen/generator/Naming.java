package top.egon.familyaibutler.codegen.generator;

import java.util.List;
import java.util.Locale;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.generator
 * @ClassName: Naming
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: 数据库命名到 Java 命名的转换工具
 * @Version: 1.0
 */
public class Naming {
    private final List<String> tablePrefixes;

    public Naming(List<String> tablePrefixes) {
        this.tablePrefixes = tablePrefixes;
    }

    /**
     * 表名转类名。
     *
     * @param tableName 表名
     * @return 类名
     */
    public String tableToClass(String tableName) {
        String name = tableName;
        for (String prefix : tablePrefixes) {
            if (name.startsWith(prefix)) {
                name = name.substring(prefix.length());
                break;
            }
        }
        return upperCamel(name);
    }

    /**
     * 字段名转属性名。
     *
     * @param columnName 字段名
     * @return 属性名
     */
    public String columnToField(String columnName) {
        String upperCamel = upperCamel(columnName);
        return Character.toLowerCase(upperCamel.charAt(0)) + upperCamel.substring(1);
    }

    /**
     * 下划线命名转大驼峰命名。
     *
     * @param value 下划线名称
     * @return 大驼峰名称
     */
    public String upperCamel(String value) {
        StringBuilder builder = new StringBuilder();
        for (String part : value.split("_")) {
            if (!part.isBlank()) {
                builder.append(part.substring(0, 1).toUpperCase(Locale.ROOT))
                        .append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return builder.toString();
    }

    /**
     * 类名转属性名。
     *
     * @param className 类名
     * @return 属性名
     */
    public String classToField(String className) {
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
}
