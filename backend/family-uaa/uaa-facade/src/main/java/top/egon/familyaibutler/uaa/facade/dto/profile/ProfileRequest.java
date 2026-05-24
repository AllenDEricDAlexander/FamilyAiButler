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
        /**ProfileID。*/
        @DocField(description = "Profile ID", required = false, example = "profile-001")
        String profileId,
        /**账号ID。*/
        @DocField(description = "账号 ID", required = false, example = "account-001")
        String accountId,
        /**昵称。*/
        @DocField(description = "昵称", required = false, example = "Mario")
        String nickname,
        /**头像地址。*/
        @DocField(description = "头像地址", required = false, example = "https://example.com/avatar.png")
        String avatar,
        /**语言。*/
        @DocField(description = "语言", required = false, example = "zh-CN")
        String language,
        /**地区。*/
        @DocField(description = "地区", required = false, example = "CN")
        String region,
        /**Profile类型。*/
        @DocField(description = "Profile 类型", required = false, example = "PERSONAL")
        String profileType
) {
}
