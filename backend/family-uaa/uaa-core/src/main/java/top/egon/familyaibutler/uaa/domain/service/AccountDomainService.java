/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.service
 * @FileName: AccountDomainService.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号领域服务文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.service;

import top.egon.familyaibutler.uaa.domain.model.aggregate.Account;
import top.egon.familyaibutler.uaa.domain.model.aggregate.Profile;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.service
 * @ClassName: AccountDomainService
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号领域服务
 * @Version: 1.0
 */
public class AccountDomainService {

    /**
     * 注册 C 端账号。
     *
     * @param username 用户名
     * @param email    邮箱
     * @param phone    手机号
     * @return 账号聚合
     */
    public Account registerConsumerAccount(String username, String email, String phone) {
        if (isBlank(username) && isBlank(email) && isBlank(phone)) {
            throw new IllegalArgumentException("用户名、邮箱、手机号至少提供一个");
        }
        return Account.createConsumer(username, email, phone);
    }

    /**
     * 创建默认 Profile。
     *
     * @param account 账号聚合
     * @return 默认 Profile 列表
     */
    public List<Profile> createDefaultProfiles(Account account) {
        return List.of(Profile.createMain(account.getAccountId(), account.getUsername()));
    }

    /**
     * 判断字符串是否为空。
     *
     * @param value 字符串
     * @return true 表示为空
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
