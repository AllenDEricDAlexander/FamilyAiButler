/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.security.jwt
 * @FileName: FamilyJwtProperties.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-23:50
 * @Description: 统一 JWT 配置文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.common.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.security.jwt
 * @ClassName: FamilyJwtProperties
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-23:50
 * @Description: 统一 JWT 配置
 * @Version: 1.0
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "family.security.jwt")
public class FamilyJwtProperties {

    private boolean enabled = false;

    private String authorizationHeader = "Authorization";

    private String tokenPrefix = "";

    private String accessKey;

    private String refreshKey;

    private long accessTokenExpireTime = 300000L;

    private long refreshTokenExpireTime = 2592000000L;
}
