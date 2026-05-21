/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource
 * @FileName: UaaResourceAuthorizationFilter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:45
 * @Description: UAA 资源授权过滤器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.resource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionRequest;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionResponse;

import java.io.IOException;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource
 * @ClassName: UaaResourceAuthorizationFilter
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:45
 * @Description: UAA 资源授权过滤器
 * @Version: 1.0
 */
public class UaaResourceAuthorizationFilter extends OncePerRequestFilter {
    private final UaaResourceServerProperties properties;
    private final UaaResourceAuthorizationClient authorizationClient;
    private final Environment environment;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 创建 UAA 资源授权过滤器。
     *
     * @param properties          UAA 资源服务配置
     * @param authorizationClient UAA 资源授权客户端
     * @param environment         环境变量
     */
    public UaaResourceAuthorizationFilter(UaaResourceServerProperties properties,
                                          UaaResourceAuthorizationClient authorizationClient,
                                          Environment environment) {
        this.properties = properties;
        this.authorizationClient = authorizationClient;
        this.environment = environment;
    }

    /**
     * 执行资源授权过滤。
     *
     * @param request     请求对象
     * @param response    响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isPermit(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "EMPTY_TOKEN");
            return;
        }
        AuthorizationDecisionResponse decision = authorizationClient.decide(new AuthorizationDecisionRequest(authorization,
                resolveServiceName(), request.getRequestURI(), request.getMethod()));
        if (decision == null || !decision.allowed()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, decision == null ? "UAA_DECISION_UNAVAILABLE" : decision.reason());
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 判断请求是否在放行列表内。
     *
     * @param requestUri 请求路径
     * @return true 表示放行
     */
    private boolean isPermit(String requestUri) {
        for (String pattern : properties.getPermitPatterns()) {
            if (antPathMatcher.match(pattern, requestUri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析资源服务名称。
     *
     * @return 服务名称
     */
    private String resolveServiceName() {
        if (StringUtils.hasText(properties.getServiceName())) {
            return properties.getServiceName();
        }
        return environment.getProperty("spring.application.name", "");
    }
}
