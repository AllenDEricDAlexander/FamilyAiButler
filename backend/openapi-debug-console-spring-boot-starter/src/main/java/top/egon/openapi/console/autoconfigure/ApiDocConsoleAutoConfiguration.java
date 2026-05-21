/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.autoconfigure
 * @FileName: ApiDocConsoleAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:25
 * @Description: OpenAPI 调试文档控制台自动配置文件
 * @Version: 1.0
 */
package top.egon.openapi.console.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.function.client.WebClient;
import top.egon.openapi.console.ApiDocConsoleProperties;
import top.egon.openapi.console.client.ApiDocConsoleHttpClient;
import top.egon.openapi.console.client.ApiDocConsoleReactiveHttpClient;
import top.egon.openapi.console.client.ApiDocConsoleVirtualThreadHttpClient;
import top.egon.openapi.console.core.ApiDocConsoleDocumentRenderer;
import top.egon.openapi.console.core.ApiDocConsoleService;
import top.egon.openapi.console.core.ApiDocConsoleSessionService;
import top.egon.openapi.console.filter.ApiDocConsoleSecurityWebFilter;
import top.egon.openapi.console.web.ApiDocConsoleController;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.autoconfigure
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
     * @return WebClient.Builder 返回 WebClient 构造器
     */
    @Bean
    @ConditionalOnMissingBean
    public WebClient.Builder apiDocConsoleWebClientBuilder() {
        return WebClient.builder();
    }

    /**
     * 创建控制台 HTTP 客户端
     *
     * @param properties       接口文档平台配置
     * @param webClientBuilder WebClient 构造器
     * @return ApiDocConsoleHttpClient 返回控制台 HTTP 客户端
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public ApiDocConsoleHttpClient apiDocConsoleHttpClient(ApiDocConsoleProperties properties,
                                                           WebClient.Builder webClientBuilder) {
        if (useVirtualThreadClient(properties)) {
            return new ApiDocConsoleVirtualThreadHttpClient(properties);
        }
        return new ApiDocConsoleReactiveHttpClient(properties, webClientBuilder);
    }

    /**
     * 判断是否使用虚拟线程 HTTP 客户端
     *
     * @param properties 接口文档平台配置
     * @return boolean 返回 true 表示使用虚拟线程客户端
     */
    private boolean useVirtualThreadClient(ApiDocConsoleProperties properties) {
        return properties.getClient().getEngine() == ApiDocConsoleProperties.ClientEngine.AUTO
                || properties.getClient().getEngine() == ApiDocConsoleProperties.ClientEngine.VIRTUAL_THREAD;
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
     * 创建接口文档平台控制器
     *
     * @param properties     接口文档平台配置
     * @param sessionService 会话服务
     * @param consoleService 核心服务
     * @param objectMapper   Jackson 映射器
     * @return ApiDocConsoleController 返回控制器
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiDocConsoleController apiDocConsoleController(ApiDocConsoleProperties properties,
                                                           ApiDocConsoleSessionService sessionService,
                                                           ApiDocConsoleService consoleService,
                                                           ObjectMapper objectMapper) {
        return new ApiDocConsoleController(properties, sessionService, consoleService, objectMapper);
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
