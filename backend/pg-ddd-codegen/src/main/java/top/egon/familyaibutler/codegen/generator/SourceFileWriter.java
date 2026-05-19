package top.egon.familyaibutler.codegen.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.generator
 * @ClassName: SourceFileWriter
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: 生成文件写入器
 * @Version: 1.0
 */
public class SourceFileWriter {

    /**
     * 按写入策略写入生成文件。
     *
     * @param outputDir 输出目录
     * @param files     生成文件列表
     * @return 文件写入记录
     */
    public List<GeneratedFileRecord> write(Path outputDir, List<GeneratedSourceFile> files) {
        List<GeneratedFileRecord> records = new ArrayList<>();
        for (GeneratedSourceFile file : files) {
            records.add(writeOne(outputDir, file));
        }
        return records;
    }

    /**
     * 写入单个文件。
     *
     * @param outputDir 输出目录
     * @param file      生成文件
     * @return 文件写入记录
     */
    private GeneratedFileRecord writeOne(Path outputDir, GeneratedSourceFile file) {
        Path target = outputDir.resolve(file.getRelativePath()).normalize();
        boolean written = false;
        try {
            Files.createDirectories(target.getParent());
            if (file.getPolicy() == WritePolicy.CREATE_ONLY && Files.exists(target)) {
                written = false;
            } else if (file.getPolicy() == WritePolicy.MERGE && Files.exists(target)) {
                Files.writeString(target, mergeGeneratedBlocks(Files.readString(target), file.getContent()));
                written = true;
            } else {
                Files.writeString(target, file.getContent());
                written = true;
            }
            return GeneratedFileRecord.builder()
                    .path(file.getRelativePath().toString())
                    .policy(file.getPolicy())
                    .checksum(sha256(Files.readString(target)))
                    .written(written)
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("写入生成文件失败: " + target, e);
        }
    }

    /**
     * 合并 AUTO-GENERATED 分区并保留 CUSTOM 分区。
     *
     * @param existingContent  现有内容
     * @param generatedContent 新生成内容
     * @return 合并后内容
     */
    private String mergeGeneratedBlocks(String existingContent, String generatedContent) {
        Pattern pattern = Pattern.compile("(?s)<!-- AUTO-GENERATED-START: ([^>]+) -->.*?<!-- AUTO-GENERATED-END: \\1 -->");
        Matcher matcher = pattern.matcher(generatedContent);
        String merged = existingContent;
        while (matcher.find()) {
            String blockName = matcher.group(1);
            String block = matcher.group(0);
            Pattern existingBlock = Pattern.compile("(?s)<!-- AUTO-GENERATED-START: " + Pattern.quote(blockName) + " -->.*?<!-- AUTO-GENERATED-END: " + Pattern.quote(blockName) + " -->");
            Matcher existingMatcher = existingBlock.matcher(merged);
            if (existingMatcher.find()) {
                merged = existingMatcher.replaceFirst(Matcher.quoteReplacement(block));
            }
        }
        return merged;
    }

    /**
     * 计算文本 SHA-256。
     *
     * @param content 文件内容
     * @return SHA-256 字符串
     */
    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", e);
        }
    }
}
