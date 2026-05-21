/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @FileName: InMemoryAccountGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: 内存账号网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gateway.impl;

import top.egon.familyaibutler.uaa.domain.account.gateway.AccountGateway;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Account;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @ClassName: InMemoryAccountGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: 内存账号网关实现
 * @Version: 1.0
 */
public class InMemoryAccountGatewayImpl implements AccountGateway {
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    /**
     * 保存账号聚合。
     *
     * @param account 账号聚合
     * @return 保存后的账号聚合
     */
    @Override
    public Account save(Account account) {
        accounts.put(account.getAccountId(), account);
        return account;
    }

    /**
     * 按账号 ID 查询账号。
     *
     * @param accountId 账号 ID
     * @return 账号聚合
     */
    @Override
    public Optional<Account> findByAccountId(String accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    /**
     * 按用户名查询账号。
     *
     * @param username 用户名
     * @return 账号聚合
     */
    @Override
    public Optional<Account> findByUsername(String username) {
        return accounts.values().stream()
                .filter(account -> username != null && username.equals(account.getUsername()))
                .findFirst();
    }

    /**
     * 按邮箱查询账号。
     *
     * @param email 邮箱
     * @return 账号聚合
     */
    @Override
    public Optional<Account> findByEmail(String email) {
        return accounts.values().stream()
                .filter(account -> email != null && email.equals(account.getEmail()))
                .findFirst();
    }

    /**
     * 按手机号查询账号。
     *
     * @param phone 手机号
     * @return 账号聚合
     */
    @Override
    public Optional<Account> findByPhone(String phone) {
        return accounts.values().stream()
                .filter(account -> phone != null && phone.equals(account.getPhone()))
                .findFirst();
    }
}
