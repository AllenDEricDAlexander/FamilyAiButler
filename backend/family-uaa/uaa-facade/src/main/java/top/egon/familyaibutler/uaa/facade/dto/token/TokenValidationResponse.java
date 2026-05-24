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

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @ClassName: TokenValidationResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Token 校验响应
 * @Version: 1.0
 */
public record TokenValidationResponse(
        @DocField(description = "令牌是否有效", example = "true")
        boolean valid,
        @DocField(description = "账号 ID", example = "account-001")
        String accountId,
        @DocField(description = "Profile ID", example = "profile-001")
        String profileId,
        @DocField(description = "会话 ID", example = "session-001")
        String sessionId,
        @DocField(description = "设备 ID", example = "device-001")
        String deviceId,
        @DocField(description = "认证版本号", example = "1")
        long authVersion,
        @DocField(description = "权益版本号", example = "1")
        long entitlementVersion,
        @DocField(description = "校验结果原因", example = "VALID")
        String reason
) {
}
