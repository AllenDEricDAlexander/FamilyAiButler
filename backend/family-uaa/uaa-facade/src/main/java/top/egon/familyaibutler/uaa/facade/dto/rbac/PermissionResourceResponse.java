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
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @ClassName: PermissionResourceResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:00
 * @Description: 权限资源响应
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeRbacPermissionResourceResponse", description = "认证授权权限资源响应")
public record PermissionResourceResponse(
        @DocField(description = "权限资源编码", example = "FAMILY_PASSWORD_READ")
        String resourceCode,
        @DocField(description = "权限资源名称", example = "查看家庭密码")
        String resourceName,
        @DocField(description = "权限资源类型", example = "API")
        PermissionResourceType resourceType,
        @DocField(description = "资源所属服务", example = "family-core")
        String resourceService,
        @DocField(description = "资源路径匹配规则", example = "/password-view/**")
        String pathPattern,
        @DocField(description = "访问动作", example = "READ")
        String action,
        @DocField(description = "权限资源状态", example = "ENABLED")
        String status
) {
}
