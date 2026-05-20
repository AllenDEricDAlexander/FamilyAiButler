/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @FileName: ApiDocConsoleClientTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:30
 * @Description: OpenAPI 调试文档控制台客户端能力测试文件
 * @Version: 1.0
 */
package top.egon.openapi.console;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import top.egon.openapi.console.client.ApiDocConsoleHttpClient;
import top.egon.openapi.console.client.ApiDocConsoleHttpRequest;
import top.egon.openapi.console.client.ApiDocConsoleHttpResponse;
import top.egon.openapi.console.client.ApiDocConsoleVirtualThreadHttpClient;
import top.egon.openapi.console.core.ApiDocConsoleDocumentRenderer;
import top.egon.openapi.console.core.ApiDocConsoleService;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @ClassName: ApiDocConsoleClientTest
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:30
 * @Description: OpenAPI 调试文档控制台客户端能力测试
 * @Version: 1.0
 */
class ApiDocConsoleClientTest {

    /**
     * 测试普通执行读取响应体，压测执行不读取完整响应体
     */
    @Test
    void testLoadTestUsesLightweightHttpRequest() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        ApiDocConsoleService consoleService = consoleService(properties("http://demo"), httpClient, null);
        ApiDocConsolePayloads.ExecuteRequest executeRequest = executeRequest("demo", "/hello");

        ApiDocConsolePayloads.ExecuteResponse executeResponse = consoleService.execute(executeRequest).block(Duration.ofSeconds(3));
        ApiDocConsolePayloads.LoadTestRequest loadTestRequest = new ApiDocConsolePayloads.LoadTestRequest();
        loadTestRequest.setRequest(executeRequest);
        loadTestRequest.setTotalRequests(3);
        loadTestRequest.setConcurrency(2);
        ApiDocConsolePayloads.LoadTestResult loadTestResult = consoleService.loadTest(loadTestRequest).block(Duration.ofSeconds(3));

        Assertions.assertNotNull(executeResponse);
        Assertions.assertEquals("OK", executeResponse.getBody());
        Assertions.assertNotNull(loadTestResult);
        Assertions.assertEquals(3, loadTestResult.getTotal());
        Assertions.assertTrue(httpClient.requests.get(0).isReadResponseBody());
        Assertions.assertTrue(httpClient.requests.subList(1, httpClient.requests.size()).stream().noneMatch(ApiDocConsoleHttpRequest::isReadResponseBody));
    }

    /**
     * 测试服务发现实例使用轮询方式分摊请求
     */
    @Test
    void testDiscoveryInstancesUseRoundRobin() {
        RecordingHttpClient httpClient = new RecordingHttpClient();
        StaticDiscoveryClient discoveryClient = new StaticDiscoveryClient(List.of(
                new SimpleServiceInstance("demo", URI.create("http://127.0.0.1:18081")),
                new SimpleServiceInstance("demo", URI.create("http://127.0.0.1:18082"))));
        ApiDocConsoleService consoleService = consoleService(properties("http://demo"), httpClient, discoveryClient);

        consoleService.execute(executeRequest("demo", "/users")).block(Duration.ofSeconds(3));
        consoleService.execute(executeRequest("demo", "/users")).block(Duration.ofSeconds(3));

        Assertions.assertEquals(18081, httpClient.requests.get(0).getUri().getPort());
        Assertions.assertEquals(18082, httpClient.requests.get(1).getUri().getPort());
    }

    /**
     * 测试虚拟线程 HTTP 客户端可以创建并关闭
     */
    @Test
    void testVirtualThreadHttpClientCanBeCreatedAndClosed() {
        ApiDocConsoleVirtualThreadHttpClient httpClient = new ApiDocConsoleVirtualThreadHttpClient(new ApiDocConsoleProperties());

        Assertions.assertDoesNotThrow(httpClient::close);
    }

    /**
     * 创建控制台服务
     *
     * @param properties      控制台配置
     * @param httpClient      HTTP 客户端
     * @param discoveryClient 服务发现客户端
     * @return ApiDocConsoleService 返回控制台服务
     */
    private ApiDocConsoleService consoleService(ApiDocConsoleProperties properties,
                                                ApiDocConsoleHttpClient httpClient,
                                                ReactiveDiscoveryClient discoveryClient) {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        if (discoveryClient != null) {
            beanFactory.registerSingleton("reactiveDiscoveryClient", discoveryClient);
        }
        return new ApiDocConsoleService(
                properties,
                new ObjectMapper(),
                httpClient,
                new ApiDocConsoleDocumentRenderer(),
                beanFactory.getBeanProvider(ReactiveDiscoveryClient.class),
                beanFactory.getBeanProvider(org.springframework.cloud.client.discovery.DiscoveryClient.class));
    }

    /**
     * 创建控制台配置
     *
     * @param baseUrl 服务基础地址
     * @return ApiDocConsoleProperties 返回控制台配置
     */
    private ApiDocConsoleProperties properties(String baseUrl) {
        ApiDocConsoleProperties properties = new ApiDocConsoleProperties();
        ApiDocConsoleProperties.ServiceRoute route = new ApiDocConsoleProperties.ServiceRoute();
        route.setId("demo");
        route.setName("demo");
        route.setOpenApiUrl(baseUrl + "/v3/api-docs");
        route.setBaseUrl(baseUrl);
        properties.getServices().add(route);
        properties.getLoadTest().setMaxRequests(10);
        properties.getLoadTest().setMaxConcurrency(3);
        properties.getLoadTest().setMaxActiveRuns(2);
        properties.getLoadTest().setMaxActiveConcurrency(3);
        return properties;
    }

    /**
     * 创建调试请求
     *
     * @param serviceId 服务 ID
     * @param path      请求路径
     * @return ExecuteRequest 返回调试请求
     */
    private ApiDocConsolePayloads.ExecuteRequest executeRequest(String serviceId, String path) {
        ApiDocConsolePayloads.ExecuteRequest request = new ApiDocConsolePayloads.ExecuteRequest();
        request.setServiceId(serviceId);
        request.setMethod("GET");
        request.setPath(path);
        return request;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: RecordingHttpClient
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-20Day-00:30
     * @Description: 记录请求的测试 HTTP 客户端
     * @Version: 1.0
     */
    private static class RecordingHttpClient implements ApiDocConsoleHttpClient {

        private final List<ApiDocConsoleHttpRequest> requests = new ArrayList<>();

        /**
         * 执行并记录请求
         *
         * @param request HTTP 请求
         * @return Mono<ApiDocConsoleHttpResponse> 返回固定响应
         */
        @Override
        public Mono<ApiDocConsoleHttpResponse> execute(ApiDocConsoleHttpRequest request) {
            requests.add(request);
            ApiDocConsoleHttpResponse response = new ApiDocConsoleHttpResponse();
            response.setStatus(200);
            response.setDurationMillis(10);
            response.setBody(request.isReadResponseBody() ? "OK" : "");
            return Mono.just(response);
        }
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: StaticDiscoveryClient
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-20Day-00:30
     * @Description: 固定实例服务发现客户端
     * @Version: 1.0
     */
    private record StaticDiscoveryClient(List<ServiceInstance> instances) implements ReactiveDiscoveryClient {

        /**
         * 返回服务说明
         *
         * @return String 返回说明
         */
        @Override
        public String description() {
            return "static";
        }

        /**
         * 查询服务实例
         *
         * @param serviceId 服务 ID
         * @return Flux<ServiceInstance> 返回固定服务实例
         */
        @Override
        public Flux<ServiceInstance> getInstances(String serviceId) {
            return Flux.fromIterable(instances);
        }

        /**
         * 查询服务列表
         *
         * @return Flux<String> 返回服务列表
         */
        @Override
        public Flux<String> getServices() {
            return Flux.just("demo");
        }
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: SimpleServiceInstance
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-20Day-00:30
     * @Description: 简单服务实例
     * @Version: 1.0
     */
    private record SimpleServiceInstance(String serviceId, URI uri) implements ServiceInstance {

        /**
         * 获取实例 ID
         *
         * @return String 返回实例 ID
         */
        @Override
        public String getInstanceId() {
            return serviceId + "-" + uri.getPort();
        }

        /**
         * 获取服务 ID
         *
         * @return String 返回服务 ID
         */
        @Override
        public String getServiceId() {
            return serviceId;
        }

        /**
         * 获取主机名
         *
         * @return String 返回主机名
         */
        @Override
        public String getHost() {
            return uri.getHost();
        }

        /**
         * 获取端口
         *
         * @return int 返回端口
         */
        @Override
        public int getPort() {
            return uri.getPort();
        }

        /**
         * 是否 HTTPS
         *
         * @return boolean 返回是否 HTTPS
         */
        @Override
        public boolean isSecure() {
            return "https".equalsIgnoreCase(uri.getScheme());
        }

        /**
         * 获取元数据
         *
         * @return Map<String, String> 返回元数据
         */
        @Override
        public Map<String, String> getMetadata() {
            return Map.of();
        }

        /**
         * 获取实例 URI
         *
         * @return URI 返回实例 URI
         */
        @Override
        public URI getUri() {
            return uri;
        }
    }
}
