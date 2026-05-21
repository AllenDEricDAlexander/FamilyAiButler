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

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @ClassName: BindRoleResourceRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:00
 * @Description: 绑定角色和权限资源请求
 * @Version: 1.0
 */
public record BindRoleResourceRequest(
        @NotBlank String roleCode,
        @NotBlank String resourceCode
) {
}
