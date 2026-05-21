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
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/adapter/web/AccountController.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/adapter/rpc/dubbo/AuthDubboAdapter.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/manage/AuthManage.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/manage/impl/AuthManageImpl.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/executor/command/AccountCommandExe.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/executor/query/AccountQueryExe.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/command/account/RegisterAccountCommand.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/result/account/AccountResponse.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/account/model/aggregate/Account.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/auth/model/aggregate/AuthSession.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/oauth/model/aggregate/OAuthClient.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/rbac/model/aggregate/Role.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/account/gateway/AccountGateway.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/auth/gateway/TokenGateway.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/infrastructure/gateway/impl/MpAccountGatewayImpl.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/infrastructure")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/adapter/AccountController.java")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/AuthServiceI.java")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/AuthServiceImpl.java")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/model")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/gateway")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/service")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/infrastructure/gatewayimpl")).doesNotExist();
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
                "do",
                "repository",
                "configuration",
                "enums",
                "filter",
                "utils",
                "vo",
                "domain/dto",
                "domain/repository",
                "infrastructure/persistence/impl",
                "adapter/rpc/grpc"
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
                    .doesNotContain("top.egon.familyaibutler.uaa.adapter.web")
                    .doesNotContain("Mapper")
                    .doesNotContain("Repository")
                    .doesNotContain("PO");
        }
    }

    /**
     * 校验应用层不直接承载 facade 契约 Provider。
     *
     * @throws Exception 文件读取异常
     */
    @Test
    void applicationShouldNotImplementFacadeContracts() throws Exception {
        Path applicationPath = Path.of("src/main/java/top/egon/familyaibutler/uaa/application");
        try (Stream<Path> files = Files.walk(applicationPath)) {
            List<String> sources = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
            assertThat(String.join("\n", sources))
                    .doesNotContain("implements AccountManage, AccountFacade")
                    .doesNotContain("implements AuthManage, AuthFacade")
                    .doesNotContain("implements AuthorizationManage, AuthorizationFacade")
                    .doesNotContain("implements ProfileManage, ProfileFacade")
                    .doesNotContain("implements TokenManage, TokenFacade")
                    .doesNotContain("extends OAuthClientFacade")
                    .doesNotContain("extends RbacFacade");
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
