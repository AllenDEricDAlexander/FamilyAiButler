/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.model.enums
 * @FileName: CredentialType.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 凭证类型枚举文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.account.model.enums;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.model.enums
 * @ClassName: CredentialType
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 凭证类型枚举
 * @Version: 1.0
 */
public enum CredentialType {
    PASSWORD,
    EMAIL_CODE,
    SMS_CODE,
    OAUTH,
    API_KEY,
    SERVICE_SECRET
}
