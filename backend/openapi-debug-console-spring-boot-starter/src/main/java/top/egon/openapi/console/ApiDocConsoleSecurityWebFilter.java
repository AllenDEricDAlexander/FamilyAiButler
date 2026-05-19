/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @FileName: ApiDocConsoleSecurityWebFilter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:50
 * @Description: OpenAPI 调试文档控制台安全过滤器文件
 * @Version: 1.0
 */
package top.egon.openapi.console;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @ClassName: ApiDocConsoleSecurityWebFilter
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:50
 * @Description: OpenAPI 调试文档控制台安全过滤器
 * @Version: 1.0
 */
@RequiredArgsConstructor
public class ApiDocConsoleSecurityWebFilter implements WebFilter {

    private final ApiDocConsoleProperties properties;

    private final ApiDocConsoleSessionService sessionService;

    /**
     * 过滤控制台请求
     *
     * @param exchange WebFlux 请求上下文
     * @param chain    WebFlux 过滤器链
     * @return Mono<Void> 返回过滤结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!path.startsWith(properties.getBasePath())) {
            return chain.filter(exchange);
        }
        fillSecurityHeaders(exchange);
        if (!sessionService.isConsoleOpen()) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    /**
     * 填充安全响应头
     *
     * @param exchange WebFlux 请求上下文
     */
    private void fillSecurityHeaders(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getResponse().getHeaders();
        headers.set("X-Frame-Options", "DENY");
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("Referrer-Policy", "no-referrer");
        headers.set("Content-Security-Policy", "default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; script-src 'self'; connect-src 'self'; frame-ancestors 'none'");
    }
}
