/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core.autoconfigure
 * @FileName: FamilyLogCoreAutoConfigurationTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-20:40
 * @Description: 家庭日志核心自动装配测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.core.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.TaskDecorator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core.autoconfigure
 * @ClassName: FamilyLogCoreAutoConfigurationTest
 * @Author: atluofu
 * @CreateTime: 2026-05-21 20:40
 * @Description: 家庭日志核心自动装配测试
 * @Version: 1.0
 */
class FamilyLogCoreAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FamilyLogCoreAutoConfiguration.class));

    /**
     * 校验默认会注册日志属性和线程池装饰器。
     */
    @Test
    void shouldRegisterPropertiesAndTaskDecoratorByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TaskDecorator.class);
            assertThat(context).hasSingleBean(top.egon.familyaibutler.framework.log.core.FamilyLogProperties.class);
        });
    }

    /**
     * 校验关闭异步透传后不注册线程池装饰器。
     */
    @Test
    void shouldSkipTaskDecoratorWhenAsyncDisabled() {
        contextRunner.withPropertyValues("family.log.async-enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(TaskDecorator.class));
    }
}
