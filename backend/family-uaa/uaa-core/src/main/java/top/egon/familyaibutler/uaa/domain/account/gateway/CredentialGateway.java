/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.gateway
 * @FileName: CredentialGateway.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: 凭证领域网关文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.account.gateway;

import top.egon.familyaibutler.uaa.domain.account.model.entity.Credential;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.account.gateway
 * @ClassName: CredentialGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: 凭证领域网关
 * @Version: 1.0
 */
public interface CredentialGateway {

    /**
     * 保存凭证。
     *
     * @param credential 凭证实体
     * @return 保存后的凭证实体
     */
    Credential save(Credential credential);

    /**
     * 查询账号的密码凭证。
     *
     * @param accountId 账号 ID
     * @return 密码凭证
     */
    Optional<Credential> findPasswordCredential(String accountId);
}
