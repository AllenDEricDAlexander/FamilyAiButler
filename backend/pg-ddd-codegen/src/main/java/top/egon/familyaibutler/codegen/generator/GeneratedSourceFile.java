package top.egon.familyaibutler.codegen.generator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.generator
 * @ClassName: GeneratedSourceFile
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: 待写入的生成文件
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedSourceFile {
    private Path relativePath;
    private String content;
    private WritePolicy policy;
}
