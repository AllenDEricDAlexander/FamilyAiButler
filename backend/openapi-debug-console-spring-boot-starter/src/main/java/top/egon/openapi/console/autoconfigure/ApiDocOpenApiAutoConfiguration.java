/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.autoconfigure
 * @FileName: ApiDocOpenApiAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:35
 * @Description: OpenAPI 生产端自动配置文件
 * @Version: 1.0
 */
package top.egon.openapi.console.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.egon.openapi.console.ApiDocConsoleProperties;
import top.egon.openapi.console.openapi.ApiDocOpenApiController;
import top.egon.openapi.console.openapi.ApiDocOpenApiSchemaGenerator;
import top.egon.openapi.console.openapi.ApiDocSpringMvcOpenApiGenerator;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.autoconfigure
 * @ClassName: ApiDocOpenApiAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:35
 * @Description: OpenAPI 生产端自动配置
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass({DispatcherServlet.class, RequestMappingHandlerMapping.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(ApiDocConsoleProperties.class)
@ConditionalOnProperty(prefix = "egon.openapi.console.producer", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ApiDocOpenApiAutoConfiguration {

    /**
     * 创建 OpenAPI Schema 生成器
     *
     * @param objectMapper Jackson 映射器
     * @return ApiDocOpenApiSchemaGenerator 返回 OpenAPI Schema 生成器
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiDocOpenApiSchemaGenerator apiDocOpenApiSchemaGenerator(ObjectMapper objectMapper) {
        return new ApiDocOpenApiSchemaGenerator(objectMapper);
    }

    /**
     * 创建 Spring MVC OpenAPI 文档生成器
     *
     * @param handlerMapping  Spring MVC 映射处理器
     * @param schemaGenerator OpenAPI Schema 生成器
     * @param properties      OpenAPI 控制台配置
     * @return ApiDocSpringMvcOpenApiGenerator 返回 Spring MVC OpenAPI 文档生成器
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiDocSpringMvcOpenApiGenerator apiDocSpringMvcOpenApiGenerator(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
                                                                           ApiDocOpenApiSchemaGenerator schemaGenerator,
                                                                           ApiDocConsoleProperties properties) {
        return new ApiDocSpringMvcOpenApiGenerator(handlerMapping, schemaGenerator, properties);
    }

    /**
     * 创建 OpenAPI JSON 控制器
     *
     * @param openApiGenerator Spring MVC OpenAPI 文档生成器
     * @return ApiDocOpenApiController 返回 OpenAPI JSON 控制器
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiDocOpenApiController apiDocOpenApiController(ApiDocSpringMvcOpenApiGenerator openApiGenerator) {
        return new ApiDocOpenApiController(openApiGenerator);
    }
}
