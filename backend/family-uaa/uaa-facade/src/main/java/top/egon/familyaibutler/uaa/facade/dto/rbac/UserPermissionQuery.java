/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @FileName: UserPermissionQuery.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:00
 * @Description: 用户权限查询文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.rbac;

import jakarta.validation.constraints.NotBlank;
import top.egon.familyaibutler.uaa.facade.enums.PermissionResourceType;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @ClassName: UserPermissionQuery
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:00
 * @Description: 用户权限查询
 * @Version: 1.0
 */
public record UserPermissionQuery(
        @NotBlank String accountId,
        PermissionResourceType resourceType
) {
}
