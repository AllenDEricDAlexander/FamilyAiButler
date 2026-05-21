/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.adapter.filter
 * @FileName: OpenApiDocsBlockFilterTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-18:20
 * @Description: 网关 OpenAPI 原生文档入口阻断过滤器测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.adapter.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.egon.openapi.console.ApiDocConsoleProperties;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.adapter.filter
 * @ClassName: OpenApiDocsBlockFilterTest
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-18:20
 * @Description: 网关 OpenAPI 原生文档入口阻断过滤器测试
 * @Version: 1.0
 */
class OpenApiDocsBlockFilterTest {

    /**
     * 校验外部直接访问 gateway 路由下的 OpenAPI 原生文档会被阻断。
     */
    @Test
    void filterShouldBlockExternalOpenApiDocsPath() {
        OpenApiDocsBlockFilter filter = new OpenApiDocsBlockFilter(properties());
        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();
        GatewayFilterChain chain = exchange -> {
            forwardedExchange.set(exchange);
            return Mono.empty();
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/uaa/v3/api-docs").build());

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(forwardedExchange.get()).isNull();
    }

    /**
     * 校验控制台内部聚合请求可以访问 gateway 路由下的 OpenAPI 原生文档。
     */
    @Test
    void filterShouldAllowInternalConsoleOpenApiDocsPath() {
        OpenApiDocsBlockFilter filter = new OpenApiDocsBlockFilter(properties());
        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();
        GatewayFilterChain chain = exchange -> {
            forwardedExchange.set(exchange);
            return Mono.empty();
        };
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/uaa/v3/api-docs")
                .header("X-OpenAPI-Console-Token", "internal-openapi-token")
                .build());

        filter.filter(exchange, chain).block();

        assertThat(forwardedExchange.get()).isNotNull();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    /**
     * 创建 OpenAPI 控制台测试配置。
     *
     * @return ApiDocConsoleProperties 返回控制台配置
     */
    private ApiDocConsoleProperties properties() {
        ApiDocConsoleProperties properties = new ApiDocConsoleProperties();
        ApiDocConsoleProperties.ServiceRoute route = new ApiDocConsoleProperties.ServiceRoute();
        route.setId("family-uaa");
        route.setOpenApiAccessToken("internal-openapi-token");
        properties.getServices().add(route);
        return properties;
    }
}
