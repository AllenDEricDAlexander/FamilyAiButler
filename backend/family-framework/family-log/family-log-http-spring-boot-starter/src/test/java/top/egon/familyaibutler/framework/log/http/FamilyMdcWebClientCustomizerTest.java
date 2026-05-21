/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.http
 * @FileName: FamilyMdcWebClientCustomizerTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:10
 * @Description: WebClient MDC 自定义器测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import top.egon.familyaibutler.framework.log.core.FamilyLogMdcKeys;
import top.egon.familyaibutler.framework.log.core.FamilyLogProperties;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.http
 * @ClassName: FamilyMdcWebClientCustomizerTest
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:10
 * @Description: WebClient MDC 自定义器测试
 * @Version: 1.0
 */
class FamilyMdcWebClientCustomizerTest {

    /**
     * 清理测试线程 MDC。
     */
    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    /**
     * 校验 WebClient 会优先透传 Reactor Context 中的 trace 请求头。
     */
    @Test
    void shouldPropagateHeadersForWebClient() {
        AtomicReference<ClientRequest> requestReference = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            requestReference.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
        };
        WebClient.Builder builder = WebClient.builder().exchangeFunction(exchangeFunction);
        new FamilyMdcWebClientCustomizer(new FamilyLogProperties()).customize(builder);
        WebClient webClient = builder.baseUrl("http://uaa").build();
        Map<String, String> reactorContext = Map.of(
                FamilyLogMdcKeys.TRACE_ID, "trace_webclient",
                FamilyLogMdcKeys.REQUEST_ID, "request_webclient"
        );

        webClient.get().uri("/authorization/decide")
                .retrieve()
                .toBodilessEntity()
                .contextWrite(context -> context.put(FamilyLogMdcKeys.REACTOR_CONTEXT_KEY, reactorContext))
                .block();

        assertThat(requestReference.get().headers().getFirst(FamilyLogMdcKeys.HEADER_TRACE_ID)).isEqualTo("trace_webclient");
        assertThat(requestReference.get().headers().getFirst(FamilyLogMdcKeys.HEADER_REQUEST_ID)).isEqualTo("request_webclient");
    }
}
