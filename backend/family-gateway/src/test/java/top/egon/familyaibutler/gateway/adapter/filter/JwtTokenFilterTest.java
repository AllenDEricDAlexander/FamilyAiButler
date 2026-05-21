/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.adapter.filter
 * @FileName: JwtTokenFilterTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:40
 * @Description: JWT 网关过滤器测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.adapter.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import top.egon.familyaibutler.common.security.FamilySecurityHeaders;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtClaims;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtProperties;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtService;
import top.egon.familyaibutler.gateway.application.GatewayAccessService;
import top.egon.familyaibutler.gateway.domain.gateway.AuthorizationDecisionGateway;
import top.egon.familyaibutler.gateway.infrastructure.configuration.FamilyButlerGateWayProperties;
import top.egon.familyaibutler.gateway.infrastructure.security.FamilyJwtTokenGatewayImpl;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionRequest;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionResponse;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.adapter.filter
 * @ClassName: JwtTokenFilterTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:40
 * @Description: JWT 网关过滤器测试
 * @Version: 1.0
 */
class JwtTokenFilterTest {

    /**
     * 校验网关验证 JWT 后透传可信身份头。
     */
    @Test
    void filterShouldInjectTrustedIdentityHeaders() {
        FamilyJwtService familyJwtService = new FamilyJwtService(jwtProperties());
        String accessToken = familyJwtService.createAccessToken(new FamilyJwtClaims("jwt_1", "acc_1", "prof_1",
                "family-web", "sess_1", "dev_1", 7L, 11L, "NORMAL", "family-uaa", "family-api",
                Instant.now().plusSeconds(300)));
        FamilyButlerGateWayProperties properties = new FamilyButlerGateWayProperties();
        properties.getJwt().setIgnoreUrlSet(Set.of());
        AtomicReference<AuthorizationDecisionRequest> decisionRequest = new AtomicReference<>();
        AuthorizationDecisionGateway authorizationGateway = request -> {
            decisionRequest.set(request);
            return Mono.just(new AuthorizationDecisionResponse(true, "ALLOW", "acc_1", "prof_1",
                    "family-web", "sess_1", "dev_1"));
        };
        GatewayAccessService accessService = new GatewayAccessService(new FamilyJwtTokenGatewayImpl(familyJwtService),
                authorizationGateway);
        JwtTokenFilter filter = new JwtTokenFilter(new ObjectMapper(), accessService, properties);
        AtomicReference<org.springframework.web.server.ServerWebExchange> forwardedExchange = new AtomicReference<>();
        GatewayFilterChain chain = exchange -> {
            forwardedExchange.set(exchange);
            return Mono.empty();
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/base/password/view/list")
                .header("Authorization", "Bearer " + accessToken)
                .header(FamilySecurityHeaders.ACCOUNT_ID, "spoofed")
                .build());

        filter.filter(exchange, chain).block();

        var headers = forwardedExchange.get().getRequest().getHeaders();
        assertThat(headers.getFirst(FamilySecurityHeaders.ACCOUNT_ID)).isEqualTo("acc_1");
        assertThat(headers.getFirst(FamilySecurityHeaders.PROFILE_ID)).isEqualTo("prof_1");
        assertThat(headers.getFirst(FamilySecurityHeaders.CLIENT_ID)).isEqualTo("family-web");
        assertThat(headers.getFirst(FamilySecurityHeaders.SESSION_ID)).isEqualTo("sess_1");
        assertThat(headers.getFirst(FamilySecurityHeaders.DEVICE_ID)).isEqualTo("dev_1");
        assertThat(headers.getFirst(FamilySecurityHeaders.AUTH_VERSION)).isEqualTo("7");
        assertThat(headers.getFirst(FamilySecurityHeaders.ENTITLEMENT_VERSION)).isEqualTo("11");
        assertThat(decisionRequest.get().resourceService()).isEqualTo("family-core");
        assertThat(decisionRequest.get().resourcePath()).isEqualTo("/password/view/list");
        assertThat(decisionRequest.get().action()).isEqualTo("GET");
    }

    /**
     * 校验网关在 UAA 授权拒绝后不会转发请求。
     */
    @Test
    void filterShouldRejectWhenUaaAuthorizationDenied() {
        FamilyJwtService familyJwtService = new FamilyJwtService(jwtProperties());
        String accessToken = familyJwtService.createAccessToken(new FamilyJwtClaims("jwt_1", "acc_1", "prof_1",
                "family-web", "sess_1", "dev_1", 7L, 11L, "NORMAL", "family-uaa", "family-api",
                Instant.now().plusSeconds(300)));
        FamilyButlerGateWayProperties properties = new FamilyButlerGateWayProperties();
        properties.getJwt().setIgnoreUrlSet(Set.of());
        AuthorizationDecisionGateway authorizationGateway = request -> Mono.just(new AuthorizationDecisionResponse(false,
                "RBAC_DENIED", "acc_1", "prof_1", "family-web", "sess_1", "dev_1"));
        GatewayAccessService accessService = new GatewayAccessService(new FamilyJwtTokenGatewayImpl(familyJwtService),
                authorizationGateway);
        JwtTokenFilter filter = new JwtTokenFilter(new ObjectMapper(), accessService, properties);
        AtomicReference<org.springframework.web.server.ServerWebExchange> forwardedExchange = new AtomicReference<>();
        GatewayFilterChain chain = exchange -> {
            forwardedExchange.set(exchange);
            return Mono.empty();
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/base/password/view/list")
                .header("Authorization", "Bearer " + accessToken)
                .build());

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(forwardedExchange.get()).isNull();
    }

    /**
     * 创建 JWT 测试配置。
     *
     * @return JWT 服务配置
     */
    private FamilyJwtProperties jwtProperties() {
        FamilyJwtProperties properties = new FamilyJwtProperties();
        properties.setAccessKey(base64Key("access-key-for-gateway-filter-test-2026-aaaaaaaaaaaaaaaaaaaaaa"));
        properties.setRefreshKey(base64Key("refresh-key-for-gateway-filter-test-2026-bbbbbbbbbbbbbbbbbb"));
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
