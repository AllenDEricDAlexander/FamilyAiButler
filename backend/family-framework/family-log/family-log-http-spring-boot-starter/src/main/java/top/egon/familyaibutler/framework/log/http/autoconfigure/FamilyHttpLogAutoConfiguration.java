/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.http.autoconfigure
 * @FileName: FamilyHttpLogAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:10
 * @Description: HTTP 日志自动装配文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.http.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import top.egon.familyaibutler.framework.log.core.FamilyLogProperties;
import top.egon.familyaibutler.framework.log.http.FamilyMdcRestClientCustomizer;
import top.egon.familyaibutler.framework.log.http.FamilyMdcWebClientCustomizer;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.http.autoconfigure
 * @ClassName: FamilyHttpLogAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:10
 * @Description: HTTP 日志自动装配
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "family.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FamilyHttpLogAutoConfiguration {

    /**
     * 注册 RestClient 日志链路透传自定义器。
     *
     * @param properties 日志配置
     * @return RestClientCustomizer 返回 RestClient 日志链路透传自定义器
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.web.client.RestClient")
    @ConditionalOnProperty(prefix = "family.log", name = {"mdc-enabled", "http-enabled"},
            havingValue = "true", matchIfMissing = true)
    public RestClientCustomizer familyMdcRestClientCustomizer(FamilyLogProperties properties) {
        return new FamilyMdcRestClientCustomizer(properties);
    }

    /**
     * 注册 WebClient 日志链路透传自定义器。
     *
     * @param properties 日志配置
     * @return WebClientCustomizer 返回 WebClient 日志链路透传自定义器
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.web.reactive.function.client.WebClient")
    @ConditionalOnProperty(prefix = "family.log", name = {"mdc-enabled", "http-enabled"},
            havingValue = "true", matchIfMissing = true)
    public WebClientCustomizer familyMdcWebClientCustomizer(FamilyLogProperties properties) {
        return new FamilyMdcWebClientCustomizer(properties);
    }
}
