/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.account
 * @FileName: RegisterAccountRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 注册账号请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.account;

import jakarta.validation.constraints.NotBlank;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.account
 * @ClassName: RegisterAccountRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 注册账号请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeRegisterAccountRequest", description = "认证授权账号注册请求")
public record RegisterAccountRequest(
        @DocField(description = "用户名", example = "mario")
        String username,
        @DocField(description = "邮箱", example = "mario@example.com")
        String email,
        @DocField(description = "手机号", example = "13800138000")
        String phone,
        @DocField(description = "登录密码", example = "Passw0rd!")
        @NotBlank String password
) {
}
