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

    /**
     * 测试 Servlet 环境控制台可以通过 MVC Controller 执行调试请求
     */
    @Test
    void testMvcControllerExecuteWithSession() {
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
        ApiDocConsoleMvcController controller = new ApiDocConsoleMvcController(properties, sessionService, consoleService);
        ApiDocConsolePayloads.ExecuteRequest executeRequest = new ApiDocConsolePayloads.ExecuteRequest();
        executeRequest.setServiceId("demo");
        executeRequest.setMethod("GET");
        executeRequest.setPath("/hello");
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        String sessionCookie = sessionService.createSessionCookie("admin").toString().split(";", 2)[0];
        String[] cookieParts = sessionCookie.split("=", 2);
        servletRequest.setCookies(new Cookie(cookieParts[0], cookieParts[1]));

        ResponseEntity<Object> response = controller.execute(executeRequest, servletRequest);

        Assertions.assertEquals(200, response.getStatusCode().value());
        Assertions.assertInstanceOf(ApiDocConsolePayloads.ExecuteResponse.class, response.getBody());
        Assertions.assertEquals("mvc-ok", ((ApiDocConsolePayloads.ExecuteResponse) response.getBody()).getBody());
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
        ApiDocConsoleProperties.ServiceRoute route = new ApiDocConsoleProperties.ServiceRoute();
        route.setId("demo");
        route.setName("demo");
        route.setOpenApiUrl("http://demo/v3/api-docs");
        route.setBaseUrl("http://demo");
        properties.getServices().add(route);
        return properties;
    }
}
