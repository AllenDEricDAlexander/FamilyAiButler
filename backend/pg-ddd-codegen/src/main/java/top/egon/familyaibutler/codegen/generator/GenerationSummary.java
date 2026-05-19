package top.egon.familyaibutler.codegen.generator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.generator
 * @ClassName: GenerationSummary
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: 代码生成执行摘要
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerationSummary {
    @Builder.Default
    private List<GeneratedFileRecord> generatedFiles = new ArrayList<>();
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}
