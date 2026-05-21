/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.webflux.autoconfigure
 * @FileName: FamilyGatewayLogAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:10
 * @Description: Gateway 日志自动装配文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.webflux.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import top.egon.familyaibutler.framework.log.core.FamilyLogProperties;
import top.egon.familyaibutler.framework.log.webflux.FamilyMdcGatewayGlobalFilter;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.webflux.autoconfigure
 * @ClassName: FamilyGatewayLogAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:10
 * @Description: Gateway 日志自动装配
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.cloud.gateway.filter.GlobalFilter")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(prefix = "family.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FamilyGatewayLogAutoConfiguration {

    /**
     * 注册 Gateway MDC 全局过滤器。
     *
     * @param properties 日志配置
     * @return Object 返回 Gateway MDC 全局过滤器
     */
    @Bean
    @ConditionalOnProperty(prefix = "family.log", name = {"mdc-enabled", "webflux-enabled"},
            havingValue = "true", matchIfMissing = true)
    public Object familyMdcGatewayGlobalFilter(FamilyLogProperties properties) {
        return new FamilyMdcGatewayGlobalFilter(properties);
    }
}
