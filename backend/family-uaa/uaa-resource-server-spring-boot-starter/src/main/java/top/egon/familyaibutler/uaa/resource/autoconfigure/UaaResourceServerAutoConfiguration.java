/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource.autoconfigure
 * @FileName: UaaResourceServerAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:45
 * @Description: UAA 资源服务自动配置文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.resource.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;
import top.egon.familyaibutler.uaa.resource.RestUaaResourceAuthorizationClient;
import top.egon.familyaibutler.uaa.resource.UaaResourceAuthorizationClient;
import top.egon.familyaibutler.uaa.resource.UaaResourceAuthorizationFilter;
import top.egon.familyaibutler.uaa.resource.UaaResourceServerProperties;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource.autoconfigure
 * @ClassName: UaaResourceServerAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:45
 * @Description: UAA 资源服务自动配置
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(UaaResourceServerProperties.class)
@ConditionalOnProperty(prefix = "family.uaa.resource-server", name = "enabled", havingValue = "true", matchIfMissing = true)
public class UaaResourceServerAutoConfiguration {

    /**
     * 创建 REST UAA 资源授权客户端。
     *
     * @param properties        UAA 资源服务配置
     * @param restClientBuilder REST 客户端构造器
     * @return UAA 资源授权客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public UaaResourceAuthorizationClient uaaResourceAuthorizationClient(UaaResourceServerProperties properties,
                                                                         RestClient.Builder restClientBuilder) {
        return new RestUaaResourceAuthorizationClient(properties, restClientBuilder);
    }

    /**
     * 创建 UAA 资源授权过滤器注册。
     *
     * @param properties          UAA 资源服务配置
     * @param authorizationClient UAA 资源授权客户端
     * @param environment         环境变量
     * @return 过滤器注册
     */
    @Bean
    @ConditionalOnMissingBean(name = "uaaResourceAuthorizationFilterRegistration")
    public FilterRegistrationBean<UaaResourceAuthorizationFilter> uaaResourceAuthorizationFilterRegistration(
            UaaResourceServerProperties properties, UaaResourceAuthorizationClient authorizationClient, Environment environment) {
        FilterRegistrationBean<UaaResourceAuthorizationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new UaaResourceAuthorizationFilter(properties, authorizationClient, environment));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
