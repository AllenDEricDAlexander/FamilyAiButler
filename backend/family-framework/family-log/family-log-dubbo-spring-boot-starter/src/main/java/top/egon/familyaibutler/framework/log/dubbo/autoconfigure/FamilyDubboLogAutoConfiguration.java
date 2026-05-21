/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.dubbo.autoconfigure
 * @FileName: FamilyDubboLogAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:35
 * @Description: Dubbo 日志自动装配文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.dubbo.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import top.egon.familyaibutler.framework.log.dubbo.FamilyDubboTraceFilter;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.dubbo.autoconfigure
 * @ClassName: FamilyDubboLogAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:35
 * @Description: Dubbo 日志自动装配
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.apache.dubbo.rpc.Filter")
@ConditionalOnProperty(prefix = "family.log", name = {"enabled", "dubbo-enabled"},
        havingValue = "true", matchIfMissing = true)
public class FamilyDubboLogAutoConfiguration {

    /**
     * 注册 Dubbo 链路透传过滤器。
     *
     * @return Object 返回 Dubbo 链路透传过滤器
     */
    @Bean
    public Object familyDubboTraceFilter() {
        return new FamilyDubboTraceFilter();
    }
}
