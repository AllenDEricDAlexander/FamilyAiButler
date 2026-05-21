/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @FileName: MpTokenGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:40
 * @Description: MyBatis Plus Token 网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gatewayimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.stereotype.Repository;
import top.egon.familyaibutler.uaa.domain.gateway.TokenGateway;
import top.egon.familyaibutler.uaa.domain.model.entity.TokenRecord;
import top.egon.familyaibutler.uaa.domain.model.enums.TokenStatus;
import top.egon.familyaibutler.uaa.domain.model.valueobject.TokenClaims;
import top.egon.familyaibutler.uaa.domain.service.TokenDomainService;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.converter.UaaMpConverter;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.AccessTokenPO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.RefreshTokenPO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper.AccessTokenMapper;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper.RefreshTokenMapper;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @ClassName: MpTokenGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:40
 * @Description: MyBatis Plus Token 网关实现
 * @Version: 1.0
 */
@Repository
public class MpTokenGatewayImpl implements TokenGateway {
    private final AccessTokenMapper accessTokenMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final UaaMpConverter uaaMpConverter;
    private final TokenDomainService tokenDomainService;

    /**
     * 创建 MyBatis Plus Token 网关实现。
     *
     * @param accessTokenMapper  访问令牌 Mapper
     * @param refreshTokenMapper 刷新令牌 Mapper
     * @param uaaMpConverter     UAA 转换器
     * @param tokenDomainService Token 领域服务
     */
    public MpTokenGatewayImpl(AccessTokenMapper accessTokenMapper, RefreshTokenMapper refreshTokenMapper,
                              UaaMpConverter uaaMpConverter, TokenDomainService tokenDomainService) {
        this.accessTokenMapper = accessTokenMapper;
        this.refreshTokenMapper = refreshTokenMapper;
        this.uaaMpConverter = uaaMpConverter;
        this.tokenDomainService = tokenDomainService;
    }

    /**
     * 保存访问令牌声明。
     *
     * @param accessToken 访问令牌
     * @param claims      令牌声明
     */
    @Override
    public void saveAccessTokenClaims(String accessToken, TokenClaims claims) {
        String accessTokenHash = tokenDomainService.hashToken(accessToken);
        AccessTokenPO tokenPO = uaaMpConverter.toAccessTokenPO(accessTokenHash, claims);
        if (accessTokenMapper.selectById(accessTokenHash) == null) {
            accessTokenMapper.insert(tokenPO);
        } else {
            accessTokenMapper.updateById(tokenPO);
        }
    }

    /**
     * 查询访问令牌声明。
     *
     * @param accessToken 访问令牌
     * @return 令牌声明
     */
    @Override
    public Optional<TokenClaims> findAccessTokenClaims(String accessToken) {
        String accessTokenHash = tokenDomainService.hashToken(accessToken);
        return Optional.ofNullable(accessTokenMapper.selectById(accessTokenHash)).map(uaaMpConverter::toTokenClaims);
    }

    /**
     * 保存刷新令牌记录。
     *
     * @param tokenRecord 刷新令牌记录
     * @return 保存后的刷新令牌记录
     */
    @Override
    public TokenRecord saveRefreshToken(TokenRecord tokenRecord) {
        RefreshTokenPO tokenPO = uaaMpConverter.toRefreshTokenPO(tokenRecord);
        if (refreshTokenMapper.selectById(tokenRecord.getTokenId()) == null) {
            refreshTokenMapper.insert(tokenPO);
        } else {
            refreshTokenMapper.updateById(tokenPO);
        }
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
        LambdaQueryWrapper<RefreshTokenPO> wrapper = new LambdaQueryWrapper<RefreshTokenPO>()
                .eq(RefreshTokenPO::getTokenHash, tokenHash)
                .last("limit 1");
        return Optional.ofNullable(refreshTokenMapper.selectOne(wrapper)).map(uaaMpConverter::toTokenRecord);
    }

    /**
     * 按哈希撤销刷新令牌。
     *
     * @param tokenHash Token 哈希
     * @return true 表示撤销成功
     */
    @Override
    public boolean revokeRefreshTokenByHash(String tokenHash) {
        return findRefreshTokenByHash(tokenHash)
                .map(token -> {
                    token.revoke();
                    saveRefreshToken(token);
                    return true;
                })
                .orElse(false);
    }

    /**
     * 按账号撤销 Token。
     *
     * @param accountId 账号 ID
     * @return true 表示撤销成功
     */
    @Override
    public boolean revokeByAccountId(String accountId) {
        revokeRefreshTokens(RefreshTokenPO::getAccountId, accountId);
        deleteAccessTokens(AccessTokenPO::getAccountId, accountId);
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
        revokeRefreshTokens(RefreshTokenPO::getDeviceId, deviceId);
        deleteAccessTokens(AccessTokenPO::getDeviceId, deviceId);
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
        revokeRefreshTokens(RefreshTokenPO::getSessionId, sessionId);
        deleteAccessTokens(AccessTokenPO::getSessionId, sessionId);
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
        revokeRefreshTokens(RefreshTokenPO::getClientId, clientId);
        deleteAccessTokens(AccessTokenPO::getClientId, clientId);
        return true;
    }

    /**
     * 撤销刷新令牌列表。
     *
     * @param column 查询列
     * @param value  查询值
     */
    private void revokeRefreshTokens(SFunction<RefreshTokenPO, ?> column, String value) {
        LambdaQueryWrapper<RefreshTokenPO> wrapper = new LambdaQueryWrapper<RefreshTokenPO>().eq(column, value);
        refreshTokenMapper.selectList(wrapper).forEach(tokenPO -> {
            tokenPO.setStatus(TokenStatus.REVOKED.name());
            refreshTokenMapper.updateById(tokenPO);
        });
    }

    /**
     * 删除访问令牌列表。
     *
     * @param column 查询列
     * @param value  查询值
     */
    private void deleteAccessTokens(SFunction<AccessTokenPO, ?> column, String value) {
        LambdaQueryWrapper<AccessTokenPO> wrapper = new LambdaQueryWrapper<AccessTokenPO>().eq(column, value);
        accessTokenMapper.delete(wrapper);
    }
}
