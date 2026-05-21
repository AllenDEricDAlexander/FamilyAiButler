/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.http
 * @FileName: FamilyMdcWebClientCustomizer.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:10
 * @Description: WebClient MDC 自定义器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.http;

import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.context.ContextView;
import top.egon.familyaibutler.framework.log.core.FamilyLogMdcKeys;
import top.egon.familyaibutler.framework.log.core.FamilyLogProperties;
import top.egon.familyaibutler.framework.log.core.FamilyLogUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.http
 * @ClassName: FamilyMdcWebClientCustomizer
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:10
 * @Description: WebClient MDC 自定义器
 * @Version: 1.0
 */
public class FamilyMdcWebClientCustomizer implements WebClientCustomizer {

    private final FamilyLogProperties properties;

    public FamilyMdcWebClientCustomizer(FamilyLogProperties properties) {
        this.properties = properties;
    }

    /**
     * 为 WebClient 增加日志链路透传过滤器。
     *
     * @param builder WebClient 构建器
     */
    @Override
    public void customize(WebClient.Builder builder) {
        builder.filter((request, next) -> reactor.core.publisher.Mono.deferContextual(contextView -> {
            ClientRequest.Builder requestBuilder = ClientRequest.from(request);
            applyHeaders(requestBuilder, mergeContext(contextView));
            return next.exchange(requestBuilder.build());
        }));
    }

    /**
     * 合并线程 MDC 与 Reactor Context 中的日志上下文。
     *
     * @param contextView Reactor Context
     * @return Map<String, String> 返回合并后的日志上下文
     */
    private Map<String, String> mergeContext(ContextView contextView) {
        Map<String, String> contextMap = new LinkedHashMap<>(FamilyLogUtil.copyFamilyContext());
        if (contextView.hasKey(FamilyLogMdcKeys.REACTOR_CONTEXT_KEY)) {
            contextMap.putAll(contextView.get(FamilyLogMdcKeys.REACTOR_CONTEXT_KEY));
        }
        return contextMap;
    }

    /**
     * 将 MDC 中的透传字段写入请求头。
     *
     * @param requestBuilder 请求构建器
     * @param contextMap     日志上下文
     */
    private void applyHeaders(ClientRequest.Builder requestBuilder, Map<String, String> contextMap) {
        requestBuilder.headers(headers -> {
            for (Map.Entry<String, String> entry : FamilyLogMdcKeys.propagationHeaderToKeyMap().entrySet()) {
                if (!properties.getPropagationHeaderNames().contains(entry.getKey())) {
                    continue;
                }
                String headerValue = contextMap.get(entry.getValue());
                if (!FamilyLogUtil.isBlank(headerValue)) {
                    headers.set(entry.getKey(), headerValue);
                }
            }
        });
    }
}
