/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.architecture
 * @FileName: UaaDddArchitectureTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: uaa-core DDD 分层结构测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.architecture
 * @ClassName: UaaDddArchitectureTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: uaa-core DDD 分层结构测试
 * @Version: 1.0
 */
class UaaDddArchitectureTest {

    /**
     * 校验认证授权核心模块具备 DDD 四层结构。
     *
     * @throws Exception 文件读取异常
     */
    @Test
    void shouldProvideDddLayers() throws Exception {
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/UserAuthenticationAuthorizationApplication.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/adapter")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/infrastructure")).exists();
    }

    /**
     * 校验认证授权核心模块没有旧式根包目录。
     */
    @Test
    void shouldNotContainForbiddenRootPackages() {
        List<String> forbiddenPackages = List.of(
                "controller",
                "service",
                "mapper",
                "po",
                "repository",
                "configuration",
                "filter",
                "utils",
                "vo",
                "domain/dto",
                "domain/repository",
                "infrastructure/persistence/impl"
        );
        for (String forbiddenPackage : forbiddenPackages) {
            assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/" + forbiddenPackage)).doesNotExist();
        }
    }

    /**
     * 校验领域层不依赖外层实现。
     *
     * @throws Exception 文件读取异常
     */
    @Test
    void domainShouldNotDependOnOuterLayers() throws Exception {
        Path domainPath = Path.of("src/main/java/top/egon/familyaibutler/uaa/domain");
        try (Stream<Path> files = Files.walk(domainPath)) {
            List<String> sources = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
            assertThat(String.join("\n", sources))
                    .doesNotContain("org.springframework.web")
                    .doesNotContain("top.egon.familyaibutler.uaa.application.dto")
                    .doesNotContain("top.egon.familyaibutler.uaa.infrastructure")
                    .doesNotContain("top.egon.familyaibutler.uaa.adapter")
                    .doesNotContain("Mapper")
                    .doesNotContain("Repository")
                    .doesNotContain("PO");
        }
    }

    /**
     * 读取 Java 源码文件内容。
     *
     * @param path Java 源码路径
     * @return Java 源码文本
     */
    private String readSource(Path path) {
        try {
            return Files.readString(path);
        } catch (Exception exception) {
            throw new IllegalStateException("读取源码文件失败: " + path, exception);
        }
    }
}
