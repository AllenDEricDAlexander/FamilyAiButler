/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @FileName: ProfileServiceI.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: Profile 应用服务接口文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application;

import top.egon.familyaibutler.uaa.facade.dto.profile.ProfileRequest;
import top.egon.familyaibutler.uaa.facade.dto.profile.ProfileResponse;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @ClassName: ProfileServiceI
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: Profile 应用服务接口
 * @Version: 1.0
 */
public interface ProfileServiceI {

    /**
     * 创建 Profile。
     *
     * @param request Profile 请求
     * @return Profile 响应
     */
    ProfileResponse createProfile(ProfileRequest request);

    /**
     * 修改 Profile。
     *
     * @param request Profile 请求
     * @return Profile 响应
     */
    ProfileResponse updateProfile(ProfileRequest request);

    /**
     * 删除 Profile。
     *
     * @param profileId Profile ID
     * @return true 表示删除成功
     */
    boolean deleteProfile(String profileId);

    /**
     * 查询账号下的 Profile 列表。
     *
     * @param accountId 账号 ID
     * @return Profile 列表
     */
    List<ProfileResponse> listProfiles(String accountId);
}
