/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage.impl
 * @FileName: OAuthClientManageImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-16:45
 * @Description: OAuth Client 应用服务实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application.manage.impl;

import org.springframework.stereotype.Service;
import top.egon.familyaibutler.uaa.application.manage.OAuthClientManage;
import top.egon.familyaibutler.uaa.domain.auth.service.TokenDomainService;
import top.egon.familyaibutler.uaa.domain.oauth.gateway.OAuthClientGateway;
import top.egon.familyaibutler.uaa.domain.oauth.model.aggregate.OAuthClient;
import top.egon.familyaibutler.uaa.domain.oauth.model.enums.OAuthClientStatus;
import top.egon.familyaibutler.uaa.facade.dto.oauthclient.CreateOAuthClientRequest;
import top.egon.familyaibutler.uaa.facade.dto.oauthclient.OAuthClientResponse;

import java.util.List;
import java.util.Set;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage.impl
 * @ClassName: OAuthClientManageImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 16:45
 * @Description: OAuth Client 应用服务实现
 * @Version: 1.0
 */
@Service
public class OAuthClientManageImpl implements OAuthClientManage {
    private final OAuthClientGateway oAuthClientGateway;
    private final TokenDomainService tokenDomainService;

    /**
     * 创建 OAuth Client 应用服务实现。
     *
     * @param oAuthClientGateway OAuth Client 网关
     * @param tokenDomainService Token 领域服务
     */
    public OAuthClientManageImpl(OAuthClientGateway oAuthClientGateway, TokenDomainService tokenDomainService) {
        this.oAuthClientGateway = oAuthClientGateway;
        this.tokenDomainService = tokenDomainService;
    }

    /**
     * 创建 OAuth Client。
     *
     * @param request 创建请求
     * @return OAuth Client 响应
     */
    @Override
    public OAuthClientResponse create(CreateOAuthClientRequest request) {
        if (oAuthClientGateway.findByClientId(request.clientId()).isPresent()) {
            throw new IllegalArgumentException("OAuth Client 已存在");
        }
        String secretHash = request.clientSecret() == null || request.clientSecret().isBlank()
                ? null : tokenDomainService.hashToken(request.clientSecret());
        OAuthClient client = OAuthClient.restore(request.clientId(), request.clientName(), secretHash, OAuthClientStatus.ACTIVE,
                defaultSet(request.grantTypes(), Set.of("PASSWORD", "REFRESH_TOKEN")),
                defaultSet(request.scopes(), Set.of("openid", "profile")),
                defaultSet(request.resourcePatterns(), Set.of()),
                defaultLong(request.accessTokenTtlSeconds(), 300L),
                defaultLong(request.refreshTokenTtlSeconds(), 2592000L));
        return toResponse(oAuthClientGateway.save(client));
    }

    /**
     * 按客户端 ID 查询 OAuth Client。
     *
     * @param clientId 客户端 ID
     * @return OAuth Client 响应
     */
    @Override
    public OAuthClientResponse get(String clientId) {
        return oAuthClientGateway.findByClientId(clientId).map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("OAuth Client 不存在"));
    }

    /**
     * 查询 OAuth Client 列表。
     *
     * @return OAuth Client 列表
     */
    @Override
    public List<OAuthClientResponse> list() {
        return oAuthClientGateway.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * 转换 OAuth Client 响应。
     *
     * @param client OAuth Client
     * @return OAuth Client 响应
     */
    private OAuthClientResponse toResponse(OAuthClient client) {
        return new OAuthClientResponse(client.getClientId(), client.getClientName(), client.getStatus().name(),
                client.requiresSecret(), client.getGrantTypes(), client.getScopes(), client.getResourcePatterns(),
                client.getAccessTokenTtlSeconds(), client.getRefreshTokenTtlSeconds());
    }

    /**
     * 默认集合。
     *
     * @param actual       实际集合
     * @param defaultValue 默认集合
     * @return 目标集合
     */
    private Set<String> defaultSet(Set<String> actual, Set<String> defaultValue) {
        return actual == null || actual.isEmpty() ? defaultValue : actual;
    }

    /**
     * 默认长整型值。
     *
     * @param actual       实际值
     * @param defaultValue 默认值
     * @return 目标值
     */
    private long defaultLong(Long actual, long defaultValue) {
        return actual == null || actual <= 0 ? defaultValue : actual;
    }
}
