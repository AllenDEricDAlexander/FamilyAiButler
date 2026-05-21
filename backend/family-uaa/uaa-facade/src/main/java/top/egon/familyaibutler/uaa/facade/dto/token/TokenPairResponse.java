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

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @ClassName: TokenPairResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 令牌对响应
 * @Version: 1.0
 */
public record TokenPairResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        String tokenType,
        String accountId,
        String profileId,
        String sessionId,
        String deviceId
) {
}
