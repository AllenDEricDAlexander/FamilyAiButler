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
        @DocField(description = "访问令牌", example = "access-token-001")
        String accessToken,
        @DocField(description = "刷新令牌", example = "refresh-token-001")
        String refreshToken,
        @DocField(description = "访问令牌剩余有效期秒数", example = "7200")
        long accessTokenExpiresIn,
        @DocField(description = "刷新令牌剩余有效期秒数", example = "2592000")
        long refreshTokenExpiresIn,
        @DocField(description = "令牌类型", example = "Bearer")
        String tokenType,
        @DocField(description = "账号 ID", example = "account-001")
        String accountId,
        @DocField(description = "Profile ID", example = "profile-001")
        String profileId,
        @DocField(description = "会话 ID", example = "session-001")
        String sessionId,
        @DocField(description = "设备 ID", example = "device-001")
        String deviceId
) {
}
