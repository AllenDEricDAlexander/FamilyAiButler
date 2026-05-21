package top.egon.familyaibutler.family.infrastructure.persistence.mp.converter;

import org.springframework.stereotype.Component;
import top.egon.familyaibutler.family.application.dto.CreatePasswordViewCommand;
import top.egon.familyaibutler.family.application.dto.UpdatePasswordViewCommand;
import top.egon.familyaibutler.family.application.dto.PasswordViewPageQuery;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.dataobject.PasswordViewPO;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.persistence.mp.converter
 * @ClassName: PasswordViewMpConverter
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 账号密码命令与 MyBatis Plus 数据对象转换器
 * @Version: 1.0
 */
@Component
public class PasswordViewMpConverter {

    /**
     * 创建命令转换为数据对象。
     *
     * @param command 创建命令
     * @return 数据对象
     */
    public PasswordViewPO toDataObject(CreatePasswordViewCommand command) {
        return PasswordViewPO.builder()
                .name(command.name())
                .password(command.password())
                .description(command.description())
                .accountNumber(command.accountNumber())
                .websit(command.websit())
                .likeStatus(Boolean.TRUE.equals(command.likeStatus()))
                .category(command.category())
                .build();
    }

    /**
     * 查询对象转换为数据对象。
     *
     * @param query 查询对象
     * @return 数据对象
     */
    public PasswordViewPO toDataObject(PasswordViewPageQuery query) {
        return PasswordViewPO.builder()
                .name(query.name())
                .password(query.password())
                .description(query.description())
                .accountNumber(query.accountNumber())
                .websit(query.websit())
                .likeStatus(Boolean.TRUE.equals(query.likeStatus()))
                .category(query.category())
                .build();
    }

    /**
     * 将修改命令应用到已有数据对象。
     *
     * @param command 修改命令
     * @param target  已有数据对象
     */
    public void apply(UpdatePasswordViewCommand command, PasswordViewPO target) {
        target.setName(command.name())
                .setPassword(command.password())
                .setDescription(command.description())
                .setAccountNumber(command.accountNumber())
                .setWebsit(command.websit())
                .setLikeStatus(Boolean.TRUE.equals(command.likeStatus()))
                .setCategory(command.category());
    }
}
