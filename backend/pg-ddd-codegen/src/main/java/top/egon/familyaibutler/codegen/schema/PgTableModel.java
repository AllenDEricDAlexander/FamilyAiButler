package top.egon.familyaibutler.codegen.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.schema
 * @ClassName: PgTableModel
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: PostgreSQL 表元模型
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PgTableModel {
    private String schemaName;
    private String tableName;
    private String tableComment;
    @Builder.Default
    private List<PgColumnModel> columns = new ArrayList<>();
    @Builder.Default
    private List<PgConstraintModel> constraints = new ArrayList<>();
    @Builder.Default
    private List<PgIndexModel> indexes = new ArrayList<>();
    private boolean partitioned;
    private boolean view;

    /**
     * 按字段名查找字段模型。
     *
     * @param columnName 字段名
     * @return 字段模型
     */
    public Optional<PgColumnModel> getColumn(String columnName) {
        return columns.stream()
                .filter(column -> column.getColumnName().equals(columnName))
                .findFirst();
    }

    /**
     * 查找当前表的主键字段。
     *
     * @return 主键字段
     */
    public Optional<PgColumnModel> getPrimaryKeyColumn() {
        return columns.stream()
                .filter(PgColumnModel::isPrimaryKey)
                .findFirst();
    }
}
