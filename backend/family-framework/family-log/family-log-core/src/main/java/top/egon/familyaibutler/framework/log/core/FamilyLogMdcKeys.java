/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @FileName: FamilyLogMdcKeys.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-20:40
 * @Description: 家庭日志 MDC 键常量文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @ClassName: FamilyLogMdcKeys
 * @Author: atluofu
 * @CreateTime: 2026-05-21 20:40
 * @Description: 家庭日志 MDC 键常量
 * @Version: 1.0
 */
public final class FamilyLogMdcKeys {
    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String PARENT_SPAN_ID = "parentSpanId";
    public static final String REQUEST_ID = "requestId";
    public static final String ACCOUNT_ID = "accountId";
    public static final String PROFILE_ID = "profileId";
    public static final String CLIENT_ID = "clientId";
    public static final String SESSION_ID = "sessionId";
    public static final String DEVICE_ID = "deviceId";
    public static final String RISK_LEVEL = "riskLevel";
    public static final String REQUEST_METHOD = "requestMethod";
    public static final String REQUEST_URI = "requestUri";
    public static final String REMOTE_IP = "remoteIp";
    public static final String RPC_SYSTEM = "rpcSystem";
    public static final String RPC_SERVICE = "rpcService";
    public static final String RPC_METHOD = "rpcMethod";
    public static final String HEADER_TRACE_ID = "X-Trace-Id";
    public static final String HEADER_B3_TRACE_ID = "X-B3-TraceId";
    public static final String HEADER_TRACE_PARENT = "traceparent";
    public static final String HEADER_REQUEST_ID = "X-Request-Id";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-Id";
    public static final String HEADER_ACCOUNT_ID = "X-Family-Account-Id";
    public static final String HEADER_PROFILE_ID = "X-Family-Profile-Id";
    public static final String HEADER_CLIENT_ID = "X-Family-Client-Id";
    public static final String HEADER_SESSION_ID = "X-Family-Session-Id";
    public static final String HEADER_DEVICE_ID = "X-Family-Device-Id";
    public static final String HEADER_RISK_LEVEL = "X-Family-Risk-Level";
    public static final String REACTOR_CONTEXT_KEY = "family.log.context";

    private static final List<String> DEFAULT_TRACE_HEADERS = List.of(HEADER_TRACE_ID, HEADER_B3_TRACE_ID, HEADER_TRACE_PARENT);
    private static final List<String> DEFAULT_REQUEST_ID_HEADERS = List.of(HEADER_REQUEST_ID, HEADER_CORRELATION_ID);
    private static final List<String> FAMILY_KEYS = List.of(
            TRACE_ID, SPAN_ID, PARENT_SPAN_ID, REQUEST_ID, ACCOUNT_ID, PROFILE_ID, CLIENT_ID, SESSION_ID,
            DEVICE_ID, RISK_LEVEL, REQUEST_METHOD, REQUEST_URI, REMOTE_IP, RPC_SYSTEM, RPC_SERVICE, RPC_METHOD
    );
    private static final Map<String, String> PROPAGATION_HEADER_TO_KEY = Map.ofEntries(
            Map.entry(HEADER_TRACE_ID, TRACE_ID),
            Map.entry(HEADER_REQUEST_ID, REQUEST_ID),
            Map.entry(HEADER_ACCOUNT_ID, ACCOUNT_ID),
            Map.entry(HEADER_PROFILE_ID, PROFILE_ID),
            Map.entry(HEADER_CLIENT_ID, CLIENT_ID),
            Map.entry(HEADER_SESSION_ID, SESSION_ID),
            Map.entry(HEADER_DEVICE_ID, DEVICE_ID),
            Map.entry(HEADER_RISK_LEVEL, RISK_LEVEL)
    );

    private FamilyLogMdcKeys() {
    }

    /**
     * 获取默认 trace 请求头列表。
     *
     * @return List<String> 返回默认 trace 请求头列表
     */
    public static List<String> defaultTraceHeaders() {
        return DEFAULT_TRACE_HEADERS;
    }

    /**
     * 获取默认 requestId 请求头列表。
     *
     * @return List<String> 返回默认 requestId 请求头列表
     */
    public static List<String> defaultRequestIdHeaders() {
        return DEFAULT_REQUEST_ID_HEADERS;
    }

    /**
     * 获取 family 日志组件维护的 MDC 键列表。
     *
     * @return List<String> 返回 family 日志 MDC 键列表
     */
    public static List<String> familyKeys() {
        return FAMILY_KEYS;
    }

    /**
     * 获取出站透传请求头名称列表。
     *
     * @return List<String> 返回出站透传请求头名称列表
     */
    public static List<String> propagationHeaders() {
        return List.copyOf(PROPAGATION_HEADER_TO_KEY.keySet());
    }

    /**
     * 获取透传请求头与 MDC 键的映射关系。
     *
     * @return Map<String, String> 返回透传请求头和 MDC 键映射
     */
    public static Map<String, String> propagationHeaderToKeyMap() {
        return new LinkedHashMap<>(PROPAGATION_HEADER_TO_KEY);
    }

}
