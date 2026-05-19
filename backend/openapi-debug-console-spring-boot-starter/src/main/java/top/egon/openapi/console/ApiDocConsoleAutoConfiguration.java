/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @FileName: ApiDocConsoleAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:25
 * @Description: OpenAPI 调试文档控制台自动配置文件
 * @Version: 1.0
 */
package top.egon.openapi.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @ClassName: ApiDocConsoleAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:25
 * @Description: OpenAPI 调试文档控制台自动配置
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass(DispatcherHandler.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableConfigurationProperties(ApiDocConsoleProperties.class)
public class ApiDocConsoleAutoConfiguration {

    /**
     * 创建 WebClient 构造器
     *
     * @param exchangeFilterFunctions WebClient 过滤器
     * @return WebClient.Builder 返回 WebClient 构造器
     */
    @Bean
    @ConditionalOnMissingBean
    public WebClient.Builder apiDocConsoleWebClientBuilder(ObjectProvider<ExchangeFilterFunction> exchangeFilterFunctions) {
        WebClient.Builder builder = WebClient.builder();
        exchangeFilterFunctions.stream()
                .filter(filter -> filter.getClass().getName().contains("LoadBalancerExchangeFilterFunction"))
                .findFirst()
                .ifPresent(builder::filter);
        return builder;
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
     * @param properties       接口文档平台配置
     * @param objectMapper     Jackson 映射器
     * @param webClientBuilder WebClient 构造器
     * @param documentRenderer 文档导出渲染器
     * @param discoveryClients 响应式服务发现客户端
     * @return ApiDocConsoleService 返回核心服务
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiDocConsoleService apiDocConsoleService(ApiDocConsoleProperties properties,
                                                     ObjectMapper objectMapper,
                                                     WebClient.Builder webClientBuilder,
                                                     ApiDocConsoleDocumentRenderer documentRenderer,
                                                     ObjectProvider<ReactiveDiscoveryClient> discoveryClients) {
        return new ApiDocConsoleService(properties, objectMapper, webClientBuilder, documentRenderer, discoveryClients);
    }

    /**
     * 创建接口文档平台控制器
     *
     * @param properties     接口文档平台配置
     * @param sessionService 会话服务
     * @param consoleService 核心服务
     * @return ApiDocConsoleController 返回控制器
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiDocConsoleController apiDocConsoleController(ApiDocConsoleProperties properties,
                                                           ApiDocConsoleSessionService sessionService,
                                                           ApiDocConsoleService consoleService) {
        return new ApiDocConsoleController(properties, sessionService, consoleService);
    }

    /**
     * 创建接口文档平台安全过滤器
     *
     * @param properties     接口文档平台配置
     * @param sessionService 会话服务
     * @return ApiDocConsoleSecurityWebFilter 返回安全过滤器
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiDocConsoleSecurityWebFilter apiDocConsoleSecurityWebFilter(ApiDocConsoleProperties properties,
                                                                         ApiDocConsoleSessionService sessionService) {
        return new ApiDocConsoleSecurityWebFilter(properties, sessionService);
    }
}
