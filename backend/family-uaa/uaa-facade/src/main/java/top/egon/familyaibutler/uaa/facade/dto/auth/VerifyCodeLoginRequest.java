/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @FileName: VerifyCodeLoginRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 验证码登录请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.auth;

import jakarta.validation.constraints.NotBlank;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @ClassName: VerifyCodeLoginRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 验证码登录请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeVerifyCodeLoginRequest", description = "认证授权验证码登录请求")
public record VerifyCodeLoginRequest(
        @DocField(description = "账号标识，支持邮箱或手机号", example = "mario@example.com")
        @NotBlank String principal,
        @DocField(description = "验证码", example = "123456")
        @NotBlank String verifyCode,
        @DocField(description = "OAuth 客户端 ID", example = "family-web")
        String clientId,
        @DocField(description = "设备名称", example = "Mario iPhone")
        String deviceName,
        @DocField(description = "设备指纹", example = "device-fingerprint-001")
        String deviceFingerprint
) {
}
