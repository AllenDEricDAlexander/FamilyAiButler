/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @FileName: AuthorizationDecisionServiceTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:20
 * @Description: 授权决策应用服务测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application;

import org.junit.jupiter.api.Test;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtProperties;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtService;
import top.egon.familyaibutler.uaa.application.dto.account.RegisterAccountCommand;
import top.egon.familyaibutler.uaa.domain.model.aggregate.OAuthClient;
import top.egon.familyaibutler.uaa.domain.model.aggregate.PermissionResource;
import top.egon.familyaibutler.uaa.domain.model.aggregate.Role;
import top.egon.familyaibutler.uaa.domain.model.enums.PermissionResourceType;
import top.egon.familyaibutler.uaa.domain.model.valueobject.TokenClaims;
import top.egon.familyaibutler.uaa.domain.service.AccountDomainService;
import top.egon.familyaibutler.uaa.domain.service.CredentialDomainService;
import top.egon.familyaibutler.uaa.domain.service.TokenDomainService;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionRequest;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionResponse;
import top.egon.familyaibutler.uaa.facade.dto.auth.PasswordLoginRequest;
import top.egon.familyaibutler.uaa.infrastructure.gatewayimpl.InMemoryAccountGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gatewayimpl.InMemoryCredentialGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gatewayimpl.InMemoryDeviceGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gatewayimpl.InMemoryOAuthClientGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gatewayimpl.InMemoryProfileGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gatewayimpl.InMemoryRbacGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gatewayimpl.InMemorySecurityNotificationGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gatewayimpl.InMemorySessionGatewayImpl;
import top.egon.familyaibutler.uaa.infrastructure.gatewayimpl.InMemoryTokenGatewayImpl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @ClassName: AuthorizationDecisionServiceTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:20
 * @Description: 授权决策应用服务测试
 * @Version: 1.0
 */
class AuthorizationDecisionServiceTest {

    /**
     * 校验授权决策使用 JWT、Token 记录和接入方资源范围共同判断。
     */
    @Test
    void decideShouldAllowOnlyValidTokenAndClientResource() {
        InMemoryAccountGatewayImpl accountGateway = new InMemoryAccountGatewayImpl();
        InMemoryCredentialGatewayImpl credentialGateway = new InMemoryCredentialGatewayImpl();
        InMemoryProfileGatewayImpl profileGateway = new InMemoryProfileGatewayImpl();
        InMemorySecurityNotificationGatewayImpl securityNotificationGateway = new InMemorySecurityNotificationGatewayImpl();
        InMemoryDeviceGatewayImpl deviceGateway = new InMemoryDeviceGatewayImpl();
        InMemorySessionGatewayImpl sessionGateway = new InMemorySessionGatewayImpl();
        InMemoryTokenGatewayImpl tokenGateway = new InMemoryTokenGatewayImpl();
        InMemoryOAuthClientGatewayImpl oAuthClientGateway = new InMemoryOAuthClientGatewayImpl();
        InMemoryRbacGatewayImpl rbacGateway = new InMemoryRbacGatewayImpl();
        CredentialDomainService credentialDomainService = new CredentialDomainService();
        TokenDomainService tokenDomainService = new TokenDomainService();
        FamilyJwtService familyJwtService = new FamilyJwtService(jwtProperties());
        AccountCommandService accountCommandService = new AccountCommandService(new AccountDomainService(), credentialDomainService,
                accountGateway, credentialGateway, profileGateway);
        oAuthClientGateway.save(OAuthClient.createPublicClient("family-web", "Family Web", Set.of("PASSWORD", "REFRESH_TOKEN"),
                Set.of("openid", "profile"), Set.of("family-core:/**")));
        rbacGateway.saveRole(Role.active("family-admin", "Family Admin"));
        rbacGateway.saveResource(PermissionResource.active("password-view-page", "Password View Page", PermissionResourceType.API,
                "family-core", "/password/**", "read"));
        AuthServiceImpl authService = new AuthServiceImpl(accountGateway, credentialGateway, deviceGateway, profileGateway,
                securityNotificationGateway, sessionGateway, tokenGateway, oAuthClientGateway, credentialDomainService,
                tokenDomainService, familyJwtService);
        AuthorizationServiceImpl authorizationService = new AuthorizationServiceImpl(accountGateway, tokenGateway, oAuthClientGateway, rbacGateway, familyJwtService);

        accountCommandService.registerByUsername(new RegisterAccountCommand("mario", "mario@example.com", "13800000000", "S3cret@123"));
        var tokenPair = authService.loginByPassword(new PasswordLoginRequest(
                "mario", "S3cret@123", "family-web", null, "Chrome", "fingerprint-1"));
        rbacGateway.bindRoleResource("family-admin", "password-view-page");
        rbacGateway.bindAccountRole(tokenPair.accountId(), "family-admin");
        AuthorizationDecisionResponse allowed = authorizationService.decide(new AuthorizationDecisionRequest(
                tokenPair.accessToken(), "family-core", "/password/view/list", "read"));
        AuthorizationDecisionResponse rbacDenied = authorizationService.decide(new AuthorizationDecisionRequest(
                tokenPair.accessToken(), "family-core", "/secret/list", "read"));
        AuthorizationDecisionResponse denied = authorizationService.decide(new AuthorizationDecisionRequest(
                tokenPair.accessToken(), "family-ai-qwen", "/chat", "read"));
        AuthorizationDecisionResponse invalid = authorizationService.decide(new AuthorizationDecisionRequest(
                "bad-token", "family-core", "/password/view/list", "read"));
        TokenClaims claims = tokenGateway.findAccessTokenClaims(tokenPair.accessToken()).orElseThrow();
        tokenGateway.saveAccessTokenClaims(tokenPair.accessToken(), new TokenClaims(claims.accountId(), claims.profileId(),
                claims.clientId(), claims.sessionId(), claims.deviceId(), claims.authVersion(), claims.entitlementVersion(),
                "HIGH", claims.expiresAt()));
        AuthorizationDecisionResponse riskDenied = authorizationService.decide(new AuthorizationDecisionRequest(
                tokenPair.accessToken(), "family-core", "/password/view/list", "read"));

        assertThat(allowed.allowed()).isTrue();
        assertThat(allowed.accountId()).isEqualTo(tokenPair.accountId());
        assertThat(allowed.clientId()).isEqualTo("family-web");
        assertThat(rbacDenied.allowed()).isFalse();
        assertThat(rbacDenied.reason()).isEqualTo("RBAC_DENIED");
        assertThat(denied.allowed()).isFalse();
        assertThat(denied.reason()).isEqualTo("RESOURCE_DENIED");
        assertThat(invalid.allowed()).isFalse();
        assertThat(invalid.reason()).isEqualTo("TOKEN_INVALID");
        assertThat(riskDenied.allowed()).isFalse();
        assertThat(riskDenied.reason()).isEqualTo("RISK_DENIED");
    }

    /**
     * 创建 JWT 测试配置。
     *
     * @return JWT 服务配置
     */
    private FamilyJwtProperties jwtProperties() {
        FamilyJwtProperties properties = new FamilyJwtProperties();
        properties.setAccessKey(base64Key("access-key-for-uaa-authorization-test-2026-aaaaaaaaaaaaaaaaaaa"));
        properties.setRefreshKey(base64Key("refresh-key-for-uaa-authorization-test-2026-bbbbbbbbbbbbbbb"));
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
