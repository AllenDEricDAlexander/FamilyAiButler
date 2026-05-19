package top.egon.familyaibutler.codegen.ddl;

import top.egon.familyaibutler.codegen.schema.ConstraintType;
import top.egon.familyaibutler.codegen.schema.PgColumnModel;
import top.egon.familyaibutler.codegen.schema.PgConstraintModel;
import top.egon.familyaibutler.codegen.schema.PgEnumModel;
import top.egon.familyaibutler.codegen.schema.PgIndexModel;
import top.egon.familyaibutler.codegen.schema.PgSchemaModel;
import top.egon.familyaibutler.codegen.schema.PgTableModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.ddl
 * @ClassName: PureParseSchemaReader
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: PostgreSQL DDL 纯解析模式 Schema Reader
 * @Version: 1.0
 */
public class PureParseSchemaReader {
    private static final Pattern CREATE_TYPE = Pattern.compile("(?is)^CREATE\\s+TYPE\\s+([\\w.\\\"]+)\\s+AS\\s+ENUM\\s*\\((.*)\\)$");
    private static final Pattern CREATE_TABLE = Pattern.compile("(?is)^CREATE\\s+TABLE\\s+([\\w.\\\"]+)\\s*\\((.*)\\)$");
    private static final Pattern COMMENT_TABLE = Pattern.compile("(?is)^COMMENT\\s+ON\\s+TABLE\\s+([\\w.\\\"]+)\\s+IS\\s+'(.*)'$");
    private static final Pattern COMMENT_COLUMN = Pattern.compile("(?is)^COMMENT\\s+ON\\s+COLUMN\\s+([\\w.\\\"]+)\\.([\\w\\\"]+)\\s+IS\\s+'(.*)'$");
    private static final Pattern CREATE_INDEX = Pattern.compile("(?is)^CREATE\\s+(UNIQUE\\s+)?INDEX\\s+([\\w\\\"]+)\\s+ON\\s+([\\w.\\\"]+)\\s*(?:USING\\s+\\w+)?\\s*\\((.*)\\)(?:\\s+WHERE\\s+(.*))?$");
    private static final Pattern ALTER_TABLE = Pattern.compile("(?is)^ALTER\\s+TABLE\\s+([\\w.\\\"]+)\\s+(.*)$");

    /**
     * 读取 DDL 并构建 Schema 元模型。
     *
     * @param schemaName Schema 名称
     * @param ddlTexts   DDL 文本列表
     * @return Schema 元模型
     */
    public PgSchemaModel read(String schemaName, List<String> ddlTexts) {
        PgSchemaModel schemaModel = PgSchemaModel.builder().schemaName(schemaName).build();
        DdlStatementSplitter splitter = new DdlStatementSplitter();
        for (String ddlText : ddlTexts) {
            for (String statement : splitter.split(removeLineComments(ddlText))) {
                parseStatement(schemaModel, statement);
            }
        }
        markEnumColumns(schemaModel);
        return schemaModel;
    }

    /**
     * 根据 SQL 类型分派解析逻辑。
     *
     * @param schemaModel Schema 元模型
     * @param statement   SQL 语句
     */
    private void parseStatement(PgSchemaModel schemaModel, String statement) {
        Matcher createType = CREATE_TYPE.matcher(statement);
        Matcher createTable = CREATE_TABLE.matcher(statement);
        Matcher commentTable = COMMENT_TABLE.matcher(statement);
        Matcher commentColumn = COMMENT_COLUMN.matcher(statement);
        Matcher createIndex = CREATE_INDEX.matcher(statement);
        Matcher alterTable = ALTER_TABLE.matcher(statement);
        if (createType.matches()) {
            schemaModel.getEnums().add(parseEnum(createType));
        } else if (createTable.matches()) {
            schemaModel.getTables().add(parseTable(schemaModel.getSchemaName(), createTable));
        } else if (commentTable.matches()) {
            applyTableComment(schemaModel, normalizeName(commentTable.group(1)), commentTable.group(2));
        } else if (commentColumn.matches()) {
            applyColumnComment(schemaModel, normalizeName(commentColumn.group(1)), normalizeName(commentColumn.group(2)), commentColumn.group(3));
        } else if (createIndex.matches()) {
            applyIndex(schemaModel, createIndex);
        } else if (alterTable.matches()) {
            applyAlterTable(schemaModel, alterTable);
        }
    }

    /**
     * 解析 PostgreSQL enum。
     *
     * @param matcher 正则匹配结果
     * @return 枚举模型
     */
    private PgEnumModel parseEnum(Matcher matcher) {
        List<String> values = splitCommaAware(matcher.group(2)).stream()
                .map(value -> value.trim().replace("'", ""))
                .toList();
        return PgEnumModel.builder()
                .typeName(normalizeName(matcher.group(1)))
                .values(values)
                .build();
    }

    /**
     * 解析 CREATE TABLE。
     *
     * @param schemaName Schema 名称
     * @param matcher    正则匹配结果
     * @return 表模型
     */
    private PgTableModel parseTable(String schemaName, Matcher matcher) {
        PgTableModel table = PgTableModel.builder()
                .schemaName(schemaName)
                .tableName(normalizeName(matcher.group(1)))
                .build();
        for (String item : splitCommaAware(matcher.group(2))) {
            String trimmed = item.trim();
            if (trimmed.toUpperCase(Locale.ROOT).startsWith("CONSTRAINT")
                    || trimmed.toUpperCase(Locale.ROOT).startsWith("PRIMARY KEY")
                    || trimmed.toUpperCase(Locale.ROOT).startsWith("FOREIGN KEY")
                    || trimmed.toUpperCase(Locale.ROOT).startsWith("UNIQUE")
                    || trimmed.toUpperCase(Locale.ROOT).startsWith("CHECK")) {
                table.getConstraints().add(parseConstraint(trimmed));
            } else {
                table.getColumns().add(parseColumn(trimmed));
            }
        }
        applyInlineConstraints(table);
        return table;
    }

    /**
     * 解析字段定义。
     *
     * @param definition 字段定义
     * @return 字段模型
     */
    private PgColumnModel parseColumn(String definition) {
        List<String> tokens = splitWhitespace(definition);
        String columnName = normalizeName(tokens.getFirst());
        String pgType = readType(tokens);
        String upperDefinition = definition.toUpperCase(Locale.ROOT);
        PgColumnModel column = PgColumnModel.builder()
                .columnName(columnName)
                .pgType(normalizeType(pgType))
                .nullable(!upperDefinition.contains(" NOT NULL"))
                .primaryKey(upperDefinition.contains(" PRIMARY KEY"))
                .unique(upperDefinition.contains(" UNIQUE"))
                .identity(upperDefinition.contains("IDENTITY"))
                .serial(normalizeType(pgType).contains("serial"))
                .generated(upperDefinition.contains(" GENERATED "))
                .defaultExpression(readDefaultExpression(definition))
                .build();
        readTypeShape(column, pgType);
        return column;
    }

    /**
     * 解析约束定义。
     *
     * @param definition 约束定义
     * @return 约束模型
     */
    private PgConstraintModel parseConstraint(String definition) {
        String upper = definition.toUpperCase(Locale.ROOT);
        String name = definition.toUpperCase(Locale.ROOT).startsWith("CONSTRAINT") ? splitWhitespace(definition).get(1) : null;
        if (upper.contains("PRIMARY KEY")) {
            return PgConstraintModel.builder().constraintName(name).type(ConstraintType.PRIMARY_KEY).columns(extractColumnsAfter(definition, "PRIMARY KEY")).build();
        }
        if (upper.contains("UNIQUE")) {
            return PgConstraintModel.builder().constraintName(name).type(ConstraintType.UNIQUE).columns(extractColumnsAfter(definition, "UNIQUE")).build();
        }
        if (upper.contains("FOREIGN KEY")) {
            PgConstraintModel constraint = PgConstraintModel.builder().constraintName(name).type(ConstraintType.FOREIGN_KEY).columns(extractColumnsAfter(definition, "FOREIGN KEY")).build();
            Matcher references = Pattern.compile("(?is)REFERENCES\\s+([\\w.\\\"]+)\\s*\\(([^)]*)\\)").matcher(definition);
            if (references.find()) {
                constraint.setReferencedTable(normalizeName(references.group(1)));
                constraint.setReferencedColumns(toNames(references.group(2)));
            }
            return constraint;
        }
        String expression = "";
        Matcher check = Pattern.compile("(?is)CHECK\\s*\\((.*)\\)").matcher(definition);
        if (check.find()) {
            expression = check.group(1).trim();
        }
        return PgConstraintModel.builder().constraintName(name).type(ConstraintType.CHECK).checkExpression(expression).build();
    }

    /**
     * 应用表注释。
     *
     * @param schemaModel Schema 元模型
     * @param tableName   表名
     * @param comment     注释
     */
    private void applyTableComment(PgSchemaModel schemaModel, String tableName, String comment) {
        schemaModel.getTable(tableName).ifPresent(table -> table.setTableComment(comment));
    }

    /**
     * 应用字段注释。
     *
     * @param schemaModel Schema 元模型
     * @param tableName   表名
     * @param columnName  字段名
     * @param comment     注释
     */
    private void applyColumnComment(PgSchemaModel schemaModel, String tableName, String columnName, String comment) {
        schemaModel.getTable(tableName).flatMap(table -> table.getColumn(columnName))
                .ifPresent(column -> column.setColumnComment(comment));
    }

    /**
     * 应用索引定义。
     *
     * @param schemaModel Schema 元模型
     * @param matcher     正则匹配结果
     */
    private void applyIndex(PgSchemaModel schemaModel, Matcher matcher) {
        String tableName = normalizeName(matcher.group(3));
        PgIndexModel index = PgIndexModel.builder()
                .unique(matcher.group(1) != null)
                .indexName(normalizeName(matcher.group(2)))
                .columns(toNames(matcher.group(4)))
                .partial(matcher.group(5) != null)
                .whereExpression(matcher.group(5))
                .expressionIndex(matcher.group(4).contains("("))
                .indexDefinition(matcher.group(0))
                .build();
        schemaModel.getTable(tableName).ifPresent(table -> table.getIndexes().add(index));
    }

    /**
     * 应用 ALTER TABLE 的 ADD COLUMN 和 ADD CONSTRAINT。
     *
     * @param schemaModel Schema 元模型
     * @param matcher     正则匹配结果
     */
    private void applyAlterTable(PgSchemaModel schemaModel, Matcher matcher) {
        String tableName = normalizeName(matcher.group(1));
        String action = matcher.group(2).trim();
        Optional<PgTableModel> table = schemaModel.getTable(tableName);
        if (table.isEmpty()) {
            return;
        }
        String upper = action.toUpperCase(Locale.ROOT);
        if (upper.startsWith("ADD COLUMN")) {
            table.get().getColumns().add(parseColumn(action.substring("ADD COLUMN".length()).trim()));
        } else if (upper.startsWith("ADD CONSTRAINT")) {
            table.get().getConstraints().add(parseConstraint(action.substring("ADD ".length()).trim()));
            applyInlineConstraints(table.get());
        }
    }

    /**
     * 将表级和字段级约束同步到字段标记上。
     *
     * @param table 表模型
     */
    private void applyInlineConstraints(PgTableModel table) {
        for (PgConstraintModel constraint : table.getConstraints()) {
            for (String columnName : constraint.getColumns()) {
                table.getColumn(columnName).ifPresent(column -> {
                    if (constraint.getType() == ConstraintType.PRIMARY_KEY) {
                        column.setPrimaryKey(true);
                        column.setNullable(false);
                    }
                    if (constraint.getType() == ConstraintType.UNIQUE) {
                        column.setUnique(true);
                    }
                });
            }
        }
    }

    /**
     * 标记字段是否引用 PostgreSQL enum。
     *
     * @param schemaModel Schema 元模型
     */
    private void markEnumColumns(PgSchemaModel schemaModel) {
        for (PgTableModel table : schemaModel.getTables()) {
            for (PgColumnModel column : table.getColumns()) {
                schemaModel.getEnum(column.getPgType()).ifPresent(enumModel -> {
                    column.setEnumType(true);
                    column.setEnumTypeName(enumModel.getTypeName());
                });
            }
        }
    }

    /**
     * 读取字段类型定义。
     *
     * @param tokens 字段定义 token
     * @return 字段类型
     */
    private String readType(List<String> tokens) {
        String first = tokens.size() > 1 ? tokens.get(1) : "";
        if (first.contains("(") && !first.contains(")")) {
            StringBuilder type = new StringBuilder(first);
            for (int index = 2; index < tokens.size(); index++) {
                type.append(" ").append(tokens.get(index));
                if (tokens.get(index).contains(")")) {
                    break;
                }
            }
            return type.toString();
        }
        if (tokens.size() > 3 && "timestamp".equalsIgnoreCase(first) && "with".equalsIgnoreCase(tokens.get(2))) {
            return "timestamptz";
        }
        if (tokens.size() > 2 && ("double".equalsIgnoreCase(first) || "character".equalsIgnoreCase(first))) {
            return first + " " + tokens.get(2);
        }
        return first;
    }

    /**
     * 读取默认值表达式。
     *
     * @param definition 字段定义
     * @return 默认值表达式
     */
    private String readDefaultExpression(String definition) {
        Matcher matcher = Pattern.compile("(?is)\\sDEFAULT\\s+(.+?)(?:\\s+NOT\\s+NULL|\\s+PRIMARY\\s+KEY|\\s+UNIQUE|$)").matcher(definition);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    /**
     * 读取字段长度、精度和小数位。
     *
     * @param column 字段模型
     * @param pgType PostgreSQL 类型
     */
    private void readTypeShape(PgColumnModel column, String pgType) {
        Matcher matcher = Pattern.compile("(?is)(\\w+)\\s*\\(([^)]*)\\)").matcher(pgType);
        if (!matcher.find()) {
            return;
        }
        List<String> numbers = splitCommaAware(matcher.group(2)).stream().map(String::trim).toList();
        if (numbers.size() == 1) {
            column.setLength(parseInteger(numbers.getFirst()));
        } else if (numbers.size() == 2) {
            column.setPrecision(parseInteger(numbers.get(0)));
            column.setScale(parseInteger(numbers.get(1)));
        }
    }

    /**
     * 根据关键词提取括号内字段。
     *
     * @param definition 约束定义
     * @param keyword    关键词
     * @return 字段列表
     */
    private List<String> extractColumnsAfter(String definition, String keyword) {
        int start = definition.toUpperCase(Locale.ROOT).indexOf(keyword);
        if (start < 0) {
            return List.of();
        }
        Matcher matcher = Pattern.compile("\\(([^)]*)\\)").matcher(definition.substring(start + keyword.length()));
        return matcher.find() ? toNames(matcher.group(1)) : List.of();
    }

    /**
     * 按逗号切分并处理括号与字符串。
     *
     * @param value 输入文本
     * @return 切分结果
     */
    private List<String> splitCommaAware(String value) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenDepth = 0;
        boolean singleQuoted = false;
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);
            if (ch == '\'') {
                singleQuoted = !singleQuoted;
            } else if (!singleQuoted && ch == '(') {
                parenDepth++;
            } else if (!singleQuoted && ch == ')') {
                parenDepth--;
            }
            if (ch == ',' && parenDepth == 0 && !singleQuoted) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        if (!current.isEmpty()) {
            result.add(current.toString().trim());
        }
        return result;
    }

    /**
     * 将逗号分隔字段转换为标准字段名。
     *
     * @param columns 字段文本
     * @return 字段列表
     */
    private List<String> toNames(String columns) {
        return splitCommaAware(columns).stream()
                .map(String::trim)
                .map(this::normalizeName)
                .toList();
    }

    /**
     * 切分空白 token。
     *
     * @param value 输入文本
     * @return token 列表
     */
    private List<String> splitWhitespace(String value) {
        return Arrays.stream(value.trim().split("\\s+")).filter(token -> !token.isBlank()).toList();
    }

    /**
     * 规范化标识符。
     *
     * @param name 标识符
     * @return 标准名称
     */
    private String normalizeName(String name) {
        String normalized = name.trim().replace("\"", "");
        int dot = normalized.lastIndexOf('.');
        return dot >= 0 ? normalized.substring(dot + 1) : normalized;
    }

    /**
     * 规范化 PostgreSQL 类型。
     *
     * @param pgType PostgreSQL 类型
     * @return 标准类型
     */
    private String normalizeType(String pgType) {
        return pgType.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    /**
     * 解析整数。
     *
     * @param value 文本
     * @return 整数
     */
    private Integer parseInteger(String value) {
        return value.matches("\\d+") ? Integer.parseInt(value) : null;
    }

    /**
     * 删除单行注释。
     *
     * @param ddl DDL 文本
     * @return 去除注释后的 DDL
     */
    private String removeLineComments(String ddl) {
        return ddl.replaceAll("(?m)^\\s*--.*$", "");
    }
}
