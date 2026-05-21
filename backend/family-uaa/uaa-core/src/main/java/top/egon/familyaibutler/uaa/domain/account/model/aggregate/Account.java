/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.model.aggregate
 * @FileName: Account.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号聚合文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.account.model.aggregate;

import top.egon.familyaibutler.uaa.domain.account.model.enums.AccountStatus;
import top.egon.familyaibutler.uaa.domain.account.model.enums.AccountType;

import java.util.UUID;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.model.aggregate
 * @ClassName: Account
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号聚合
 * @Version: 1.0
 */
public class Account {
    private final String accountId;
    private final String username;
    private final String email;
    private final String phone;
    private final AccountType accountType;
    private AccountStatus status;
    private long authVersion;
    private long entitlementVersion;
    private long sessionVersion;
    private long riskVersion;

    private Account(String accountId, String username, String email, String phone, AccountType accountType, AccountStatus status) {
        this.accountId = accountId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.accountType = accountType;
        this.status = status;
        this.authVersion = 1L;
        this.entitlementVersion = 1L;
        this.sessionVersion = 1L;
        this.riskVersion = 1L;
    }

    /**
     * 创建 C 端账号。
     *
     * @param username 用户名
     * @param email    邮箱
     * @param phone    手机号
     * @return 账号聚合
     */
    public static Account createConsumer(String username, String email, String phone) {
        return new Account("acc_" + UUID.randomUUID(), username, email, phone, AccountType.CONSUMER, AccountStatus.NORMAL);
    }

    /**
     * 还原账号聚合。
     *
     * @param accountId          账号 ID
     * @param username           用户名
     * @param email              邮箱
     * @param phone              手机号
     * @param accountType        账号类型
     * @param status             账号状态
     * @param authVersion        权限版本
     * @param entitlementVersion 权益版本
     * @param sessionVersion     会话版本
     * @param riskVersion        风控版本
     * @return 账号聚合
     */
    public static Account restore(String accountId, String username, String email, String phone, AccountType accountType,
                                  AccountStatus status, long authVersion, long entitlementVersion, long sessionVersion,
                                  long riskVersion) {
        Account account = new Account(accountId, username, email, phone, accountType, status);
        account.authVersion = authVersion;
        account.entitlementVersion = entitlementVersion;
        account.sessionVersion = sessionVersion;
        account.riskVersion = riskVersion;
        return account;
    }

    /**
     * 判断账号是否可登录。
     *
     * @return true 表示账号可登录
     */
    public boolean canLogin() {
        return status == AccountStatus.NORMAL || status == AccountStatus.PASSWORD_EXPIRED || status == AccountStatus.MFA_REQUIRED;
    }

    /**
     * 修改账号状态。
     *
     * @param status 账号状态
     */
    public void changeStatus(AccountStatus status) {
        if (this.status == AccountStatus.DELETED && status != AccountStatus.DELETED) {
            throw new IllegalStateException("已注销账号不允许恢复状态");
        }
        if (this.status == status) {
            return;
        }
        this.status = status;
        this.authVersion++;
        this.riskVersion++;
        this.sessionVersion++;
    }

    /**
     * 申请注销账号。
     */
    public void requestDeletion() {
        changeStatus(AccountStatus.DELETION_REQUESTED);
    }

    /**
     * 确认注销账号。
     */
    public void confirmDeletion() {
        if (status != AccountStatus.DELETION_REQUESTED) {
            throw new IllegalStateException("账号未申请注销，不能直接确认注销");
        }
        changeStatus(AccountStatus.DELETED);
    }

    /**
     * 获取账号 ID。
     *
     * @return 账号 ID
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * 获取用户名。
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获取邮箱。
     *
     * @return 邮箱
     */
    public String getEmail() {
        return email;
    }

    /**
     * 获取手机号。
     *
     * @return 手机号
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 获取账号类型。
     *
     * @return 账号类型
     */
    public AccountType getAccountType() {
        return accountType;
    }

    /**
     * 获取账号状态。
     *
     * @return 账号状态
     */
    public AccountStatus getStatus() {
        return status;
    }

    /**
     * 获取权限版本。
     *
     * @return 权限版本
     */
    public long getAuthVersion() {
        return authVersion;
    }

    /**
     * 获取权益版本。
     *
     * @return 权益版本
     */
    public long getEntitlementVersion() {
        return entitlementVersion;
    }

    /**
     * 获取会话版本。
     *
     * @return 会话版本
     */
    public long getSessionVersion() {
        return sessionVersion;
    }

    /**
     * 获取风控版本。
     *
     * @return 风控版本
     */
    public long getRiskVersion() {
        return riskVersion;
    }
}
