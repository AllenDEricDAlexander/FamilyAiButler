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

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.command.account
 * @ClassName: RegisterAccountCommand
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 注册账号命令
 * @Version: 1.0
 */
public record RegisterAccountCommand(
        String username,
        String email,
        String phone,
        String password
) {
}
