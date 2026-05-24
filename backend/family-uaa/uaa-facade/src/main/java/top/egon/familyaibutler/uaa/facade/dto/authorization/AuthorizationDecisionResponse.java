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
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.authorization
 * @ClassName: AuthorizationDecisionResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:20
 * @Description: 授权决策响应
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeAuthorizationDecisionResponse", description = "认证授权资源访问授权决策响应")
public record AuthorizationDecisionResponse(
        /**是否允许访问。*/
        @DocField(description = "是否允许访问", required = false, example = "true")
        boolean allowed,
        /**授权决策原因。*/
        @DocField(description = "授权决策原因", required = false, example = "ALLOW")
        String reason,
        /**账号ID。*/
        @DocField(description = "账号 ID", required = false, example = "account-001")
        String accountId,
        /**ProfileID。*/
        @DocField(description = "Profile ID", required = false, example = "profile-001")
        String profileId,
        /**OAuth客户端ID。*/
        @DocField(description = "OAuth 客户端 ID", required = false, example = "family-web")
        String clientId,
        /**会话ID。*/
        @DocField(description = "会话 ID", required = false, example = "session-001")
        String sessionId,
        /**设备ID。*/
        @DocField(description = "设备 ID", required = false, example = "device-001")
        String deviceId
) {
}
