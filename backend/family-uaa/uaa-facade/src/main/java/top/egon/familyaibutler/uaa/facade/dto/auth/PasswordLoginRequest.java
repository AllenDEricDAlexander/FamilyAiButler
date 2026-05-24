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
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @ClassName: PasswordLoginRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 密码登录请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadePasswordLoginRequest", description = "认证授权密码登录请求")
public record PasswordLoginRequest(
        /**账号标识，支持用户名、邮箱或手机号。*/
        @DocField(description = "账号标识，支持用户名、邮箱或手机号", required = true, example = "mario@example.com")
        @NotBlank String principal,
        /**登录密码。*/
        @DocField(description = "登录密码", required = true, example = "Passw0rd!")
        @NotBlank String password,
        /**OAuth客户端ID。*/
        @DocField(description = "OAuth 客户端 ID", required = false, example = "family-web")
        String clientId,
        /**OAuth客户端密钥。*/
        @DocField(description = "OAuth 客户端密钥", required = false, example = "family-secret")
        String clientSecret,
        /**设备名称。*/
        @DocField(description = "设备名称", required = false, example = "Mario iPhone")
        String deviceName,
        /**设备指纹。*/
        @DocField(description = "设备指纹", required = false, example = "device-fingerprint-001")
        String deviceFingerprint
) {
}
