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
        boolean allowed,
        String reason,
        String accountId,
        String profileId,
        String clientId,
        String sessionId,
        String deviceId
) {
}
