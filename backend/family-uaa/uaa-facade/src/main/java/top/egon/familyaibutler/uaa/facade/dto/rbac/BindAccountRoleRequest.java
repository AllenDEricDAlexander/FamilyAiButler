/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @FileName: BindAccountRoleRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:00
 * @Description: 绑定账号和角色请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.rbac;

import jakarta.validation.constraints.NotBlank;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @ClassName: BindAccountRoleRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:00
 * @Description: 绑定账号和角色请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeRbacBindAccountRoleRequest", description = "认证授权绑定账号角色请求")
public record BindAccountRoleRequest(
        /**账号ID。*/
        @DocField(description = "账号 ID", required = true, example = "account-001")
        @NotBlank String accountId,
        /**角色编码。*/
        @DocField(description = "角色编码", required = true, example = "FAMILY_ADMIN")
        @NotBlank String roleCode
) {
}
