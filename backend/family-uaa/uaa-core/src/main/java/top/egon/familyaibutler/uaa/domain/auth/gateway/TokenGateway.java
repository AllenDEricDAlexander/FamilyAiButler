/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.auth.gateway
 * @FileName: TokenGateway.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: Token 领域网关文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.auth.gateway;

import top.egon.familyaibutler.uaa.domain.auth.model.entity.TokenRecord;
import top.egon.familyaibutler.uaa.domain.auth.model.valueobject.TokenClaims;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.auth.gateway
 * @ClassName: TokenGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: Token 领域网关
 * @Version: 1.0
 */
public interface TokenGateway {

    /**
     * 保存访问令牌声明。
     *
     * @param accessToken 访问令牌
     * @param claims      令牌声明
     */
    void saveAccessTokenClaims(String accessToken, TokenClaims claims);

    /**
     * 查询访问令牌声明。
     *
     * @param accessToken 访问令牌
     * @return 令牌声明
     */
    Optional<TokenClaims> findAccessTokenClaims(String accessToken);

    /**
     * 保存刷新令牌记录。
     *
     * @param tokenRecord 刷新令牌记录
     * @return 保存后的刷新令牌记录
     */
    TokenRecord saveRefreshToken(TokenRecord tokenRecord);

    /**
     * 按哈希查询刷新令牌记录。
     *
     * @param tokenHash Token 哈希
     * @return 刷新令牌记录
     */
    Optional<TokenRecord> findRefreshTokenByHash(String tokenHash);

    /**
     * 按哈希撤销刷新令牌。
     *
     * @param tokenHash Token 哈希
     * @return true 表示撤销成功
     */
    boolean revokeRefreshTokenByHash(String tokenHash);

    /**
     * 按账号撤销 Token。
     *
     * @param accountId 账号 ID
     * @return true 表示撤销成功
     */
    boolean revokeByAccountId(String accountId);

    /**
     * 按设备撤销 Token。
     *
     * @param deviceId 设备 ID
     * @return true 表示撤销成功
     */
    boolean revokeByDeviceId(String deviceId);

    /**
     * 按会话撤销 Token。
     *
     * @param sessionId 会话 ID
     * @return true 表示撤销成功
     */
    boolean revokeBySessionId(String sessionId);

    /**
     * 按客户端撤销 Token。
     *
     * @param clientId 客户端 ID
     * @return true 表示撤销成功
     */
    boolean revokeByClientId(String clientId);
}
