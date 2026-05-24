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
        /**权限资源编码。*/
        @DocField(description = "权限资源编码", required = false, example = "FAMILY_PASSWORD_READ")
        String resourceCode,
        /**权限资源名称。*/
        @DocField(description = "权限资源名称", required = false, example = "查看家庭密码")
        String resourceName,
        /**权限资源类型。*/
        @DocField(description = "权限资源类型", required = false, example = "API")
        PermissionResourceType resourceType,
        /**资源所属服务。*/
        @DocField(description = "资源所属服务", required = false, example = "family-core")
        String resourceService,
        /**资源路径匹配规则。*/
        @DocField(description = "资源路径匹配规则", required = false, example = "/password-view/**")
        String pathPattern,
        /**访问动作。*/
        @DocField(description = "访问动作", required = false, example = "READ")
        String action,
        /**权限资源状态。*/
        @DocField(description = "权限资源状态", required = false, example = "ENABLED")
        String status
) {
}
