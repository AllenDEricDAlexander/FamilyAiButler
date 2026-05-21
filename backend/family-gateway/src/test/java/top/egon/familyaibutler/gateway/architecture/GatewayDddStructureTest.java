/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.architecture
 * @FileName: GatewayDddStructureTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-22:10
 * @Description: family-gateway DDD 分层结构测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.architecture
 * @ClassName: GatewayDddStructureTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 22:10
 * @Description: family-gateway DDD 分层结构测试
 * @Version: 1.0
 */
class GatewayDddStructureTest {

    /**
     * 校验网关模块具备 adapter / application / domain / infrastructure 分层入口。
     */
    @Test
    void shouldProvideGatewayDddLayers() {
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/gateway/adapter/filter/JwtTokenFilter.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/gateway/adapter/filter/OpenApiDocsBlockFilter.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/gateway/adapter/handler/GlobalExceptionHandler.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/gateway/application/GatewayAccessService.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/gateway/application/dto/GatewayAccessDecision.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/gateway/domain/gateway/AuthorizationDecisionGateway.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/gateway/domain/gateway/TokenGateway.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/gateway/domain/exception/BusinessException.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/gateway/infrastructure/configuration/FamilyButlerGateWayProperties.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/gateway/infrastructure/configuration/GatewayConfiguration.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/gateway/infrastructure/gatewayimpl/UaaAuthorizationDecisionGatewayImpl.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/gateway/infrastructure/security/FamilyJwtTokenGatewayImpl.java")).exists();
    }

    /**
     * 校验网关模块没有回退到旧的横向根包目录。
     */
    @Test
    void shouldNotContainForbiddenRootPackages() {
        List<String> forbiddenPackages = List.of(
                "config",
                "exception",
                "filter",
                "handler",
                "security",
                "util"
        );
        for (String forbiddenPackage : forbiddenPackages) {
            assertThat(Path.of("src/main/java/top/egon/familyaibutler/gateway/" + forbiddenPackage)).doesNotExist();
        }
    }

    /**
     * 校验应用层不反向依赖 Web 适配层或基础设施实现层。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void applicationShouldNotDependOnAdapterOrInfrastructureImpl() throws Exception {
        Path applicationPath = Path.of("src/main/java/top/egon/familyaibutler/gateway/application");
        try (Stream<Path> files = Files.walk(applicationPath)) {
            List<String> applicationSources = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
            assertThat(String.join("\n", applicationSources))
                    .doesNotContain("top.egon.familyaibutler.gateway.adapter")
                    .doesNotContain("top.egon.familyaibutler.gateway.infrastructure.gatewayimpl")
                    .doesNotContain("org.springframework.web.server");
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
