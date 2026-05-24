/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @FileName: ResetPasswordRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:00
 * @Description: 重置密码请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.auth;

import jakarta.validation.constraints.NotBlank;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @ClassName: ResetPasswordRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:00
 * @Description: 重置密码请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeResetPasswordRequest", description = "认证授权重置密码请求")
public record ResetPasswordRequest(
        /**账号标识，支持用户名、邮箱或手机号。*/
        @DocField(description = "账号标识，支持用户名、邮箱或手机号", required = true, example = "mario@example.com")
        @NotBlank String principal,
        /**验证码。*/
        @DocField(description = "验证码", required = true, example = "123456")
        @NotBlank String verificationCode,
        /**新密码。*/
        @DocField(description = "新密码", required = true, example = "NewPassw0rd!")
        @NotBlank String newPassword
) {
}
