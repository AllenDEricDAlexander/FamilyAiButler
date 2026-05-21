/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.profile
 * @FileName: ProfileResponse.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: Profile 响应文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.profile;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.profile
 * @ClassName: ProfileResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Profile 响应
 * @Version: 1.0
 */
public record ProfileResponse(
        String profileId,
        String accountId,
        String nickname,
        String avatar,
        String language,
        String region,
        String profileType,
        boolean deleted
) {
}
