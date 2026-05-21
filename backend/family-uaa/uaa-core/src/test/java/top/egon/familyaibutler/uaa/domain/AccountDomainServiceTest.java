/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain
 * @FileName: AccountDomainServiceTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号领域服务测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain;

import org.junit.jupiter.api.Test;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Account;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Profile;
import top.egon.familyaibutler.uaa.domain.account.model.enums.AccountStatus;
import top.egon.familyaibutler.uaa.domain.account.model.enums.AccountType;
import top.egon.familyaibutler.uaa.domain.account.model.enums.ProfileType;
import top.egon.familyaibutler.uaa.domain.account.service.AccountDomainService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain
 * @ClassName: AccountDomainServiceTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号领域服务测试
 * @Version: 1.0
 */
class AccountDomainServiceTest {

    /**
     * 校验注册账号时自动创建主 Profile。
     */
    @Test
    void registerConsumerAccountShouldCreateMainProfile() {
        AccountDomainService accountDomainService = new AccountDomainService();

        Account account = accountDomainService.registerConsumerAccount("mario", "mario@example.com", "13800000000");
        List<Profile> profiles = accountDomainService.createDefaultProfiles(account);

        assertThat(account.getStatus()).isEqualTo(AccountStatus.NORMAL);
        assertThat(account.getAccountType()).isEqualTo(AccountType.CONSUMER);
        assertThat(account.canLogin()).isTrue();
        assertThat(profiles).hasSize(1);
        assertThat(profiles.get(0).getProfileType()).isEqualTo(ProfileType.MAIN);
        assertThat(profiles.get(0).getAccountId()).isEqualTo(account.getAccountId());
    }

    /**
     * 校验被禁用账号不能继续登录。
     */
    @Test
    void suspendedAccountShouldNotLogin() {
        Account account = Account.createConsumer("mario", "mario@example.com", "13800000000");

        account.changeStatus(AccountStatus.SUSPENDED);

        assertThat(account.canLogin()).isFalse();
    }

    /**
     * 校验 Profile 删除不等于账号注销。
     */
    @Test
    void deletedProfileShouldNotDeleteAccount() {
        Account account = Account.createConsumer("mario", "mario@example.com", "13800000000");
        Profile profile = Profile.createMain(account.getAccountId(), "mario");

        profile.delete();

        assertThat(profile.isDeleted()).isTrue();
        assertThat(account.getStatus()).isEqualTo(AccountStatus.NORMAL);
    }
}
