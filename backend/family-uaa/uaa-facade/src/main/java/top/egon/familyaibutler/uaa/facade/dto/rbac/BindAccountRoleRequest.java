/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @FileName: BindAccountRoleRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:00
 * @Description: 绑定账号和角色请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.rbac;

import jakarta.validation.constraints.NotBlank;
import top.egon.openapi.console.annotation.DocField;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.rbac
 * @ClassName: BindAccountRoleRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:00
 * @Description: 绑定账号和角色请求
 * @Version: 1.0
 */
public record BindAccountRoleRequest(
        @DocField(description = "账号 ID", example = "account-001")
        @NotBlank String accountId,
        @DocField(description = "角色编码", example = "FAMILY_ADMIN")
        @NotBlank String roleCode
) {
}
