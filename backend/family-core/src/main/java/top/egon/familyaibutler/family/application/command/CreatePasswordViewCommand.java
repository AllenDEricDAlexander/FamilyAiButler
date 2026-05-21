package top.egon.familyaibutler.family.application.command;

import top.egon.familyaibutler.family.domain.passwordview.model.enums.PasswordCategoryEnum;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.command
 * @ClassName: CreatePasswordViewCommand
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 创建账号密码命令对象
 * @Version: 1.0
 */
public record CreatePasswordViewCommand(
        String name,
        String password,
        String description,
        String accountNumber,
        String websit,
        Boolean likeStatus,
        PasswordCategoryEnum category
) {
}
