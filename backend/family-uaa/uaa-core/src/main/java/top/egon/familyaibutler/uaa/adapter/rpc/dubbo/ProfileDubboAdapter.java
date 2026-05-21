/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @FileName: ProfileDubboAdapter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:20
 * @Description: Profile facade 适配器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.rpc.dubbo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.uaa.application.manage.ProfileManage;
import top.egon.familyaibutler.uaa.facade.ProfileFacade;
import top.egon.familyaibutler.uaa.facade.dto.profile.ProfileRequest;
import top.egon.familyaibutler.uaa.facade.dto.profile.ProfileResponse;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @ClassName: ProfileDubboAdapter
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:20
 * @Description: Profile facade 适配器
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
public class ProfileDubboAdapter implements ProfileFacade {
    private final ProfileManage profileService;

    /**
     * 创建 Profile。
     *
     * @param request Profile 请求
     * @return Profile 响应
     */
    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        return profileService.createProfile(request);
    }

    /**
     * 修改 Profile。
     *
     * @param request Profile 请求
     * @return Profile 响应
     */
    @Override
    public ProfileResponse updateProfile(ProfileRequest request) {
        return profileService.updateProfile(request);
    }

    /**
     * 删除 Profile。
     *
     * @param profileId Profile ID
     * @return true 表示删除成功
     */
    @Override
    public boolean deleteProfile(String profileId) {
        return profileService.deleteProfile(profileId);
    }

    /**
     * 切换当前 Profile。
     *
     * @param profileId Profile ID
     * @return Profile 响应
     */
    @Override
    public ProfileResponse switchProfile(String profileId) {
        return profileService.switchProfile(profileId);
    }

    /**
     * 查询账号下的 Profile 列表。
     *
     * @param accountId 账号 ID
     * @return Profile 列表
     */
    @Override
    public List<ProfileResponse> listProfiles(String accountId) {
        return profileService.listProfiles(accountId);
    }
}
