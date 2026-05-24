/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @FileName: RoleResponse.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:00
 * @Description: 角色响应文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.rbac;

import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @ClassName: RoleResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:00
 * @Description: 角色响应
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeRbacRoleResponse", description = "认证授权角色响应")
public record RoleResponse(
        @DocField(description = "角色编码", example = "FAMILY_ADMIN")
        String roleCode,
        @DocField(description = "角色名称", example = "家庭管理员")
        String roleName,
        @DocField(description = "角色状态", example = "ENABLED")
        String status
) {
}
