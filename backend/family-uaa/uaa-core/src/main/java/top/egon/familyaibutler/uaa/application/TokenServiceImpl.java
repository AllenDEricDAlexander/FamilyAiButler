/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @FileName: TokenServiceImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: Token 应用服务实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application;

import org.springframework.stereotype.Service;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtClaims;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtService;
import top.egon.familyaibutler.uaa.domain.gateway.SessionGateway;
import top.egon.familyaibutler.uaa.domain.gateway.TokenGateway;
import top.egon.familyaibutler.uaa.domain.model.aggregate.AuthSession;
import top.egon.familyaibutler.uaa.domain.model.entity.TokenRecord;
import top.egon.familyaibutler.uaa.domain.model.valueobject.TokenClaims;
import top.egon.familyaibutler.uaa.domain.service.TokenDomainService;
import top.egon.familyaibutler.uaa.facade.TokenFacade;
import top.egon.familyaibutler.uaa.facade.dto.token.RefreshTokenRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.RevokeTokenRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenPairResponse;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenValidationRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenValidationResponse;

import java.time.Instant;
import java.util.UUID;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @ClassName: TokenServiceImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Token 应用服务实现
 * @Version: 1.0
 */
@Service
public class TokenServiceImpl implements TokenServiceI, TokenFacade {
    private static final long ACCESS_TOKEN_EXPIRES_IN = 300L;
    private static final long REFRESH_TOKEN_EXPIRES_IN = 2592000L;

    private final TokenGateway tokenGateway;
    private final SessionGateway sessionGateway;
    private final TokenDomainService tokenDomainService;
    private final FamilyJwtService familyJwtService;

    /**
     * 创建 Token 应用服务实现。
     *
     * @param tokenGateway       Token 网关
     * @param sessionGateway     会话网关
     * @param tokenDomainService Token 领域服务
     * @param familyJwtService   统一 JWT 服务
     */
    public TokenServiceImpl(TokenGateway tokenGateway, SessionGateway sessionGateway, TokenDomainService tokenDomainService,
                            FamilyJwtService familyJwtService) {
        this.tokenGateway = tokenGateway;
        this.sessionGateway = sessionGateway;
        this.tokenDomainService = tokenDomainService;
        this.familyJwtService = familyJwtService;
    }

    /**
     * 刷新访问令牌。
     *
     * @param request 刷新请求
     * @return 令牌对
     */
    @Override
    public TokenPairResponse refreshAccessToken(RefreshTokenRequest request) {
        String tokenHash = tokenDomainService.hashToken(request.refreshToken());
        TokenRecord refreshToken = tokenGateway.findRefreshTokenByHash(tokenHash)
                .filter(TokenRecord::isActive)
                .orElseThrow(() -> new IllegalArgumentException("刷新令牌无效"));
        AuthSession session = sessionGateway.findBySessionId(refreshToken.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("会话不存在"));
        Instant accessTokenExpiresAt = Instant.now().plusSeconds(ACCESS_TOKEN_EXPIRES_IN);
        String accessToken = familyJwtService.createAccessToken(new FamilyJwtClaims("jwt_" + UUID.randomUUID(),
                refreshToken.getAccountId(), session.getProfileId(), refreshToken.getClientId(), refreshToken.getSessionId(),
                refreshToken.getDeviceId(), 0L, 0L, "NORMAL", "family-uaa", "family-api", accessTokenExpiresAt));
        TokenClaims claims = new TokenClaims(refreshToken.getAccountId(), session.getProfileId(), refreshToken.getClientId(),
                refreshToken.getSessionId(), refreshToken.getDeviceId(), 0L, 0L, "NORMAL",
                accessTokenExpiresAt);
        tokenGateway.saveAccessTokenClaims(accessToken, claims);
        return new TokenPairResponse(accessToken, request.refreshToken(), ACCESS_TOKEN_EXPIRES_IN, REFRESH_TOKEN_EXPIRES_IN,
                "Bearer", refreshToken.getAccountId(), session.getProfileId(), refreshToken.getSessionId(), refreshToken.getDeviceId());
    }

    /**
     * 校验访问令牌。
     *
     * @param request 校验请求
     * @return 校验结果
     */
    @Override
    public TokenValidationResponse validateAccessToken(TokenValidationRequest request) {
        if (request.accessToken() == null || request.accessToken().isBlank()) {
            return new TokenValidationResponse(false, null, null, null, null, 0L, 0L, "EMPTY_TOKEN");
        }
        String accessToken = familyJwtService.resolveAuthorizationToken(request.accessToken());
        if (familyJwtService.parseAccessJwtClaims(accessToken).isEmpty()) {
            return new TokenValidationResponse(false, null, null, null, null, 0L, 0L, "TOKEN_INVALID");
        }
        return tokenGateway.findAccessTokenClaims(accessToken)
                .map(this::toValidationResponse)
                .orElseGet(() -> new TokenValidationResponse(false, null, null, null, null, 0L, 0L, "TOKEN_NOT_FOUND"));
    }

    /**
     * 撤销单个 Token。
     *
     * @param request 撤销请求
     * @return true 表示撤销成功
     */
    @Override
    public boolean revokeToken(RevokeTokenRequest request) {
        if (request.token() != null && !request.token().isBlank()) {
            return tokenGateway.revokeRefreshTokenByHash(tokenDomainService.hashToken(request.token()));
        }
        if (request.accountId() != null && !request.accountId().isBlank()) {
            return tokenGateway.revokeByAccountId(request.accountId());
        }
        if (request.deviceId() != null && !request.deviceId().isBlank()) {
            return tokenGateway.revokeByDeviceId(request.deviceId());
        }
        if (request.clientId() != null && !request.clientId().isBlank()) {
            return tokenGateway.revokeByClientId(request.clientId());
        }
        return false;
    }

    /**
     * 按账号撤销 Token。
     *
     * @param accountId 账号 ID
     * @return true 表示撤销成功
     */
    @Override
    public boolean revokeAccountTokens(String accountId) {
        return tokenGateway.revokeByAccountId(accountId);
    }

    /**
     * 按设备撤销 Token。
     *
     * @param deviceId 设备 ID
     * @return true 表示撤销成功
     */
    @Override
    public boolean revokeDeviceTokens(String deviceId) {
        return tokenGateway.revokeByDeviceId(deviceId);
    }

    /**
     * 转换 Token 校验响应。
     *
     * @param claims Token 声明
     * @return Token 校验响应
     */
    private TokenValidationResponse toValidationResponse(TokenClaims claims) {
        if (claims.isExpired()) {
            return new TokenValidationResponse(false, claims.accountId(), claims.profileId(), claims.sessionId(),
                    claims.deviceId(), claims.authVersion(), claims.entitlementVersion(), "TOKEN_EXPIRED");
        }
        return new TokenValidationResponse(true, claims.accountId(), claims.profileId(), claims.sessionId(),
                claims.deviceId(), claims.authVersion(), claims.entitlementVersion(), "VALID");
    }
}
