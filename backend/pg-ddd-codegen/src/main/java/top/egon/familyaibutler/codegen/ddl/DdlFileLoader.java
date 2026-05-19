package top.egon.familyaibutler.codegen.ddl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.ddl
 * @ClassName: DdlFileLoader
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: DDL 文件加载器
 * @Version: 1.0
 */
public class DdlFileLoader {

    /**
     * 加载单文件或 Flyway migration 目录中的 SQL 文件。
     *
     * @param inputs DDL 输入路径
     * @return DDL 文本列表
     */
    public List<String> load(List<Path> inputs) {
        List<Path> files = new ArrayList<>();
        for (Path input : inputs) {
            if (Files.isDirectory(input)) {
                files.addAll(loadDirectory(input));
            } else {
                files.add(input);
            }
        }
        files.sort(Comparator.comparing(path -> path.getFileName().toString()));
        return files.stream().map(this::readString).toList();
    }

    /**
     * 读取目录下的 SQL 文件。
     *
     * @param directory SQL 目录
     * @return SQL 文件列表
     */
    private List<Path> loadDirectory(Path directory) {
        try (Stream<Path> stream = Files.list(directory)) {
            return stream.filter(path -> path.getFileName().toString().endsWith(".sql"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        } catch (IOException e) {
            throw new IllegalArgumentException("读取 DDL 目录失败: " + directory, e);
        }
    }

    /**
     * 读取 SQL 文件内容。
     *
     * @param path SQL 文件路径
     * @return SQL 文本
     */
    private String readString(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalArgumentException("读取 DDL 文件失败: " + path, e);
        }
    }
}
