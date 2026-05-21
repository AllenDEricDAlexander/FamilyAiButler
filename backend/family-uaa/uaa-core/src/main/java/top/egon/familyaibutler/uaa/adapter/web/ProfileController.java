/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @FileName: ProfileController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: Profile Web 控制器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.uaa.application.manage.ProfileManage;
import top.egon.familyaibutler.uaa.facade.dto.profile.ProfileRequest;
import top.egon.familyaibutler.uaa.facade.dto.profile.ProfileResponse;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @ClassName: ProfileController
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Profile Web 控制器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/profile")
public class ProfileController {
    private final ProfileManage profileService;

    /**
     * 创建 Profile Web 控制器。
     *
     * @param profileService Profile 应用服务
     */
    public ProfileController(ProfileManage profileService) {
        this.profileService = profileService;
    }

    /**
     * 创建 Profile。
     *
     * @param request Profile 请求
     * @return Profile 响应
     */
    @PostMapping
    public Result<ProfileResponse> createProfile(@RequestBody @Valid ProfileRequest request) {
        return Result.success(profileService.createProfile(request));
    }

    /**
     * 修改 Profile。
     *
     * @param request Profile 请求
     * @return Profile 响应
     */
    @PutMapping
    public Result<ProfileResponse> updateProfile(@RequestBody @Valid ProfileRequest request) {
        return Result.success(profileService.updateProfile(request));
    }

    /**
     * 删除 Profile。
     *
     * @param profileId Profile ID
     * @return true 表示删除成功
     */
    @DeleteMapping("/{profileId}")
    public Result<Boolean> deleteProfile(@PathVariable String profileId) {
        return Result.success(profileService.deleteProfile(profileId));
    }

    /**
     * 查询账号下的 Profile 列表。
     *
     * @param accountId 账号 ID
     * @return Profile 列表
     */
    @GetMapping("/account/{accountId}")
    public Result<List<ProfileResponse>> listProfiles(@PathVariable String accountId) {
        return Result.success(profileService.listProfiles(accountId));
    }
}
