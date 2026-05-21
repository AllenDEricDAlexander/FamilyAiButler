/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @FileName: InMemoryTokenGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: 内存 Token 网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gatewayimpl;

import top.egon.familyaibutler.uaa.domain.gateway.TokenGateway;
import top.egon.familyaibutler.uaa.domain.model.entity.TokenRecord;
import top.egon.familyaibutler.uaa.domain.model.valueobject.TokenClaims;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @ClassName: InMemoryTokenGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: 内存 Token 网关实现
 * @Version: 1.0
 */
public class InMemoryTokenGatewayImpl implements TokenGateway {
    private final Map<String, TokenClaims> accessTokenClaims = new ConcurrentHashMap<>();
    private final Map<String, TokenRecord> refreshTokens = new ConcurrentHashMap<>();

    /**
     * 保存访问令牌声明。
     *
     * @param accessToken 访问令牌
     * @param claims      令牌声明
     */
    @Override
    public void saveAccessTokenClaims(String accessToken, TokenClaims claims) {
        accessTokenClaims.put(accessToken, claims);
    }

    /**
     * 查询访问令牌声明。
     *
     * @param accessToken 访问令牌
     * @return 令牌声明
     */
    @Override
    public Optional<TokenClaims> findAccessTokenClaims(String accessToken) {
        return Optional.ofNullable(accessTokenClaims.get(accessToken));
    }

    /**
     * 保存刷新令牌记录。
     *
     * @param tokenRecord 刷新令牌记录
     * @return 保存后的刷新令牌记录
     */
    @Override
    public TokenRecord saveRefreshToken(TokenRecord tokenRecord) {
        refreshTokens.put(tokenRecord.getTokenHash(), tokenRecord);
        return tokenRecord;
    }

    /**
     * 按哈希查询刷新令牌记录。
     *
     * @param tokenHash Token 哈希
     * @return 刷新令牌记录
     */
    @Override
    public Optional<TokenRecord> findRefreshTokenByHash(String tokenHash) {
        return Optional.ofNullable(refreshTokens.get(tokenHash));
    }

    /**
     * 按哈希撤销刷新令牌。
     *
     * @param tokenHash Token 哈希
     * @return true 表示撤销成功
     */
    @Override
    public boolean revokeRefreshTokenByHash(String tokenHash) {
        return findRefreshTokenByHash(tokenHash).map(token -> {
            token.revoke();
            return true;
        }).orElse(false);
    }

    /**
     * 按账号撤销 Token。
     *
     * @param accountId 账号 ID
     * @return true 表示撤销成功
     */
    @Override
    public boolean revokeByAccountId(String accountId) {
        refreshTokens.values().stream()
                .filter(token -> token.getAccountId().equals(accountId))
                .forEach(TokenRecord::revoke);
        accessTokenClaims.entrySet().removeIf(entry -> entry.getValue().accountId().equals(accountId));
        return true;
    }

    /**
     * 按设备撤销 Token。
     *
     * @param deviceId 设备 ID
     * @return true 表示撤销成功
     */
    @Override
    public boolean revokeByDeviceId(String deviceId) {
        refreshTokens.values().stream()
                .filter(token -> token.getDeviceId().equals(deviceId))
                .forEach(TokenRecord::revoke);
        accessTokenClaims.entrySet().removeIf(entry -> entry.getValue().deviceId().equals(deviceId));
        return true;
    }

    /**
     * 按会话撤销 Token。
     *
     * @param sessionId 会话 ID
     * @return true 表示撤销成功
     */
    @Override
    public boolean revokeBySessionId(String sessionId) {
        refreshTokens.values().stream()
                .filter(token -> token.getSessionId().equals(sessionId))
                .forEach(TokenRecord::revoke);
        accessTokenClaims.entrySet().removeIf(entry -> entry.getValue().sessionId().equals(sessionId));
        return true;
    }

    /**
     * 按客户端撤销 Token。
     *
     * @param clientId 客户端 ID
     * @return true 表示撤销成功
     */
    @Override
    public boolean revokeByClientId(String clientId) {
        refreshTokens.values().stream()
                .filter(token -> token.getClientId().equals(clientId))
                .forEach(TokenRecord::revoke);
        accessTokenClaims.entrySet().removeIf(entry -> entry.getValue().clientId().equals(clientId));
        return true;
    }
}
