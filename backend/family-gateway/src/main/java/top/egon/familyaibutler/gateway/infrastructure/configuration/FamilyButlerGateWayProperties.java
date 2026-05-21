/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.infrastructure.configuration
 * @FileName: FamilyButlerGateWayProperties.java
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-15Day-21:09
 * @Description: 网关配置属性文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.infrastructure.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.infrastructure.configuration
 * @ClassName: FamilyButlerGateWayProperties
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-15Day-21:09
 * @Description: 配置文件
 * @Version: 1.0
 */
@Setter
@Getter
@Slf4j
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "family-gateway")
public class FamilyButlerGateWayProperties {

    private Jwt jwt = new Jwt();
    private Uaa uaa = new Uaa();

    /**
     * @BelongsProject: familyaibutler
     * @BelongsPackage: top.egon.familyaibutler.gateway.infrastructure.configuration
     * @ClassName: Jwt
     * @Author: atluofu
     * @CreateTime: 2025Year-08Month-15Day-21:09
     * @Description: JWT 网关配置
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class Jwt {

        private Set<String> ignoreUrlSet = new HashSet<>(Arrays.asList(
                "/uaa/auth/login/password",
                "/uaa/auth/login/email-code",
                "/uaa/auth/login/sms-code",
                "/uaa/account/register",
                "/uaa/auth/password/recovery",
                "/uaa/auth/password/reset",
                "/base/v3/api-docs/**",
                "/uaa/v3/api-docs/**",
                "/ai/v3/api-docs/**",
                "/openapi-console/**"
        ));
    }

    /**
     * @BelongsProject: familyaibutler
     * @BelongsPackage: top.egon.familyaibutler.gateway.infrastructure.configuration
     * @ClassName: Uaa
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-20Day-22:30
     * @Description: UAA 网关配置
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class Uaa {
        private String baseUrl = "http://127.0.0.1:39092";
    }

}
