package top.egon.familyaibutler.family.infrastructure.persistence.mp.converter;

import org.springframework.stereotype.Component;
import top.egon.familyaibutler.family.domain.passwordview.model.aggregate.PasswordView;
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
     * 领域模型转换为数据对象。
     *
     * @param passwordView 账号密码领域模型
     * @return 数据对象
     */
    public PasswordViewPO toDataObject(PasswordView passwordView) {
        return PasswordViewPO.builder()
                .id(passwordView.getId())
                .businessId(passwordView.getBusinessId())
                .name(passwordView.getName())
                .password(passwordView.getPassword())
                .description(passwordView.getDescription())
                .accountNumber(passwordView.getAccountNumber())
                .websit(passwordView.getWebsit())
                .likeStatus(Boolean.TRUE.equals(passwordView.getLikeStatus()))
                .category(passwordView.getCategory())
                .lastViewTime(passwordView.getLastViewTime())
                .build();
    }

    /**
     * 数据对象转换为领域模型。
     *
     * @param dataObject 数据对象
     * @return 账号密码领域模型
     */
    public PasswordView toDomain(PasswordViewPO dataObject) {
        PasswordView passwordView = new PasswordView();
        passwordView.setId(dataObject.getId());
        passwordView.setBusinessId(dataObject.getBusinessId());
        passwordView.setName(dataObject.getName());
        passwordView.setPassword(dataObject.getPassword());
        passwordView.setDescription(dataObject.getDescription());
        passwordView.setAccountNumber(dataObject.getAccountNumber());
        passwordView.setWebsit(dataObject.getWebsit());
        passwordView.setLikeStatus(dataObject.getLikeStatus());
        passwordView.setCategory(dataObject.getCategory());
        passwordView.setLastViewTime(dataObject.getLastViewTime());
        return passwordView;
    }

    /**
     * 将领域模型应用到已有数据对象。
     *
     * @param passwordView 账号密码领域模型
     * @param target       已有数据对象
     */
    public void apply(PasswordView passwordView, PasswordViewPO target) {
        target.setName(passwordView.getName())
                .setPassword(passwordView.getPassword())
                .setDescription(passwordView.getDescription())
                .setAccountNumber(passwordView.getAccountNumber())
                .setWebsit(passwordView.getWebsit())
                .setLikeStatus(Boolean.TRUE.equals(passwordView.getLikeStatus()))
                .setCategory(passwordView.getCategory());
    }
}
