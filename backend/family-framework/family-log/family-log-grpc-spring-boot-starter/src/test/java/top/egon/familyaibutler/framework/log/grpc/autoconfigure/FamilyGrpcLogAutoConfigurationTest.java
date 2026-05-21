/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.grpc.autoconfigure
 * @FileName: FamilyGrpcLogAutoConfigurationTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:50
 * @Description: gRPC 日志自动装配测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.grpc.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.grpc.autoconfigure
 * @ClassName: FamilyGrpcLogAutoConfigurationTest
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:50
 * @Description: gRPC 日志自动装配测试
 * @Version: 1.0
 */
class FamilyGrpcLogAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FamilyGrpcLogAutoConfiguration.class));

    /**
     * 校验 gRPC classpath 存在时可以注册客户端和服务端拦截器。
     */
    @Test
    void shouldRegisterGrpcTraceInterceptors() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("familyGrpcTraceClientInterceptor");
            assertThat(context).hasBean("familyGrpcTraceServerInterceptor");
        });
    }
}
