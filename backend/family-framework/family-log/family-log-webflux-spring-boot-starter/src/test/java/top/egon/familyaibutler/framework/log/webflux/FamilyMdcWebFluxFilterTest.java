/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.webflux
 * @FileName: FamilyMdcWebFluxFilterTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:10
 * @Description: WebFlux MDC 过滤器测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.webflux;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import top.egon.familyaibutler.framework.log.core.FamilyLogMdcKeys;
import top.egon.familyaibutler.framework.log.core.FamilyLogProperties;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.webflux
 * @ClassName: FamilyMdcWebFluxFilterTest
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:10
 * @Description: WebFlux MDC 过滤器测试
 * @Version: 1.0
 */
class FamilyMdcWebFluxFilterTest {

    /**
     * 清理测试线程 MDC。
     */
    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    /**
     * 校验 WebFlux 过滤器会写入 Reactor 上下文和响应头，并恢复原线程上下文。
     */
    @Test
    void shouldBindHeadersIntoReactorContextAndRestoreThreadContext() {
        FamilyMdcWebFluxFilter filter = new FamilyMdcWebFluxFilter(new FamilyLogProperties());
        AtomicReference<Map<String, String>> reactorContext = new AtomicReference<>();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/gateway/test").header(FamilyLogMdcKeys.HEADER_TRACE_ID, "trace_webflux").build()
        );
        MDC.put("customKey", "customValue");
        WebFilterChain chain = webExchange -> Mono.deferContextual(contextView -> {
            reactorContext.set(contextView.get(FamilyLogMdcKeys.REACTOR_CONTEXT_KEY));
            assertThat(MDC.get(FamilyLogMdcKeys.TRACE_ID)).isEqualTo("trace_webflux");
            return Mono.empty();
        });

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(reactorContext.get().get(FamilyLogMdcKeys.TRACE_ID)).isEqualTo("trace_webflux");
        assertThat(exchange.getResponse().getHeaders().getFirst(FamilyLogMdcKeys.HEADER_TRACE_ID)).isEqualTo("trace_webflux");
        assertThat(MDC.get("customKey")).isEqualTo("customValue");
        assertThat(MDC.get(FamilyLogMdcKeys.TRACE_ID)).isNull();
    }

    /**
     * 校验 Gateway 全局过滤器会把日志上下文透传到下游路由请求头。
     */
    @Test
    void shouldMutateGatewayRouteRequestHeadersWithTraceAndIdentityContext() {
        FamilyMdcGatewayGlobalFilter filter = new FamilyMdcGatewayGlobalFilter(new FamilyLogProperties());
        AtomicReference<ServerWebExchange> routedExchange = new AtomicReference<>();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/family/password")
                        .header(FamilyLogMdcKeys.HEADER_TRACE_ID, "trace_gateway")
                        .header(FamilyLogMdcKeys.HEADER_REQUEST_ID, "request_gateway")
                        .header(FamilyLogMdcKeys.HEADER_ACCOUNT_ID, "account_1")
                        .build()
        );
        GatewayFilterChain chain = webExchange -> {
            routedExchange.set(webExchange);
            return Mono.empty();
        };

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(routedExchange.get().getRequest().getHeaders().getFirst(FamilyLogMdcKeys.HEADER_TRACE_ID))
                .isEqualTo("trace_gateway");
        assertThat(routedExchange.get().getRequest().getHeaders().getFirst(FamilyLogMdcKeys.HEADER_REQUEST_ID))
                .isEqualTo("request_gateway");
        assertThat(routedExchange.get().getRequest().getHeaders().getFirst(FamilyLogMdcKeys.HEADER_ACCOUNT_ID))
                .isEqualTo("account_1");
        assertThat(exchange.getResponse().getHeaders().getFirst(FamilyLogMdcKeys.HEADER_TRACE_ID))
                .isEqualTo("trace_gateway");
        assertThat(MDC.get(FamilyLogMdcKeys.TRACE_ID)).isNull();
    }
}
