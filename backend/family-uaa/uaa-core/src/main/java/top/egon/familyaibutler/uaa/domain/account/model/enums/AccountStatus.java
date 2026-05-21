/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.model.enums
 * @FileName: AccountStatus.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号状态枚举文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.account.model.enums;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.model.enums
 * @ClassName: AccountStatus
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号状态枚举
 * @Version: 1.0
 */
public enum AccountStatus {
    NORMAL,
    UNVERIFIED,
    LOCKED,
    SUSPENDED,
    BANNED,
    DELETION_REQUESTED,
    DELETED,
    RISK_LIMITED,
    PENDING_REVIEW,
    PASSWORD_EXPIRED,
    MFA_REQUIRED
}
