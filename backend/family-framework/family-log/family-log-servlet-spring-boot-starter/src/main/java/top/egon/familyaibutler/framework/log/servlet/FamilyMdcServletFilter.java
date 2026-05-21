/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.servlet
 * @FileName: FamilyMdcServletFilter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:10
 * @Description: Servlet MDC 过滤器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import top.egon.familyaibutler.framework.log.core.FamilyLogContext;
import top.egon.familyaibutler.framework.log.core.FamilyLogMdcKeys;
import top.egon.familyaibutler.framework.log.core.FamilyLogProperties;
import top.egon.familyaibutler.framework.log.core.FamilyLogUtil;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.servlet
 * @ClassName: FamilyMdcServletFilter
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:10
 * @Description: Servlet MDC 过滤器
 * @Version: 1.0
 */
public class FamilyMdcServletFilter extends OncePerRequestFilter {

    private final FamilyLogProperties properties;

    public FamilyMdcServletFilter(FamilyLogProperties properties) {
        this.properties = properties;
    }

    /**
     * 为 Servlet 请求填充并清理日志上下文。
     *
     * @param request     Servlet 请求
     * @param response    Servlet 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        FamilyLogContext previousContext = FamilyLogContext.capture();
        FamilyLogContext requestContext = FamilyLogContext.fromMap(buildRequestContext(request));
        try {
            requestContext.applyToMdc();
            writeResponseHeaders(response);
            filterChain.doFilter(request, response);
        } finally {
            previousContext.writeToMdc();
        }
    }

    /**
     * 构建当前请求的日志上下文。
     *
     * @param request Servlet 请求
     * @return Map<String, String> 返回请求日志上下文
     */
    private Map<String, String> buildRequestContext(HttpServletRequest request) {
        Map<String, String> contextMap = new LinkedHashMap<>();
        String traceId = FamilyLogUtil.normalizeTraceId(FamilyLogUtil.firstNonBlank(properties.getTraceHeaderNames(), request::getHeader));
        String requestId = FamilyLogUtil.firstNonBlank(properties.getRequestIdHeaderNames(), request::getHeader);
        contextMap.put(FamilyLogMdcKeys.TRACE_ID, FamilyLogUtil.isBlank(traceId) ? FamilyLogUtil.newId() : traceId);
        contextMap.put(FamilyLogMdcKeys.REQUEST_ID, FamilyLogUtil.isBlank(requestId) ? FamilyLogUtil.newId() : requestId);
        contextMap.put(FamilyLogMdcKeys.REQUEST_METHOD, request.getMethod());
        contextMap.put(FamilyLogMdcKeys.REQUEST_URI, request.getRequestURI());
        contextMap.put(FamilyLogMdcKeys.REMOTE_IP, resolveRemoteIp(request));
        fillHeaderContext(contextMap, request);
        return contextMap;
    }

    /**
     * 从请求头中补齐身份透传上下文。
     *
     * @param contextMap 日志上下文
     * @param request    Servlet 请求
     */
    private void fillHeaderContext(Map<String, String> contextMap, HttpServletRequest request) {
        for (Map.Entry<String, String> entry : FamilyLogMdcKeys.propagationHeaderToKeyMap().entrySet()) {
            if (FamilyLogMdcKeys.HEADER_TRACE_ID.equals(entry.getKey()) || FamilyLogMdcKeys.HEADER_REQUEST_ID.equals(entry.getKey())) {
                continue;
            }
            String headerValue = request.getHeader(entry.getKey());
            if (!FamilyLogUtil.isBlank(headerValue)) {
                contextMap.put(entry.getValue(), headerValue);
            }
        }
    }

    /**
     * 将 trace 相关响应头写回客户端。
     *
     * @param response Servlet 响应
     */
    private void writeResponseHeaders(HttpServletResponse response) {
        if (!properties.isResponseTraceHeaderEnabled()) {
            return;
        }
        response.setHeader(FamilyLogMdcKeys.HEADER_TRACE_ID, FamilyLogUtil.traceId());
        response.setHeader(FamilyLogMdcKeys.HEADER_REQUEST_ID, FamilyLogUtil.requestId());
    }

    /**
     * 解析真实客户端 IP。
     *
     * @param request Servlet 请求
     * @return String 返回真实客户端 IP
     */
    private String resolveRemoteIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (!FamilyLogUtil.isBlank(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
