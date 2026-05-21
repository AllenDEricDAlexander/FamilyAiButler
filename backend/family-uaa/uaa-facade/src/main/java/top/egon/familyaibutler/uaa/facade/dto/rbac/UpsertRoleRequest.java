/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @FileName: UpsertRoleRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:00
 * @Description: 新增或更新角色请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.rbac;

import jakarta.validation.constraints.NotBlank;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @ClassName: UpsertRoleRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:00
 * @Description: 新增或更新角色请求
 * @Version: 1.0
 */
public record UpsertRoleRequest(
        @NotBlank String roleCode,
        @NotBlank String roleName
) {
}
