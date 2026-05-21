/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure
 * @FileName: UaaPersistenceIntegrationTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:10
 * @Description: UAA 持久化集成测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.egon.familyaibutler.uaa.domain.gateway.AccountGateway;
import top.egon.familyaibutler.uaa.domain.gateway.CredentialGateway;
import top.egon.familyaibutler.uaa.domain.gateway.DeviceGateway;
import top.egon.familyaibutler.uaa.domain.gateway.OAuthClientGateway;
import top.egon.familyaibutler.uaa.domain.gateway.ProfileGateway;
import top.egon.familyaibutler.uaa.domain.gateway.RbacGateway;
import top.egon.familyaibutler.uaa.domain.gateway.SessionGateway;
import top.egon.familyaibutler.uaa.domain.gateway.TokenGateway;
import top.egon.familyaibutler.uaa.domain.model.aggregate.Account;
import top.egon.familyaibutler.uaa.domain.model.aggregate.AuthSession;
import top.egon.familyaibutler.uaa.domain.model.aggregate.OAuthClient;
import top.egon.familyaibutler.uaa.domain.model.aggregate.PermissionResource;
import top.egon.familyaibutler.uaa.domain.model.aggregate.Profile;
import top.egon.familyaibutler.uaa.domain.model.aggregate.Role;
import top.egon.familyaibutler.uaa.domain.model.entity.Device;
import top.egon.familyaibutler.uaa.domain.model.entity.TokenRecord;
import top.egon.familyaibutler.uaa.domain.model.enums.DeviceType;
import top.egon.familyaibutler.uaa.domain.model.enums.PermissionResourceType;
import top.egon.familyaibutler.uaa.domain.model.valueobject.TokenClaims;
import top.egon.familyaibutler.uaa.domain.service.CredentialDomainService;
import top.egon.familyaibutler.uaa.domain.service.TokenDomainService;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure
 * @ClassName: UaaPersistenceIntegrationTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:10
 * @Description: UAA 持久化集成测试
 * @Version: 1.0
 */
@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.sql.init.mode=always"
})
class UaaPersistenceIntegrationTest {
    private final AccountGateway accountGateway;
    private final CredentialGateway credentialGateway;
    private final ProfileGateway profileGateway;
    private final DeviceGateway deviceGateway;
    private final SessionGateway sessionGateway;
    private final TokenGateway tokenGateway;
    private final OAuthClientGateway oAuthClientGateway;
    private final RbacGateway rbacGateway;
    private final CredentialDomainService credentialDomainService;
    private final TokenDomainService tokenDomainService;

    /**
     * 创建 UAA 持久化集成测试。
     *
     * @param accountGateway          账号网关
     * @param credentialGateway       凭证网关
     * @param profileGateway          Profile 网关
     * @param deviceGateway           设备网关
     * @param sessionGateway          会话网关
     * @param tokenGateway            Token 网关
     * @param oAuthClientGateway      OAuth Client 网关
     * @param rbacGateway             RBAC 网关
     * @param credentialDomainService 凭证领域服务
     * @param tokenDomainService      Token 领域服务
     */
    @Autowired
    UaaPersistenceIntegrationTest(AccountGateway accountGateway, CredentialGateway credentialGateway,
                                  ProfileGateway profileGateway, DeviceGateway deviceGateway,
                                  SessionGateway sessionGateway, TokenGateway tokenGateway, OAuthClientGateway oAuthClientGateway,
                                  RbacGateway rbacGateway, CredentialDomainService credentialDomainService,
                                  TokenDomainService tokenDomainService) {
        this.accountGateway = accountGateway;
        this.credentialGateway = credentialGateway;
        this.profileGateway = profileGateway;
        this.deviceGateway = deviceGateway;
        this.sessionGateway = sessionGateway;
        this.tokenGateway = tokenGateway;
        this.oAuthClientGateway = oAuthClientGateway;
        this.rbacGateway = rbacGateway;
        this.credentialDomainService = credentialDomainService;
        this.tokenDomainService = tokenDomainService;
    }

    /**
     * 校验账号、凭证、Profile、设备、会话和 Token 可以持久化并读回。
     */
    @Test
    void shouldPersistAndReadBackUaaCoreAggregates() {
        String suffix = String.valueOf(System.nanoTime());
        Account account = accountGateway.save(Account.createConsumer("mario_" + suffix, "mario_" + suffix + "@example.com", "138" + suffix.substring(0, 8)));
        credentialGateway.save(credentialDomainService.createPasswordCredential(account.getAccountId(), "S3cret@123"));
        Profile profile = profileGateway.save(Profile.createMain(account.getAccountId(), account.getUsername()));
        Device device = deviceGateway.save(Device.register(account.getAccountId(), "Chrome", DeviceType.WEB, "fingerprint-" + suffix));
        AuthSession session = sessionGateway.save(AuthSession.create(account.getAccountId(), profile.getProfileId(), device.getDeviceId(), "family-web"));
        OAuthClient client = oAuthClientGateway.save(OAuthClient.createPublicClient("family-web-" + suffix, "Family Web " + suffix,
                Set.of("PASSWORD", "REFRESH_TOKEN"), Set.of("openid", "profile"), Set.of("family-core:/password/**")));
        Role role = rbacGateway.saveRole(Role.active("role-" + suffix, "Role " + suffix));
        PermissionResource resource = rbacGateway.saveResource(PermissionResource.active("password-api-" + suffix,
                "Password API " + suffix, PermissionResourceType.API, "family-core", "/password/**", "GET"));
        rbacGateway.bindRoleResource(role.getRoleCode(), resource.getResourceCode());
        rbacGateway.bindAccountRole(account.getAccountId(), role.getRoleCode());
        TokenRecord refreshToken = tokenGateway.saveRefreshToken(tokenDomainService.createRefreshTokenRecord(session, device, "refresh-" + suffix));
        TokenClaims claims = new TokenClaims(account.getAccountId(), profile.getProfileId(), session.getClientId(),
                session.getSessionId(), device.getDeviceId(), account.getAuthVersion(), account.getEntitlementVersion(),
                "NORMAL", Instant.now().plusSeconds(300L));
        String accessToken = "access." + suffix + "." + "x".repeat(260);
        tokenGateway.saveAccessTokenClaims(accessToken, claims);

        assertThat(accountGateway.findByPrincipal(account.getUsername()).map(Account::getAccountId)).contains(account.getAccountId());
        assertThat(credentialGateway.findPasswordCredential(account.getAccountId())).isPresent();
        assertThat(profileGateway.findByAccountId(account.getAccountId())).hasSize(1);
        assertThat(deviceGateway.findByFingerprint(account.getAccountId(), device.getFingerprint()).map(Device::getDeviceId)).contains(device.getDeviceId());
        assertThat(sessionGateway.findBySessionId(session.getSessionId()).map(AuthSession::getSessionId)).contains(session.getSessionId());
        assertThat(oAuthClientGateway.findByClientId(client.getClientId()).map(OAuthClient::getClientId)).contains(client.getClientId());
        assertThat(oAuthClientGateway.findAll()).extracting(OAuthClient::getClientId).contains(client.getClientId());
        assertThat(rbacGateway.findResourcesByAccountId(account.getAccountId())).extracting(PermissionResource::getResourceCode)
                .contains(resource.getResourceCode());
        assertThat(tokenGateway.findRefreshTokenByHash(refreshToken.getTokenHash()).map(TokenRecord::getTokenId)).contains(refreshToken.getTokenId());
        assertThat(tokenGateway.findAccessTokenClaims(accessToken).map(TokenClaims::accountId)).contains(claims.accountId());
    }
}
