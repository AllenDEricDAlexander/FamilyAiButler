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
import top.egon.openapi.console.annotation.DocField;

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
        @DocField(description = "账号标识，支持用户名、邮箱或手机号", example = "mario@example.com")
        @NotBlank String principal,
        @DocField(description = "登录密码", example = "Passw0rd!")
        @NotBlank String password,
        @DocField(description = "OAuth 客户端 ID", example = "family-web")
        String clientId,
        @DocField(description = "OAuth 客户端密钥", example = "family-secret")
        String clientSecret,
        @DocField(description = "设备名称", example = "Mario iPhone")
        String deviceName,
        @DocField(description = "设备指纹", example = "device-fingerprint-001")
        String deviceFingerprint
) {
}
