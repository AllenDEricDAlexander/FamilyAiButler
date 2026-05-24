/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @FileName: ApiDocConsoleMvcControllerTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:35
 * @Description: OpenAPI 调试文档控制台 MVC 控制器测试文件
 * @Version: 1.0
 */
package top.egon.openapi.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import reactor.core.publisher.Mono;
import top.egon.openapi.console.client.ApiDocConsoleHttpResponse;
import top.egon.openapi.console.core.ApiDocConsoleDocumentRenderer;
import top.egon.openapi.console.core.ApiDocConsoleService;
import top.egon.openapi.console.core.ApiDocConsoleSessionService;
import top.egon.openapi.console.web.ApiDocConsoleMvcController;

import java.time.Duration;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @ClassName: ApiDocConsoleMvcControllerTest
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:35
 * @Description: OpenAPI 调试文档控制台 MVC 控制器测试
 * @Version: 1.0
 */
class ApiDocConsoleMvcControllerTest {

    private static final String OPENAPI_JSON = """
            {"openapi":"3.0.3","info":{"title":"Demo API","version":"v1"},"paths":{}}
            """;

    /**
     * 测试 Servlet 环境控制台可以通过 MVC Controller 执行调试请求
     */
    @Test
    void testMvcControllerExecuteWithSession() throws Exception {
        ApiDocConsoleProperties properties = secureProperties();
        ApiDocConsoleSessionService sessionService = new ApiDocConsoleSessionService(properties, new MockEnvironment());
        ApiDocConsoleService consoleService = new ApiDocConsoleService(
                properties,
                new ObjectMapper(),
                request -> {
                    ApiDocConsoleHttpResponse response = new ApiDocConsoleHttpResponse();
                    response.setStatus(200);
                    response.setDurationMillis(12);
                    response.setBody("mvc-ok");
                    return Mono.just(response);
                },
                new ApiDocConsoleDocumentRenderer(),
                new DefaultListableBeanFactory().getBeanProvider(org.springframework.cloud.client.discovery.ReactiveDiscoveryClient.class),
                new DefaultListableBeanFactory().getBeanProvider(org.springframework.cloud.client.discovery.DiscoveryClient.class));
        ApiDocConsoleMvcController controller = new ApiDocConsoleMvcController(properties, sessionService, consoleService, new ObjectMapper());
        ApiDocConsolePayloads.ExecuteRequest executeRequest = new ApiDocConsolePayloads.ExecuteRequest();
        executeRequest.setServiceId("demo");
        executeRequest.setMethod("GET");
        executeRequest.setPath("/hello");
        String requestBody = new ObjectMapper().writeValueAsString(executeRequest);
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        String sessionCookie = sessionService.createSessionCookie("admin").toString().split(";", 2)[0];
        String[] cookieParts = sessionCookie.split("=", 2);
        servletRequest.setCookies(new Cookie(cookieParts[0], cookieParts[1]));

        ResponseEntity<Object> response = controller.execute(requestBody, servletRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertInstanceOf(ApiDocConsolePayloads.ExecuteResponse.class, response.getBody());
        Assertions.assertEquals("mvc-ok", ((ApiDocConsolePayloads.ExecuteResponse) response.getBody()).getBody());
    }

    /**
     * 测试只读模式仍允许按服务范围导出 OpenAPI JSON。
     */
    @Test
    void testMvcControllerExportOpenApiJsonInReadOnlyMode() {
        ApiDocConsoleProperties properties = secureProperties();
        properties.setMode(ApiDocConsoleProperties.Mode.READ_ONLY);
        ApiDocConsoleSessionService sessionService = new ApiDocConsoleSessionService(properties, new MockEnvironment());
        ApiDocConsoleMvcController controller = new ApiDocConsoleMvcController(properties, sessionService,
                exportConsoleService(properties), new ObjectMapper());
        MockHttpServletRequest servletRequest = signedExportRequest(sessionService, "format=openapi-json&scope=service");

        ResponseEntity<byte[]> response = controller.export("demo", "openapi-json", "service", servletRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8", response.getHeaders().getContentType().toString());
        Assertions.assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("demo.openapi.json"));
        Assertions.assertTrue(new String(response.getBody(), java.nio.charset.StandardCharsets.UTF_8).contains("\"openapi\":\"3.0.3\""));
    }

    /**
     * 测试 json 别名兼容导出 OpenAPI JSON。
     */
    @Test
    void testMvcControllerExportJsonAlias() {
        ApiDocConsoleProperties properties = secureProperties();
        ApiDocConsoleSessionService sessionService = new ApiDocConsoleSessionService(properties, new MockEnvironment());
        ApiDocConsoleMvcController controller = new ApiDocConsoleMvcController(properties, sessionService,
                exportConsoleService(properties), new ObjectMapper());
        MockHttpServletRequest servletRequest = signedExportRequest(sessionService, "format=json");

        ResponseEntity<byte[]> response = controller.export("demo", "json", "service", servletRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertEquals(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8", response.getHeaders().getContentType().toString());
        Assertions.assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("demo.openapi.json"));
    }

    /**
     * 测试导出接口拒绝非服务范围。
     */
    @Test
    void testMvcControllerExportRejectsUnsupportedScope() {
        ApiDocConsoleProperties properties = secureProperties();
        ApiDocConsoleSessionService sessionService = new ApiDocConsoleSessionService(properties, new MockEnvironment());
        ApiDocConsoleMvcController controller = new ApiDocConsoleMvcController(properties, sessionService,
                exportConsoleService(properties), new ObjectMapper());
        MockHttpServletRequest servletRequest = signedExportRequest(sessionService, "format=md&scope=operation");

        ResponseEntity<byte[]> response = controller.export("demo", "md", "operation", servletRequest);

        Assertions.assertEquals(400, response.getStatusCode().value());
    }

    /**
     * 创建安全控制台配置
     *
     * @return ApiDocConsoleProperties 返回安全控制台配置
     */
    private ApiDocConsoleProperties secureProperties() {
        ApiDocConsoleProperties properties = new ApiDocConsoleProperties();
        properties.setEnabled(true);
        properties.setMode(ApiDocConsoleProperties.Mode.FULL);
        properties.getAuth().setUsername("admin");
        properties.getAuth().setPassword("SecurePassword@2026");
        properties.getAuth().setSessionSecret("SecureSessionSecretForOpenApiConsole2026");
        properties.getAuth().setTtl(Duration.ofHours(1));
        properties.getAuth().setRequestSigningEnabled(false);
        ApiDocConsoleProperties.ServiceRoute route = new ApiDocConsoleProperties.ServiceRoute();
        route.setId("demo");
        route.setName("demo");
        route.setOpenApiUrl("http://demo/v3/api-docs");
        route.setBaseUrl("http://demo");
        properties.getServices().add(route);
        return properties;
    }

    /**
     * 创建导出测试控制台服务。
     *
     * @param properties 控制台配置
     * @return ApiDocConsoleService 返回控制台服务
     */
    private ApiDocConsoleService exportConsoleService(ApiDocConsoleProperties properties) {
        return new ApiDocConsoleService(
                properties,
                new ObjectMapper(),
                request -> {
                    ApiDocConsoleHttpResponse response = new ApiDocConsoleHttpResponse();
                    response.setStatus(200);
                    response.setDurationMillis(12);
                    response.setBody(OPENAPI_JSON);
                    return Mono.just(response);
                },
                new ApiDocConsoleDocumentRenderer(),
                new DefaultListableBeanFactory().getBeanProvider(org.springframework.cloud.client.discovery.ReactiveDiscoveryClient.class),
                new DefaultListableBeanFactory().getBeanProvider(org.springframework.cloud.client.discovery.DiscoveryClient.class));
    }

    /**
     * 创建携带登录 Cookie 的导出请求。
     *
     * @param sessionService 会话服务
     * @param queryString    查询串
     * @return MockHttpServletRequest 返回 Servlet 请求
     */
    private MockHttpServletRequest signedExportRequest(ApiDocConsoleSessionService sessionService, String queryString) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.setMethod("GET");
        servletRequest.setRequestURI("/openapi-console/api/export/demo");
        servletRequest.setQueryString(queryString);
        String sessionCookie = sessionService.createSessionCookie("admin").toString().split(";", 2)[0];
        String[] cookieParts = sessionCookie.split("=", 2);
        servletRequest.setCookies(new Cookie(cookieParts[0], cookieParts[1]));
        return servletRequest;
    }
}
