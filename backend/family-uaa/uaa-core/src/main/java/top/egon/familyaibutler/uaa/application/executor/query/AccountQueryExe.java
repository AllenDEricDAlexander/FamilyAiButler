/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.executor.query
 * @FileName: AccountQueryExe.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号查询应用服务文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application.executor.query;

import org.springframework.stereotype.Service;
import top.egon.familyaibutler.uaa.domain.account.gateway.AccountGateway;
import top.egon.familyaibutler.uaa.domain.account.gateway.ProfileGateway;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Account;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Profile;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.executor.query
 * @ClassName: AccountQueryExe
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号查询应用服务
 * @Version: 1.0
 */
@Service
public class AccountQueryExe {
    /**
     * Account 网关。
     */
    private final AccountGateway accountGateway;
    /**
     * Profile 网关。
     */
    private final ProfileGateway profileGateway;

    /**
     * 创建账号查询应用服务。
     *
     * @param accountGateway 账号网关
     * @param profileGateway Profile 网关
     */
    public AccountQueryExe(AccountGateway accountGateway, ProfileGateway profileGateway) {
        this.accountGateway = accountGateway;
        this.profileGateway = profileGateway;
    }

    /**
     * 查询账号 Profile 列表。
     *
     * @param accountId 账号 ID
     * @return Profile 列表
     */
    public List<Profile> listProfiles(String accountId) {
        accountGateway.findByAccountId(accountId).orElseThrow(() -> new IllegalArgumentException("账号不存在"));
        return profileGateway.findByAccountId(accountId);
    }

    /**
     * 查询账号。
     *
     * @param accountId 账号 ID
     * @return 账号聚合
     */
    public Account findAccount(String accountId) {
        return accountGateway.findByAccountId(accountId).orElseThrow(() -> new IllegalArgumentException("账号不存在"));
    }
}
