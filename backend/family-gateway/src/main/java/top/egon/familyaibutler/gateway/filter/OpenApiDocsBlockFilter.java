package top.egon.familyaibutler.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.filter
 * @ClassName: OpenApiDocsBlockFilter
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-23:25
 * @Description: 网关 OpenAPI 原生文档入口阻断过滤器
 * @Version: 1.0
 */
@Component
public class OpenApiDocsBlockFilter implements GlobalFilter, Ordered {

    /**
     * 过滤网关转发的 OpenAPI 原生文档请求
     *
     * @param exchange 网关请求上下文
     * @param chain    网关过滤器链
     * @return Mono<Void> 返回过滤结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isOpenApiDocsPath(path)) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    /**
     * 判断是否为 OpenAPI 原生文档路径
     *
     * @param path 请求路径
     * @return boolean 返回 true 表示 OpenAPI 原生文档路径
     */
    private boolean isOpenApiDocsPath(String path) {
        return "/v3/api-docs".equals(path)
                || path.startsWith("/v3/api-docs/")
                || path.endsWith("/v3/api-docs")
                || path.contains("/v3/api-docs/");
    }

    /**
     * 获取过滤器顺序
     *
     * @return int 返回过滤器顺序
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
