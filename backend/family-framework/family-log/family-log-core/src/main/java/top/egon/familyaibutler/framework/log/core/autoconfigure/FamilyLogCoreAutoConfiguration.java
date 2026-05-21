/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core.autoconfigure
 * @FileName: FamilyLogCoreAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-20:40
 * @Description: 家庭日志核心自动装配文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.core.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskDecorator;
import top.egon.familyaibutler.framework.log.core.FamilyLogProperties;
import top.egon.familyaibutler.framework.log.core.FamilyMdcTaskDecorator;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core.autoconfigure
 * @ClassName: FamilyLogCoreAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026-05-21 20:40
 * @Description: 家庭日志核心自动装配
 * @Version: 1.0
 */
@AutoConfiguration
@EnableConfigurationProperties(FamilyLogProperties.class)
@ConditionalOnProperty(prefix = "family.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FamilyLogCoreAutoConfiguration {

    /**
     * 注册异步线程 MDC 装饰器。
     *
     * @return TaskDecorator 返回异步线程 MDC 装饰器
     */
    @Bean
    @ConditionalOnMissingBean(TaskDecorator.class)
    @ConditionalOnProperty(prefix = "family.log", name = "async-enabled", havingValue = "true", matchIfMissing = true)
    public TaskDecorator familyMdcTaskDecorator() {
        return new FamilyMdcTaskDecorator();
    }
}
