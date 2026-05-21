/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.http
 * @FileName: FamilyMdcRestClientCustomizer.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:10
 * @Description: RestClient MDC 自定义器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.http;

import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import top.egon.familyaibutler.framework.log.core.FamilyLogMdcKeys;
import top.egon.familyaibutler.framework.log.core.FamilyLogProperties;
import top.egon.familyaibutler.framework.log.core.FamilyLogUtil;

import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.http
 * @ClassName: FamilyMdcRestClientCustomizer
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:10
 * @Description: RestClient MDC 自定义器
 * @Version: 1.0
 */
public class FamilyMdcRestClientCustomizer implements RestClientCustomizer {

    private final FamilyLogProperties properties;

    public FamilyMdcRestClientCustomizer(FamilyLogProperties properties) {
        this.properties = properties;
    }

    /**
     * 为 RestClient 增加日志链路透传拦截器。
     *
     * @param builder RestClient 构建器
     */
    @Override
    public void customize(RestClient.Builder builder) {
        builder.requestInterceptor((request, body, execution) -> {
            applyHeaders(request.getHeaders(), FamilyLogUtil.copyFamilyContext());
            return execution.execute(request, body);
        });
    }

    /**
     * 将 MDC 中的透传字段写入请求头。
     *
     * @param headers    请求头
     * @param contextMap 日志上下文
     */
    private void applyHeaders(HttpHeaders headers, Map<String, String> contextMap) {
        for (Map.Entry<String, String> entry : FamilyLogMdcKeys.propagationHeaderToKeyMap().entrySet()) {
            if (!properties.getPropagationHeaderNames().contains(entry.getKey())) {
                continue;
            }
            String headerValue = contextMap.get(entry.getValue());
            if (!FamilyLogUtil.isBlank(headerValue)) {
                headers.set(entry.getKey(), headerValue);
            }
        }
    }
}
