/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @FileName: TokenPairResponse.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 令牌对响应文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.token;

import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @ClassName: TokenPairResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 令牌对响应
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeTokenPairResponse", description = "认证授权令牌对响应")
public record TokenPairResponse(
        /**访问令牌。*/
        @DocField(description = "访问令牌", required = false, example = "access-token-001")
        String accessToken,
        /**刷新令牌。*/
        @DocField(description = "刷新令牌", required = false, example = "refresh-token-001")
        String refreshToken,
        /**访问令牌剩余有效期秒数。*/
        @DocField(description = "访问令牌剩余有效期秒数", required = false, example = "7200")
        long accessTokenExpiresIn,
        /**刷新令牌剩余有效期秒数。*/
        @DocField(description = "刷新令牌剩余有效期秒数", required = false, example = "2592000")
        long refreshTokenExpiresIn,
        /**令牌类型。*/
        @DocField(description = "令牌类型", required = false, example = "Bearer")
        String tokenType,
        /**账号ID。*/
        @DocField(description = "账号 ID", required = false, example = "account-001")
        String accountId,
        /**ProfileID。*/
        @DocField(description = "Profile ID", required = false, example = "profile-001")
        String profileId,
        /**会话ID。*/
        @DocField(description = "会话 ID", required = false, example = "session-001")
        String sessionId,
        /**设备ID。*/
        @DocField(description = "设备 ID", required = false, example = "device-001")
        String deviceId
) {
}
