/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.result.account
 * @FileName: AccountResponse.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号响应文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application.result.account;

import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.result.account
 * @ClassName: AccountResponse
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号响应
 * @Version: 1.0
 */
@DocModel(name = "UaaCoreAccountResponse", description = "认证授权账号详情响应")
public record AccountResponse(
        @DocField(description = "账号 ID", example = "account-001") String accountId,
        @DocField(description = "用户名", example = "mario") String username,
        @DocField(description = "邮箱", example = "mario@example.com") String email,
        @DocField(description = "手机号", example = "13800000000") String phone,
        @DocField(description = "账号状态", example = "ACTIVE") String status,
        @DocField(description = "账号类型", example = "NORMAL") String accountType,
        @DocField(description = "认证版本", example = "1") long authVersion,
        @DocField(description = "权益版本", example = "1") long entitlementVersion,
        @DocField(description = "会话版本", example = "1") long sessionVersion,
        @DocField(description = "风控版本", example = "1") long riskVersion
) {
}
