/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.client
 * @FileName: ApiDocConsoleVirtualThreadHttpClient.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:45
 * @Description: OpenAPI 调试文档控制台虚拟线程 HTTP 客户端文件
 * @Version: 1.0
 */
package top.egon.openapi.console.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import top.egon.openapi.console.ApiDocConsoleProperties;

import javax.net.ssl.SSLSession;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.client
 * @ClassName: ApiDocConsoleVirtualThreadHttpClient
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:45
 * @Description: OpenAPI 调试文档控制台虚拟线程 HTTP 客户端
 * @Version: 1.0
 */
public class ApiDocConsoleVirtualThreadHttpClient implements ApiDocConsoleHttpClient, AutoCloseable {

    private static final List<String> BLOCKED_HEADERS = List.of(
            HttpHeaders.HOST,
            HttpHeaders.CONTENT_LENGTH,
            HttpHeaders.TRANSFER_ENCODING
    );

    private final ExecutorService executorService;

    private final Scheduler scheduler;

    private final HttpClient httpClient;

    /**
     * 创建虚拟线程 HTTP 客户端
     *
     * @param properties 控制台配置
     */
    public ApiDocConsoleVirtualThreadHttpClient(ApiDocConsoleProperties properties) {
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.scheduler = Schedulers.fromExecutorService(executorService);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getClient().getConnectTimeout())
                .executor(executorService)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /**
     * 执行代理 HTTP 请求
     *
     * @param request HTTP 请求
     * @return Mono<ApiDocConsoleHttpResponse> 返回 HTTP 响应
     */
    @Override
    public Mono<ApiDocConsoleHttpResponse> execute(ApiDocConsoleHttpRequest request) {
        return Mono.fromCallable(() -> doExecute(request)).subscribeOn(scheduler);
    }

    /**
     * 执行阻塞式 HTTP 请求
     *
     * @param request HTTP 请求
     * @return ApiDocConsoleHttpResponse 返回 HTTP 响应
     * @throws Exception 请求异常
     */
    private ApiDocConsoleHttpResponse doExecute(ApiDocConsoleHttpRequest request) throws Exception {
        long startTime = System.currentTimeMillis();
        HttpRequest httpRequest = buildRequest(request);
        HttpResponse<String> response;
        if (request.isReadResponseBody()) {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } else {
            HttpResponse<Void> discardResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
            response = new DiscardedBodyResponse(discardResponse);
        }
        ApiDocConsoleHttpResponse result = new ApiDocConsoleHttpResponse();
        result.setStatus(response.statusCode());
        result.setDurationMillis(System.currentTimeMillis() - startTime);
        result.setBody(request.isReadResponseBody() ? limitBody(response.body(), request.getMaxResponseSize()) : "");
        response.headers().map().forEach((key, value) -> result.getHeaders().put(key, String.join(",", value)));
        return result;
    }

    /**
     * 创建 JDK HTTP 请求
     *
     * @param request HTTP 请求
     * @return HttpRequest 返回 JDK HTTP 请求
     */
    private HttpRequest buildRequest(ApiDocConsoleHttpRequest request) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(request.getUri())
                .timeout(request.getTimeout());
        request.getHeaders().forEach((key, values) -> {
            if (StringUtils.hasText(key) && BLOCKED_HEADERS.stream().noneMatch(key::equalsIgnoreCase)) {
                values.forEach(value -> builder.header(key, value));
            }
        });
        if (StringUtils.hasText(request.getBody()) && methodWithBody(request.getMethod())) {
            builder.method(request.getMethod().name(), HttpRequest.BodyPublishers.ofString(request.getBody(), StandardCharsets.UTF_8));
        } else {
            builder.method(request.getMethod().name(), HttpRequest.BodyPublishers.noBody());
        }
        return builder.build();
    }

    /**
     * 判断请求方法是否支持请求体
     *
     * @param method HTTP 方法
     * @return boolean 返回 true 表示支持请求体
     */
    private boolean methodWithBody(HttpMethod method) {
        return HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method) || HttpMethod.PATCH.equals(method);
    }

    /**
     * 限制响应体大小
     *
     * @param body    响应体
     * @param maxSize 最大字节数
     * @return String 返回截断后的响应体
     */
    private String limitBody(String body, int maxSize) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxSize) {
            return body;
        }
        return new String(bytes, 0, maxSize, StandardCharsets.UTF_8) + "\n... response truncated by openapi debug console ...";
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console.client
     * @ClassName: DiscardedBodyResponse
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-20Day-00:50
     * @Description: 丢弃响应体后的字符串响应适配器
     * @Version: 1.0
     */
    private record DiscardedBodyResponse(HttpResponse<Void> delegate) implements HttpResponse<String> {

        /**
         * 获取响应状态码
         *
         * @return int 返回状态码
         */
        @Override
        public int statusCode() {
            return delegate.statusCode();
        }

        /**
         * 获取原始请求
         *
         * @return HttpRequest 返回原始请求
         */
        @Override
        public HttpRequest request() {
            return delegate.request();
        }

        /**
         * 获取上一个响应
         *
         * @return Optional<HttpResponse<String>> 返回上一个响应
         */
        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        /**
         * 获取响应头
         *
         * @return java.net.http.HttpHeaders 返回响应头
         */
        @Override
        public java.net.http.HttpHeaders headers() {
            return delegate.headers();
        }

        /**
         * 获取响应体
         *
         * @return String 返回空响应体
         */
        @Override
        public String body() {
            return "";
        }

        /**
         * 获取 SSL 会话
         *
         * @return Optional<SSLSession> 返回 SSL 会话
         */
        @Override
        public Optional<SSLSession> sslSession() {
            return delegate.sslSession();
        }

        /**
         * 获取响应 URI
         *
         * @return java.net.URI 返回响应 URI
         */
        @Override
        public java.net.URI uri() {
            return delegate.uri();
        }

        /**
         * 获取 HTTP 版本
         *
         * @return HttpClient.Version 返回 HTTP 版本
         */
        @Override
        public HttpClient.Version version() {
            return delegate.version();
        }
    }

    /**
     * 关闭虚拟线程客户端
     */
    @Override
    public void close() {
        scheduler.dispose();
        executorService.shutdownNow();
    }
}
