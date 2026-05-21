/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.oauthclient
 * @FileName: CreateOAuthClientRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-16:45
 * @Description: 创建 OAuth Client 请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.oauthclient;

import jakarta.validation.constraints.NotBlank;

import java.util.Set;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.oauthclient
 * @ClassName: CreateOAuthClientRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 16:45
 * @Description: 创建 OAuth Client 请求
 * @Version: 1.0
 */
public record CreateOAuthClientRequest(
        @NotBlank String clientId,
        @NotBlank String clientName,
        String clientSecret,
        Set<String> grantTypes,
        Set<String> scopes,
        Set<String> resourcePatterns,
        Long accessTokenTtlSeconds,
        Long refreshTokenTtlSeconds
) {
}
