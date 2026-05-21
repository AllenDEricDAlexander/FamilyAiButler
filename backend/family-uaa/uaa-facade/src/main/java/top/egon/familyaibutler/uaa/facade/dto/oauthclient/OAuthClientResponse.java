/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.oauthclient
 * @FileName: OAuthClientResponse.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-16:45
 * @Description: OAuth Client 响应文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.oauthclient;

import java.util.Set;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.oauthclient
 * @ClassName: OAuthClientResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 16:45
 * @Description: OAuth Client 响应
 * @Version: 1.0
 */
public record OAuthClientResponse(
        String clientId,
        String clientName,
        String status,
        boolean confidential,
        Set<String> grantTypes,
        Set<String> scopes,
        Set<String> resourcePatterns,
        long accessTokenTtlSeconds,
        long refreshTokenTtlSeconds
) {
}
