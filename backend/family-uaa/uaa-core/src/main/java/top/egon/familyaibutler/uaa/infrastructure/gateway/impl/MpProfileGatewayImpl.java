/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @FileName: MpProfileGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:35
 * @Description: MyBatis Plus Profile 网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import top.egon.familyaibutler.uaa.domain.account.gateway.ProfileGateway;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Profile;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.converter.UaaMpConverter;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.ProfilePO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper.ProfileMapper;

import java.util.List;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @ClassName: MpProfileGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:35
 * @Description: MyBatis Plus Profile 网关实现
 * @Version: 1.0
 */
@Repository
public class MpProfileGatewayImpl implements ProfileGateway {
    private final ProfileMapper profileMapper;
    private final UaaMpConverter uaaMpConverter;

    /**
     * 创建 MyBatis Plus Profile 网关实现。
     *
     * @param profileMapper  Profile Mapper
     * @param uaaMpConverter UAA 转换器
     */
    public MpProfileGatewayImpl(ProfileMapper profileMapper, UaaMpConverter uaaMpConverter) {
        this.profileMapper = profileMapper;
        this.uaaMpConverter = uaaMpConverter;
    }

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
        ProfilePO profilePO = uaaMpConverter.toProfilePO(profile);
        if (profileMapper.selectById(profile.getProfileId()) == null) {
            profileMapper.insert(profilePO);
        } else {
            profileMapper.updateById(profilePO);
        }
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
        return Optional.ofNullable(profileMapper.selectById(profileId)).map(uaaMpConverter::toProfile);
    }

    /**
     * 按账号 ID 查询 Profile 列表。
     *
     * @param accountId 账号 ID
     * @return Profile 列表
     */
    @Override
    public List<Profile> findByAccountId(String accountId) {
        LambdaQueryWrapper<ProfilePO> wrapper = new LambdaQueryWrapper<ProfilePO>().eq(ProfilePO::getAccountId, accountId);
        return profileMapper.selectList(wrapper).stream().map(uaaMpConverter::toProfile).toList();
    }
}
