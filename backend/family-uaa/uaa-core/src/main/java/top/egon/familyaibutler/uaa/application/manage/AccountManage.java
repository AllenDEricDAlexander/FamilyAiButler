/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage
 * @FileName: AccountManage.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号应用服务接口文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application.manage;

import top.egon.familyaibutler.uaa.application.command.account.RegisterAccountCommand;
import top.egon.familyaibutler.uaa.application.result.account.AccountResponse;
import top.egon.familyaibutler.uaa.domain.account.model.enums.AccountStatus;
import top.egon.familyaibutler.uaa.facade.dto.account.AccountSummaryResponse;
import top.egon.familyaibutler.uaa.facade.dto.account.ChangeAccountStatusRequest;
import top.egon.familyaibutler.uaa.facade.dto.account.DeleteAccountRequest;
import top.egon.familyaibutler.uaa.facade.dto.account.RegisterAccountRequest;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage
 * @ClassName: AccountManage
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号应用服务接口
 * @Version: 1.0
 */
public interface AccountManage {

    /**
     * 通过用户名注册账号。
     *
     * @param command 注册账号命令
     * @return 账号响应
     */
    AccountResponse registerByUsername(RegisterAccountCommand command);

    /**
     * 通过用户名注册账号。
     *
     * @param request 注册请求
     * @return 账号摘要
     */
    AccountSummaryResponse registerByUsername(RegisterAccountRequest request);

    /**
     * 通过邮箱注册账号。
     *
     * @param request 注册请求
     * @return 账号摘要
     */
    AccountSummaryResponse registerByEmail(RegisterAccountRequest request);

    /**
     * 通过手机号注册账号。
     *
     * @param request 注册请求
     * @return 账号摘要
     */
    AccountSummaryResponse registerByPhone(RegisterAccountRequest request);

    /**
     * 查询账号摘要。
     *
     * @param accountId 账号 ID
     * @return 账号摘要
     */
    AccountSummaryResponse findAccountSummary(String accountId);

    /**
     * 修改账号状态。
     *
     * @param accountId 账号 ID
     * @param status    账号状态
     * @return 账号响应
     */
    AccountResponse changeAccountStatus(String accountId, AccountStatus status);

    /**
     * 修改账号状态。
     *
     * @param request 状态修改请求
     * @return 账号摘要
     */
    AccountSummaryResponse changeAccountStatus(ChangeAccountStatusRequest request);

    /**
     * 申请注销账号。
     *
     * @param accountId 账号 ID
     * @return 账号响应
     */
    AccountResponse requestDeletion(String accountId);

    /**
     * 申请注销账号。
     *
     * @param request 注销请求
     * @return 账号摘要
     */
    AccountSummaryResponse requestAccountDeletion(DeleteAccountRequest request);

    /**
     * 确认注销账号。
     *
     * @param accountId        账号 ID
     * @param verificationCode 二次验证码
     * @return 账号响应
     */
    AccountResponse confirmDeletion(String accountId, String verificationCode);

    /**
     * 确认注销账号。
     *
     * @param request 注销请求
     * @return 账号摘要
     */
    AccountSummaryResponse confirmAccountDeletion(DeleteAccountRequest request);
}
