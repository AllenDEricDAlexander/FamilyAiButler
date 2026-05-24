/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.account
 * @FileName: ChangeAccountStatusRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 修改账号状态请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.account;

import jakarta.validation.constraints.NotBlank;
import top.egon.openapi.console.annotation.DocField;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.account
 * @ClassName: ChangeAccountStatusRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 修改账号状态请求
 * @Version: 1.0
 */
public record ChangeAccountStatusRequest(
        @DocField(description = "账号 ID", example = "account-001")
        @NotBlank String accountId,
        @DocField(description = "账号状态", example = "ENABLED")
        @NotBlank String status
) {
}
