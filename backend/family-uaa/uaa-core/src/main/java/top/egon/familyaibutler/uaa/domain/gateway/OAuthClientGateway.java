/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.gateway
 * @FileName: OAuthClientGateway.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:00
 * @Description: OAuth Client 网关文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.gateway;

import top.egon.familyaibutler.uaa.domain.model.aggregate.OAuthClient;

import java.util.List;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.gateway
 * @ClassName: OAuthClientGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:00
 * @Description: OAuth Client 网关
 * @Version: 1.0
 */
public interface OAuthClientGateway {

    /**
     * 保存 OAuth Client。
     *
     * @param client OAuth Client
     * @return OAuth Client
     */
    OAuthClient save(OAuthClient client);

    /**
     * 按客户端 ID 查询。
     *
     * @param clientId 客户端 ID
     * @return OAuth Client
     */
    Optional<OAuthClient> findByClientId(String clientId);

    /**
     * 查询全部 OAuth Client。
     *
     * @return OAuth Client 列表
     */
    List<OAuthClient> findAll();
}
