/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.client
 * @FileName: ApiDocConsoleReactiveHttpClient.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:45
 * @Description: OpenAPI 调试文档控制台响应式 HTTP 客户端文件
 * @Version: 1.0
 */
package top.egon.openapi.console.client;

import io.netty.channel.ChannelOption;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import top.egon.openapi.console.ApiDocConsoleProperties;

import java.nio.charset.StandardCharsets;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.client
 * @ClassName: ApiDocConsoleReactiveHttpClient
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:45
 * @Description: OpenAPI 调试文档控制台响应式 HTTP 客户端
 * @Version: 1.0
 */
public class ApiDocConsoleReactiveHttpClient implements ApiDocConsoleHttpClient {

    private final WebClient webClient;

    /**
     * 创建响应式 HTTP 客户端
     *
     * @param properties       控制台配置
     * @param webClientBuilder WebClient 构造器
     */
    public ApiDocConsoleReactiveHttpClient(ApiDocConsoleProperties properties, WebClient.Builder webClientBuilder) {
        ApiDocConsoleProperties.Client clientProperties = properties.getClient();
        ConnectionProvider connectionProvider = ConnectionProvider.builder("openapi-console")
                .maxConnections(clientProperties.getMaxConnections())
                .pendingAcquireMaxCount(clientProperties.getPendingAcquireMaxCount())
                .pendingAcquireTimeout(clientProperties.getPendingAcquireTimeout())
                .maxIdleTime(clientProperties.getMaxIdleTime())
                .maxLifeTime(clientProperties.getMaxLifeTime())
                .build();
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(clientProperties.getConnectTimeout().toMillis()))
                .responseTimeout(clientProperties.getResponseTimeout())
                .compress(clientProperties.isCompressionEnabled());
        if (clientProperties.isWiretap()) {
            httpClient = httpClient.wiretap(true);
        }
        int maxSize = (int) properties.getMaxResponseSize().toBytes();
        this.webClient = webClientBuilder.clone()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxSize))
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
        long startTime = System.currentTimeMillis();
        WebClient.RequestBodySpec requestSpec = webClient
                .method(request.getMethod())
                .uri(request.getUri())
                .headers(headers -> headers.addAll(request.getHeaders()));
        Mono<ClientResponse> responseMono = StringUtils.hasText(request.getBody()) && methodWithBody(request.getMethod())
                ? requestSpec.body(BodyInserters.fromValue(request.getBody())).exchangeToMono(Mono::just)
                : requestSpec.exchangeToMono(Mono::just);
        return responseMono
                .flatMap(response -> toResponse(response, startTime, request))
                .timeout(request.getTimeout());
    }

    /**
     * 转换响应对象
     *
     * @param response  客户端响应
     * @param startTime 开始时间
     * @param request   HTTP 请求
     * @return Mono<ApiDocConsoleHttpResponse> 返回响应对象
     */
    private Mono<ApiDocConsoleHttpResponse> toResponse(ClientResponse response, long startTime, ApiDocConsoleHttpRequest request) {
        Mono<String> bodyMono = request.isReadResponseBody()
                ? response.bodyToMono(String.class).defaultIfEmpty("").map(body -> limitBody(body, request.getMaxResponseSize()))
                : response.releaseBody().thenReturn("");
        return bodyMono.map(body -> {
            ApiDocConsoleHttpResponse result = new ApiDocConsoleHttpResponse();
            result.setStatus(response.statusCode().value());
            result.setDurationMillis(System.currentTimeMillis() - startTime);
            result.setBody(body);
            response.headers().asHttpHeaders().forEach((key, value) -> result.getHeaders().put(key, String.join(",", value)));
            return result;
        });
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
}
