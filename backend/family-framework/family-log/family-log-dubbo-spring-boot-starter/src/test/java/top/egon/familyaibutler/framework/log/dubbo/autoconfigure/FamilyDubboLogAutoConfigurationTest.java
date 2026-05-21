/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.dubbo.autoconfigure
 * @FileName: FamilyDubboLogAutoConfigurationTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:50
 * @Description: Dubbo 日志自动装配测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.dubbo.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.dubbo.autoconfigure
 * @ClassName: FamilyDubboLogAutoConfigurationTest
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:50
 * @Description: Dubbo 日志自动装配测试
 * @Version: 1.0
 */
class FamilyDubboLogAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FamilyDubboLogAutoConfiguration.class));

    /**
     * 校验 Dubbo classpath 存在时可以注册链路透传过滤器。
     */
    @Test
    void shouldRegisterDubboTraceFilter() {
        contextRunner.run(context -> assertThat(context).hasBean("familyDubboTraceFilter"));
    }
}
