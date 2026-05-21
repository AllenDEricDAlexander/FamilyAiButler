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

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.account
 * @ClassName: RegisterAccountRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 注册账号请求
 * @Version: 1.0
 */
public record RegisterAccountRequest(
        String username,
        String email,
        String phone,
        @NotBlank String password
) {
}
