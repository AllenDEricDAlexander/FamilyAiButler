/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage.impl
 * @FileName: ProfileManageImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: Profile 应用服务实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application.manage.impl;

import org.springframework.stereotype.Service;
import top.egon.familyaibutler.uaa.application.manage.ProfileManage;
import top.egon.familyaibutler.uaa.domain.account.gateway.AccountGateway;
import top.egon.familyaibutler.uaa.domain.account.gateway.ProfileGateway;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Profile;
import top.egon.familyaibutler.uaa.domain.account.model.enums.ProfileType;
import top.egon.familyaibutler.uaa.facade.dto.profile.ProfileRequest;
import top.egon.familyaibutler.uaa.facade.dto.profile.ProfileResponse;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage.impl
 * @ClassName: ProfileManageImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: Profile 应用服务实现
 * @Version: 1.0
 */
@Service
public class ProfileManageImpl implements ProfileManage {
    /**
     * Account 网关。
     */
    private final AccountGateway accountGateway;
    /**
     * Profile 网关。
     */
    private final ProfileGateway profileGateway;

    /**
     * 创建 Profile 应用服务实现。
     *
     * @param accountGateway 账号网关
     * @param profileGateway Profile 网关
     */
    public ProfileManageImpl(AccountGateway accountGateway, ProfileGateway profileGateway) {
        this.accountGateway = accountGateway;
        this.profileGateway = profileGateway;
    }

    /**
     * 创建 Profile。
     *
     * @param request Profile 请求
     * @return Profile 响应
     */
    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        accountGateway.findByAccountId(request.accountId()).orElseThrow(() -> new IllegalArgumentException("账号不存在"));
        ProfileType profileType = request.profileType() == null ? ProfileType.MAIN : ProfileType.valueOf(request.profileType());
        Profile profile = Profile.create(request.accountId(), profileType, request.nickname());
        profile.updateBasicInfo(request.nickname(), request.avatar(), request.language(), request.region());
        return toResponse(profileGateway.save(profile));
    }

    /**
     * 修改 Profile。
     *
     * @param request Profile 请求
     * @return Profile 响应
     */
    @Override
    public ProfileResponse updateProfile(ProfileRequest request) {
        Profile profile = profileGateway.findByProfileId(request.profileId())
                .orElseThrow(() -> new IllegalArgumentException("Profile 不存在"));
        profile.updateBasicInfo(request.nickname(), request.avatar(), request.language(), request.region());
        return toResponse(profileGateway.save(profile));
    }

    /**
     * 删除 Profile。
     *
     * @param profileId Profile ID
     * @return true 表示删除成功
     */
    @Override
    public boolean deleteProfile(String profileId) {
        return profileGateway.findByProfileId(profileId)
                .map(profile -> {
                    profile.delete();
                    profileGateway.save(profile);
                    return true;
                })
                .orElse(false);
    }

    /**
     * 切换当前 Profile。
     *
     * @param profileId Profile ID
     * @return Profile 响应
     */
    @Override
    public ProfileResponse switchProfile(String profileId) {
        return profileGateway.findByProfileId(profileId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Profile 不存在"));
    }

    /**
     * 查询账号下的 Profile 列表。
     *
     * @param accountId 账号 ID
     * @return Profile 列表
     */
    @Override
    public List<ProfileResponse> listProfiles(String accountId) {
        return profileGateway.findByAccountId(accountId).stream().map(this::toResponse).toList();
    }

    /**
     * 转换 Profile 响应。
     *
     * @param profile Profile 聚合
     * @return Profile 响应
     */
    private ProfileResponse toResponse(Profile profile) {
        return new ProfileResponse(profile.getProfileId(), profile.getAccountId(), profile.getNickname(), profile.getAvatar(),
                profile.getLanguage(), profile.getRegion(), profile.getProfileType().name(), profile.isDeleted());
    }
}
