/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.architecture
 * @FileName: UaaFacadeArchitectureTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: UAA facade 模块边界测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.architecture
 * @ClassName: UaaFacadeArchitectureTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: UAA facade 模块边界测试
 * @Version: 1.0
 */
class UaaFacadeArchitectureTest {

    /**
     * 校验 facade 暴露第一批稳定契约入口。
     *
     * @throws Exception 文件读取异常
     */
    @Test
    void shouldProvideFirstBatchFacadeContracts() throws Exception {
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/facade/AccountFacade.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/facade/AuthFacade.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/facade/TokenFacade.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/facade/ProfileFacade.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/facade/dto/account/RegisterAccountRequest.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/facade/dto/auth/PasswordLoginRequest.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/facade/dto/token/TokenPairResponse.java")).exists();
    }

    /**
     * 校验 facade 不泄露实现层、Web 层和持久化层依赖。
     *
     * @throws Exception 文件读取异常
     */
    @Test
    void facadeShouldNotDependOnImplementationDetails() throws Exception {
        Path facadePath = Path.of("src/main/java/top/egon/familyaibutler/uaa/facade");
        try (Stream<Path> files = Files.walk(facadePath)) {
            List<String> sources = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
            assertThat(String.join("\n", sources))
                    .doesNotContain("infrastructure")
                    .doesNotContain("adapter")
                    .doesNotContain("Controller")
                    .doesNotContain("Mapper")
                    .doesNotContain("Repository")
                    .doesNotContain("UserPO")
                    .doesNotContain("RolePO")
                    .doesNotContain("PermissionPO")
                    .doesNotContain("org.springframework.web")
                    .doesNotContain("com.baomidou")
                    .doesNotContain("jakarta.persistence");
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
            throw new IllegalStateException("读取 facade 源码失败: " + path, exception);
        }
    }
}
