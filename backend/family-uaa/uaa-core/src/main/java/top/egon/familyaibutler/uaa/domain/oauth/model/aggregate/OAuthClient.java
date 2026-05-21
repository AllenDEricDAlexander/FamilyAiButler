/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.oauth.model.aggregate
 * @FileName: OAuthClient.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:00
 * @Description: OAuth Client 聚合文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.oauth.model.aggregate;

import top.egon.familyaibutler.uaa.domain.oauth.model.enums.OAuthClientStatus;

import java.util.Set;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.oauth.model.aggregate
 * @ClassName: OAuthClient
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:00
 * @Description: OAuth Client 聚合
 * @Version: 1.0
 */
public class OAuthClient {
    private final String clientId;
    private final String clientName;
    private final String clientSecretHash;
    private final OAuthClientStatus status;
    private final Set<String> grantTypes;
    private final Set<String> scopes;
    private final Set<String> resourcePatterns;
    private final long accessTokenTtlSeconds;
    private final long refreshTokenTtlSeconds;

    private OAuthClient(String clientId, String clientName, String clientSecretHash, OAuthClientStatus status,
                        Set<String> grantTypes, Set<String> scopes, Set<String> resourcePatterns,
                        long accessTokenTtlSeconds, long refreshTokenTtlSeconds) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientSecretHash = clientSecretHash;
        this.status = status;
        this.grantTypes = Set.copyOf(grantTypes);
        this.scopes = Set.copyOf(scopes);
        this.resourcePatterns = Set.copyOf(resourcePatterns);
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    /**
     * 创建公开客户端。
     *
     * @param clientId         客户端 ID
     * @param clientName       客户端名称
     * @param grantTypes       授权类型
     * @param scopes           授权范围
     * @param resourcePatterns 可访问资源模式
     * @return OAuth Client
     */
    public static OAuthClient createPublicClient(String clientId, String clientName, Set<String> grantTypes,
                                                 Set<String> scopes, Set<String> resourcePatterns) {
        return restore(clientId, clientName, null, OAuthClientStatus.ACTIVE, grantTypes, scopes, resourcePatterns,
                300L, 2592000L);
    }

    /**
     * 还原 OAuth Client。
     *
     * @param clientId               客户端 ID
     * @param clientName             客户端名称
     * @param clientSecretHash       客户端密钥哈希
     * @param status                 状态
     * @param grantTypes             授权类型
     * @param scopes                 授权范围
     * @param resourcePatterns       可访问资源模式
     * @param accessTokenTtlSeconds  Access Token 有效期秒数
     * @param refreshTokenTtlSeconds Refresh Token 有效期秒数
     * @return OAuth Client
     */
    public static OAuthClient restore(String clientId, String clientName, String clientSecretHash, OAuthClientStatus status,
                                      Set<String> grantTypes, Set<String> scopes, Set<String> resourcePatterns,
                                      long accessTokenTtlSeconds, long refreshTokenTtlSeconds) {
        return new OAuthClient(clientId, clientName, clientSecretHash, status, grantTypes, scopes, resourcePatterns,
                accessTokenTtlSeconds, refreshTokenTtlSeconds);
    }

    /**
     * 判断客户端是否支持授权类型。
     *
     * @param grantType 授权类型
     * @return true 表示支持
     */
    public boolean supportsGrantType(String grantType) {
        return grantTypes.contains(grantType);
    }

    /**
     * 判断客户端是否可用。
     *
     * @return true 表示可用
     */
    public boolean isActive() {
        return status == OAuthClientStatus.ACTIVE;
    }

    /**
     * 判断客户端是否需要密钥。
     *
     * @return true 表示需要密钥
     */
    public boolean requiresSecret() {
        return clientSecretHash != null && !clientSecretHash.isBlank();
    }

    /**
     * 获取客户端 ID。
     *
     * @return 客户端 ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 获取客户端名称。
     *
     * @return 客户端名称
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * 获取客户端密钥哈希。
     *
     * @return 客户端密钥哈希
     */
    public String getClientSecretHash() {
        return clientSecretHash;
    }

    /**
     * 获取状态。
     *
     * @return 状态
     */
    public OAuthClientStatus getStatus() {
        return status;
    }

    /**
     * 获取授权类型。
     *
     * @return 授权类型
     */
    public Set<String> getGrantTypes() {
        return grantTypes;
    }

    /**
     * 获取授权范围。
     *
     * @return 授权范围
     */
    public Set<String> getScopes() {
        return scopes;
    }

    /**
     * 获取可访问资源模式。
     *
     * @return 可访问资源模式
     */
    public Set<String> getResourcePatterns() {
        return resourcePatterns;
    }

    /**
     * 获取 Access Token 有效期秒数。
     *
     * @return Access Token 有效期秒数
     */
    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }

    /**
     * 获取 Refresh Token 有效期秒数。
     *
     * @return Refresh Token 有效期秒数
     */
    public long getRefreshTokenTtlSeconds() {
        return refreshTokenTtlSeconds;
    }
}
