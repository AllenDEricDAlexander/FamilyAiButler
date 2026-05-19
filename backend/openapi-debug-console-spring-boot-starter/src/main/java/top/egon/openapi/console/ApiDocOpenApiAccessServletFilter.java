/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @FileName: ApiDocOpenApiAccessServletFilter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-23:20
 * @Description: OpenAPI JSON 内部访问控制 Servlet 过滤器文件
 * @Version: 1.0
 */
package top.egon.openapi.console;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @ClassName: ApiDocOpenApiAccessServletFilter
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-23:20
 * @Description: OpenAPI JSON 内部访问控制 Servlet 过滤器
 * @Version: 1.0
 */
@RequiredArgsConstructor
public class ApiDocOpenApiAccessServletFilter implements Filter {

    private final ApiDocConsoleProperties properties;

    /**
     * 过滤 OpenAPI JSON 请求
     *
     * @param request  Servlet 请求
     * @param response Servlet 响应
     * @param chain    过滤器链
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }
        ApiDocConsoleProperties.AccessControl accessControl = properties.getProducer().getAccessControl();
        if (!accessControl.isEnabled() || !isOpenApiPath(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        String inputToken = httpRequest.getHeader(accessControl.getHeaderName());
        if (StringUtils.hasText(inputToken) && constantEquals(accessControl.getToken(), inputToken)) {
            chain.doFilter(request, response);
            return;
        }
        httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * 判断是否为 OpenAPI JSON 请求
     *
     * @param request HTTP 请求
     * @return boolean 返回 true 表示 OpenAPI JSON 请求
     */
    private boolean isOpenApiPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return "/v3/api-docs".equals(path) || path.startsWith("/v3/api-docs/");
    }

    /**
     * 常量时间字符串比较
     *
     * @param left  左侧字符串
     * @param right 右侧字符串
     * @return boolean 返回 true 表示相等
     */
    private boolean constantEquals(String left, String right) {
        if (!StringUtils.hasText(left) || right == null) {
            return false;
        }
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }
}
