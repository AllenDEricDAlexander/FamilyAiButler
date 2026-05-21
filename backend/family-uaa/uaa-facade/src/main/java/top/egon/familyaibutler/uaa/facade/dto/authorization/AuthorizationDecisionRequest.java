/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.authorization
 * @FileName: AuthorizationDecisionRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:20
 * @Description: 授权决策请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.authorization;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.authorization
 * @ClassName: AuthorizationDecisionRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:20
 * @Description: 授权决策请求
 * @Version: 1.0
 */
public record AuthorizationDecisionRequest(
        String accessToken,
        String resourceService,
        String resourcePath,
        String action
) {
}
