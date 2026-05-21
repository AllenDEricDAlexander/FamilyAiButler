/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @FileName: AccountDubboAdapter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:20
 * @Description: 账号 facade 适配器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.rpc.dubbo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.uaa.application.manage.AccountManage;
import top.egon.familyaibutler.uaa.facade.AccountFacade;
import top.egon.familyaibutler.uaa.facade.dto.account.AccountSummaryResponse;
import top.egon.familyaibutler.uaa.facade.dto.account.ChangeAccountStatusRequest;
import top.egon.familyaibutler.uaa.facade.dto.account.DeleteAccountRequest;
import top.egon.familyaibutler.uaa.facade.dto.account.RegisterAccountRequest;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @ClassName: AccountDubboAdapter
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:20
 * @Description: 账号 facade 适配器
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
public class AccountDubboAdapter implements AccountFacade {
    private final AccountManage accountService;

    /**
     * 通过用户名注册账号。
     *
     * @param request 注册请求
     * @return 账号摘要
     */
    @Override
    public AccountSummaryResponse registerByUsername(RegisterAccountRequest request) {
        return accountService.registerByUsername(request);
    }

    /**
     * 通过邮箱注册账号。
     *
     * @param request 注册请求
     * @return 账号摘要
     */
    @Override
    public AccountSummaryResponse registerByEmail(RegisterAccountRequest request) {
        return accountService.registerByEmail(request);
    }

    /**
     * 通过手机号注册账号。
     *
     * @param request 注册请求
     * @return 账号摘要
     */
    @Override
    public AccountSummaryResponse registerByPhone(RegisterAccountRequest request) {
        return accountService.registerByPhone(request);
    }

    /**
     * 查询账号摘要。
     *
     * @param accountId 账号 ID
     * @return 账号摘要
     */
    @Override
    public AccountSummaryResponse findAccountSummary(String accountId) {
        return accountService.findAccountSummary(accountId);
    }

    /**
     * 修改账号状态。
     *
     * @param request 状态修改请求
     * @return 账号摘要
     */
    @Override
    public AccountSummaryResponse changeAccountStatus(ChangeAccountStatusRequest request) {
        return accountService.changeAccountStatus(request);
    }

    /**
     * 申请注销账号。
     *
     * @param request 注销请求
     * @return 账号摘要
     */
    @Override
    public AccountSummaryResponse requestAccountDeletion(DeleteAccountRequest request) {
        return accountService.requestAccountDeletion(request);
    }

    /**
     * 确认注销账号。
     *
     * @param request 注销请求
     * @return 账号摘要
     */
    @Override
    public AccountSummaryResponse confirmAccountDeletion(DeleteAccountRequest request) {
        return accountService.confirmAccountDeletion(request);
    }
}
