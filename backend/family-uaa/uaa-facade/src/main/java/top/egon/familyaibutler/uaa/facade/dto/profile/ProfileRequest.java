/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.profile
 * @FileName: ProfileRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: Profile 请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.profile;

import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.profile
 * @ClassName: ProfileRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Profile 请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeProfileRequest", description = "认证授权用户档案请求")
public record ProfileRequest(
        @DocField(description = "Profile ID", example = "profile-001")
        String profileId,
        @DocField(description = "账号 ID", example = "account-001")
        String accountId,
        @DocField(description = "昵称", example = "Mario")
        String nickname,
        @DocField(description = "头像地址", example = "https://example.com/avatar.png")
        String avatar,
        @DocField(description = "语言", example = "zh-CN")
        String language,
        @DocField(description = "地区", example = "CN")
        String region,
        @DocField(description = "Profile 类型", example = "PERSONAL")
        String profileType
) {
}
