package top.egon.familyaibutler.gateway.config;

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
 * @BelongsPackage: top.egon.familyaibutler.gateway.filter
 * @ClassName: GatewayProperties
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

    @Getter
    @Setter
    public static class Jwt {

        private Set<String> ignoreurlset = new HashSet<>(Arrays.asList(
                "/uaa/user/login",
                "/uaa/user/register",
                "/uaa/user/refresh",
                "/openapi-console/**"
        ));
    }

}
