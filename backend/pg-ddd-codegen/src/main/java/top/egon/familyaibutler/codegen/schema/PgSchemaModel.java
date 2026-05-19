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
 * @ClassName: PgSchemaModel
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: PostgreSQL Schema 元模型
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PgSchemaModel {
    private String schemaName;
    @Builder.Default
    private List<PgTableModel> tables = new ArrayList<>();
    @Builder.Default
    private List<PgEnumModel> enums = new ArrayList<>();

    /**
     * 根据表名查找表模型。
     *
     * @param tableName 表名
     * @return 表模型
     */
    public Optional<PgTableModel> getTable(String tableName) {
        return tables.stream()
                .filter(table -> table.getTableName().equals(tableName))
                .findFirst();
    }

    /**
     * 根据 PostgreSQL 类型名查找枚举模型。
     *
     * @param typeName 类型名
     * @return 枚举模型
     */
    public Optional<PgEnumModel> getEnum(String typeName) {
        return enums.stream()
                .filter(enumModel -> enumModel.getTypeName().equals(typeName))
                .findFirst();
    }
}
