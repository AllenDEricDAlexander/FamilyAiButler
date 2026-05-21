/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @FileName: AuthTokenServiceTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 认证令牌应用服务测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtService;
import top.egon.familyaibutler.uaa.application.command.account.RegisterAccountCommand;
import top.egon.familyaibutler.uaa.application.executor.command.AccountCommandExe;
import top.egon.familyaibutler.uaa.application.manage.impl.AuthManageImpl;
import top.egon.familyaibutler.uaa.application.manage.impl.TokenManageImpl;
import top.egon.familyaibutler.uaa.domain.account.service.AccountDomainService;
import top.egon.familyaibutler.uaa.domain.account.service.CredentialDomainService;
import top.egon.familyaibutler.uaa.domain.auth.model.aggregate.AuthSession;
import top.egon.familyaibutler.uaa.domain.auth.model.entity.Device;
import top.egon.familyaibutler.uaa.domain.auth.model.entity.TokenRecord;
import top.egon.familyaibutler.uaa.domain.auth.model.enums.DeviceType;
import top.egon.familyaibutler.uaa.domain.auth.model.enums.SessionStatus;
import top.egon.familyaibutler.uaa.domain.auth.model.enums.TokenStatus;
import top.egon.familyaibutler.uaa.domain.auth.service.TokenDomainService;
import top.egon.familyaibutler.uaa.domain.oauth.model.aggregate.OAuthClient;
import top.egon.familyaibutler.uaa.facade.dto.auth.PasswordLoginRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.PasswordRecoveryRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.ResetPasswordRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.RefreshTokenRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenPairResponse;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenValidationRequest;
import top.egon.familyaibutler.uaa.infrastructure.gateway.impl.InMemoryAccountGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gateway.impl.InMemoryCredentialGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gateway.impl.InMemoryDeviceGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gateway.impl.InMemoryOAuthClientGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gateway.impl.InMemoryProfileGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gateway.impl.InMemorySecurityNotificationGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gateway.impl.InMemorySessionGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gateway.impl.InMemoryTokenGatewayImpl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @ClassName: AuthTokenServiceTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 认证令牌应用服务测试
 * @Version: 1.0
 */
class AuthTokenServiceTest {

    /**
     * 校验令牌领域服务签发访问令牌和刷新令牌记录。
     */
    @Test
    void issueTokenPairShouldCreateSessionDeviceAndRefreshRecord() {
        TokenDomainService tokenDomainService = new TokenDomainService();
        Device device = Device.register("acc_1", "Chrome", DeviceType.WEB, "fingerprint");
        AuthSession session = AuthSession.create("acc_1", "prof_1", device.getDeviceId(), "web-app");

        TokenRecord tokenRecord = tokenDomainService.createRefreshTokenRecord(session, device, "refresh-token");

        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(device.getAccountId()).isEqualTo("acc_1");
        assertThat(tokenRecord.getStatus()).isEqualTo(TokenStatus.ACTIVE);
        assertThat(tokenRecord.getSessionId()).isEqualTo(session.getSessionId());
        assertThat(tokenRecord.getDeviceId()).isEqualTo(device.getDeviceId());
        assertThat(tokenRecord.getTokenHash()).isNotEqualTo("refresh-token");
    }

    /**
     * 校验撤销刷新令牌后令牌不可继续使用。
     */
    @Test
    void revokedTokenShouldNotBeActive() {
        TokenDomainService tokenDomainService = new TokenDomainService();
        Device device = Device.register("acc_1", "Chrome", DeviceType.WEB, "fingerprint");
        AuthSession session = AuthSession.create("acc_1", "prof_1", device.getDeviceId(), "web-app");
        TokenRecord tokenRecord = tokenDomainService.createRefreshTokenRecord(session, device, "refresh-token");

        tokenRecord.revoke();

        assertThat(tokenRecord.isActive()).isFalse();
        assertThat(tokenRecord.getStatus()).isEqualTo(TokenStatus.REVOKED);
    }

    /**
     * 校验密码登录、访问令牌校验和刷新令牌流程。
     */
    @Test
    void passwordLoginShouldIssueValidateAndRefreshToken() {
        InMemoryAccountGatewayImpl accountGateway = new InMemoryAccountGatewayImpl();
        InMemoryCredentialGatewayImpl credentialGateway = new InMemoryCredentialGatewayImpl();
        InMemoryProfileGatewayImpl profileGateway = new InMemoryProfileGatewayImpl();
        InMemorySecurityNotificationGatewayImpl securityNotificationGateway = new InMemorySecurityNotificationGatewayImpl();
        InMemoryDeviceGatewayImpl deviceGateway = new InMemoryDeviceGatewayImpl();
        InMemorySessionGatewayImpl sessionGateway = new InMemorySessionGatewayImpl();
        InMemoryTokenGatewayImpl tokenGateway = new InMemoryTokenGatewayImpl();
        InMemoryOAuthClientGatewayImpl oAuthClientGateway = new InMemoryOAuthClientGatewayImpl();
        CredentialDomainService credentialDomainService = new CredentialDomainService();
        TokenDomainService tokenDomainService = new TokenDomainService();
        FamilyJwtService familyJwtService = new FamilyJwtService(jwtProperties());
        AccountCommandExe accountCommandService = new AccountCommandExe(new AccountDomainService(), credentialDomainService,
                accountGateway, credentialGateway, profileGateway);
        oAuthClientGateway.save(OAuthClient.createPublicClient("family-web", "Family Web", Set.of("PASSWORD", "REFRESH_TOKEN"),
                Set.of("openid", "profile"), Set.of("/**")));
        AuthManageImpl authService = new AuthManageImpl(accountGateway, credentialGateway, deviceGateway, profileGateway,
                securityNotificationGateway, sessionGateway, tokenGateway, oAuthClientGateway, credentialDomainService,
                tokenDomainService, familyJwtService);
        TokenManageImpl tokenService = new TokenManageImpl(tokenGateway, sessionGateway, tokenDomainService, familyJwtService);

        accountCommandService.registerByUsername(new RegisterAccountCommand("mario", "mario@example.com", "13800000000", "S3cret@123"));
        TokenPairResponse tokenPair = authService.loginByPassword(new PasswordLoginRequest(
                "mario", "S3cret@123", "family-web", null, "Chrome", "fingerprint-1"));
        var validation = tokenService.validateAccessToken(new TokenValidationRequest(tokenPair.accessToken(), "profile", "read"));
        TokenPairResponse refreshed = tokenService.refreshAccessToken(new RefreshTokenRequest(
                tokenPair.refreshToken(), tokenPair.accountId(), tokenPair.deviceId()));
        Claims jwtClaims = familyJwtService.parseAccessClaims(tokenPair.accessToken()).orElseThrow();

        assertThat(tokenPair.accessToken()).contains(".");
        assertThat(tokenPair.refreshToken()).startsWith("refresh_");
        assertThat(validation.valid()).isTrue();
        assertThat(validation.accountId()).isEqualTo(tokenPair.accountId());
        assertThat(jwtClaims.getSubject()).isEqualTo(tokenPair.accountId());
        assertThat(jwtClaims.get("authorities")).isNull();
        assertThat(refreshed.accessToken()).contains(".");
        assertThat(refreshed.sessionId()).isEqualTo(tokenPair.sessionId());
    }

    /**
     * 校验密码模式登录必须使用合法接入方。
     */
    @Test
    void passwordLoginShouldRejectIllegalClient() {
        InMemoryAccountGatewayImpl accountGateway = new InMemoryAccountGatewayImpl();
        InMemoryCredentialGatewayImpl credentialGateway = new InMemoryCredentialGatewayImpl();
        InMemoryProfileGatewayImpl profileGateway = new InMemoryProfileGatewayImpl();
        InMemorySecurityNotificationGatewayImpl securityNotificationGateway = new InMemorySecurityNotificationGatewayImpl();
        InMemoryDeviceGatewayImpl deviceGateway = new InMemoryDeviceGatewayImpl();
        InMemorySessionGatewayImpl sessionGateway = new InMemorySessionGatewayImpl();
        InMemoryTokenGatewayImpl tokenGateway = new InMemoryTokenGatewayImpl();
        InMemoryOAuthClientGatewayImpl oAuthClientGateway = new InMemoryOAuthClientGatewayImpl();
        CredentialDomainService credentialDomainService = new CredentialDomainService();
        TokenDomainService tokenDomainService = new TokenDomainService();
        FamilyJwtService familyJwtService = new FamilyJwtService(jwtProperties());
        AccountCommandExe accountCommandService = new AccountCommandExe(new AccountDomainService(), credentialDomainService,
                accountGateway, credentialGateway, profileGateway);
        AuthManageImpl authService = new AuthManageImpl(accountGateway, credentialGateway, deviceGateway, profileGateway,
                securityNotificationGateway, sessionGateway, tokenGateway, oAuthClientGateway, credentialDomainService,
                tokenDomainService, familyJwtService);

        accountCommandService.registerByUsername(new RegisterAccountCommand("mario", "mario@example.com", "13800000000", "S3cret@123"));

        assertThatThrownBy(() -> authService.loginByPassword(new PasswordLoginRequest(
                "mario", "S3cret@123", "illegal-client", null, "Chrome", "fingerprint-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("接入方");
    }

    /**
     * 校验找回密码重置后旧 Token 被撤销，新密码可登录。
     */
    @Test
    void resetPasswordShouldRevokeOldTokensAndAcceptNewPassword() {
        InMemoryAccountGatewayImpl accountGateway = new InMemoryAccountGatewayImpl();
        InMemoryCredentialGatewayImpl credentialGateway = new InMemoryCredentialGatewayImpl();
        InMemoryProfileGatewayImpl profileGateway = new InMemoryProfileGatewayImpl();
        InMemorySecurityNotificationGatewayImpl securityNotificationGateway = new InMemorySecurityNotificationGatewayImpl();
        InMemoryDeviceGatewayImpl deviceGateway = new InMemoryDeviceGatewayImpl();
        InMemorySessionGatewayImpl sessionGateway = new InMemorySessionGatewayImpl();
        InMemoryTokenGatewayImpl tokenGateway = new InMemoryTokenGatewayImpl();
        InMemoryOAuthClientGatewayImpl oAuthClientGateway = new InMemoryOAuthClientGatewayImpl();
        CredentialDomainService credentialDomainService = new CredentialDomainService();
        TokenDomainService tokenDomainService = new TokenDomainService();
        FamilyJwtService familyJwtService = new FamilyJwtService(jwtProperties());
        AccountCommandExe accountCommandService = new AccountCommandExe(new AccountDomainService(), credentialDomainService,
                accountGateway, credentialGateway, profileGateway);
        oAuthClientGateway.save(OAuthClient.createPublicClient("family-web", "Family Web", Set.of("PASSWORD", "REFRESH_TOKEN"),
                Set.of("openid", "profile"), Set.of("/**")));
        AuthManageImpl authService = new AuthManageImpl(accountGateway, credentialGateway, deviceGateway, profileGateway,
                securityNotificationGateway, sessionGateway, tokenGateway, oAuthClientGateway, credentialDomainService,
                tokenDomainService, familyJwtService);
        TokenManageImpl tokenService = new TokenManageImpl(tokenGateway, sessionGateway, tokenDomainService, familyJwtService);

        accountCommandService.registerByUsername(new RegisterAccountCommand("mario", "mario@example.com", "13800000000", "S3cret@123"));
        TokenPairResponse oldToken = authService.loginByPassword(new PasswordLoginRequest(
                "mario", "S3cret@123", "family-web", null, "Chrome", "fingerprint-1"));
        String challengeId = authService.requestPasswordRecovery(new PasswordRecoveryRequest("mario", "EMAIL"));
        boolean reset = authService.resetPassword(new ResetPasswordRequest("mario", challengeId, "N3wS3cret@456"));
        TokenPairResponse newToken = authService.loginByPassword(new PasswordLoginRequest(
                "mario", "N3wS3cret@456", "family-web", null, "Chrome", "fingerprint-1"));
        var oldValidation = tokenService.validateAccessToken(new TokenValidationRequest(oldToken.accessToken(), "profile", "read"));
        var newValidation = tokenService.validateAccessToken(new TokenValidationRequest(newToken.accessToken(), "profile", "read"));

        assertThat(reset).isTrue();
        assertThat(oldValidation.valid()).isFalse();
        assertThat(newValidation.valid()).isTrue();
    }

    /**
     * 创建 JWT 测试配置。
     *
     * @return JWT 服务配置
     */
    private top.egon.familyaibutler.common.security.jwt.FamilyJwtProperties jwtProperties() {
        top.egon.familyaibutler.common.security.jwt.FamilyJwtProperties properties =
                new top.egon.familyaibutler.common.security.jwt.FamilyJwtProperties();
        properties.setAccessKey(base64Key("access-key-for-uaa-auth-token-test-2026-aaaaaaaaaaaaaaaaaaaa"));
        properties.setRefreshKey(base64Key("refresh-key-for-uaa-auth-token-test-2026-bbbbbbbbbbbbbbbbb"));
        properties.setAccessTokenExpireTime(300000L);
        properties.setRefreshTokenExpireTime(2592000000L);
        return properties;
    }

    /**
     * 创建 Base64 测试密钥。
     *
     * @param raw 原始密钥
     * @return Base64 密钥
     */
    private String base64Key(String raw) {
        String normalizedRaw = (raw + "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ").substring(0, 64);
        return Base64.getEncoder().encodeToString(normalizedRaw.getBytes(StandardCharsets.UTF_8));
    }
}
