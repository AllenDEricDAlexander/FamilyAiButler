/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @FileName: BindRoleResourceRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:00
 * @Description: 绑定角色和权限资源请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.rbac;

import jakarta.validation.constraints.NotBlank;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @ClassName: BindRoleResourceRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:00
 * @Description: 绑定角色和权限资源请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeRbacBindRoleResourceRequest", description = "认证授权绑定角色权限资源请求")
public record BindRoleResourceRequest(
        /**角色编码。*/
        @DocField(description = "角色编码", required = true, example = "FAMILY_ADMIN")
        @NotBlank String roleCode,
        /**权限资源编码。*/
        @DocField(description = "权限资源编码", required = true, example = "FAMILY_PASSWORD_READ")
        @NotBlank String resourceCode
) {
}
