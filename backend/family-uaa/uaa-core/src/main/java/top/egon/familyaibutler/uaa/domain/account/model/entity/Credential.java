/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.model.entity
 * @FileName: Credential.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 凭证实体文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.account.model.entity;

import top.egon.familyaibutler.uaa.domain.account.model.enums.CredentialType;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.model.entity
 * @ClassName: Credential
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 凭证实体
 * @Version: 1.0
 */
public class Credential {
    private final String accountId;
    private final CredentialType credentialType;
    private final String credentialHash;

    /**
     * 创建凭证实体。
     *
     * @param accountId      账号 ID
     * @param credentialType 凭证类型
     * @param credentialHash 凭证哈希
     */
    public Credential(String accountId, CredentialType credentialType, String credentialHash) {
        this.accountId = accountId;
        this.credentialType = credentialType;
        this.credentialHash = credentialHash;
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
     * 获取凭证类型。
     *
     * @return 凭证类型
     */
    public CredentialType getCredentialType() {
        return credentialType;
    }

    /**
     * 获取凭证哈希。
     *
     * @return 凭证哈希
     */
    public String getCredentialHash() {
        return credentialHash;
    }
}
