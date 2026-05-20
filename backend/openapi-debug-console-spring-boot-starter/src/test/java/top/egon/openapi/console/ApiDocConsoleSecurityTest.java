/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @FileName: ApiDocConsoleSecurityTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-23:05
 * @Description: OpenAPI 调试文档控制台安全能力测试文件
 * @Version: 1.0
 */
package top.egon.openapi.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import reactor.core.publisher.Mono;
import top.egon.openapi.console.client.ApiDocConsoleHttpClient;
import top.egon.openapi.console.client.ApiDocConsoleHttpResponse;
import top.egon.openapi.console.core.ApiDocConsoleDocumentRenderer;
import top.egon.openapi.console.core.ApiDocConsoleService;
import top.egon.openapi.console.core.ApiDocConsoleSessionService;
import top.egon.openapi.console.filter.ApiDocOpenApiAccessServletFilter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @ClassName: ApiDocConsoleSecurityTest
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-23:05
 * @Description: OpenAPI 调试文档控制台安全能力测试
 * @Version: 1.0
 */
class ApiDocConsoleSecurityTest {

    /**
     * 测试开启控制台时拒绝默认账号和签名密钥
     */
    @Test
    void testRejectDefaultConsoleCredentialWhenEnabled() {
        ApiDocConsoleProperties properties = new ApiDocConsoleProperties();
        properties.setEnabled(true);

        Assertions.assertThrows(IllegalStateException.class,
                () -> new ApiDocConsoleSessionService(properties, new MockEnvironment()));
    }

    /**
     * 测试登录失败达到阈值后锁定账号来源
     */
    @Test
    void testLockLoginAfterFailures() {
        ApiDocConsoleSessionService sessionService = new ApiDocConsoleSessionService(secureProperties(), new MockEnvironment());

        Assertions.assertFalse(sessionService.validateLogin("admin", "bad", "127.0.0.1"));
        Assertions.assertFalse(sessionService.validateLogin("admin", "bad", "127.0.0.1"));
        Assertions.assertFalse(sessionService.validateLogin("admin", "SecurePassword@2026", "127.0.0.1"));
    }

    /**
     * 测试生产环境强制使用 Secure Cookie
     */
    @Test
    void testProdCookieForcesSecure() {
        ApiDocConsoleProperties properties = secureProperties();
        properties.setEnvironment("prod");
        ApiDocConsoleSessionService sessionService = new ApiDocConsoleSessionService(properties, new MockEnvironment());

        ResponseCookie cookie = sessionService.createSessionCookie("admin");

        Assertions.assertTrue(cookie.toString().contains("Secure"));
    }

    /**
     * 测试业务模块 OpenAPI JSON 访问缺少内部 Token 时返回 404
     */
    @Test
    void testOpenApiAccessServletFilterRejectsMissingToken() throws Exception {
        ApiDocOpenApiAccessServletFilter filter = new ApiDocOpenApiAccessServletFilter(secureProperties());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v3/api-docs");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        Assertions.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }

    /**
     * 测试业务模块 OpenAPI JSON 访问携带内部 Token 时放行
     */
    @Test
    void testOpenApiAccessServletFilterAllowsInternalToken() throws Exception {
        ApiDocConsoleProperties properties = secureProperties();
        ApiDocOpenApiAccessServletFilter filter = new ApiDocOpenApiAccessServletFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v3/api-docs");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(properties.getProducer().getAccessControl().getHeaderName(), "internal-openapi-token");

        filter.doFilter(request, response, new MockFilterChain());

        Assertions.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    /**
     * 测试网关聚合 OpenAPI JSON 时自动携带服务内部 Header
     */
    @Test
    void testFetchOpenApiUsesConfiguredInternalHeader() throws Exception {
        AtomicReference<String> accessToken = new AtomicReference<>();
        ApiDocConsoleProperties properties = secureProperties();
        ApiDocConsoleProperties.ServiceRoute route = new ApiDocConsoleProperties.ServiceRoute();
        route.setId("demo");
        route.setName("demo");
        route.setOpenApiUrl("http://demo/v3/api-docs");
        route.setBaseUrl("http://demo");
        route.setOpenApiAccessToken("internal-openapi-token");
        properties.getServices().add(route);
        ApiDocConsoleHttpClient httpClient = request -> {
            accessToken.set(request.getHeaders().getFirst("X-OpenAPI-Console-Token"));
            ApiDocConsoleHttpResponse response = new ApiDocConsoleHttpResponse();
            response.setStatus(HttpStatus.OK.value());
            response.setBody("{\"openapi\":\"3.1.0\",\"info\":{\"title\":\"demo\",\"version\":\"v1\"},\"paths\":{}}");
            return Mono.just(response);
        };

        ApiDocConsoleService consoleService = new ApiDocConsoleService(
                properties,
                new ObjectMapper(),
                httpClient,
                new ApiDocConsoleDocumentRenderer(),
                new DefaultListableBeanFactory().getBeanProvider(org.springframework.cloud.client.discovery.ReactiveDiscoveryClient.class),
                new DefaultListableBeanFactory().getBeanProvider(org.springframework.cloud.client.discovery.DiscoveryClient.class));

        consoleService.fetchOpenApi("demo").block();

        Assertions.assertEquals("internal-openapi-token", accessToken.get());
    }

    /**
     * 创建安全测试配置
     *
     * @return ApiDocConsoleProperties 返回安全测试配置
     */
    private ApiDocConsoleProperties secureProperties() {
        ApiDocConsoleProperties properties = new ApiDocConsoleProperties();
        properties.setEnabled(true);
        properties.getAuth().setUsername("admin");
        properties.getAuth().setPassword("SecurePassword@2026");
        properties.getAuth().setSessionSecret("SecureSessionSecretForOpenApiConsole2026");
        properties.getAuth().setMaxLoginFailures(2);
        properties.getAuth().setLoginLockDuration(Duration.ofMinutes(10));
        properties.getProducer().getAccessControl().setEnabled(true);
        properties.getProducer().getAccessControl().setToken("internal-openapi-token");
        return properties;
    }
}
