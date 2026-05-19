package top.egon.familyaibutler.codegen.generator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.generator
 * @ClassName: GeneratedFileRecord
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: 生成索引文件记录
 * @Version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedFileRecord {
    private String path;
    private WritePolicy policy;
    private String checksum;
    private boolean written;
}
