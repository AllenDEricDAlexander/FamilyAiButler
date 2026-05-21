/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @FileName: AccountServiceTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号应用服务测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application;

import org.junit.jupiter.api.Test;
import top.egon.familyaibutler.uaa.application.dto.account.RegisterAccountCommand;
import top.egon.familyaibutler.uaa.domain.gateway.AccountGateway;
import top.egon.familyaibutler.uaa.domain.gateway.CredentialGateway;
import top.egon.familyaibutler.uaa.domain.gateway.ProfileGateway;
import top.egon.familyaibutler.uaa.domain.model.aggregate.Account;
import top.egon.familyaibutler.uaa.domain.model.aggregate.Profile;
import top.egon.familyaibutler.uaa.domain.model.entity.Credential;
import top.egon.familyaibutler.uaa.domain.model.enums.ProfileType;
import top.egon.familyaibutler.uaa.domain.service.AccountDomainService;
import top.egon.familyaibutler.uaa.domain.service.CredentialDomainService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @ClassName: AccountServiceTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号应用服务测试
 * @Version: 1.0
 */
class AccountServiceTest {

    /**
     * 校验账号应用服务注册账号并保存默认 Profile。
     */
    @Test
    void registerByUsernameShouldPersistAccountAndMainProfile() {
        InMemoryAccountGateway accountGateway = new InMemoryAccountGateway();
        InMemoryCredentialGateway credentialGateway = new InMemoryCredentialGateway();
        InMemoryProfileGateway profileGateway = new InMemoryProfileGateway();
        AccountCommandService commandService = new AccountCommandService(new AccountDomainService(), new CredentialDomainService(),
                accountGateway, credentialGateway, profileGateway);
        AccountQueryService queryService = new AccountQueryService(accountGateway, profileGateway);
        AccountServiceImpl accountService = new AccountServiceImpl(commandService, queryService);

        var response = accountService.registerByUsername(new RegisterAccountCommand("mario", "mario@example.com", "13800000000", "S3cret@123"));

        assertThat(response.accountId()).isNotBlank();
        assertThat(accountGateway.accounts).hasSize(1);
        assertThat(credentialGateway.credentials).hasSize(1);
        assertThat(profileGateway.profiles).hasSize(1);
        assertThat(profileGateway.profiles.get(0).getProfileType()).isEqualTo(ProfileType.MAIN);
    }

    /**
     * 账号内存网关测试替身。
     */
    private static class InMemoryAccountGateway implements AccountGateway {
        private final List<Account> accounts = new ArrayList<>();

        @Override
        public Account save(Account account) {
            accounts.add(account);
            return account;
        }

        @Override
        public Optional<Account> findByAccountId(String accountId) {
            return accounts.stream()
                    .filter(account -> account.getAccountId().equals(accountId))
                    .findFirst();
        }

        @Override
        public Optional<Account> findByUsername(String username) {
            return accounts.stream()
                    .filter(account -> username != null && username.equals(account.getUsername()))
                    .findFirst();
        }

        @Override
        public Optional<Account> findByEmail(String email) {
            return accounts.stream()
                    .filter(account -> email != null && email.equals(account.getEmail()))
                    .findFirst();
        }

        @Override
        public Optional<Account> findByPhone(String phone) {
            return accounts.stream()
                    .filter(account -> phone != null && phone.equals(account.getPhone()))
                    .findFirst();
        }
    }

    /**
     * 凭证内存网关测试替身。
     */
    private static class InMemoryCredentialGateway implements CredentialGateway {
        private final List<Credential> credentials = new ArrayList<>();

        @Override
        public Credential save(Credential credential) {
            credentials.add(credential);
            return credential;
        }

        @Override
        public Optional<Credential> findPasswordCredential(String accountId) {
            return credentials.stream()
                    .filter(credential -> credential.getAccountId().equals(accountId))
                    .findFirst();
        }
    }

    /**
     * Profile 内存网关测试替身。
     */
    private static class InMemoryProfileGateway implements ProfileGateway {
        private final List<Profile> profiles = new ArrayList<>();

        @Override
        public List<Profile> saveAll(List<Profile> profiles) {
            this.profiles.addAll(profiles);
            return profiles;
        }

        @Override
        public Profile save(Profile profile) {
            profiles.removeIf(item -> item.getProfileId().equals(profile.getProfileId()));
            profiles.add(profile);
            return profile;
        }

        @Override
        public Optional<Profile> findByProfileId(String profileId) {
            return profiles.stream()
                    .filter(profile -> profile.getProfileId().equals(profileId))
                    .findFirst();
        }

        @Override
        public List<Profile> findByAccountId(String accountId) {
            return profiles.stream()
                    .filter(profile -> profile.getAccountId().equals(accountId))
                    .toList();
        }
    }
}
