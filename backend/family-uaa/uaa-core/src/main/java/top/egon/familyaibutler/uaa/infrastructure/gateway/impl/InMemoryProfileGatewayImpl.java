/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @FileName: InMemoryProfileGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: 内存 Profile 网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gateway.impl;

import top.egon.familyaibutler.uaa.domain.account.gateway.ProfileGateway;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Profile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @ClassName: InMemoryProfileGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: 内存 Profile 网关实现
 * @Version: 1.0
 */
public class InMemoryProfileGatewayImpl implements ProfileGateway {
    private final Map<String, Profile> profiles = new ConcurrentHashMap<>();

    /**
     * 批量保存 Profile。
     *
     * @param profiles Profile 列表
     * @return 保存后的 Profile 列表
     */
    @Override
    public List<Profile> saveAll(List<Profile> profiles) {
        profiles.forEach(this::save);
        return profiles;
    }

    /**
     * 保存单个 Profile。
     *
     * @param profile Profile 聚合
     * @return 保存后的 Profile
     */
    @Override
    public Profile save(Profile profile) {
        profiles.put(profile.getProfileId(), profile);
        return profile;
    }

    /**
     * 按 Profile ID 查询 Profile。
     *
     * @param profileId Profile ID
     * @return Profile 聚合
     */
    @Override
    public Optional<Profile> findByProfileId(String profileId) {
        return Optional.ofNullable(profiles.get(profileId));
    }

    /**
     * 按账号 ID 查询 Profile 列表。
     *
     * @param accountId 账号 ID
     * @return Profile 列表
     */
    @Override
    public List<Profile> findByAccountId(String accountId) {
        return profiles.values().stream()
                .filter(profile -> profile.getAccountId().equals(accountId))
                .toList();
    }
}
