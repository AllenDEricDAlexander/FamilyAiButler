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
        /**
         * 账号 ID。
         */
        @DocField(description = "账号 ID", required = false, example = "account-001") String accountId,
        /**
         * 用户名。
         */
        @DocField(description = "用户名", required = false, example = "mario") String username,
        /**
         * 邮箱。
         */
        @DocField(description = "邮箱", required = false, example = "mario@example.com") String email,
        /**
         * 手机号。
         */
        @DocField(description = "手机号", required = false, example = "13800000000") String phone,
        /**
         * 账号状态。
         */
        @DocField(description = "账号状态", required = false, example = "ACTIVE") String status,
        /**
         * 账号类型。
         */
        @DocField(description = "账号类型", required = false, example = "NORMAL") String accountType,
        /**
         * 认证版本。
         */
        @DocField(description = "认证版本", required = false, example = "1") long authVersion,
        /**
         * 权益版本。
         */
        @DocField(description = "权益版本", required = false, example = "1") long entitlementVersion,
        /**
         * 会话版本。
         */
        @DocField(description = "会话版本", required = false, example = "1") long sessionVersion,
        /**
         * 风控版本。
         */
        @DocField(description = "风控版本", required = false, example = "1") long riskVersion
) {
}
