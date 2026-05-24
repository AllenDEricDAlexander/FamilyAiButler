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

import top.egon.openapi.console.annotation.DocField;

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
        @DocField(description = "访问令牌", example = "access-token-001")
        String accessToken,
        @DocField(description = "资源所属服务", example = "family-core")
        String resourceService,
        @DocField(description = "资源路径", example = "/password-view/1")
        String resourcePath,
        @DocField(description = "访问动作", example = "READ")
        String action
) {
}
