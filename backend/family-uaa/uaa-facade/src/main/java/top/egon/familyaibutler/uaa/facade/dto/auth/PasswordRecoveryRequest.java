/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @FileName: PasswordRecoveryRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:00
 * @Description: 找回密码请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.auth;

import jakarta.validation.constraints.NotBlank;
import top.egon.openapi.console.annotation.DocField;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @ClassName: PasswordRecoveryRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:00
 * @Description: 找回密码请求
 * @Version: 1.0
 */
public record PasswordRecoveryRequest(
        @DocField(description = "账号标识，支持用户名、邮箱或手机号", example = "mario@example.com")
        @NotBlank String principal,
        @DocField(description = "找回密码通道", example = "EMAIL")
        String channel
) {
}
