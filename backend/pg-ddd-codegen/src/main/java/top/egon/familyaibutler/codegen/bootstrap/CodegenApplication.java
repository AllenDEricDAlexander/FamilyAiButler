package top.egon.familyaibutler.codegen.bootstrap;

import top.egon.familyaibutler.codegen.generator.CodeGenerationService;
import top.egon.familyaibutler.codegen.generator.GenerationSummary;

import java.nio.file.Path;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.bootstrap
 * @ClassName: CodegenApplication
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: PostgreSQL DDD/COLA 代码生成器命令行入口
 * @Version: 1.0
 */
public class CodegenApplication {

    /**
     * 命令行入口。
     *
     * @param args 命令行参数，支持 --config generator.yml
     */
    public static void main(String[] args) {
        Path configPath = parseConfigPath(args);
        GenerationSummary summary = new CodeGenerationService().generate(configPath);
        System.out.println("Generated files: " + summary.getGeneratedFiles().size());
        if (!summary.getWarnings().isEmpty()) {
            System.out.println("Warnings:");
            summary.getWarnings().forEach(warning -> System.out.println("- " + warning));
        }
    }

    /**
     * 解析配置文件路径。
     *
     * @param args 命令行参数
     * @return 配置文件路径
     */
    private static Path parseConfigPath(String[] args) {
        for (int index = 0; index < args.length; index++) {
            if ("--config".equals(args[index]) && index + 1 < args.length) {
                return Path.of(args[index + 1]).toAbsolutePath().normalize();
            }
        }
        return Path.of("generator.yml").toAbsolutePath().normalize();
    }
}
