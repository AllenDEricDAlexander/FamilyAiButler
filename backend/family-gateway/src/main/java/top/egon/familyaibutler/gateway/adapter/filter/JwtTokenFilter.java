/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.adapter.filter
 * @FileName: JwtTokenFilter.java
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-15Day-20:45
 * @Description: 认证过滤器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.adapter.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.egon.familyaibutler.common.security.FamilySecurityHeaders;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtClaims;
import top.egon.familyaibutler.gateway.application.GatewayAccessService;
import top.egon.familyaibutler.gateway.application.dto.GatewayAccessDecision;
import top.egon.familyaibutler.gateway.infrastructure.configuration.FamilyButlerGateWayProperties;

import java.nio.charset.StandardCharsets;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.adapter.filter
 * @ClassName: JwtTokenFilter
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-15Day-20:45
 * @Description: 认证过滤器
 * @Version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;
    private final GatewayAccessService gatewayAccessService;
    private final FamilyButlerGateWayProperties familyButlerGateWayProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String url = exchange.getRequest().getURI().getPath();

        // todo IP 黑名单

        // todo 限流

        // todo 请求过滤
        log.debug("白名单: {}", familyButlerGateWayProperties.getJwt().getIgnoreUrlSet());

        // todo 日志审计体系

        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(clearIdentityHeaders(exchange));
        }

        for (String ignoreUrl : familyButlerGateWayProperties.getJwt().getIgnoreUrlSet()) {
            if (pathMatches(ignoreUrl, url)) {
                // todo 透传 tenantID jwt附带信息
                return chain.filter(clearIdentityHeaders(exchange));
            }
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(gatewayAccessService.authorizationHeader());
        ServerHttpResponse resp = exchange.getResponse();

        return gatewayAccessService.authorize(authorization, url, resolveAction(exchange))
                .flatMap(decision -> handleDecision(exchange, chain, resp, decision));
    }

    /**
     * 判断请求路径是否匹配白名单
     *
     * @param pattern 白名单路径模式
     * @param path    请求路径
     * @return boolean 返回 true 表示匹配
     */
    private boolean pathMatches(String pattern, String path) {
        if (StringUtils.isBlank(pattern)) {
            return false;
        }
        String normalizedPattern = pattern.startsWith("/") ? pattern : "/" + pattern;
        if (normalizedPattern.endsWith("/**")) {
            String prefix = normalizedPattern.substring(0, normalizedPattern.length() - 3);
            return path.equals(prefix) || path.startsWith(prefix + "/");
        }
        return path.equals(normalizedPattern);
    }

    /**
     * 解析资源操作。
     *
     * @param exchange 请求交换对象
     * @return 资源操作
     */
    private String resolveAction(ServerWebExchange exchange) {
        return exchange.getRequest().getMethod() == null ? "*" : exchange.getRequest().getMethod().name();
    }

    /**
     * 处理网关访问决策。
     *
     * @param exchange 网关请求上下文
     * @param chain    网关过滤器链
     * @param resp     响应对象
     * @param decision 网关访问决策
     * @return Mono<Void> 返回过滤结果
     */
    private Mono<Void> handleDecision(ServerWebExchange exchange, GatewayFilterChain chain, ServerHttpResponse resp,
                                      GatewayAccessDecision decision) {
        if (!decision.authenticated()) {
            return authError(resp, decision.reason());
        }
        if (!decision.allowed()) {
            return forbidden(resp, decision.reason());
        }
        ServerWebExchange authenticatedExchange = exchange.mutate()
                .request(builder -> builder.headers(headers -> applyIdentityHeaders(headers, decision.claims())))
                .build();
        return chain.filter(authenticatedExchange);
    }

    /**
     * 清理外部伪造身份请求头。
     *
     * @param exchange 网关请求上下文
     * @return ServerWebExchange 返回清理后的请求上下文
     */
    private ServerWebExchange clearIdentityHeaders(ServerWebExchange exchange) {
        return exchange.mutate()
                .request(builder -> builder.headers(headers -> FamilySecurityHeaders.identityHeaders().forEach(headers::remove)))
                .build();
    }

    /**
     * 透传可信身份请求头。
     *
     * @param headers 请求头
     * @param claims  JWT 身份声明
     */
    private void applyIdentityHeaders(HttpHeaders headers, FamilyJwtClaims claims) {
        FamilySecurityHeaders.identityHeaders().forEach(headers::remove);
        headers.set(FamilySecurityHeaders.ACCOUNT_ID, claims.accountId());
        headers.set(FamilySecurityHeaders.PROFILE_ID, claims.profileId());
        headers.set(FamilySecurityHeaders.CLIENT_ID, claims.clientId());
        headers.set(FamilySecurityHeaders.SESSION_ID, claims.sessionId());
        headers.set(FamilySecurityHeaders.DEVICE_ID, claims.deviceId());
        headers.set(FamilySecurityHeaders.AUTH_VERSION, String.valueOf(claims.authVersion()));
        headers.set(FamilySecurityHeaders.ENTITLEMENT_VERSION, String.valueOf(claims.entitlementVersion()));
        headers.set(FamilySecurityHeaders.RISK_LEVEL, claims.riskLevel());
    }

    /**
     * 返回认证失败响应
     *
     * @param resp 响应对象
     * @param msg  错误信息
     * @return Mono<Void> 返回响应写入结果
     */
    private Mono<Void> authError(ServerHttpResponse resp, String msg) {
        resp.setStatusCode(HttpStatus.UNAUTHORIZED);
        resp.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String returnStr = "";
        try {
            // todo 输出 result
            returnStr = objectMapper.writeValueAsString("请求失败" + msg);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        DataBuffer buffer = resp.bufferFactory().wrap(returnStr.getBytes(StandardCharsets.UTF_8));

        return resp.writeWith(Flux.just(buffer));
    }

    /**
     * 返回授权失败响应。
     *
     * @param resp 响应对象
     * @param msg  错误信息
     * @return Mono<Void> 返回响应写入结果
     */
    private Mono<Void> forbidden(ServerHttpResponse resp, String msg) {
        resp.setStatusCode(HttpStatus.FORBIDDEN);
        resp.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        String returnStr = "";
        try {
            returnStr = objectMapper.writeValueAsString("请求失败" + msg);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        DataBuffer buffer = resp.bufferFactory().wrap(returnStr.getBytes(StandardCharsets.UTF_8));
        return resp.writeWith(Flux.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
