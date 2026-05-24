/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @FileName: TokenValidationRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: Token 校验请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.token;

import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @ClassName: TokenValidationRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Token 校验请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeTokenValidationRequest", description = "认证授权令牌校验请求")
public record TokenValidationRequest(
        /**访问令牌。*/
        @DocField(description = "访问令牌", required = false, example = "access-token-001")
        String accessToken,
        /**资源标识。*/
        @DocField(description = "资源标识", required = false, example = "/password-view/1")
        String resource,
        /**访问动作。*/
        @DocField(description = "访问动作", required = false, example = "READ")
        String action
) {
}
