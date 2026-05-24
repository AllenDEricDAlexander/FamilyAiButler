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

import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

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
@DocModel(name = "UaaFacadeOAuthClientResponse", description = "认证授权 OAuth 客户端响应")
public record OAuthClientResponse(
        @DocField(description = "OAuth 客户端 ID", example = "family-web")
        String clientId,
        @DocField(description = "OAuth 客户端名称", example = "Family Web")
        String clientName,
        @DocField(description = "OAuth 客户端状态", example = "ENABLED")
        String status,
        @DocField(description = "是否机密客户端", example = "true")
        boolean confidential,
        @DocField(description = "授权类型集合", example = "[\"password\",\"refresh_token\"]")
        Set<String> grantTypes,
        @DocField(description = "授权范围集合", example = "[\"profile\",\"family\"]")
        Set<String> scopes,
        @DocField(description = "资源路径匹配规则集合", example = "[\"/family/**\"]")
        Set<String> resourcePatterns,
        @DocField(description = "访问令牌有效期秒数", example = "7200")
        long accessTokenTtlSeconds,
        @DocField(description = "刷新令牌有效期秒数", example = "2592000")
        long refreshTokenTtlSeconds
) {
}
