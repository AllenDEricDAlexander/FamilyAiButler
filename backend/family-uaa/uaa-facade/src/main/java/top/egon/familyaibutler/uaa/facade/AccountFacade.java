/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade
 * @FileName: AccountFacade.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号 facade 契约文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade;

import top.egon.familyaibutler.uaa.facade.dto.account.AccountSummaryResponse;
import top.egon.familyaibutler.uaa.facade.dto.account.ChangeAccountStatusRequest;
import top.egon.familyaibutler.uaa.facade.dto.account.DeleteAccountRequest;
import top.egon.familyaibutler.uaa.facade.dto.account.RegisterAccountRequest;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade
 * @ClassName: AccountFacade
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号 facade 契约
 * @Version: 1.0
 */
public interface AccountFacade {

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
     * @param request 状态修改请求
     * @return 账号摘要
     */
    AccountSummaryResponse changeAccountStatus(ChangeAccountStatusRequest request);

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
     * @param request 注销请求
     * @return 账号摘要
     */
    AccountSummaryResponse confirmAccountDeletion(DeleteAccountRequest request);
}
