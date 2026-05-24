/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.account
 * @FileName: DeleteAccountRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 注销账号请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.account;

import jakarta.validation.constraints.NotBlank;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.account
 * @ClassName: DeleteAccountRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 注销账号请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeDeleteAccountRequest", description = "认证授权账号注销请求")
public record DeleteAccountRequest(
        @DocField(description = "账号 ID", example = "account-001")
        @NotBlank String accountId,
        @DocField(description = "注销验证码", example = "123456")
        String verificationCode
) {
}
