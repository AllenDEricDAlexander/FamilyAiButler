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
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @ClassName: LogoutRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 退出登录请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeLogoutRequest", description = "认证授权退出登录请求")
public record LogoutRequest(
        /**账号ID。*/
        @DocField(description = "账号 ID", required = false, example = "account-001")
        String accountId,
        /**会话ID。*/
        @DocField(description = "会话 ID", required = false, example = "session-001")
        String sessionId,
        /**设备ID。*/
        @DocField(description = "设备 ID", required = false, example = "device-001")
        String deviceId
) {
}
