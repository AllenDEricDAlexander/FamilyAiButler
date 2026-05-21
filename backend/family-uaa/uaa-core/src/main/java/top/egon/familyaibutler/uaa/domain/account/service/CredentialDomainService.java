/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.service
 * @FileName: CredentialDomainService.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: 凭证领域服务文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.account.service;

import top.egon.familyaibutler.uaa.domain.account.model.entity.Credential;
import top.egon.familyaibutler.uaa.domain.account.model.enums.CredentialType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.service
 * @ClassName: CredentialDomainService
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: 凭证领域服务
 * @Version: 1.0
 */
public class CredentialDomainService {

    /**
     * 创建密码凭证。
     *
     * @param accountId   账号 ID
     * @param rawPassword 密码明文
     * @return 密码凭证
     */
    public Credential createPasswordCredential(String accountId, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return new Credential(accountId, CredentialType.PASSWORD, hashSecret(rawPassword));
    }

    /**
     * 校验密码。
     *
     * @param credential  凭证实体
     * @param rawPassword 密码明文
     * @return true 表示校验通过
     */
    public boolean matchesPassword(Credential credential, String rawPassword) {
        return credential != null
                && credential.getCredentialType() == CredentialType.PASSWORD
                && credential.getCredentialHash().equals(hashSecret(rawPassword));
    }

    /**
     * 计算凭证密文。
     *
     * @param secret 凭证明文
     * @return 凭证密文
     */
    public String hashSecret(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", exception);
        }
    }
}
