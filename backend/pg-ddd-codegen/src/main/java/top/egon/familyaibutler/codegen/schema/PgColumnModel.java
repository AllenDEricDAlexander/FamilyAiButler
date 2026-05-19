package top.egon.familyaibutler.codegen.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.schema
 * @ClassName: PgColumnModel
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: PostgreSQL 字段元模型
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PgColumnModel {
    private String columnName;
    private String columnComment;
    private String pgType;
    private Integer length;
    private Integer precision;
    private Integer scale;
    private boolean nullable;
    private boolean primaryKey;
    private boolean unique;
    private boolean identity;
    private boolean serial;
    private boolean generated;
    private boolean enumType;
    private String defaultExpression;
    private String enumTypeName;
    private String enumJavaName;
}
