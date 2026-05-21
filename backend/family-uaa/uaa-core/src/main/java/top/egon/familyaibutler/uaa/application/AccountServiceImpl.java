/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @FileName: AccountServiceImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号应用服务实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application;

import org.springframework.stereotype.Service;
import top.egon.familyaibutler.uaa.application.dto.account.AccountResponse;
import top.egon.familyaibutler.uaa.application.dto.account.RegisterAccountCommand;
import top.egon.familyaibutler.uaa.domain.model.enums.AccountStatus;
import top.egon.familyaibutler.uaa.facade.AccountFacade;
import top.egon.familyaibutler.uaa.facade.dto.account.AccountSummaryResponse;
import top.egon.familyaibutler.uaa.facade.dto.account.ChangeAccountStatusRequest;
import top.egon.familyaibutler.uaa.facade.dto.account.DeleteAccountRequest;
import top.egon.familyaibutler.uaa.facade.dto.account.RegisterAccountRequest;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @ClassName: AccountServiceImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号应用服务实现
 * @Version: 1.0
 */
@Service
public class AccountServiceImpl implements AccountServiceI, AccountFacade {
    private final AccountCommandService accountCommandService;
    private final AccountQueryService accountQueryService;

    /**
     * 创建账号应用服务实现。
     *
     * @param accountCommandService 账号命令应用服务
     * @param accountQueryService   账号查询应用服务
     */
    public AccountServiceImpl(AccountCommandService accountCommandService, AccountQueryService accountQueryService) {
        this.accountCommandService = accountCommandService;
        this.accountQueryService = accountQueryService;
    }

    /**
     * 通过用户名注册账号。
     *
     * @param command 注册账号命令
     * @return 账号响应
     */
    @Override
    public AccountResponse registerByUsername(RegisterAccountCommand command) {
        return accountCommandService.registerByUsername(command);
    }

    /**
     * 修改账号状态。
     *
     * @param accountId 账号 ID
     * @param status    账号状态
     * @return 账号响应
     */
    @Override
    public AccountResponse changeAccountStatus(String accountId, AccountStatus status) {
        return accountCommandService.changeAccountStatus(accountId, status);
    }

    /**
     * 申请注销账号。
     *
     * @param accountId 账号 ID
     * @return 账号响应
     */
    @Override
    public AccountResponse requestDeletion(String accountId) {
        return accountCommandService.requestDeletion(accountId);
    }

    /**
     * 确认注销账号。
     *
     * @param accountId        账号 ID
     * @param verificationCode 二次验证码
     * @return 账号响应
     */
    @Override
    public AccountResponse confirmDeletion(String accountId, String verificationCode) {
        return accountCommandService.confirmDeletion(accountId, verificationCode);
    }

    /**
     * 通过用户名注册账号。
     *
     * @param request 注册请求
     * @return 账号摘要
     */
    @Override
    public AccountSummaryResponse registerByUsername(RegisterAccountRequest request) {
        return toSummary(registerByUsername(toCommand(request)));
    }

    /**
     * 通过邮箱注册账号。
     *
     * @param request 注册请求
     * @return 账号摘要
     */
    @Override
    public AccountSummaryResponse registerByEmail(RegisterAccountRequest request) {
        return toSummary(registerByUsername(toCommand(request)));
    }

    /**
     * 通过手机号注册账号。
     *
     * @param request 注册请求
     * @return 账号摘要
     */
    @Override
    public AccountSummaryResponse registerByPhone(RegisterAccountRequest request) {
        return toSummary(registerByUsername(toCommand(request)));
    }

    /**
     * 查询账号摘要。
     *
     * @param accountId 账号 ID
     * @return 账号摘要
     */
    @Override
    public AccountSummaryResponse findAccountSummary(String accountId) {
        return toSummary(accountQueryService.findAccount(accountId));
    }

    /**
     * 修改账号状态。
     *
     * @param request 状态修改请求
     * @return 账号摘要
     */
    @Override
    public AccountSummaryResponse changeAccountStatus(ChangeAccountStatusRequest request) {
        return toSummary(changeAccountStatus(request.accountId(), AccountStatus.valueOf(request.status())));
    }

    /**
     * 申请注销账号。
     *
     * @param request 注销请求
     * @return 账号摘要
     */
    @Override
    public AccountSummaryResponse requestAccountDeletion(DeleteAccountRequest request) {
        return toSummary(requestDeletion(request.accountId()));
    }

    /**
     * 确认注销账号。
     *
     * @param request 注销请求
     * @return 账号摘要
     */
    @Override
    public AccountSummaryResponse confirmAccountDeletion(DeleteAccountRequest request) {
        return toSummary(confirmDeletion(request.accountId(), request.verificationCode()));
    }

    /**
     * facade 请求转换为应用命令。
     *
     * @param request 注册请求
     * @return 注册命令
     */
    private RegisterAccountCommand toCommand(RegisterAccountRequest request) {
        return new RegisterAccountCommand(request.username(), request.email(), request.phone(), request.password());
    }

    /**
     * 应用响应转换为 facade 摘要。
     *
     * @param response 账号响应
     * @return 账号摘要
     */
    private AccountSummaryResponse toSummary(AccountResponse response) {
        return new AccountSummaryResponse(response.accountId(), response.username(), response.email(), response.phone(),
                response.status(), response.accountType(), response.authVersion(), response.entitlementVersion(),
                response.sessionVersion(), response.riskVersion());
    }

    /**
     * 账号聚合转换为 facade 摘要。
     *
     * @param account 账号聚合
     * @return 账号摘要
     */
    private AccountSummaryResponse toSummary(top.egon.familyaibutler.uaa.domain.model.aggregate.Account account) {
        return new AccountSummaryResponse(account.getAccountId(), account.getUsername(), account.getEmail(), account.getPhone(),
                account.getStatus().name(), account.getAccountType().name(), account.getAuthVersion(),
                account.getEntitlementVersion(), account.getSessionVersion(), account.getRiskVersion());
    }
}
