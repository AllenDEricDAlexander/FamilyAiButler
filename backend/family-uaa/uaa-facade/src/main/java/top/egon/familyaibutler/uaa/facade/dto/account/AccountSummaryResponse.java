/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.account
 * @FileName: AccountSummaryResponse.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号摘要响应文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.account;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.account
 * @ClassName: AccountSummaryResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号摘要响应
 * @Version: 1.0
 */
public record AccountSummaryResponse(
        String accountId,
        String username,
        String email,
        String phone,
        String status,
        String accountType,
        long authVersion,
        long entitlementVersion,
        long sessionVersion,
        long riskVersion
) {
}
