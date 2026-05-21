/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @FileName: LogoutRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 退出登录请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.auth;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @ClassName: LogoutRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 退出登录请求
 * @Version: 1.0
 */
public record LogoutRequest(
        String accountId,
        String sessionId,
        String deviceId
) {
}
