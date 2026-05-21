/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @FileName: RefreshTokenRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 刷新令牌请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.token;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @ClassName: RefreshTokenRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 刷新令牌请求
 * @Version: 1.0
 */
public record RefreshTokenRequest(
        String refreshToken,
        String clientId,
        String deviceId
) {
}
