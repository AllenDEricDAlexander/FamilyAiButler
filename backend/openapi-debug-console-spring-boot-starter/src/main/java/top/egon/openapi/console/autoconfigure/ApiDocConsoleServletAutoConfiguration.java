/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.autoconfigure
 * @FileName: ApiDocConsoleServletAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-18:05
 * @Description: OpenAPI 调试文档控制台 Servlet 环境自动配置文件
 * @Version: 1.0
 */
package top.egon.openapi.console.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RestController;
import top.egon.openapi.console.ApiDocConsoleProperties;
import top.egon.openapi.console.client.ApiDocConsoleHttpClient;
import top.egon.openapi.console.client.ApiDocConsoleVirtualThreadHttpClient;
import top.egon.openapi.console.core.ApiDocConsoleDocumentRenderer;
import top.egon.openapi.console.core.ApiDocConsoleService;
import top.egon.openapi.console.core.ApiDocConsoleSessionService;
import top.egon.openapi.console.filter.ApiDocConsoleServletFilter;
import top.egon.openapi.console.filter.ApiDocOpenApiAccessServletFilter;
import top.egon.openapi.console.web.ApiDocConsoleMvcController;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.autoconfigure
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
     * 创建控制台 HTTP 客户端
     *
     * @param properties 接口文档平台配置
     * @return ApiDocConsoleHttpClient 返回控制台 HTTP 客户端
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public ApiDocConsoleHttpClient apiDocConsoleHttpClient(ApiDocConsoleProperties properties) {
        return new ApiDocConsoleVirtualThreadHttpClient(properties);
    }

    /**
     * 创建接口文档平台会话服务
     *
     * @param properties  接口文档平台配置
     * @param environment Spring 环境对象
     * @return ApiDocConsoleSessionService 返回会话服务
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiDocConsoleSessionService apiDocConsoleSessionService(ApiDocConsoleProperties properties,
                                                                   Environment environment) {
        return new ApiDocConsoleSessionService(properties, environment);
    }

    /**
     * 创建接口文档导出渲染器
     *
     * @return ApiDocConsoleDocumentRenderer 返回文档导出渲染器
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiDocConsoleDocumentRenderer apiDocConsoleDocumentRenderer() {
        return new ApiDocConsoleDocumentRenderer();
    }

    /**
     * 创建接口文档平台核心服务
     *
     * @param properties               接口文档平台配置
     * @param objectMapper             Jackson 映射器
     * @param httpClient               控制台 HTTP 客户端
     * @param documentRenderer         文档导出渲染器
     * @param discoveryClients         响应式服务发现客户端
     * @param blockingDiscoveryClients 阻塞式服务发现客户端
     * @return ApiDocConsoleService 返回核心服务
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiDocConsoleService apiDocConsoleService(ApiDocConsoleProperties properties,
                                                     ObjectMapper objectMapper,
                                                     ApiDocConsoleHttpClient httpClient,
                                                     ApiDocConsoleDocumentRenderer documentRenderer,
                                                     ObjectProvider<ReactiveDiscoveryClient> discoveryClients,
                                                     ObjectProvider<DiscoveryClient> blockingDiscoveryClients) {
        return new ApiDocConsoleService(properties, objectMapper, httpClient, documentRenderer, discoveryClients, blockingDiscoveryClients);
    }

    /**
     * 创建接口文档平台 MVC 控制器
     *
     * @param properties     接口文档平台配置
     * @param sessionService 会话服务
     * @param consoleService 核心服务
     * @return ApiDocConsoleMvcController 返回 MVC 控制器
     */
    @Bean
    @ConditionalOnClass(RestController.class)
    @ConditionalOnMissingBean
    public ApiDocConsoleMvcController apiDocConsoleMvcController(ApiDocConsoleProperties properties,
                                                                 ApiDocConsoleSessionService sessionService,
                                                                 ApiDocConsoleService consoleService) {
        return new ApiDocConsoleMvcController(properties, sessionService, consoleService);
    }

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
     * 创建 OpenAPI JSON 内部访问控制过滤器
     *
     * @param properties 控制台配置
     * @return FilterRegistrationBean<ApiDocOpenApiAccessServletFilter> 返回过滤器注册信息
     */
    @Bean
    public FilterRegistrationBean<ApiDocOpenApiAccessServletFilter> apiDocOpenApiAccessServletFilter(ApiDocConsoleProperties properties) {
        FilterRegistrationBean<ApiDocOpenApiAccessServletFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ApiDocOpenApiAccessServletFilter(properties));
        registrationBean.addUrlPatterns("/v3/api-docs", "/v3/api-docs/*");
        registrationBean.setOrder(Integer.MIN_VALUE + 1);
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
