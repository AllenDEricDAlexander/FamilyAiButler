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
 * @ClassName: PgIndexModel
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: PostgreSQL 索引元模型
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PgIndexModel {
    private String indexName;
    private boolean unique;
    private boolean partial;
    private boolean expressionIndex;
    @Builder.Default
    private List<String> columns = new ArrayList<>();
    private String whereExpression;
    private String indexDefinition;
}
