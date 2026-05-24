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
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

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
@DocModel(name = "UaaFacadeCreateOAuthClientRequest", description = "认证授权创建 OAuth 客户端请求")
public record CreateOAuthClientRequest(
        /**OAuth客户端ID。*/
        @DocField(description = "OAuth 客户端 ID", required = true, example = "family-web")
        @NotBlank String clientId,
        /**OAuth客户端名称。*/
        @DocField(description = "OAuth 客户端名称", required = true, example = "Family Web")
        @NotBlank String clientName,
        /**OAuth客户端密钥。*/
        @DocField(description = "OAuth 客户端密钥", required = false, example = "family-secret")
        String clientSecret,
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
        Long accessTokenTtlSeconds,
        /**刷新令牌有效期秒数。*/
        @DocField(description = "刷新令牌有效期秒数", required = false, example = "2592000")
        Long refreshTokenTtlSeconds
) {
}
