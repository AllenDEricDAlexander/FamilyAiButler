package top.egon.familyaibutler.codegen.generator;

import top.egon.familyaibutler.codegen.schema.PgColumnModel;
import top.egon.familyaibutler.codegen.schema.PgEnumModel;
import top.egon.familyaibutler.codegen.schema.PgSchemaModel;
import top.egon.familyaibutler.codegen.schema.PgTableModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.generator
 * @ClassName: GenerationReportWriter
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: Markdown 生成报告写入器
 * @Version: 1.0
 */
public class GenerationReportWriter {

    /**
     * 写入 generation-report.md。
     *
     * @param outputDir 输出目录
     * @param contexts  聚合上下文
     * @param warnings  警告列表
     */
    public void write(Path outputDir, List<GenerationContext> contexts, List<String> warnings) {
        StringBuilder builder = new StringBuilder("# Code Generation Report\n\n");
        for (GenerationContext context : contexts) {
            PgTableModel table = context.getRootTable();
            PgSchemaModel schema = context.getSchemaModel();
            builder.append("## ").append(context.getAggregate().getName()).append("\n\n")
                    .append("Root Table:\n- ").append(table.getTableName()).append("\n\n")
                    .append("Persistence Route:\n- Command: JPA\n- Page Query: MP\n\n")
                    .append("Detected Fields:\n");
            for (PgColumnModel column : table.getColumns()) {
                String javaType = context.getTypeMapper().toJavaType(column, context.getNaming());
                builder.append("- ").append(column.getColumnName()).append(": ")
                        .append(column.getPgType()).append(" -> ").append(javaType);
                if (column.isPrimaryKey()) {
                    builder.append(" -> primary key");
                }
                if (column.isEnumType()) {
                    builder.append(" -> ").append(context.getNaming().upperCamel(column.getEnumTypeName()));
                }
                builder.append("\n");
            }
            builder.append("\nGenerated PostgreSQL Enums:\n");
            for (PgEnumModel enumModel : schema.getEnums()) {
                builder.append("- ").append(enumModel.getTypeName()).append(": ")
                        .append(String.join(", ", enumModel.getValues())).append("\n");
            }
            builder.append("\n");
        }
        builder.append("## Warnings\n\n");
        if (warnings.isEmpty()) {
            builder.append("- 无\n");
        } else {
            warnings.forEach(warning -> builder.append("- ").append(warning).append("\n"));
        }
        writeFile(outputDir.resolve("generation-report.md"), builder.toString());
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
            throw new IllegalStateException("写入生成报告失败: " + path, e);
        }
    }
}
