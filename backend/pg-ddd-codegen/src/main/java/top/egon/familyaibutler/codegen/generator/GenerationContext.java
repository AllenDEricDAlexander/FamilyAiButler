package top.egon.familyaibutler.codegen.generator;

import lombok.Builder;
import lombok.Data;
import top.egon.familyaibutler.codegen.config.GeneratorConfig;
import top.egon.familyaibutler.codegen.schema.PgSchemaModel;
import top.egon.familyaibutler.codegen.schema.PgTableModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.generator
 * @ClassName: GenerationContext
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: 单聚合代码生成上下文
 * @Version: 1.0
 */
@Data
@Builder
public class GenerationContext {
    private GeneratorConfig config;
    private PgSchemaModel schemaModel;
    private GeneratorConfig.AggregateConfig aggregate;
    private PgTableModel rootTable;
    @Builder.Default
    private List<PgTableModel> entityTables = new ArrayList<>();
    private Naming naming;
    private PgTypeMapper typeMapper;

    /**
     * 获取基础包路径。
     *
     * @return 基础包路径
     */
    public String basePackagePath() {
        return config.getProject().getBasePackage().replace('.', '/');
    }
}
