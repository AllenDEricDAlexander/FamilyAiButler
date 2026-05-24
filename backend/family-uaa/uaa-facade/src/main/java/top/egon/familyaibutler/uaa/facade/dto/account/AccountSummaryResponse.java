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

import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.account
 * @ClassName: AccountSummaryResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号摘要响应
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeAccountSummaryResponse", description = "认证授权账号摘要响应")
public record AccountSummaryResponse(
        /**账号ID。*/
        @DocField(description = "账号 ID", required = false, example = "account-001")
        String accountId,
        /**用户名。*/
        @DocField(description = "用户名", required = false, example = "mario")
        String username,
        /**邮箱。*/
        @DocField(description = "邮箱", required = false, example = "mario@example.com")
        String email,
        /**手机号。*/
        @DocField(description = "手机号", required = false, example = "13800138000")
        String phone,
        /**账号状态。*/
        @DocField(description = "账号状态", required = false, example = "ENABLED")
        String status,
        /**账号类型。*/
        @DocField(description = "账号类型", required = false, example = "PERSONAL")
        String accountType,
        /**认证版本号。*/
        @DocField(description = "认证版本号", required = false, example = "1")
        long authVersion,
        /**权益版本号。*/
        @DocField(description = "权益版本号", required = false, example = "1")
        long entitlementVersion,
        /**会话版本号。*/
        @DocField(description = "会话版本号", required = false, example = "1")
        long sessionVersion,
        /**风控版本号。*/
        @DocField(description = "风控版本号", required = false, example = "1")
        long riskVersion
) {
}
