/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource.autoconfigure
 * @FileName: UaaResourceServerAutoConfigurationTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-16:45
 * @Description: UAA 资源服务自动配置测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.resource.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import top.egon.familyaibutler.uaa.resource.UaaResourceServerProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource.autoconfigure
 * @ClassName: UaaResourceServerAutoConfigurationTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 16:45
 * @Description: UAA 资源服务自动配置测试
 * @Version: 1.0
 */
class UaaResourceServerAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RestClientAutoConfiguration.class,
                    UaaResourceServerAutoConfiguration.class));

    /**
     * 校验自动配置可以绑定资源服务配置并注册授权过滤器。
     */
    @Test
    void shouldBindPropertiesAndRegisterAuthorizationFilter() {
        contextRunner.withPropertyValues(
                "family.uaa.resource-server.service-name=family-core",
                "family.uaa.resource-server.authorization-base-url=http://uaa",
                "family.uaa.resource-server.permit-patterns[0]=/actuator/**"
        ).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(UaaResourceServerProperties.class);
            assertThat(context.getBean(UaaResourceServerProperties.class).getServiceName()).isEqualTo("family-core");
            assertThat(context).hasBean("uaaResourceAuthorizationFilterRegistration");
        });
    }
}
