/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @FileName: InMemoryCredentialGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: 内存凭证网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gateway.impl;

import top.egon.familyaibutler.uaa.domain.account.gateway.CredentialGateway;
import top.egon.familyaibutler.uaa.domain.account.model.entity.Credential;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @ClassName: InMemoryCredentialGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: 内存凭证网关实现
 * @Version: 1.0
 */
public class InMemoryCredentialGatewayImpl implements CredentialGateway {
    private final Map<String, Credential> passwordCredentials = new ConcurrentHashMap<>();

    /**
     * 保存凭证。
     *
     * @param credential 凭证实体
     * @return 保存后的凭证实体
     */
    @Override
    public Credential save(Credential credential) {
        passwordCredentials.put(credential.getAccountId(), credential);
        return credential;
    }

    /**
     * 查询账号的密码凭证。
     *
     * @param accountId 账号 ID
     * @return 密码凭证
     */
    @Override
    public Optional<Credential> findPasswordCredential(String accountId) {
        return Optional.ofNullable(passwordCredentials.get(accountId));
    }
}
