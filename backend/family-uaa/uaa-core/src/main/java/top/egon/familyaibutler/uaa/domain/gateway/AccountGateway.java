/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.gateway
 * @FileName: AccountGateway.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号领域网关文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.gateway;

import top.egon.familyaibutler.uaa.domain.model.aggregate.Account;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.gateway
 * @ClassName: AccountGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号领域网关
 * @Version: 1.0
 */
public interface AccountGateway {

    /**
     * 保存账号聚合。
     *
     * @param account 账号聚合
     * @return 保存后的账号聚合
     */
    Account save(Account account);

    /**
     * 按账号 ID 查询账号。
     *
     * @param accountId 账号 ID
     * @return 账号聚合
     */
    Optional<Account> findByAccountId(String accountId);

    /**
     * 按用户名查询账号。
     *
     * @param username 用户名
     * @return 账号聚合
     */
    default Optional<Account> findByUsername(String username) {
        return Optional.empty();
    }

    /**
     * 按邮箱查询账号。
     *
     * @param email 邮箱
     * @return 账号聚合
     */
    default Optional<Account> findByEmail(String email) {
        return Optional.empty();
    }

    /**
     * 按手机号查询账号。
     *
     * @param phone 手机号
     * @return 账号聚合
     */
    default Optional<Account> findByPhone(String phone) {
        return Optional.empty();
    }

    /**
     * 按登录主体查询账号。
     *
     * @param principal 登录主体，可以是用户名、邮箱或手机号
     * @return 账号聚合
     */
    default Optional<Account> findByPrincipal(String principal) {
        if (principal == null || principal.isBlank()) {
            return Optional.empty();
        }
        Optional<Account> byUsername = findByUsername(principal);
        if (byUsername.isPresent()) {
            return byUsername;
        }
        Optional<Account> byEmail = findByEmail(principal);
        if (byEmail.isPresent()) {
            return byEmail;
        }
        return findByPhone(principal);
    }
}
