/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource
 * @FileName: UaaResourceServerProperties.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:45
 * @Description: UAA 资源服务配置文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.resource;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource
 * @ClassName: UaaResourceServerProperties
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:45
 * @Description: UAA 资源服务配置
 * @Version: 1.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "family.uaa.resource-server")
public class UaaResourceServerProperties {
    private boolean enabled = true;
    private String serviceName;
    private String authorizationBaseUrl = "http://127.0.0.1:39092";
    private Set<String> permitPatterns = new LinkedHashSet<>(Set.of("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**"));
}
