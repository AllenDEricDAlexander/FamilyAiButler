package top.egon.familyaibutler.codegen.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.schema
 * @ClassName: PgConstraintModel
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: PostgreSQL 约束元模型
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PgConstraintModel {
    private String constraintName;
    private ConstraintType type;
    @Builder.Default
    private List<String> columns = new ArrayList<>();
    private String checkExpression;
    private String referencedTable;
    @Builder.Default
    private List<String> referencedColumns = new ArrayList<>();
}
