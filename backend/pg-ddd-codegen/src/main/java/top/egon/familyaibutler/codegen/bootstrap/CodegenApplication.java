package top.egon.familyaibutler.codegen.bootstrap;

import top.egon.familyaibutler.codegen.generator.CodeGenerationService;
import top.egon.familyaibutler.codegen.generator.GenerationSummary;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
    private static final String EXAMPLE_CONFIG_RESOURCE = "examples/generator-example.yml";
    private static final String EXAMPLE_DDL_RESOURCE = "examples/schema-example.sql";
    private static final Path DEFAULT_DEMO_DIR = Path.of("target/codegen-demo");

    /**
     * 命令行入口。
     *
     * @param args 命令行参数，支持 --config generator.yml、--init、--demo
     */
    public static void main(String[] args) {
        new CodegenApplication().run(args);
    }

    /**
     * 执行代码生成器启动逻辑。
     *
     * @param args 命令行参数
     */
    void run(String[] args) {
        if (args.length == 0) {
            runDemo(DEFAULT_DEMO_DIR);
            return;
        }
        if (hasArg(args, "--help") || hasArg(args, "-h")) {
            printUsage();
            return;
        }
        if ("--init".equals(args[0])) {
            Path targetDir = optionalPath(args, 1, Path.of("."));
            copyStarterFiles(targetDir, false);
            System.out.println("Generator template files initialized at: " + targetDir.toAbsolutePath().normalize());
            return;
        }
        if ("--demo".equals(args[0])) {
            runDemo(optionalPath(args, 1, DEFAULT_DEMO_DIR));
            return;
        }
        generate(parseConfigPath(args));
    }

    /**
     * 运行内置 demo，适合 IDEA 直接启动验证。
     *
     * @param demoDir demo 工作目录
     */
    private void runDemo(Path demoDir) {
        copyStarterFiles(demoDir, true);
        Path configPath = demoDir.resolve("generator.yml").toAbsolutePath().normalize();
        System.out.println("Running demo generator config: " + configPath);
        generate(configPath);
    }

    /**
     * 执行指定配置文件的代码生成。
     *
     * @param configPath generator.yml 路径
     */
    private void generate(Path configPath) {
        GenerationSummary summary = new CodeGenerationService().generate(configPath);
        System.out.println("Generated files: " + summary.getGeneratedFiles().size());
        if (!summary.getWarnings().isEmpty()) {
            System.out.println("Warnings:");
            summary.getWarnings().forEach(warning -> System.out.println("- " + warning));
        }
    }

    /**
     * 复制内置 generator.yml 和 schema.sql 模板。
     *
     * @param targetDir 目标目录
     * @param overwrite 是否覆盖已有文件
     */
    private void copyStarterFiles(Path targetDir, boolean overwrite) {
        Path normalizedDir = targetDir.toAbsolutePath().normalize();
        try {
            Files.createDirectories(normalizedDir);
            copyResource(EXAMPLE_CONFIG_RESOURCE, normalizedDir.resolve("generator.yml"), overwrite);
            copyResource(EXAMPLE_DDL_RESOURCE, normalizedDir.resolve("schema.sql"), overwrite);
        } catch (IOException exception) {
            throw new IllegalStateException("初始化代码生成器模板失败: " + normalizedDir, exception);
        }
    }

    /**
     * 从 classpath 复制单个模板资源。
     *
     * @param resourceName 资源名称
     * @param targetPath   目标路径
     * @param overwrite    是否覆盖已有文件
     * @throws IOException 文件读写异常
     */
    private void copyResource(String resourceName, Path targetPath, boolean overwrite) throws IOException {
        if (!overwrite && Files.exists(targetPath)) {
            return;
        }
        try (InputStream inputStream = CodegenApplication.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalStateException("代码生成器内置模板不存在: " + resourceName);
            }
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
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

    /**
     * 判断是否存在指定参数。
     *
     * @param args      命令行参数
     * @param targetArg 目标参数
     * @return 是否存在
     */
    private boolean hasArg(String[] args, String targetArg) {
        for (String arg : args) {
            if (targetArg.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 读取可选路径参数。
     *
     * @param args         命令行参数
     * @param index        参数位置
     * @param defaultValue 默认路径
     * @return 解析后的路径
     */
    private Path optionalPath(String[] args, int index, Path defaultValue) {
        if (args.length > index && args[index] != null && !args[index].isBlank()) {
            return Path.of(args[index]).toAbsolutePath().normalize();
        }
        return defaultValue.toAbsolutePath().normalize();
    }

    /**
     * 输出命令行帮助。
     */
    private void printUsage() {
        System.out.println("""
                Usage:
                  java CodegenApplication
                  java CodegenApplication --demo [dir]
                  java CodegenApplication --init [dir]
                  java CodegenApplication --config generator.yml
                
                No args runs the bundled demo at target/codegen-demo, which is convenient for IDEA Run.
                """);
    }
}
