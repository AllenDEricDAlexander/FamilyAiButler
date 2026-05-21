package top.egon.familyaibutler.family.domain.gateway;

import top.egon.familyaibutler.family.domain.model.aggregate.PasswordView;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.gateway
 * @ClassName: PasswordViewGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 账号密码写模型网关
 * @Version: 1.0
 */
public interface PasswordViewGateway {

    /**
     * 按主键查找账号密码聚合。
     *
     * @param id 聚合主键
     * @return 账号密码聚合
     */
    Optional<PasswordView> find(Long id);

    /**
     * 保存账号密码聚合。
     *
     * @param aggregate 账号密码聚合
     * @return 保存后的聚合
     */
    PasswordView save(PasswordView aggregate);
}
