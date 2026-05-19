package top.egon.familyaibutler.codegen.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.generator
 * @ClassName: GeneratedIndexWriter
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: generated-index.json 写入器
 * @Version: 1.0
 */
public class GeneratedIndexWriter {

    /**
     * 写入生成索引。
     *
     * @param outputDir 输出目录
     * @param records   文件记录
     */
    public void write(Path outputDir, List<GeneratedFileRecord> records) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n")
                .append("  \"generatedAt\": \"").append(OffsetDateTime.now()).append("\",\n")
                .append("  \"files\": [\n");
        for (int index = 0; index < records.size(); index++) {
            GeneratedFileRecord record = records.get(index);
            builder.append("    {\n")
                    .append("      \"path\": \"").append(escape(record.getPath())).append("\",\n")
                    .append("      \"policy\": \"").append(record.getPolicy()).append("\",\n")
                    .append("      \"checksum\": \"").append(record.getChecksum()).append("\",\n")
                    .append("      \"written\": ").append(record.isWritten()).append("\n")
                    .append("    }");
            if (index + 1 < records.size()) {
                builder.append(",");
            }
            builder.append("\n");
        }
        builder.append("  ]\n")
                .append("}\n");
        writeFile(outputDir.resolve(".generated/codegen-index.json"), builder.toString());
    }

    /**
     * 转义 JSON 字符串。
     *
     * @param value 原始值
     * @return 转义后值
     */
    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * 写入文本文件。
     *
     * @param path    文件路径
     * @param content 文件内容
     */
    private void writeFile(Path path, String content) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, content);
        } catch (IOException e) {
            throw new IllegalStateException("写入生成索引失败: " + path, e);
        }
    }
}
