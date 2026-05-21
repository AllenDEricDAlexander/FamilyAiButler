/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.webflux
 * @FileName: FamilyMdcWebFluxFilter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:10
 * @Description: WebFlux MDC 过滤器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.webflux;

import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import top.egon.familyaibutler.framework.log.core.FamilyLogContext;
import top.egon.familyaibutler.framework.log.core.FamilyLogMdcKeys;
import top.egon.familyaibutler.framework.log.core.FamilyLogProperties;
import top.egon.familyaibutler.framework.log.core.FamilyLogUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.webflux
 * @ClassName: FamilyMdcWebFluxFilter
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:10
 * @Description: WebFlux MDC 过滤器
 * @Version: 1.0
 */
public class FamilyMdcWebFluxFilter implements WebFilter {

    private final FamilyLogProperties properties;

    public FamilyMdcWebFluxFilter(FamilyLogProperties properties) {
        this.properties = properties;
    }

    /**
     * 为 WebFlux 请求写入并清理日志上下文。
     *
     * @param exchange 请求交换对象
     * @param chain    WebFlux 过滤器链
     * @return Mono<Void> 返回过滤执行结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        FamilyLogContext previousContext = FamilyLogContext.capture();
        FamilyLogContext requestContext = FamilyLogContext.fromMap(buildRequestContext(exchange));
        return Mono.defer(() -> {
                    requestContext.applyToMdc();
                    writeResponseHeaders(exchange.getResponse().getHeaders());
                    return chain.filter(exchange);
                })
                .contextWrite(context -> context.put(FamilyLogMdcKeys.REACTOR_CONTEXT_KEY, requestContext.contextMap()))
                .doFinally(signalType -> previousContext.writeToMdc());
    }

    /**
     * 构建当前请求的日志上下文。
     *
     * @param exchange 请求交换对象
     * @return Map<String, String> 返回请求日志上下文
     */
    private Map<String, String> buildRequestContext(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        Map<String, String> contextMap = new LinkedHashMap<>();
        String traceId = FamilyLogUtil.normalizeTraceId(FamilyLogUtil.firstNonBlank(properties.getTraceHeaderNames(), headers::getFirst));
        String requestId = FamilyLogUtil.firstNonBlank(properties.getRequestIdHeaderNames(), headers::getFirst);
        contextMap.put(FamilyLogMdcKeys.TRACE_ID, FamilyLogUtil.isBlank(traceId) ? FamilyLogUtil.newId() : traceId);
        contextMap.put(FamilyLogMdcKeys.REQUEST_ID, FamilyLogUtil.isBlank(requestId) ? FamilyLogUtil.newId() : requestId);
        contextMap.put(FamilyLogMdcKeys.REQUEST_METHOD, exchange.getRequest().getMethod() == null ? null : exchange.getRequest().getMethod().name());
        contextMap.put(FamilyLogMdcKeys.REQUEST_URI, exchange.getRequest().getURI().getPath());
        contextMap.put(FamilyLogMdcKeys.REMOTE_IP, exchange.getRequest().getRemoteAddress() == null ? null :
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
        for (Map.Entry<String, String> entry : FamilyLogMdcKeys.propagationHeaderToKeyMap().entrySet()) {
            if (FamilyLogMdcKeys.HEADER_TRACE_ID.equals(entry.getKey()) || FamilyLogMdcKeys.HEADER_REQUEST_ID.equals(entry.getKey())) {
                continue;
            }
            String headerValue = headers.getFirst(entry.getKey());
            if (!FamilyLogUtil.isBlank(headerValue)) {
                contextMap.put(entry.getValue(), headerValue);
            }
        }
        return contextMap;
    }

    /**
     * 将 trace 相关响应头写回客户端。
     *
     * @param headers 响应头
     */
    private void writeResponseHeaders(HttpHeaders headers) {
        if (!properties.isResponseTraceHeaderEnabled()) {
            return;
        }
        headers.set(FamilyLogMdcKeys.HEADER_TRACE_ID, FamilyLogUtil.traceId());
        headers.set(FamilyLogMdcKeys.HEADER_REQUEST_ID, FamilyLogUtil.requestId());
    }
}
