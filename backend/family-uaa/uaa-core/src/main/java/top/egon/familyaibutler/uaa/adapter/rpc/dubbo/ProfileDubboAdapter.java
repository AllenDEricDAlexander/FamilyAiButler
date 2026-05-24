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
import top.egon.openapi.console.annotation.DocBody;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocParamIn;
import top.egon.openapi.console.annotation.DocParameter;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;
import top.egon.openapi.console.annotation.DocTypeReference;

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
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-profile-dubbo",
        serviceName = "Profile Dubbo 服务", serviceDescription = "账号 Profile 创建、修改、删除、切换和查询 RPC 能力", protocol = DocProtocol.DUBBO_TRIPLE)
public class ProfileDubboAdapter implements ProfileFacade {
    private final ProfileManage profileService;

    /**
     * 创建 Profile。
     *
     * @param request Profile 请求
     * @return Profile 响应
     */
    @Override
    @DocOperation(summary = "创建 Profile", description = "为账号创建 Profile",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = ProfileRequest.class))),
            response = @DocResponse(description = "创建成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = ProfileResponse.class)))
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
    @DocOperation(summary = "修改 Profile", description = "修改账号 Profile",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = ProfileRequest.class))),
            response = @DocResponse(description = "修改成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = ProfileResponse.class)))
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
    @DocOperation(summary = "删除 Profile", description = "按 Profile ID 删除 Profile",
            request = @DocRequest(params = {
                    @DocParameter(name = "profileId", in = DocParamIn.AUTO, description = "Profile ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.STRING), example = "profile-001")
            }),
            response = @DocResponse(description = "删除成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN)))
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
    @DocOperation(summary = "切换当前 Profile", description = "按 Profile ID 切换当前 Profile",
            request = @DocRequest(params = {
                    @DocParameter(name = "profileId", in = DocParamIn.AUTO, description = "Profile ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.STRING), example = "profile-001")
            }),
            response = @DocResponse(description = "切换成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = ProfileResponse.class)))
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
    @DocOperation(summary = "查询账号下的 Profile 列表", description = "按账号 ID 查询 Profile 列表",
            request = @DocRequest(params = {
                    @DocParameter(name = "accountId", in = DocParamIn.AUTO, description = "账号 ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.STRING), example = "account-001")
            }),
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.GENERIC, ref = ProfileListDataType.class)))
    public List<ProfileResponse> listProfiles(String accountId) {
        return profileService.listProfiles(accountId);
    }

    public static final class ProfileListDataType extends DocTypeReference<List<ProfileResponse>> {
    }
}
