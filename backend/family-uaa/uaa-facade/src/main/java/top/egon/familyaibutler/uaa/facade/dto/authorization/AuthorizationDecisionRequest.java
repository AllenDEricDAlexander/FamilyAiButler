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
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.authorization
 * @ClassName: AuthorizationDecisionRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:20
 * @Description: 授权决策请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeAuthorizationDecisionRequest", description = "认证授权资源访问授权决策请求")
public record AuthorizationDecisionRequest(
        /**访问令牌。*/
        @DocField(description = "访问令牌", required = false, example = "access-token-001")
        String accessToken,
        /**资源所属服务。*/
        @DocField(description = "资源所属服务", required = false, example = "family-core")
        String resourceService,
        /**资源路径。*/
        @DocField(description = "资源路径", required = false, example = "/password-view/1")
        String resourcePath,
        /**访问动作。*/
        @DocField(description = "访问动作", required = false, example = "READ")
        String action
) {
}
