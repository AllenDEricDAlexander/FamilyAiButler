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

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @ClassName: RoleResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:00
 * @Description: 角色响应
 * @Version: 1.0
 */
public record RoleResponse(
        String roleCode,
        String roleName,
        String status
) {
}
