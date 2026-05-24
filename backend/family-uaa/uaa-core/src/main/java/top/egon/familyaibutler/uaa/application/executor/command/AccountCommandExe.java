/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.executor.command
 * @FileName: AccountCommandExe.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号命令应用服务文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application.executor.command;

import org.springframework.stereotype.Service;
import top.egon.familyaibutler.uaa.application.command.account.RegisterAccountCommand;
import top.egon.familyaibutler.uaa.application.result.account.AccountResponse;
import top.egon.familyaibutler.uaa.domain.account.gateway.AccountGateway;
import top.egon.familyaibutler.uaa.domain.account.gateway.CredentialGateway;
import top.egon.familyaibutler.uaa.domain.account.gateway.ProfileGateway;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Account;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Profile;
import top.egon.familyaibutler.uaa.domain.account.model.enums.AccountStatus;
import top.egon.familyaibutler.uaa.domain.account.service.AccountDomainService;
import top.egon.familyaibutler.uaa.domain.account.service.CredentialDomainService;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.executor.command
 * @ClassName: AccountCommandExe
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号命令应用服务
 * @Version: 1.0
 */
@Service
public class AccountCommandExe {
    /**
     * Account 领域服务。
     */
    private final AccountDomainService accountDomainService;
    /**
     * Credential 领域服务。
     */
    private final CredentialDomainService credentialDomainService;
    /**
     * Account 网关。
     */
    private final AccountGateway accountGateway;
    /**
     * Credential 网关。
     */
    private final CredentialGateway credentialGateway;
    /**
     * Profile 网关。
     */
    private final ProfileGateway profileGateway;

    /**
     * 创建账号命令应用服务。
     *
     * @param accountDomainService    账号领域服务
     * @param credentialDomainService 凭证领域服务
     * @param accountGateway          账号网关
     * @param credentialGateway       凭证网关
     * @param profileGateway          Profile 网关
     */
    public AccountCommandExe(AccountDomainService accountDomainService, CredentialDomainService credentialDomainService,
                             AccountGateway accountGateway, CredentialGateway credentialGateway, ProfileGateway profileGateway) {
        this.accountDomainService = accountDomainService;
        this.credentialDomainService = credentialDomainService;
        this.accountGateway = accountGateway;
        this.credentialGateway = credentialGateway;
        this.profileGateway = profileGateway;
    }

    /**
     * 通过用户名注册账号。
     *
     * @param command 注册命令
     * @return 账号响应
     */
    public AccountResponse registerByUsername(RegisterAccountCommand command) {
        checkAccountUnique(command);
        Account account = accountDomainService.registerConsumerAccount(command.username(), command.email(), command.phone());
        Account savedAccount = accountGateway.save(account);
        credentialGateway.save(credentialDomainService.createPasswordCredential(savedAccount.getAccountId(), command.password()));
        List<Profile> profiles = accountDomainService.createDefaultProfiles(savedAccount);
        profileGateway.saveAll(profiles);
        return toResponse(savedAccount);
    }

    /**
     * 修改账号状态。
     *
     * @param accountId 账号 ID
     * @param status    目标状态
     * @return 账号响应
     */
    public AccountResponse changeAccountStatus(String accountId, AccountStatus status) {
        Account account = findAccount(accountId);
        account.changeStatus(status);
        return toResponse(accountGateway.save(account));
    }

    /**
     * 申请注销账号。
     *
     * @param accountId 账号 ID
     * @return 账号响应
     */
    public AccountResponse requestDeletion(String accountId) {
        Account account = findAccount(accountId);
        account.requestDeletion();
        return toResponse(accountGateway.save(account));
    }

    /**
     * 确认注销账号。
     *
     * @param accountId        账号 ID
     * @param verificationCode 二次验证码
     * @return 账号响应
     */
    public AccountResponse confirmDeletion(String accountId, String verificationCode) {
        if (verificationCode == null || verificationCode.isBlank()) {
            throw new IllegalArgumentException("确认注销需要二次验证码");
        }
        Account account = findAccount(accountId);
        account.confirmDeletion();
        return toResponse(accountGateway.save(account));
    }

    /**
     * 校验账号唯一性。
     *
     * @param command 注册命令
     */
    private void checkAccountUnique(RegisterAccountCommand command) {
        if (command.username() != null && accountGateway.findByUsername(command.username()).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (command.email() != null && accountGateway.findByEmail(command.email()).isPresent()) {
            throw new IllegalArgumentException("邮箱已存在");
        }
        if (command.phone() != null && accountGateway.findByPhone(command.phone()).isPresent()) {
            throw new IllegalArgumentException("手机号已存在");
        }
    }

    /**
     * 查询账号聚合。
     *
     * @param accountId 账号 ID
     * @return 账号聚合
     */
    private Account findAccount(String accountId) {
        return accountGateway.findByAccountId(accountId).orElseThrow(() -> new IllegalArgumentException("账号不存在"));
    }

    /**
     * 转换为账号响应。
     *
     * @param account 账号聚合
     * @return 账号响应
     */
    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getAccountId(), account.getUsername(), account.getEmail(), account.getPhone(),
                account.getStatus().name(), account.getAccountType().name(), account.getAuthVersion(),
                account.getEntitlementVersion(), account.getSessionVersion(), account.getRiskVersion());
    }
}
