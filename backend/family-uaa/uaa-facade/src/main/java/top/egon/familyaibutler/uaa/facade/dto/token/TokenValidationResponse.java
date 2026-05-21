/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @FileName: TokenValidationResponse.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: Token 校验响应文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.token;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @ClassName: TokenValidationResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Token 校验响应
 * @Version: 1.0
 */
public record TokenValidationResponse(
        boolean valid,
        String accountId,
        String profileId,
        String sessionId,
        String deviceId,
        long authVersion,
        long entitlementVersion,
        String reason
) {
}
