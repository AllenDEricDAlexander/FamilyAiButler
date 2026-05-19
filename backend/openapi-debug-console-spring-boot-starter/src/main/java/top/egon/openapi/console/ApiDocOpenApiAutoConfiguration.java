/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @FileName: ApiDocOpenApiAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:35
 * @Description: OpenAPI 生产端自动配置文件
 * @Version: 1.0
 */
package top.egon.openapi.console;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @ClassName: ApiDocOpenApiAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:35
 * @Description: OpenAPI 生产端自动配置
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@EnableConfigurationProperties(ApiDocConsoleProperties.class)
@ConditionalOnProperty(prefix = "egon.openapi.console.producer", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ApiDocOpenApiAutoConfiguration {

    /**
     * 创建默认 OpenAPI 配置
     *
     * @param properties OpenAPI 控制台配置
     * @return OpenAPI 返回默认 OpenAPI 描述
     */
    @Bean
    @ConditionalOnMissingBean(OpenAPI.class)
    public OpenAPI apiDocOpenApi(ApiDocConsoleProperties properties) {
        ApiDocConsoleProperties.Producer producer = properties.getProducer();
        Info info = new Info()
                .title(producer.getTitle())
                .description(producer.getDescription())
                .version(producer.getVersion());
        fillContact(info, producer);
        fillLicense(info, producer);
        return new OpenAPI()
                .info(info)
                .components(new Components()
                        .addSecuritySchemes(producer.getAuthorizationHeader(),
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .scheme("token")
                                        .name(producer.getAuthorizationHeader())
                                        .in(SecurityScheme.In.HEADER)))
                .addSecurityItem(new SecurityRequirement().addList(producer.getAuthorizationHeader()));
    }

    /**
     * 填充联系人信息
     *
     * @param info     OpenAPI 基础信息
     * @param producer 业务模块 OpenAPI 生产配置
     */
    private void fillContact(Info info, ApiDocConsoleProperties.Producer producer) {
        if (!StringUtils.hasText(producer.getContactName())
                && !StringUtils.hasText(producer.getContactUrl())
                && !StringUtils.hasText(producer.getContactEmail())) {
            return;
        }
        info.contact(new Contact()
                .name(producer.getContactName())
                .url(producer.getContactUrl())
                .email(producer.getContactEmail()));
    }

    /**
     * 填充许可证信息
     *
     * @param info     OpenAPI 基础信息
     * @param producer 业务模块 OpenAPI 生产配置
     */
    private void fillLicense(Info info, ApiDocConsoleProperties.Producer producer) {
        if (!StringUtils.hasText(producer.getLicenseName()) && !StringUtils.hasText(producer.getLicenseUrl())) {
            return;
        }
        info.license(new License()
                .name(producer.getLicenseName())
                .url(producer.getLicenseUrl()));
    }
}
