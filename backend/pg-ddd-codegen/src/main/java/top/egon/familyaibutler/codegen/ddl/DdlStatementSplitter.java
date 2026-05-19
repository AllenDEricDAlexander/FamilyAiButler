package top.egon.familyaibutler.codegen.ddl;

import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.ddl
 * @ClassName: DdlStatementSplitter
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: PostgreSQL DDL 语句切分器
 * @Version: 1.0
 */
public class DdlStatementSplitter {

    /**
     * 按分号切分 SQL，避免切断普通字符串和双引号标识符。
     *
     * @param ddl DDL 文本
     * @return SQL 语句列表
     */
    public List<String> split(String ddl) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        for (int index = 0; index < ddl.length(); index++) {
            char ch = ddl.charAt(index);
            if (ch == '\'' && !doubleQuoted) {
                if (singleQuoted && isEscapedQuote(ddl, index)) {
                    current.append(ch).append(ddl.charAt(index + 1));
                    index++;
                    continue;
                }
                singleQuoted = !singleQuoted;
            } else if (ch == '"' && !singleQuoted) {
                doubleQuoted = !doubleQuoted;
            }
            if (ch == ';' && !singleQuoted && !doubleQuoted) {
                addStatement(statements, current);
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        addStatement(statements, current);
        return statements;
    }

    /**
     * 判断当前位置是否为 SQL 转义单引号。
     *
     * @param ddl   DDL 文本
     * @param index 当前字符下标
     * @return 是否为转义单引号
     */
    private boolean isEscapedQuote(String ddl, int index) {
        return index + 1 < ddl.length() && ddl.charAt(index + 1) == '\'';
    }

    /**
     * 将非空 SQL 加入结果。
     *
     * @param statements SQL 语句列表
     * @param current    当前语句
     */
    private void addStatement(List<String> statements, StringBuilder current) {
        String statement = current.toString().trim();
        if (!statement.isBlank()) {
            statements.add(statement);
        }
    }
}
