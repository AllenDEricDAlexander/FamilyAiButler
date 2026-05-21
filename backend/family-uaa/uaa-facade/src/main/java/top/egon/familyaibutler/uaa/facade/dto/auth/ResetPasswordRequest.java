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

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @ClassName: ResetPasswordRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:00
 * @Description: 重置密码请求
 * @Version: 1.0
 */
public record ResetPasswordRequest(
        @NotBlank String principal,
        @NotBlank String verificationCode,
        @NotBlank String newPassword
) {
}
