/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @FileName: TokenValidationResponse.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: Token 校验响应文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.token;

import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @ClassName: TokenValidationResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Token 校验响应
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeTokenValidationResponse", description = "认证授权令牌校验响应")
public record TokenValidationResponse(
        /**令牌是否有效。*/
        @DocField(description = "令牌是否有效", required = false, example = "true")
        boolean valid,
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
        String deviceId,
        /**认证版本号。*/
        @DocField(description = "认证版本号", required = false, example = "1")
        long authVersion,
        /**权益版本号。*/
        @DocField(description = "权益版本号", required = false, example = "1")
        long entitlementVersion,
        /**校验结果原因。*/
        @DocField(description = "校验结果原因", required = false, example = "VALID")
        String reason
) {
}
