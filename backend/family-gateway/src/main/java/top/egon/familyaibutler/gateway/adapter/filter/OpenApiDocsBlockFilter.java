/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.adapter.filter
 * @FileName: OpenApiDocsBlockFilter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-23:25
 * @Description: 网关 OpenAPI 原生文档入口阻断过滤器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.adapter.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.egon.openapi.console.ApiDocConsoleProperties;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.adapter.filter
 * @ClassName: OpenApiDocsBlockFilter
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-23:25
 * @Description: 网关 OpenAPI 原生文档入口阻断过滤器
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
public class OpenApiDocsBlockFilter implements GlobalFilter, Ordered {

    private final ApiDocConsoleProperties apiDocConsoleProperties;

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
        if (isOpenApiDocsPath(path) && !isInternalOpenApiConsoleRequest(exchange)) {
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
     * 判断是否为 OpenAPI 控制台内部文档聚合请求
     *
     * @param exchange 网关请求上下文
     * @return boolean 返回 true 表示内部聚合请求
     */
    private boolean isInternalOpenApiConsoleRequest(ServerWebExchange exchange) {
        return apiDocConsoleProperties.getServices()
                .stream()
                .filter(ApiDocConsoleProperties.ServiceRoute::isEnabled)
                .anyMatch(route -> StringUtils.hasText(route.getOpenApiAccessHeader())
                        && StringUtils.hasText(route.getOpenApiAccessToken())
                        && route.getOpenApiAccessToken().equals(exchange.getRequest().getHeaders().getFirst(route.getOpenApiAccessHeader())));
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
