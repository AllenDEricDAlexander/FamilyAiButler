package top.egon.familyaibutler.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.egon.familyaibutler.gateway.config.FamilyButlerGateWayProperties;
import top.egon.familyaibutler.gateway.exception.BusinessException;
import top.egon.familyaibutler.gateway.util.JwtUtil;

import java.nio.charset.StandardCharsets;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.filter
 * @ClassName: JwtTokenFilter
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-15Day-20:45
 * @Description: 认证过滤器
 * @Version: 1.0
 */
@Slf4j
//@Component
@RequiredArgsConstructor
public class JwtTokenFilter implements GlobalFilter, Ordered {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;

    private final FamilyButlerGateWayProperties familyButlerGateWayProperties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String url = exchange.getRequest().getURI().getPath();

        // todo IP 黑名单

        // todo 限流

        // todo 请求过滤
        log.error("白名单: {}", familyButlerGateWayProperties.getJwt().getIgnoreurlset());

        // todo 日志审计体系

        for (String ignoreUrl : familyButlerGateWayProperties.getJwt().getIgnoreurlset()) {
            if (url.contains(ignoreUrl)) {
                // todo 透传 tenantID jwt附带信息
                return chain.filter(exchange);
            }
        }

        String token = exchange.getRequest().getHeaders().getFirst(familyButlerGateWayProperties.getJwt().getAuthorization());
        ServerHttpResponse resp = exchange.getResponse();

        if (StringUtils.isBlank(token)) {
            return authError(resp, "请先认证");
        }

        try {
            // todo 校验令牌 Guava Cache 中是否存在
            if (!jwtUtil.validateAccessToken(token)) {
                // todo 判断 refreshToken 刷新token
                throw new BusinessException("token已过期");
            }
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                String oldToken = response.getHeaders().getFirst(familyButlerGateWayProperties.getJwt().getAuthorization());
                String newToken = jwtUtil.refreshJWTToken(oldToken, familyButlerGateWayProperties.getJwt().getAccessTokenExpireTime(), jwtUtil.getAccessKey());
                response.getHeaders().add(familyButlerGateWayProperties.getJwt().getAuthorization(), newToken);
            }));
        } catch (Exception e) {
            return authError(resp, e.getMessage());
        }
    }

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

    @Override
    public int getOrder() {
        return -100;
    }
}