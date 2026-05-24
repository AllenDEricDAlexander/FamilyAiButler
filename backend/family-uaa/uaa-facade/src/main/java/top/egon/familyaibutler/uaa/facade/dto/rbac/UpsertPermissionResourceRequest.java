/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @FileName: UpsertPermissionResourceRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:00
 * @Description: 新增或更新权限资源请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.rbac;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import top.egon.familyaibutler.uaa.facade.enums.PermissionResourceType;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @ClassName: UpsertPermissionResourceRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:00
 * @Description: 新增或更新权限资源请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeRbacUpsertPermissionResourceRequest", description = "认证授权新增或更新权限资源请求")
public record UpsertPermissionResourceRequest(
        @DocField(description = "权限资源编码", example = "FAMILY_PASSWORD_READ")
        @NotBlank String resourceCode,
        @DocField(description = "权限资源名称", example = "查看家庭密码")
        @NotBlank String resourceName,
        @DocField(description = "权限资源类型", example = "API")
        @NotNull PermissionResourceType resourceType,
        @DocField(description = "资源所属服务", example = "family-core")
        String resourceService,
        @DocField(description = "资源路径匹配规则", example = "/password-view/**")
        String pathPattern,
        @DocField(description = "访问动作", example = "READ")
        String action
) {
}
