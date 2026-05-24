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
        /**OAuth客户端ID。*/
        @DocField(description = "OAuth 客户端 ID", required = false, example = "family-web")
        String clientId,
        /**OAuth客户端名称。*/
        @DocField(description = "OAuth 客户端名称", required = false, example = "Family Web")
        String clientName,
        /**OAuth客户端状态。*/
        @DocField(description = "OAuth 客户端状态", required = false, example = "ENABLED")
        String status,
        /**是否机密客户端。*/
        @DocField(description = "是否机密客户端", required = false, example = "true")
        boolean confidential,
        /**授权类型集合。*/
        @DocField(description = "授权类型集合", required = false, example = "[\"password\",\"refresh_token\"]")
        Set<String> grantTypes,
        /**授权范围集合。*/
        @DocField(description = "授权范围集合", required = false, example = "[\"profile\",\"family\"]")
        Set<String> scopes,
        /**资源路径匹配规则集合。*/
        @DocField(description = "资源路径匹配规则集合", required = false, example = "[\"/family/**\"]")
        Set<String> resourcePatterns,
        /**访问令牌有效期秒数。*/
        @DocField(description = "访问令牌有效期秒数", required = false, example = "7200")
        long accessTokenTtlSeconds,
        /**刷新令牌有效期秒数。*/
        @DocField(description = "刷新令牌有效期秒数", required = false, example = "2592000")
        long refreshTokenTtlSeconds
) {
}
