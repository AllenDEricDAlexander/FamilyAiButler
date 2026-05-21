package top.egon.familyaibutler.family.application.query;

import top.egon.familyaibutler.family.domain.passwordview.model.enums.PasswordCategoryEnum;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.query
 * @ClassName: PasswordViewPageQuery
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 账号密码分页查询对象
 * @Version: 1.0
 */
public record PasswordViewPageQuery(
        Integer pageNum,
        Integer pageSize,
        String name,
        String password,
        String description,
        String accountNumber,
        String websit,
        Boolean likeStatus,
        PasswordCategoryEnum category
) {
}
