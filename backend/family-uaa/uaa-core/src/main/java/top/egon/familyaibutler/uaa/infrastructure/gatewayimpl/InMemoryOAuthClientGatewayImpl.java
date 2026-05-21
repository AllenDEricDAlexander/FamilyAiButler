/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @FileName: InMemoryOAuthClientGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:00
 * @Description: 内存 OAuth Client 网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gatewayimpl;

import top.egon.familyaibutler.uaa.domain.gateway.OAuthClientGateway;
import top.egon.familyaibutler.uaa.domain.model.aggregate.OAuthClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @ClassName: InMemoryOAuthClientGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:00
 * @Description: 内存 OAuth Client 网关实现
 * @Version: 1.0
 */
public class InMemoryOAuthClientGatewayImpl implements OAuthClientGateway {
    private final Map<String, OAuthClient> clients = new ConcurrentHashMap<>();

    /**
     * 保存 OAuth Client。
     *
     * @param client OAuth Client
     * @return OAuth Client
     */
    @Override
    public OAuthClient save(OAuthClient client) {
        clients.put(client.getClientId(), client);
        return client;
    }

    /**
     * 按客户端 ID 查询。
     *
     * @param clientId 客户端 ID
     * @return OAuth Client
     */
    @Override
    public Optional<OAuthClient> findByClientId(String clientId) {
        return Optional.ofNullable(clients.get(clientId));
    }

    /**
     * 查询全部 OAuth Client。
     *
     * @return OAuth Client 列表
     */
    @Override
    public List<OAuthClient> findAll() {
        return List.copyOf(clients.values());
    }
}
