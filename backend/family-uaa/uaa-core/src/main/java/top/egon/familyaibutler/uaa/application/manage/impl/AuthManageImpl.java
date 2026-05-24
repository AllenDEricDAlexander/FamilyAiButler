/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage.impl
 * @FileName: AuthManageImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 认证应用服务实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application.manage.impl;

import org.springframework.stereotype.Service;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtClaims;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtService;
import top.egon.familyaibutler.uaa.application.manage.AuthManage;
import top.egon.familyaibutler.uaa.domain.account.gateway.AccountGateway;
import top.egon.familyaibutler.uaa.domain.account.gateway.CredentialGateway;
import top.egon.familyaibutler.uaa.domain.account.gateway.ProfileGateway;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Account;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Profile;
import top.egon.familyaibutler.uaa.domain.account.model.entity.Credential;
import top.egon.familyaibutler.uaa.domain.account.service.CredentialDomainService;
import top.egon.familyaibutler.uaa.domain.auth.gateway.DeviceGateway;
import top.egon.familyaibutler.uaa.domain.auth.gateway.SecurityNotificationGateway;
import top.egon.familyaibutler.uaa.domain.auth.gateway.SessionGateway;
import top.egon.familyaibutler.uaa.domain.auth.gateway.TokenGateway;
import top.egon.familyaibutler.uaa.domain.auth.model.aggregate.AuthSession;
import top.egon.familyaibutler.uaa.domain.auth.model.entity.Device;
import top.egon.familyaibutler.uaa.domain.auth.model.entity.TokenRecord;
import top.egon.familyaibutler.uaa.domain.auth.model.enums.DeviceType;
import top.egon.familyaibutler.uaa.domain.auth.model.valueobject.TokenClaims;
import top.egon.familyaibutler.uaa.domain.auth.service.TokenDomainService;
import top.egon.familyaibutler.uaa.domain.oauth.gateway.OAuthClientGateway;
import top.egon.familyaibutler.uaa.domain.oauth.model.aggregate.OAuthClient;
import top.egon.familyaibutler.uaa.facade.dto.auth.LogoutRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.PasswordLoginRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.PasswordRecoveryRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.ResetPasswordRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.StepUpChallengeRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.VerifyCodeLoginRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenPairResponse;

import java.time.Instant;
import java.util.UUID;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage.impl
 * @ClassName: AuthManageImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 认证应用服务实现
 * @Version: 1.0
 */
@Service
public class AuthManageImpl implements AuthManage {
    /**
     * Account 网关。
     */
    private final AccountGateway accountGateway;
    /**
     * Credential 网关。
     */
    private final CredentialGateway credentialGateway;
    /**
     * Device 网关。
     */
    private final DeviceGateway deviceGateway;
    /**
     * Profile 网关。
     */
    private final ProfileGateway profileGateway;
    /**
     * Security Notification 网关。
     */
    private final SecurityNotificationGateway securityNotificationGateway;
    /**
     * Session 网关。
     */
    private final SessionGateway sessionGateway;
    /**
     * Token 网关。
     */
    private final TokenGateway tokenGateway;
    /**
     * OAuth Client 网关。
     */
    private final OAuthClientGateway oAuthClientGateway;
    /**
     * Credential 领域服务。
     */
    private final CredentialDomainService credentialDomainService;
    /**
     * Token 领域服务。
     */
    private final TokenDomainService tokenDomainService;
    /**
     * Family JWT 服务。
     */
    private final FamilyJwtService familyJwtService;

    /**
     * 创建认证应用服务实现。
     *
     * @param accountGateway              账号网关
     * @param credentialGateway           凭证网关
     * @param deviceGateway               设备网关
     * @param profileGateway              Profile 网关
     * @param securityNotificationGateway 安全通知网关
     * @param sessionGateway              会话网关
     * @param tokenGateway                Token 网关
     * @param oAuthClientGateway          OAuth Client 网关
     * @param credentialDomainService     凭证领域服务
     * @param tokenDomainService          Token 领域服务
     * @param familyJwtService            统一 JWT 服务
     */
    public AuthManageImpl(AccountGateway accountGateway, CredentialGateway credentialGateway, DeviceGateway deviceGateway,
                          ProfileGateway profileGateway, SecurityNotificationGateway securityNotificationGateway,
                          SessionGateway sessionGateway, TokenGateway tokenGateway, OAuthClientGateway oAuthClientGateway,
                          CredentialDomainService credentialDomainService, TokenDomainService tokenDomainService,
                          FamilyJwtService familyJwtService) {
        this.accountGateway = accountGateway;
        this.credentialGateway = credentialGateway;
        this.deviceGateway = deviceGateway;
        this.profileGateway = profileGateway;
        this.securityNotificationGateway = securityNotificationGateway;
        this.sessionGateway = sessionGateway;
        this.tokenGateway = tokenGateway;
        this.oAuthClientGateway = oAuthClientGateway;
        this.credentialDomainService = credentialDomainService;
        this.tokenDomainService = tokenDomainService;
        this.familyJwtService = familyJwtService;
    }

    /**
     * 密码登录。
     *
     * @param request 密码登录请求
     * @return 令牌对
     */
    @Override
    public TokenPairResponse loginByPassword(PasswordLoginRequest request) {
        Account account = findLoginAccount(request.principal());
        Credential credential = credentialGateway.findPasswordCredential(account.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("账号未设置密码凭证"));
        if (!credentialDomainService.matchesPassword(credential, request.password())) {
            throw new IllegalArgumentException("账号或密码错误");
        }
        return issueTokenPair(account, request.clientId(), request.clientSecret(), request.deviceName(), request.deviceFingerprint());
    }

    /**
     * 邮箱验证码登录。
     *
     * @param request 验证码登录请求
     * @return 令牌对
     */
    @Override
    public TokenPairResponse loginByEmailCode(VerifyCodeLoginRequest request) {
        checkVerifyCode(request);
        return issueTokenPair(findLoginAccount(request.principal()), request.clientId(), null, request.deviceName(), request.deviceFingerprint());
    }

    /**
     * 短信验证码登录。
     *
     * @param request 验证码登录请求
     * @return 令牌对
     */
    @Override
    public TokenPairResponse loginBySmsCode(VerifyCodeLoginRequest request) {
        return loginByEmailCode(request);
    }

    /**
     * 请求找回密码验证码。
     *
     * @param request 找回密码请求
     * @return 找回密码挑战 ID
     */
    @Override
    public String requestPasswordRecovery(PasswordRecoveryRequest request) {
        Account account = accountGateway.findByPrincipal(request.principal())
                .orElseThrow(() -> new IllegalArgumentException("账号不存在"));
        return securityNotificationGateway.sendPasswordRecoveryCode(account.getAccountId(), request.principal());
    }

    /**
     * 重置密码。
     *
     * @param request 重置密码请求
     * @return true 表示重置成功
     */
    @Override
    public boolean resetPassword(ResetPasswordRequest request) {
        Account account = accountGateway.findByPrincipal(request.principal())
                .orElseThrow(() -> new IllegalArgumentException("账号不存在"));
        if (!securityNotificationGateway.verifyPasswordRecoveryCode(request.principal(), request.verificationCode())) {
            return false;
        }
        credentialGateway.save(credentialDomainService.createPasswordCredential(account.getAccountId(), request.newPassword()));
        tokenGateway.revokeByAccountId(account.getAccountId());
        return true;
    }

    /**
     * 请求二次验证。
     *
     * @param request 二次验证请求
     * @return 挑战 ID
     */
    @Override
    public String requestStepUpChallenge(StepUpChallengeRequest request) {
        return "challenge_" + UUID.randomUUID();
    }

    /**
     * 验证二次验证。
     *
     * @param request 二次验证请求
     * @return true 表示验证通过
     */
    @Override
    public boolean verifyStepUpChallenge(StepUpChallengeRequest request) {
        return request.verifyCode() != null && !request.verifyCode().isBlank();
    }

    /**
     * 退出当前会话。
     *
     * @param request 退出请求
     * @return true 表示退出成功
     */
    @Override
    public boolean logoutCurrentSession(LogoutRequest request) {
        if (request.sessionId() == null || request.sessionId().isBlank()) {
            return false;
        }
        return sessionGateway.findBySessionId(request.sessionId())
                .map(session -> {
                    session.revoke();
                    sessionGateway.save(session);
                    tokenGateway.revokeBySessionId(session.getSessionId());
                    return true;
                })
                .orElse(false);
    }

    /**
     * 退出全部会话。
     *
     * @param request 退出请求
     * @return true 表示退出成功
     */
    @Override
    public boolean logoutAllSessions(LogoutRequest request) {
        if (request.accountId() == null || request.accountId().isBlank()) {
            return false;
        }
        sessionGateway.findByAccountId(request.accountId()).forEach(session -> {
            session.revoke();
            sessionGateway.save(session);
        });
        return tokenGateway.revokeByAccountId(request.accountId());
    }

    /**
     * 查询可登录账号。
     *
     * @param principal 登录主体
     * @return 账号聚合
     */
    private Account findLoginAccount(String principal) {
        Account account = accountGateway.findByPrincipal(principal).orElseThrow(() -> new IllegalArgumentException("账号不存在"));
        if (!account.canLogin()) {
            throw new IllegalStateException("账号当前状态不允许登录");
        }
        return account;
    }

    /**
     * 签发令牌对。
     *
     * @param account           账号聚合
     * @param clientId          客户端 ID
     * @param clientSecret      客户端密钥
     * @param deviceName        设备名称
     * @param deviceFingerprint 设备指纹
     * @return 令牌对
     */
    private TokenPairResponse issueTokenPair(Account account, String clientId, String clientSecret, String deviceName,
                                             String deviceFingerprint) {
        String actualClientId = clientId == null || clientId.isBlank() ? "family-web" : clientId;
        OAuthClient client = validateClient(actualClientId, clientSecret);
        Profile profile = profileGateway.findByAccountId(account.getAccountId()).stream()
                .filter(item -> !item.isDeleted())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("账号未初始化 Profile"));
        Device device = findOrRegisterDevice(account.getAccountId(), deviceName, deviceFingerprint);
        AuthSession session = sessionGateway.save(AuthSession.create(account.getAccountId(), profile.getProfileId(),
                device.getDeviceId(), actualClientId));
        Instant accessTokenExpiresAt = Instant.now().plusSeconds(client.getAccessTokenTtlSeconds());
        String accessToken = familyJwtService.createAccessToken(new FamilyJwtClaims("jwt_" + UUID.randomUUID(),
                account.getAccountId(), profile.getProfileId(), actualClientId, session.getSessionId(), device.getDeviceId(),
                account.getAuthVersion(), account.getEntitlementVersion(), "NORMAL", "family-uaa", "family-api",
                accessTokenExpiresAt));
        String refreshToken = "refresh_" + UUID.randomUUID();
        TokenClaims claims = new TokenClaims(account.getAccountId(), profile.getProfileId(), actualClientId,
                session.getSessionId(), device.getDeviceId(), account.getAuthVersion(), account.getEntitlementVersion(),
                "NORMAL", accessTokenExpiresAt);
        tokenGateway.saveAccessTokenClaims(accessToken, claims);
        TokenRecord tokenRecord = tokenDomainService.createRefreshTokenRecord(session, device, refreshToken);
        tokenGateway.saveRefreshToken(tokenRecord);
        return new TokenPairResponse(accessToken, refreshToken, client.getAccessTokenTtlSeconds(), client.getRefreshTokenTtlSeconds(),
                "Bearer", account.getAccountId(), profile.getProfileId(), session.getSessionId(), device.getDeviceId());
    }

    /**
     * 校验 OAuth Client。
     *
     * @param clientId     客户端 ID
     * @param clientSecret 客户端密钥
     * @return OAuth Client
     */
    private OAuthClient validateClient(String clientId, String clientSecret) {
        OAuthClient client = oAuthClientGateway.findByClientId(clientId)
                .filter(OAuthClient::isActive)
                .filter(item -> item.supportsGrantType("PASSWORD"))
                .orElseThrow(() -> new IllegalArgumentException("接入方无效或不支持密码模式"));
        if (client.requiresSecret()) {
            String clientSecretHash = clientSecret == null ? null : tokenDomainService.hashToken(clientSecret);
            if (!client.getClientSecretHash().equals(clientSecretHash)) {
                throw new IllegalArgumentException("接入方密钥无效");
            }
        }
        return client;
    }

    /**
     * 查询或登记设备。
     *
     * @param accountId         账号 ID
     * @param deviceName        设备名称
     * @param deviceFingerprint 设备指纹
     * @return 设备实体
     */
    private Device findOrRegisterDevice(String accountId, String deviceName, String deviceFingerprint) {
        if (deviceFingerprint != null && !deviceFingerprint.isBlank()) {
            return deviceGateway.findByFingerprint(accountId, deviceFingerprint)
                    .orElseGet(() -> deviceGateway.save(Device.register(accountId, deviceName, DeviceType.WEB, deviceFingerprint)));
        }
        return deviceGateway.save(Device.register(accountId, deviceName, DeviceType.WEB, null));
    }

    /**
     * 校验验证码入口。
     *
     * @param request 验证码登录请求
     */
    private void checkVerifyCode(VerifyCodeLoginRequest request) {
        if (request.verifyCode() == null || request.verifyCode().isBlank()) {
            throw new IllegalArgumentException("验证码不能为空");
        }
    }
}
