/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.filter
 * @FileName: ApiDocConsoleServletFilter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-18:05
 * @Description: OpenAPI 调试文档控制台 Servlet 过滤器文件
 * @Version: 1.0
 */
package top.egon.openapi.console.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import top.egon.openapi.console.ApiDocConsoleProperties;

import java.io.IOException;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.filter
 * @ClassName: ApiDocConsoleServletFilter
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-18:05
 * @Description: OpenAPI 调试文档控制台 Servlet 静态资源保护过滤器
 * @Version: 1.0
 */
@RequiredArgsConstructor
public class ApiDocConsoleServletFilter implements Filter {

    private final ApiDocConsoleProperties properties;

    /**
     * 过滤 Servlet 控制台请求
     *
     * @param request  Servlet 请求
     * @param response Servlet 响应
     * @param chain    过滤器链
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!properties.isEnabled()) {
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        chain.doFilter(request, response);
    }
}
