/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.command.account
 * @FileName: RegisterAccountCommand.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 注册账号命令文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application.command.account;

import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.command.account
 * @ClassName: RegisterAccountCommand
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 注册账号命令
 * @Version: 1.0
 */
@DocModel(name = "UaaCoreRegisterAccountCommand", description = "注册账号命令")
public record RegisterAccountCommand(
        /**
         * 用户名。
         */
        @DocField(description = "用户名", required = false, example = "mario")
        String username,
        /**
         * 邮箱。
         */
        @DocField(description = "邮箱", required = false, example = "mario@example.com")
        String email,
        /**
         * 手机号。
         */
        @DocField(description = "手机号", required = false, example = "13800138000")
        String phone,
        /**
         * 登录密码。
         */
        @DocField(description = "登录密码", required = true, example = "Passw0rd!")
        String password
) {
}
