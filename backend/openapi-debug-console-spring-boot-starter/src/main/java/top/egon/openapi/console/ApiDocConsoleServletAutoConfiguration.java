/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @FileName: ApiDocConsoleServletAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-18:05
 * @Description: OpenAPI 调试文档控制台 Servlet 环境自动配置文件
 * @Version: 1.0
 */
package top.egon.openapi.console;

import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @ClassName: ApiDocConsoleServletAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-18:05
 * @Description: OpenAPI 调试文档控制台 Servlet 环境自动配置
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass(Filter.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(ApiDocConsoleProperties.class)
public class ApiDocConsoleServletAutoConfiguration {

    /**
     * 创建 Servlet 静态资源保护过滤器
     *
     * @param properties 控制台配置
     * @return FilterRegistrationBean<ApiDocConsoleServletFilter> 返回过滤器注册信息
     */
    @Bean
    public FilterRegistrationBean<ApiDocConsoleServletFilter> apiDocConsoleServletFilter(ApiDocConsoleProperties properties) {
        FilterRegistrationBean<ApiDocConsoleServletFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ApiDocConsoleServletFilter(properties));
        registrationBean.addUrlPatterns(normalizedBasePath(properties) + "/*");
        registrationBean.setOrder(Integer.MIN_VALUE);
        return registrationBean;
    }

    /**
     * 获取规范化基础路径
     *
     * @param properties 控制台配置
     * @return String 返回规范化基础路径
     */
    private String normalizedBasePath(ApiDocConsoleProperties properties) {
        String basePath = properties.getBasePath();
        if (basePath == null || basePath.isBlank()) {
            return "/openapi-console";
        }
        return basePath.startsWith("/") ? basePath : "/" + basePath;
    }
}
