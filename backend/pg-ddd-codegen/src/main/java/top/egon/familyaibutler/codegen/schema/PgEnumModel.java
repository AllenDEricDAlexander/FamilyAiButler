package top.egon.familyaibutler.codegen.schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.schema
 * @ClassName: PgEnumModel
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: PostgreSQL 枚举元模型
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PgEnumModel {
    private String typeName;
    private String javaName;
    private String description;
    @Builder.Default
    private List<String> values = new ArrayList<>();
    @Builder.Default
    private Map<String, String> valueCodes = new LinkedHashMap<>();
    @Builder.Default
    private Map<String, String> valueDescriptions = new LinkedHashMap<>();
}
