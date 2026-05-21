/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.core
 * @FileName: ApiDocConsoleService.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:40
 * @Description: OpenAPI 调试文档控制台核心服务文件
 * @Version: 1.0
 */
package top.egon.openapi.console.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import top.egon.openapi.console.ApiDocConsolePayloads;
import top.egon.openapi.console.ApiDocConsoleProperties;
import top.egon.openapi.console.client.ApiDocConsoleHttpClient;
import top.egon.openapi.console.client.ApiDocConsoleHttpRequest;
import top.egon.openapi.console.client.ApiDocConsoleHttpResponse;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.core
 * @ClassName: ApiDocConsoleService
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:40
 * @Description: OpenAPI 调试文档控制台核心服务
 * @Version: 1.0
 */
@Slf4j
@RequiredArgsConstructor
public class ApiDocConsoleService {

    private static final List<String> BLOCKED_REQUEST_HEADERS = List.of(
            HttpHeaders.HOST,
            HttpHeaders.CONTENT_LENGTH,
            HttpHeaders.TRANSFER_ENCODING
    );

    private final ApiDocConsoleProperties properties;

    private final ObjectMapper objectMapper;

    private final ApiDocConsoleHttpClient httpClient;

    private final ApiDocConsoleDocumentRenderer documentRenderer;

    private final ObjectProvider<ReactiveDiscoveryClient> reactiveDiscoveryClients;

    private final ObjectProvider<DiscoveryClient> discoveryClients;

    private final Map<String, AtomicInteger> discoveryIndexes = new ConcurrentHashMap<>();

    private final AtomicInteger activeLoadRuns = new AtomicInteger();

    private final AtomicInteger activeLoadConcurrency = new AtomicInteger();

    /**
     * 查询服务目录
     *
     * @param mode 当前开放模式
     * @return CatalogResponse 返回服务目录
     */
    public ApiDocConsolePayloads.CatalogResponse catalog(ApiDocConsoleProperties.Mode mode) {
        ApiDocConsolePayloads.CatalogResponse response = new ApiDocConsolePayloads.CatalogResponse();
        response.setTitle(properties.getTitle());
        response.setEnvironment(properties.getEnvironment());
        response.setMode(mode.name());
        response.setReadOnly(mode == ApiDocConsoleProperties.Mode.READ_ONLY);
        response.setCapabilities(capabilities(mode));
        List<ApiDocConsolePayloads.ServiceItem> services = properties.getServices()
                .stream()
                .filter(ApiDocConsoleProperties.ServiceRoute::isEnabled)
                .sorted(Comparator.comparingInt(ApiDocConsoleProperties.ServiceRoute::getOrder))
                .map(this::toServiceItem)
                .toList();
        response.setServices(services);
        return response;
    }

    /**
     * 拉取并标准化 OpenAPI JSON
     *
     * @param serviceId 服务 ID
     * @return Mono<JsonNode> 返回 OpenAPI JSON
     */
    public Mono<JsonNode> fetchOpenApi(String serviceId) {
        Optional<ApiDocConsoleProperties.ServiceRoute> service = findService(serviceId);
        if (service.isEmpty()) {
            return Mono.error(new IllegalArgumentException("服务不存在: " + serviceId));
        }
        return resolveTargetUri(URI.create(service.get().getOpenApiUrl()))
                .flatMap(uri -> {
                    HttpHeaders headers = new HttpHeaders();
                    fillOpenApiRequestHeaders(headers, service.get());
                    ApiDocConsoleHttpRequest request = new ApiDocConsoleHttpRequest();
                    request.setMethod(HttpMethod.GET);
                    request.setUri(uri);
                    request.setHeaders(headers);
                    request.setTimeout(properties.getRequestTimeout());
                    request.setMaxResponseSize((int) properties.getMaxResponseSize().toBytes());
                    request.setReadResponseBody(true);
                    return httpClient.execute(request);
                })
                .map(response -> readOpenApi(service.get(), response))
                .doOnError(error -> log.warn("OpenAPI 文档拉取失败 serviceId={}", serviceId, error));
    }

    /**
     * 代理执行调试请求
     *
     * @param request 调试请求
     * @return Mono<ExecuteResponse> 返回调试响应
     */
    public Mono<ApiDocConsolePayloads.ExecuteResponse> execute(ApiDocConsolePayloads.ExecuteRequest request) {
        return execute(request, true, true);
    }

    /**
     * 代理执行调试请求
     *
     * @param request          调试请求
     * @param readResponseBody 是否读取响应体
     * @param includeCurl      是否生成 cURL
     * @return Mono<ExecuteResponse> 返回调试响应
     */
    private Mono<ApiDocConsolePayloads.ExecuteResponse> execute(ApiDocConsolePayloads.ExecuteRequest request,
                                                                boolean readResponseBody,
                                                                boolean includeCurl) {
        Optional<ApiDocConsoleProperties.ServiceRoute> service = findService(request.getServiceId());
        if (service.isEmpty()) {
            return Mono.error(new IllegalArgumentException("服务不存在: " + request.getServiceId()));
        }
        validateDebugPath(service.get(), request.getPath());
        URI targetUri = buildTargetUri(service.get(), request);
        HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase(Locale.ROOT));
        String body = request.getBody() == null ? "" : request.getBody();
        return resolveTargetUri(targetUri)
                .flatMap(uri -> httpClient.execute(buildHttpRequest(request, uri, method, body, readResponseBody))
                        .map(response -> toExecuteResponse(response, request, uri, includeCurl)));
    }

    /**
     * 执行轻量压测
     *
     * @param request 压测请求
     * @return Mono<LoadTestResult> 返回压测结果
     */
    public Mono<ApiDocConsolePayloads.LoadTestResult> loadTest(ApiDocConsolePayloads.LoadTestRequest request) {
        int totalRequests = Math.max(1, Math.min(request.getTotalRequests(), properties.getLoadTest().getMaxRequests()));
        int concurrency = Math.max(1, Math.min(request.getConcurrency(), properties.getLoadTest().getMaxConcurrency()));
        if (!acquireLoadRun()) {
            return Mono.error(new IllegalStateException("当前压测任务数已达到上限"));
        }
        return Flux.range(0, totalRequests)
                .flatMap(index -> executeLoadSample(index, request.getRequest()), concurrency)
                .collectList()
                .map(this::summarizeLoadTest)
                .doFinally(signal -> releaseLoadRun());
    }

    /**
     * 导出 Markdown 文档
     *
     * @param serviceId 服务 ID
     * @return Mono<byte[]> 返回 Markdown 字节
     */
    public Mono<byte[]> exportMarkdown(String serviceId) {
        return fetchOpenApi(serviceId)
                .map(documentRenderer::renderMarkdown)
                .map(markdown -> markdown.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 导出 PDF 文档
     *
     * @param serviceId 服务 ID
     * @return Mono<byte[]> 返回 PDF 字节
     */
    public Mono<byte[]> exportPdf(String serviceId) {
        return fetchOpenApi(serviceId)
                .map(documentRenderer::renderMarkdown)
                .map(documentRenderer::renderPdf);
    }

    /**
     * 查找服务配置
     *
     * @param serviceId 服务 ID
     * @return Optional<ServiceRoute> 返回服务配置
     */
    private Optional<ApiDocConsoleProperties.ServiceRoute> findService(String serviceId) {
        return properties.getServices()
                .stream()
                .filter(ApiDocConsoleProperties.ServiceRoute::isEnabled)
                .filter(item -> item.getId().equals(serviceId))
                .findFirst();
    }

    /**
     * 转换服务目录条目
     *
     * @param route 服务配置
     * @return ServiceItem 返回服务目录条目
     */
    private ApiDocConsolePayloads.ServiceItem toServiceItem(ApiDocConsoleProperties.ServiceRoute route) {
        ApiDocConsolePayloads.ServiceItem item = new ApiDocConsolePayloads.ServiceItem();
        item.setId(route.getId());
        item.setName(route.getName());
        item.setGroup(route.getGroup());
        item.setBaseUrl(route.getBaseUrl());
        return item;
    }

    /**
     * 构造当前能力开关
     *
     * @param mode 当前开放模式
     * @return Map<String, Boolean> 返回能力开关
     */
    private Map<String, Boolean> capabilities(ApiDocConsoleProperties.Mode mode) {
        boolean full = mode == ApiDocConsoleProperties.Mode.FULL;
        Map<String, Boolean> capabilities = new LinkedHashMap<>();
        capabilities.put("catalog", mode != ApiDocConsoleProperties.Mode.OFF);
        capabilities.put("spec", mode != ApiDocConsoleProperties.Mode.OFF);
        capabilities.put("tryout", full);
        capabilities.put("loadTest", full && properties.getLoadTest().isEnabled());
        capabilities.put("export", mode != ApiDocConsoleProperties.Mode.OFF && properties.getExport().isEnabled());
        return capabilities;
    }

    /**
     * 读取 OpenAPI JSON
     *
     * @param route 服务配置
     * @param response OpenAPI 原始响应
     * @return JsonNode 返回标准化 OpenAPI JSON
     */
    private JsonNode readOpenApi(ApiDocConsoleProperties.ServiceRoute route, ApiDocConsoleHttpResponse response) {
        if (response.getStatus() < 200 || response.getStatus() >= 300) {
            throw new IllegalArgumentException("OpenAPI JSON 请求失败 status=" + response.getStatus());
        }
        try {
            String raw = response.getBody();
            if (!StringUtils.hasText(raw)) {
                throw new IllegalArgumentException("OpenAPI JSON 响应为空");
            }
            JsonNode node = objectMapper.readTree(raw);
            if (!(node instanceof ObjectNode objectNode)) {
                throw new IllegalArgumentException("OpenAPI JSON 不是对象结构");
            }
            if (!node.has("paths") || !node.get("paths").isObject()) {
                throw new IllegalArgumentException("OpenAPI JSON 缺少 paths 对象");
            }
            if (StringUtils.hasText(route.getBaseUrl())) {
                ArrayNode servers = objectMapper.createArrayNode();
                ObjectNode server = objectMapper.createObjectNode();
                server.put("url", route.getBaseUrl());
                servers.add(server);
                objectNode.set("servers", servers);
            }
            return node;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("OpenAPI JSON 解析失败", e);
        }
    }

    /**
     * 创建目标请求地址
     *
     * @param route   服务配置
     * @param request 调试请求
     * @return URI 返回目标请求地址
     */
    private URI buildTargetUri(ApiDocConsoleProperties.ServiceRoute route, ApiDocConsolePayloads.ExecuteRequest request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(route.getBaseUrl());
        String path = StringUtils.hasText(request.getPath()) ? request.getPath() : "/";
        builder.path(path.startsWith("/") ? path : "/" + path);
        request.getQuery().forEach((key, value) -> {
            if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
                builder.queryParam(key, value);
            }
        });
        return builder.build(true).toUri();
    }

    /**
     * 解析目标请求地址
     *
     * @param uri 原始请求地址
     * @return Mono<URI> 返回可访问的请求地址
     */
    private Mono<URI> resolveTargetUri(URI uri) {
        if (uri.getHost() == null || uri.getPort() != -1 || uri.getHost().contains(".")) {
            return Mono.just(uri);
        }
        ReactiveDiscoveryClient reactiveDiscoveryClient = reactiveDiscoveryClients.getIfAvailable();
        if (reactiveDiscoveryClient != null) {
            return reactiveDiscoveryClient.getInstances(uri.getHost())
                    .collectList()
                    .flatMap(instances -> {
                        if (!instances.isEmpty()) {
                            return Mono.just(buildInstanceUri(uri, chooseInstance(uri.getHost(), instances)));
                        }
                        return resolveBlockingDiscoveryUri(uri);
                    })
                    .onErrorResume(error -> resolveBlockingDiscoveryUri(uri));
        }
        return resolveBlockingDiscoveryUri(uri);
    }

    /**
     * 通过阻塞式服务发现解析目标请求地址
     *
     * @param uri 原始请求地址
     * @return Mono<URI> 返回可访问的请求地址
     */
    private Mono<URI> resolveBlockingDiscoveryUri(URI uri) {
        DiscoveryClient discoveryClient = discoveryClients.getIfAvailable();
        if (discoveryClient == null) {
            return Mono.just(uri);
        }
        List<ServiceInstance> instances = discoveryClient.getInstances(uri.getHost());
        if (instances.isEmpty()) {
            return Mono.error(new IllegalStateException("未发现服务实例: " + uri.getHost()));
        }
        return Mono.just(buildInstanceUri(uri, chooseInstance(uri.getHost(), instances)));
    }

    /**
     * 选择服务实例
     *
     * @param serviceId 服务 ID
     * @param instances 服务实例列表
     * @return ServiceInstance 返回服务实例
     */
    private ServiceInstance chooseInstance(String serviceId, List<ServiceInstance> instances) {
        AtomicInteger index = discoveryIndexes.computeIfAbsent(serviceId, key -> new AtomicInteger());
        return instances.get(Math.floorMod(index.getAndIncrement(), instances.size()));
    }

    /**
     * 创建服务实例请求地址
     *
     * @param uri      原始请求地址
     * @param instance 服务实例
     * @return URI 返回服务实例请求地址
     */
    private URI buildInstanceUri(URI uri, ServiceInstance instance) {
        URI instanceUri = instance.getUri();
        return UriComponentsBuilder.fromUri(instanceUri)
                .path(uri.getRawPath())
                .query(uri.getRawQuery())
                .build(true)
                .toUri();
    }

    /**
     * 填充代理请求头
     *
     * @param headers   HTTP 请求头
     * @param request   调试请求
     * @param targetUri 目标 URI
     * @param body      请求体
     */
    private void fillRequestHeaders(HttpHeaders headers,
                                    ApiDocConsolePayloads.ExecuteRequest request,
                                    URI targetUri,
                                    String body) {
        request.getHeaders().forEach((key, value) -> {
            if (StringUtils.hasText(key) && value != null && BLOCKED_REQUEST_HEADERS.stream().noneMatch(key::equalsIgnoreCase)) {
                headers.set(key, value);
            }
        });
        if (StringUtils.hasText(request.getContentType())) {
            headers.set(HttpHeaders.CONTENT_TYPE, request.getContentType());
        }
        fillSignatureHeaders(headers, request, targetUri, body);
    }

    /**
     * 创建 HTTP 客户端请求
     *
     * @param request          调试请求
     * @param uri              目标 URI
     * @param method           HTTP 方法
     * @param body             请求体
     * @param readResponseBody 是否读取响应体
     * @return ApiDocConsoleHttpRequest 返回 HTTP 请求
     */
    private ApiDocConsoleHttpRequest buildHttpRequest(ApiDocConsolePayloads.ExecuteRequest request,
                                                      URI uri,
                                                      HttpMethod method,
                                                      String body,
                                                      boolean readResponseBody) {
        HttpHeaders headers = new HttpHeaders();
        fillRequestHeaders(headers, request, uri, body);
        ApiDocConsoleHttpRequest httpRequest = new ApiDocConsoleHttpRequest();
        httpRequest.setMethod(method);
        httpRequest.setUri(uri);
        httpRequest.setHeaders(headers);
        httpRequest.setBody(body);
        httpRequest.setTimeout(properties.getRequestTimeout());
        httpRequest.setMaxResponseSize((int) properties.getMaxResponseSize().toBytes());
        httpRequest.setReadResponseBody(readResponseBody);
        return httpRequest;
    }

    /**
     * 填充 OpenAPI JSON 内部访问请求头
     *
     * @param headers HTTP 请求头
     * @param route   服务配置
     */
    private void fillOpenApiRequestHeaders(HttpHeaders headers, ApiDocConsoleProperties.ServiceRoute route) {
        if (StringUtils.hasText(route.getOpenApiAccessHeader()) && StringUtils.hasText(route.getOpenApiAccessToken())) {
            headers.set(route.getOpenApiAccessHeader(), route.getOpenApiAccessToken());
        }
    }

    /**
     * 校验调试请求路径
     *
     * @param route 服务配置
     * @param path  请求路径
     */
    private void validateDebugPath(ApiDocConsoleProperties.ServiceRoute route, String path) {
        String requestPath = StringUtils.hasText(path) ? path : "/";
        String normalizedPath = requestPath.startsWith("/") ? requestPath : "/" + requestPath;
        if (route.getPathDenyList().stream().anyMatch(pattern -> pathMatches(pattern, normalizedPath))) {
            throw new IllegalArgumentException("当前接口路径禁止通过 OpenAPI 控制台调试");
        }
        if (!route.getPathAllowList().isEmpty()
                && route.getPathAllowList().stream().noneMatch(pattern -> pathMatches(pattern, normalizedPath))) {
            throw new IllegalArgumentException("当前接口路径不在 OpenAPI 控制台调试白名单内");
        }
    }

    /**
     * 判断路径是否匹配
     *
     * @param pattern 路径模式
     * @param path    请求路径
     * @return boolean 返回 true 表示匹配
     */
    private boolean pathMatches(String pattern, String path) {
        if (!StringUtils.hasText(pattern)) {
            return false;
        }
        String normalizedPattern = pattern.startsWith("/") ? pattern : "/" + pattern;
        if (normalizedPattern.endsWith("/**")) {
            return path.startsWith(normalizedPattern.substring(0, normalizedPattern.length() - 3));
        }
        return normalizedPattern.equals(path);
    }

    /**
     * 填充签名请求头
     *
     * @param headers   HTTP 请求头
     * @param request   调试请求
     * @param targetUri 目标 URI
     * @param body      请求体
     */
    private void fillSignatureHeaders(HttpHeaders headers,
                                      ApiDocConsolePayloads.ExecuteRequest request,
                                      URI targetUri,
                                      String body) {
        if (!properties.getSigning().isEnabled()) {
            return;
        }
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String canonical = request.getMethod().toUpperCase(Locale.ROOT) + "\n"
                + targetUri.getRawPath() + "\n"
                + (targetUri.getRawQuery() == null ? "" : targetUri.getRawQuery()) + "\n"
                + body + "\n"
                + timestamp + "\n"
                + nonce;
        headers.set(properties.getSigning().getAccessKeyHeader(), properties.getSigning().getAccessKeyId());
        headers.set(properties.getSigning().getTimestampHeader(), timestamp);
        headers.set(properties.getSigning().getNonceHeader(), nonce);
        headers.set(properties.getSigning().getSignatureHeader(), sign(canonical));
    }

    /**
     * 转换调试响应
     *
     * @param response    HTTP 客户端响应
     * @param request     调试请求
     * @param targetUri   目标 URI
     * @param includeCurl 是否生成 cURL
     * @return ExecuteResponse 返回调试响应
     */
    private ApiDocConsolePayloads.ExecuteResponse toExecuteResponse(ApiDocConsoleHttpResponse response,
                                                                    ApiDocConsolePayloads.ExecuteRequest request,
                                                                    URI targetUri,
                                                                    boolean includeCurl) {
        ApiDocConsolePayloads.ExecuteResponse result = new ApiDocConsolePayloads.ExecuteResponse();
        result.setStatus(response.getStatus());
        result.setDurationMillis(response.getDurationMillis());
        result.setBody(response.getBody());
        result.getHeaders().putAll(response.getHeaders());
        result.setCurl(includeCurl ? buildCurl(request, targetUri) : "");
        return result;
    }

    /**
     * 生成签名
     *
     * @param canonical 待签名原文
     * @return String 返回签名
     */
    private String sign(String canonical) {
        try {
            String algorithm = properties.getSigning().getAlgorithm();
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(properties.getSigning().getSecret().getBytes(StandardCharsets.UTF_8), algorithm));
            return Base64.getEncoder().encodeToString(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("调试请求签名失败", e);
        }
    }

    /**
     * 汇总压测结果
     *
     * @param samples 压测样本
     * @return LoadTestResult 返回压测汇总
     */
    private ApiDocConsolePayloads.LoadTestResult summarizeLoadTest(List<ApiDocConsolePayloads.LoadTestSample> samples) {
        samples.sort(Comparator.comparingInt(ApiDocConsolePayloads.LoadTestSample::getIndex));
        ApiDocConsolePayloads.LoadTestResult result = new ApiDocConsolePayloads.LoadTestResult();
        result.setTotal(samples.size());
        result.setSuccess((int) samples.stream().filter(ApiDocConsolePayloads.LoadTestSample::success).count());
        result.setFailed(result.getTotal() - result.getSuccess());
        List<Long> durations = samples.stream().map(ApiDocConsolePayloads.LoadTestSample::getDurationMillis).filter(value -> value > 0).sorted().toList();
        result.setMinMillis(durations.isEmpty() ? 0 : durations.get(0));
        result.setMaxMillis(durations.isEmpty() ? 0 : durations.get(durations.size() - 1));
        result.setAvgMillis(durations.isEmpty() ? 0 : durations.stream().mapToLong(Long::longValue).average().orElse(0));
        result.setP95Millis(durations.isEmpty() ? 0 : durations.get(Math.min(durations.size() - 1, (int) Math.ceil(durations.size() * 0.95) - 1)));
        result.setStatusCounts(samples.stream().collect(Collectors.groupingBy(ApiDocConsolePayloads.LoadTestSample::getStatus, LinkedHashMap::new, Collectors.summingInt(item -> 1))));
        result.setErrors(samples.stream().map(ApiDocConsolePayloads.LoadTestSample::getError).filter(StringUtils::hasText).limit(10).collect(Collectors.toCollection(ArrayList::new)));
        result.setSamples(samples);
        return result;
    }

    /**
     * 创建压测请求样本
     *
     * @param index          请求序号
     * @param status         响应状态
     * @param durationMillis 响应耗时
     * @param error          错误信息
     * @return LoadTestSample 返回压测样本
     */
    private ApiDocConsolePayloads.LoadTestSample loadTestSample(int index, int status, long durationMillis, String error) {
        ApiDocConsolePayloads.LoadTestSample sample = new ApiDocConsolePayloads.LoadTestSample();
        sample.setIndex(index + 1);
        sample.setStatus(status);
        sample.setDurationMillis(durationMillis);
        sample.setError(error);
        return sample;
    }

    /**
     * 执行压测样本
     *
     * @param index   请求序号
     * @param request 调试请求
     * @return Mono<LoadTestSample> 返回压测样本
     */
    private Mono<ApiDocConsolePayloads.LoadTestSample> executeLoadSample(int index, ApiDocConsolePayloads.ExecuteRequest request) {
        return acquireLoadConcurrency()
                .flatMap(acquired -> {
                    if (!acquired) {
                        return Mono.just(loadTestSample(index, 0, 0, "当前压测并发已达到上限"));
                    }
                    return execute(request, false, false)
                            .map(response -> loadTestSample(index, response.getStatus(), response.getDurationMillis(), null))
                            .onErrorResume(error -> Mono.just(loadTestSample(index, 0, 0, error.getMessage())))
                            .doFinally(signal -> releaseLoadConcurrency());
                });
    }

    /**
     * 获取压测任务令牌
     *
     * @return boolean 返回是否获取成功
     */
    private boolean acquireLoadRun() {
        int current = activeLoadRuns.incrementAndGet();
        if (current <= Math.max(1, properties.getLoadTest().getMaxActiveRuns())) {
            return true;
        }
        activeLoadRuns.decrementAndGet();
        return false;
    }

    /**
     * 释放压测任务令牌
     */
    private void releaseLoadRun() {
        activeLoadRuns.decrementAndGet();
    }

    /**
     * 获取压测并发令牌
     *
     * @return Mono<Boolean> 返回是否获取成功
     */
    private Mono<Boolean> acquireLoadConcurrency() {
        if (tryAcquireLoadConcurrency()) {
            return Mono.just(true);
        }
        if (properties.getLoadTest().isRejectWhenBusy()) {
            return Mono.just(false);
        }
        return Mono.fromCallable(() -> {
            while (!tryAcquireLoadConcurrency()) {
                Thread.sleep(10);
            }
            return true;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * 尝试获取压测并发令牌
     *
     * @return boolean 返回是否获取成功
     */
    private boolean tryAcquireLoadConcurrency() {
        int current = activeLoadConcurrency.incrementAndGet();
        if (current <= Math.max(1, properties.getLoadTest().getMaxActiveConcurrency())) {
            return true;
        }
        activeLoadConcurrency.decrementAndGet();
        return false;
    }

    /**
     * 释放压测并发令牌
     */
    private void releaseLoadConcurrency() {
        activeLoadConcurrency.decrementAndGet();
    }

    /**
     * 生成 curl 命令
     *
     * @param request   调试请求
     * @param targetUri 目标 URI
     * @return String 返回 curl 命令
     */
    private String buildCurl(ApiDocConsolePayloads.ExecuteRequest request, URI targetUri) {
        StringBuilder builder = new StringBuilder("curl -X ")
                .append(request.getMethod().toUpperCase(Locale.ROOT))
                .append(" '")
                .append(targetUri)
                .append("'");
        request.getHeaders().forEach((key, value) -> {
            if (StringUtils.hasText(key) && value != null) {
                builder.append(" \\\n  -H '").append(key).append(": ").append(value.replace("'", "'\\''")).append("'");
            }
        });
        if (StringUtils.hasText(request.getBody())) {
            builder.append(" \\\n  --data '").append(request.getBody().replace("'", "'\\''")).append("'");
        }
        return builder.toString();
    }

}
