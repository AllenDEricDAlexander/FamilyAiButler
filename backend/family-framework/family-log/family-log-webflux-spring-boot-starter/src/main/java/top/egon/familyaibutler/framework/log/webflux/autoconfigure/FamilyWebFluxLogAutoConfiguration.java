/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.webflux.autoconfigure
 * @FileName: FamilyWebFluxLogAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:10
 * @Description: WebFlux 日志自动装配文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.webflux.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;
import top.egon.familyaibutler.framework.log.core.FamilyLogProperties;
import top.egon.familyaibutler.framework.log.webflux.FamilyMdcWebFluxFilter;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.webflux.autoconfigure
 * @ClassName: FamilyWebFluxLogAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:10
 * @Description: WebFlux 日志自动装配
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnMissingClass("org.springframework.cloud.gateway.filter.GlobalFilter")
@ConditionalOnProperty(prefix = "family.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FamilyWebFluxLogAutoConfiguration {

    /**
     * 注册 WebFlux MDC 过滤器。
     *
     * @param properties 日志配置
     * @return WebFilter 返回 WebFlux MDC 过滤器
     */
    @Bean
    @ConditionalOnMissingBean(FamilyMdcWebFluxFilter.class)
    @ConditionalOnProperty(prefix = "family.log", name = {"mdc-enabled", "webflux-enabled"},
            havingValue = "true", matchIfMissing = true)
    public WebFilter familyMdcWebFluxFilter(FamilyLogProperties properties) {
        return new FamilyMdcWebFluxFilter(properties);
    }
}
