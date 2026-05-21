/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.webflux
 * @FileName: FamilyMdcGatewayGlobalFilter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:10
 * @Description: Gateway MDC 全局过滤器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.webflux;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
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
 * @ClassName: FamilyMdcGatewayGlobalFilter
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:10
 * @Description: Gateway MDC 全局过滤器
 * @Version: 1.0
 */
public class FamilyMdcGatewayGlobalFilter implements GlobalFilter, Ordered {

    private final FamilyLogProperties properties;

    public FamilyMdcGatewayGlobalFilter(FamilyLogProperties properties) {
        this.properties = properties;
    }

    /**
     * 在 Gateway 过滤器链最前方建立日志上下文。
     *
     * @param exchange 网关请求上下文
     * @param chain    网关过滤器链
     * @return Mono<Void> 返回过滤执行结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        FamilyLogContext previousContext = FamilyLogContext.capture();
        FamilyLogContext requestContext = FamilyLogContext.fromMap(buildRequestContext(exchange));
        return Mono.defer(() -> {
                    requestContext.applyToMdc();
                    writeResponseHeaders(exchange.getResponse().getHeaders());
                    ServerWebExchange routedExchange = mutateRouteRequest(exchange, requestContext.contextMap());
                    return chain.filter(routedExchange);
                })
                .contextWrite(context -> context.put(FamilyLogMdcKeys.REACTOR_CONTEXT_KEY, requestContext.contextMap()))
                .doFinally(signalType -> previousContext.writeToMdc());
    }

    /**
     * 获取 Gateway 过滤器执行顺序。
     *
     * @return int 返回 Gateway 过滤器执行顺序
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 20;
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
        fillHeaderContext(contextMap, headers);
        return contextMap;
    }

    /**
     * 从请求头中补齐身份透传上下文。
     *
     * @param contextMap 日志上下文
     * @param headers    请求头
     */
    private void fillHeaderContext(Map<String, String> contextMap, HttpHeaders headers) {
        for (Map.Entry<String, String> entry : FamilyLogMdcKeys.propagationHeaderToKeyMap().entrySet()) {
            if (FamilyLogMdcKeys.HEADER_TRACE_ID.equals(entry.getKey()) || FamilyLogMdcKeys.HEADER_REQUEST_ID.equals(entry.getKey())) {
                continue;
            }
            String headerValue = headers.getFirst(entry.getKey());
            if (!FamilyLogUtil.isBlank(headerValue)) {
                contextMap.put(entry.getValue(), headerValue);
            }
        }
    }

    /**
     * 将日志上下文写入 Gateway 下游路由请求头。
     *
     * @param exchange   网关请求上下文
     * @param contextMap 日志上下文
     * @return ServerWebExchange 返回携带透传头的请求上下文
     */
    private ServerWebExchange mutateRouteRequest(ServerWebExchange exchange, Map<String, String> contextMap) {
        return exchange.mutate().request(builder -> builder.headers(headers -> {
            for (Map.Entry<String, String> entry : FamilyLogMdcKeys.propagationHeaderToKeyMap().entrySet()) {
                if (!properties.getPropagationHeaderNames().contains(entry.getKey())) {
                    continue;
                }
                String headerValue = contextMap.get(entry.getValue());
                if (!FamilyLogUtil.isBlank(headerValue)) {
                    headers.set(entry.getKey(), headerValue);
                }
            }
        })).build();
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
