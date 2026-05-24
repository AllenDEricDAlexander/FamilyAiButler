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
import top.egon.openapi.console.annotation.DocField;

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
        @DocField(description = "账号 ID", example = "account-001")
        @NotBlank String accountId,
        @DocField(description = "权限资源类型", example = "API")
        PermissionResourceType resourceType
) {
}
