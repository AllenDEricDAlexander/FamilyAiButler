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

import top.egon.openapi.console.annotation.DocField;

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
        @DocField(description = "账号 ID", example = "account-001")
        String accountId,
        @DocField(description = "会话 ID", example = "session-001")
        String sessionId,
        @DocField(description = "设备 ID", example = "device-001")
        String deviceId
) {
}
