/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.authorization
 * @FileName: AuthorizationDecisionResponse.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:20
 * @Description: 授权决策响应文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.authorization;

import top.egon.openapi.console.annotation.DocField;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.authorization
 * @ClassName: AuthorizationDecisionResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:20
 * @Description: 授权决策响应
 * @Version: 1.0
 */
public record AuthorizationDecisionResponse(
        @DocField(description = "是否允许访问", example = "true")
        boolean allowed,
        @DocField(description = "授权决策原因", example = "ALLOW")
        String reason,
        @DocField(description = "账号 ID", example = "account-001")
        String accountId,
        @DocField(description = "Profile ID", example = "profile-001")
        String profileId,
        @DocField(description = "OAuth 客户端 ID", example = "family-web")
        String clientId,
        @DocField(description = "会话 ID", example = "session-001")
        String sessionId,
        @DocField(description = "设备 ID", example = "device-001")
        String deviceId
) {
}
