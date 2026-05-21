/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @FileName: OAuthClientServiceTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-16:40
 * @Description: OAuth Client 应用服务测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application;

import org.junit.jupiter.api.Test;
import top.egon.familyaibutler.uaa.application.manage.impl.OAuthClientManageImpl;
import top.egon.familyaibutler.uaa.domain.auth.service.TokenDomainService;
import top.egon.familyaibutler.uaa.domain.oauth.model.aggregate.OAuthClient;
import top.egon.familyaibutler.uaa.facade.dto.oauthclient.CreateOAuthClientRequest;
import top.egon.familyaibutler.uaa.facade.dto.oauthclient.OAuthClientResponse;
import top.egon.familyaibutler.uaa.infrastructure.gateway.impl.InMemoryOAuthClientGatewayImpl;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @ClassName: OAuthClientServiceTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 16:40
 * @Description: OAuth Client 应用服务测试
 * @Version: 1.0
 */
class OAuthClientServiceTest {

    /**
     * 校验 OAuth Client 可以创建、查询和列表返回，并且密钥不明文落库。
     */
    @Test
    void shouldCreateGetAndListOAuthClientWithHashedSecret() {
        InMemoryOAuthClientGatewayImpl oAuthClientGateway = new InMemoryOAuthClientGatewayImpl();
        OAuthClientManageImpl service = new OAuthClientManageImpl(oAuthClientGateway, new TokenDomainService());
        CreateOAuthClientRequest request = new CreateOAuthClientRequest("admin-console", "Admin Console",
                "client-secret-123", Set.of("PASSWORD", "REFRESH_TOKEN"), Set.of("openid", "profile"),
                Set.of("family-core:/password/**"), 600L, 2592000L);

        OAuthClientResponse created = service.create(request);
        OAuthClientResponse queried = service.get("admin-console");

        assertThat(created.clientId()).isEqualTo("admin-console");
        assertThat(created.status()).isEqualTo("ACTIVE");
        assertThat(queried.resourcePatterns()).contains("family-core:/password/**");
        assertThat(service.list()).extracting(OAuthClientResponse::clientId).contains("admin-console");
        OAuthClient storedClient = oAuthClientGateway.findByClientId("admin-console").orElseThrow();
        assertThat(storedClient.requiresSecret()).isTrue();
        assertThat(storedClient.getClientSecretHash()).isNotEqualTo("client-secret-123");
    }
}
