/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @FileName: UserPermissionResponse.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:00
 * @Description: 用户权限响应文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.rbac;

import top.egon.familyaibutler.uaa.facade.enums.PermissionResourceType;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

import java.util.Set;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @ClassName: UserPermissionResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:00
 * @Description: 用户权限响应
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeRbacUserPermissionResponse", description = "认证授权用户权限响应")
public record UserPermissionResponse(
        /**账号ID。*/
        @DocField(description = "账号 ID", required = false, example = "account-001")
        String accountId,
        /**权限资源类型。*/
        @DocField(description = "权限资源类型", required = false, example = "API")
        PermissionResourceType resourceType,
        /**权限资源编码集合。*/
        @DocField(description = "权限资源编码集合", required = false, example = "[\"FAMILY_PASSWORD_READ\"]")
        Set<String> resourceCodes
) {
}
