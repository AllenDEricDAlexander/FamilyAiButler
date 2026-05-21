package top.egon.familyaibutler.codegen.bootstrap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.bootstrap
 * @ClassName: CodegenApplicationTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:10
 * @Description: 代码生成器启动入口测试
 * @Version: 1.0
 */
class CodegenApplicationTest {

    /**
     * 校验 init 模式可以生成可复制修改的 YAML 和 DDL 模板。
     *
     * @param tempDir JUnit 临时目录
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldInitializeGeneratorTemplateFiles(@TempDir Path tempDir) throws Exception {
        Path starterDir = tempDir.resolve("starter");

        new CodegenApplication().run(new String[]{"--init", starterDir.toString()});

        assertThat(starterDir.resolve("generator.yml")).exists();
        assertThat(starterDir.resolve("schema.sql")).exists();
        assertThat(Files.readString(starterDir.resolve("generator.yml")))
                .contains("project:", "ddl:", "aggregates:", "enums:");
        assertThat(Files.readString(starterDir.resolve("schema.sql")))
                .contains("CREATE TABLE demo_order", "COMMENT ON COLUMN demo_order.status");
    }

    /**
     * 校验 demo 模式可以在指定目录直接生成 DDD/COLA 示例工程，便于 IDEA 无业务参数运行。
     *
     * @param tempDir JUnit 临时目录
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldRunDemoGenerationForIdeaRunConfiguration(@TempDir Path tempDir) throws Exception {
        Path demoDir = tempDir.resolve("codegen-demo");

        new CodegenApplication().run(new String[]{"--demo", demoDir.toString()});

        assertThat(demoDir.resolve("generator.yml")).exists();
        assertThat(demoDir.resolve("schema.sql")).exists();
        assertThat(demoDir.resolve("generated-src/src/main/java/com/example/codegen/demo/DemoOrderApplication.java")).exists();
        assertThat(demoDir.resolve("generated-src/src/main/java/com/example/codegen/demo/Demo-orderApplication.java")).doesNotExist();
        assertThat(demoDir.resolve("generated-src/src/main/java/com/example/codegen/demo/domain/order/gateway/OrderGateway.java")).exists();
        assertThat(demoDir.resolve("generated-src/src/main/java/com/example/codegen/demo/infrastructure/gateway/impl/OrderGatewayImpl.java")).exists();
        assertThat(demoDir.resolve("generated-src/src/main/resources/application.yml")).exists();
        assertThat(Files.readString(demoDir.resolve("generated-src/generation-report.md"))).contains("Order", "OrderStatus");
    }
}
