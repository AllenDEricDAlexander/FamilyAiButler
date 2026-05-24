package top.egon.familyaibutler.family.application.command;

import top.egon.familyaibutler.family.domain.passwordview.model.enums.PasswordCategoryEnum;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.command
 * @ClassName: UpdatePasswordViewCommand
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 修改账号密码命令对象
 * @Version: 1.0
 */
@DocModel(name = "FamilyUpdatePasswordViewCommand", description = "修改账号密码命令对象")
public record UpdatePasswordViewCommand(
        /**
         * 账号密码 ID。
         */
        @DocField(description = "账号密码 ID", required = true, example = "1")
        Long id,
        /**
         * 账号密码名称。
         */
        @DocField(description = "账号密码名称", required = false, example = "GitHub")
        String name,
        /**
         * 密码明文。
         */
        @DocField(description = "密码明文", required = true, example = "P@ssw0rd1234")
        String password,
        /**
         * 账号密码描述。
         */
        @DocField(description = "账号密码描述", required = false, example = "GitHub 账号")
        String description,
        /**
         * 登录账号。
         */
        @DocField(description = "登录账号", required = true, example = "mario@example.com")
        String accountNumber,
        /**
         * 站点地址。
         */
        @DocField(description = "站点地址", required = true, example = "https://github.com")
        String websit,
        /**
         * 收藏状态。
         */
        @DocField(description = "收藏状态", required = false, example = "true")
        Boolean likeStatus,
        /**
         * 密码分类。
         */
        @DocField(description = "密码分类", required = false, example = "WORK")
        PasswordCategoryEnum category
) {
}
