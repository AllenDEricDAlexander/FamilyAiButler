/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @FileName: PasswordLoginRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 密码登录请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @ClassName: PasswordLoginRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 密码登录请求
 * @Version: 1.0
 */
public record PasswordLoginRequest(
        @NotBlank String principal,
        @NotBlank String password,
        String clientId,
        String clientSecret,
        String deviceName,
        String deviceFingerprint
) {
}
