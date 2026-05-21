/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.architecture
 * @FileName: QwenDddStructureTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: qwen-ai DDD 分层结构测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.ai.qwen.architecture;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.architecture
 * @ClassName: QwenDddStructureTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: qwen-ai DDD 分层结构测试
 * @Version: 1.0
 */
class QwenDddStructureTest {

    /**
     * 校验 Qwen 业务模块具备代码生成器风格的 DDD 分层入口。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldProvideGeneratedStyleDddLayers() throws Exception {
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/application/manage/ImageManage.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/application/command/ImageMessageCommand.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/application/manage/impl/ImageManageImpl.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/application/executor/command/ImageCommandExe.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/adapter/web/ImageController.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/domain/image/model/aggregate/ImageMessageTask.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/domain/image/gateway/ImageModelGateway.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/infrastructure/gateway/impl/QwenImageModelGatewayImpl.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/domain/image/model/enums/ModelNumberEnums.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/domain/image/gateway/GenImageIteration.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/infrastructure/gateway/impl/GenImageIterationImpl.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/infrastructure/configuration/ModelConfig.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/controller/ImageController.java")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/adapter/ImageController.java")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/application/ImageManage.java")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/application/ImageManageImpl.java")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/infrastructure/gatewayimpl")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/client")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/app")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/service")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/po")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/configuration")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/enums")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/advisor")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/tools")).doesNotExist();
    }

    /**
     * Controller 不直接调用 DashScope SDK，模型调用放到基础设施层。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void controllerShouldDelegateModelCallToApplicationLayer() throws Exception {
        String controller = Files.readString(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/adapter/web/ImageController.java"));
        assertThat(controller)
                .doesNotContain("MultiModalConversation")
                .doesNotContain("MultiModalConversationParam")
                .doesNotContain("qwen.application.ImageManageImpl")
                .contains("ImageManage");
    }

    /**
     * Controller 对外接口需要补齐 OpenAPI 文档注解。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void controllerShouldProvideOpenApiAnnotations() throws Exception {
        String controller = Files.readString(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/adapter/web/ImageController.java"));
        assertThat(controller)
                .contains("@Tag")
                .contains("@Operation")
                .contains("@Parameter")
                .contains("@ApiResponse");
    }

    /**
     * 校验 Qwen AI 模块没有回退到旧的根包目录。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldNotContainForbiddenRootPackages() throws Exception {
        List<String> forbiddenPackages = List.of(
                "controller",
                "service",
                "mapper",
                "po",
                "do",
                "repository",
                "configuration",
                "enums",
                "utils",
                "advisor",
                "tools",
                "domain/dto",
                "domain/repository",
                "infrastructure/persistence/impl",
                "adapter/rpc/grpc"
        );
        for (String forbiddenPackage : forbiddenPackages) {
            assertThat(Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/" + forbiddenPackage)).doesNotExist();
        }
    }

    /**
     * 校验领域层不依赖 Web、应用 DTO、基础设施和外部 AI SDK。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void domainShouldNotDependOnOuterLayers() throws Exception {
        Path domainPath = Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/domain");
        try (Stream<Path> files = Files.walk(domainPath)) {
            List<String> domainSources = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
            assertThat(String.join("\n", domainSources))
                    .doesNotContain("org.springframework.web")
                    .doesNotContain("top.egon.familyaibutler.ai.qwen.application.command")
                    .doesNotContain("top.egon.familyaibutler.ai.qwen.infrastructure")
                    .doesNotContain("top.egon.familyaibutler.ai.qwen.adapter.web")
                    .doesNotContain("com.alibaba.dashscope");
        }
    }

    /**
     * 校验 Web 适配层不直接依赖 DashScope SDK。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void adapterShouldNotAccessAiSdkDirectly() throws Exception {
        Path adapterPath = Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/adapter");
        try (Stream<Path> files = Files.walk(adapterPath)) {
            List<String> adapterSources = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
            assertThat(String.join("\n", adapterSources))
                    .doesNotContain("com.alibaba.dashscope")
                    .doesNotContain("MultiModalConversation")
                    .doesNotContain("MultiModalConversationParam");
        }
    }

    /**
     * 校验应用层不反向依赖 Web 适配对象。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void applicationShouldNotDependOnWebAdapter() throws Exception {
        Path applicationPath = Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/application");
        try (Stream<Path> files = Files.walk(applicationPath)) {
            List<String> applicationSources = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
            assertThat(String.join("\n", applicationSources))
                    .doesNotContain("org.springframework.web")
                    .doesNotContain("top.egon.familyaibutler.ai.qwen.adapter.web")
                    .doesNotContain("top.egon.familyaibutler.ai.qwen.infrastructure");
        }
    }

    /**
     * 校验 Web 适配层不反向依赖基础设施实现。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void adapterShouldNotDependOnInfrastructure() throws Exception {
        Path adapterPath = Path.of("src/main/java/top/egon/familyaibutler/ai/qwen/adapter");
        try (Stream<Path> files = Files.walk(adapterPath)) {
            List<String> adapterSources = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
            assertThat(String.join("\n", adapterSources))
                    .doesNotContain("top.egon.familyaibutler.ai.qwen.infrastructure");
        }
    }

    /**
     * 校验 Qwen AI 模块已接入 UAA 资源服务 starter。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldIntegrateUaaResourceServerStarter() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"));
        String application = Files.readString(Path.of("src/main/resources/application.yml"));
        assertThat(pom).contains("<artifactId>uaa-resource-server-spring-boot-starter</artifactId>");
        assertThat(application)
                .contains("resource-server:")
                .contains("service-name: family-ai-qwen")
                .contains("authorization-base-url: ${UAA_AUTHORIZATION_BASE_URL:http://127.0.0.1:39092}")
                .contains("/actuator/**")
                .contains("/v3/api-docs/**")
                .contains("/swagger-ui/**")
                .contains("/openapi-console/**");
    }

    /**
     * 校验 Qwen AI 模块的 Nacos 配置加载方式和 gateway 保持一致。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldLoadNacosConfigWithBootstrapStarter() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"));
        String bootstrap = Files.readString(Path.of("src/main/resources/bootstrap.yml"));
        assertThat(pom)
                .contains("<artifactId>spring-cloud-starter-bootstrap</artifactId>")
                .contains("<artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>");
        assertThat(bootstrap)
                .contains("optional:nacos:family-ai-qwen-${FAMILY_AI_BUTLER_ENV:dev}.yaml?group=DEFAULT_GROUP&refreshEnabled=true")
                .contains("file-extension: yaml");
    }

    /**
     * 校验 Nacos 备份配置包含 PostgreSQL 数据源配置。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldProvidePostgresDatasourceInNacosBackup() throws Exception {
        String backup = Files.readString(Path.of("src/main/resources/backup/family-ai-qwen-dev.yaml"));
        assertThat(backup)
                .contains("datasource:")
                .contains("jdbc:postgresql://${POSTGRES_HOST:127.0.0.1}:${POSTGRES_PORT:5432}/${POSTGRES_DB:familyaibutler}")
                .contains("driver-class-name: org.postgresql.Driver")
                .contains("open-in-view: false")
                .contains("ddl-auto: validate")
                .contains("org.hibernate.dialect.PostgreSQLDialect");
    }

    /**
     * 校验 Qwen AI 模块运行时包含 PostgreSQL 驱动。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldProvidePostgresRuntimeDriver() throws Exception {
        String pom = Files.readString(Path.of("pom.xml"));
        assertThat(pom)
                .contains("<groupId>org.postgresql</groupId>")
                .contains("<artifactId>postgresql</artifactId>")
                .contains("<scope>runtime</scope>");
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
