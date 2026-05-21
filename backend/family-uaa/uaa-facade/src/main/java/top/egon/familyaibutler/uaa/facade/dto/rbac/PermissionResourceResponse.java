/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @FileName: PermissionResourceResponse.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:00
 * @Description: 权限资源响应文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.rbac;

import top.egon.familyaibutler.uaa.facade.enums.PermissionResourceType;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @ClassName: PermissionResourceResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:00
 * @Description: 权限资源响应
 * @Version: 1.0
 */
public record PermissionResourceResponse(
        String resourceCode,
        String resourceName,
        PermissionResourceType resourceType,
        String resourceService,
        String pathPattern,
        String action,
        String status
) {
}
