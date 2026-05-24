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
import top.egon.openapi.console.annotation.DocBody;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocParam;
import top.egon.openapi.console.annotation.DocParamIn;
import top.egon.openapi.console.annotation.DocParameter;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;
import top.egon.openapi.console.annotation.DocTypeReference;
import top.egon.openapi.console.annotation.DocWrapper;

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
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-profile",
        serviceName = "Profile 服务", serviceDescription = "账号 Profile 创建、修改、删除和查询能力", protocol = DocProtocol.HTTP)
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
    @DocOperation(summary = "创建 Profile", description = "为账号创建 Profile",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = ProfileRequest.class))),
            response = @DocResponse(description = "创建成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = ProfileResponse.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
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
    @DocOperation(summary = "修改 Profile", description = "修改账号 Profile",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = ProfileRequest.class))),
            response = @DocResponse(description = "修改成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = ProfileResponse.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
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
    @DocOperation(summary = "删除 Profile", description = "按 Profile ID 删除 Profile",
            request = @DocRequest(params = {
                    @DocParameter(name = "profileId", in = DocParamIn.PATH, description = "Profile ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.STRING), example = "profile-001")
            }),
            response = @DocResponse(description = "删除成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<Boolean> deleteProfile(@PathVariable @DocParam(description = "Profile ID", required = true) String profileId) {
        return Result.success(profileService.deleteProfile(profileId));
    }

    /**
     * 查询账号下的 Profile 列表。
     *
     * @param accountId 账号 ID
     * @return Profile 列表
     */
    @GetMapping("/account/{accountId}")
    @DocOperation(summary = "查询账号下的 Profile 列表", description = "按账号 ID 查询 Profile 列表",
            request = @DocRequest(params = {
                    @DocParameter(name = "accountId", in = DocParamIn.PATH, description = "账号 ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.STRING), example = "account-001")
            }),
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.GENERIC, ref = ProfileListDataType.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<List<ProfileResponse>> listProfiles(@PathVariable @DocParam(description = "账号 ID", required = true) String accountId) {
        return Result.success(profileService.listProfiles(accountId));
    }

    public static final class ProfileListDataType extends DocTypeReference<List<ProfileResponse>> {
    }
}
