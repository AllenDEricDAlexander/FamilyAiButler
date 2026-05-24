/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.web
 * @FileName: ApiDocConsoleController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:50
 * @Description: OpenAPI 调试文档控制台控制器文件
 * @Version: 1.0
 */
package top.egon.openapi.console.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.egon.openapi.console.ApiDocConsolePayloads;
import top.egon.openapi.console.ApiDocConsoleProperties;
import top.egon.openapi.console.core.ApiDocConsoleService;
import top.egon.openapi.console.core.ApiDocConsoleSessionService;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.web
 * @ClassName: ApiDocConsoleController
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:50
 * @Description: OpenAPI 调试文档控制台控制器
 * @Version: 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("${egon.openapi.console.base-path:/openapi-console}/api")
public class ApiDocConsoleController {

    private final ApiDocConsoleProperties properties;

    private final ApiDocConsoleSessionService sessionService;

    private final ApiDocConsoleService consoleService;

    private final ObjectMapper objectMapper;

    /**
     * 创建登录挑战
     *
     * @param username 用户名
     * @param exchange WebFlux 请求上下文
     * @return ResponseEntity<LoginChallengeResponse> 返回登录挑战
     */
    @GetMapping("/login-challenge")
    public ResponseEntity<ApiDocConsolePayloads.LoginChallengeResponse> loginChallenge(@RequestParam(required = false) String username,
                                                                                       ServerWebExchange exchange) {
        if (!sessionService.isConsoleOpen()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sessionService.createLoginChallenge(username, clientId(exchange)));
    }

    /**
     * 登录控制台
     *
     * @param request  登录请求
     * @param exchange WebFlux 请求上下文
     * @return ResponseEntity<LoginResponse> 返回登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<ApiDocConsolePayloads.LoginResponse> login(@RequestBody ApiDocConsolePayloads.LoginRequest request,
                                                                     ServerWebExchange exchange) {
        if (!sessionService.isConsoleOpen()) {
            return ResponseEntity.notFound().build();
        }
        Optional<ApiDocConsoleSessionService.LoginValidation> validation = sessionService.validateLoginProof(request, clientId(exchange));
        if (validation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ApiDocConsolePayloads.LoginResponse response = new ApiDocConsolePayloads.LoginResponse();
        response.setUsername(validation.get().getUsername());
        response.setMode(sessionService.currentMode().name());
        response.setReadOnly(sessionService.isReadOnly());
        response.setExpiresAt(sessionService.nextExpiresAt());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionService.createSessionCookie(validation.get().getUsername(), validation.get().getRequestSigningSecret()).toString())
                .body(response);
    }

    /**
     * 退出登录
     *
     * @param exchange WebFlux 请求上下文
     * @return ResponseEntity<Void> 返回退出登录结果
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(ServerWebExchange exchange) {
        Optional<ApiDocConsolePayloads.UserSession> session = requireSession(exchange);
        if (session.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!validateConsoleSignature(session.get(), exchange, "")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, sessionService.createLogoutCookie().toString())
                .build();
    }

    /**
     * 查询当前登录用户
     *
     * @param exchange WebFlux 请求上下文
     * @return ResponseEntity<MeResponse> 返回当前登录用户
     */
    @GetMapping("/me")
    public ResponseEntity<ApiDocConsolePayloads.MeResponse> me(ServerWebExchange exchange) {
        ApiDocConsolePayloads.MeResponse response = new ApiDocConsolePayloads.MeResponse();
        response.setMode(sessionService.currentMode().name());
        response.setReadOnly(sessionService.isReadOnly());
        response.setCapabilities(consoleService.catalog(sessionService.currentMode()).getCapabilities());
        sessionService.resolveSession(exchange).ifPresent(session -> {
            response.setAuthenticated(true);
            response.setUsername(session.getUsername());
        });
        return ResponseEntity.ok(response);
    }

    /**
     * 查询服务目录
     *
     * @param exchange WebFlux 请求上下文
     * @return ResponseEntity<CatalogResponse> 返回服务目录
     */
    @GetMapping("/catalog")
    public ResponseEntity<ApiDocConsolePayloads.CatalogResponse> catalog(ServerWebExchange exchange) {
        Optional<ApiDocConsolePayloads.UserSession> session = requireSession(exchange);
        if (session.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!validateConsoleSignature(session.get(), exchange, "")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(consoleService.catalog(sessionService.currentMode()));
    }

    /**
     * 查询服务 OpenAPI JSON
     *
     * @param serviceId 服务 ID
     * @param exchange  WebFlux 请求上下文
     * @return Mono<ResponseEntity<JsonNode>> 返回 OpenAPI JSON
     */
    @GetMapping("/openapi/{serviceId}")
    public Mono<ResponseEntity<JsonNode>> openApi(@PathVariable String serviceId, ServerWebExchange exchange) {
        Optional<ApiDocConsolePayloads.UserSession> session = requireSession(exchange);
        if (session.isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        if (!validateConsoleSignature(session.get(), exchange, "")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return consoleService.fetchOpenApi(serviceId)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorBody(error))));
    }

    /**
     * 执行接口调试请求
     *
     * @param request  调试请求
     * @param exchange WebFlux 请求上下文
     * @return Mono<ResponseEntity<Object>> 返回调试响应
     */
    @PostMapping("/execute")
    public Mono<ResponseEntity<Object>> execute(@RequestBody(required = false) String requestBody,
                                                ServerWebExchange exchange) {
        Optional<ApiDocConsolePayloads.UserSession> session = requireSession(exchange);
        if (session.isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        if (!validateConsoleSignature(session.get(), exchange, requestBody)) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        if (sessionService.isReadOnly()) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "当前环境只读，禁止执行调试请求")));
        }
        ApiDocConsolePayloads.ExecuteRequest request;
        try {
            request = objectMapper.readValue(StringUtils.hasText(requestBody) ? requestBody : "{}", ApiDocConsolePayloads.ExecuteRequest.class);
        } catch (Exception e) {
            return Mono.just(ResponseEntity.badRequest().body((Object) Map.of("message", "请求体不是合法 JSON")));
        }
        return consoleService.execute(request)
                .map(result -> ResponseEntity.ok((Object) result))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY).body((Object) Map.of("message", error.getMessage()))));
    }

    /**
     * 执行轻量压测
     *
     * @param request  压测请求
     * @param exchange WebFlux 请求上下文
     * @return Mono<ResponseEntity<Object>> 返回压测结果
     */
    @PostMapping("/load-test")
    public Mono<ResponseEntity<Object>> loadTest(@RequestBody(required = false) String requestBody,
                                                 ServerWebExchange exchange) {
        Optional<ApiDocConsolePayloads.UserSession> session = requireSession(exchange);
        if (session.isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        if (!validateConsoleSignature(session.get(), exchange, requestBody)) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        if (sessionService.isReadOnly() || !properties.getLoadTest().isEnabled()) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "当前环境禁止压测")));
        }
        ApiDocConsolePayloads.LoadTestRequest request;
        try {
            request = objectMapper.readValue(StringUtils.hasText(requestBody) ? requestBody : "{}", ApiDocConsolePayloads.LoadTestRequest.class);
        } catch (Exception e) {
            return Mono.just(ResponseEntity.badRequest().body((Object) Map.of("message", "请求体不是合法 JSON")));
        }
        return consoleService.loadTest(request)
                .map(result -> ResponseEntity.ok((Object) result))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY).body((Object) Map.of("message", error.getMessage()))));
    }

    /**
     * 导出接口文档
     *
     * @param serviceId 服务 ID
     * @param format    导出格式
     * @param exchange  WebFlux 请求上下文
     * @return Mono<ResponseEntity<byte[]>> 返回导出文件
     */
    @GetMapping("/export/{serviceId}")
    public Mono<ResponseEntity<byte[]>> export(@PathVariable String serviceId,
                                               @RequestParam(defaultValue = "md") String format,
                                               @RequestParam(defaultValue = "service") String scope,
                                               ServerWebExchange exchange) {
        Optional<ApiDocConsolePayloads.UserSession> session = requireSession(exchange);
        if (session.isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        if (!validateConsoleSignature(session.get(), exchange, "")) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        if (!properties.getExport().isEnabled()) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        }
        if (!"service".equalsIgnoreCase(scope)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        String normalizedFormat = normalizeExportFormat(format);
        if (normalizedFormat == null) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        Mono<byte[]> content = exportContent(serviceId, normalizedFormat);
        return content.map(bytes -> exportResponse(serviceId, normalizedFormat, bytes))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY).build()));
    }

    /**
     * 校验登录会话
     *
     * @param exchange WebFlux 请求上下文
     * @return Optional<UserSession> 返回登录会话
     */
    private Optional<ApiDocConsolePayloads.UserSession> requireSession(ServerWebExchange exchange) {
        if (!sessionService.isConsoleOpen()) {
            return Optional.empty();
        }
        return sessionService.resolveSession(exchange);
    }

    /**
     * 创建错误响应体
     *
     * @param error 异常对象
     * @return JsonNode 返回错误响应
     */
    private JsonNode errorBody(Throwable error) {
        return objectMapper.createObjectNode()
                .put("message", StringUtils.hasText(error.getMessage()) ? error.getMessage() : "OpenAPI JSON 加载失败");
    }

    /**
     * 校验控制台 API 请求签名
     *
     * @param session  登录会话
     * @param exchange WebFlux 请求上下文
     * @param body     请求体
     * @return boolean 返回 true 表示签名有效
     */
    private boolean validateConsoleSignature(ApiDocConsolePayloads.UserSession session, ServerWebExchange exchange, String body) {
        return sessionService.validateRequestSignature(
                session,
                exchange.getRequest().getMethod().name(),
                pathWithQuery(exchange),
                body == null ? "" : body,
                exchange.getRequest().getHeaders().getFirst("X-OpenAPI-Console-Timestamp"),
                exchange.getRequest().getHeaders().getFirst("X-OpenAPI-Console-Nonce"),
                exchange.getRequest().getHeaders().getFirst("X-OpenAPI-Console-Signature"));
    }

    /**
     * 获取请求路径和查询串
     *
     * @param exchange WebFlux 请求上下文
     * @return String 返回路径和查询串
     */
    private String pathWithQuery(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getRawPath();
        String query = exchange.getRequest().getURI().getRawQuery();
        return query == null || query.isBlank() ? path : path + "?" + query;
    }

    /**
     * 获取客户端标识
     *
     * @param exchange WebFlux 请求上下文
     * @return String 返回客户端标识
     */
    private String clientId(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        InetSocketAddress address = exchange.getRequest().getRemoteAddress();
        if (address == null || address.getAddress() == null) {
            return "unknown";
        }
        return address.getAddress().getHostAddress();
    }

    /**
     * 构造导出响应
     *
     * @param serviceId 服务 ID
     * @param format    导出格式
     * @param bytes     文件字节
     * @return ResponseEntity<byte[]> 返回导出响应
     */
    private ResponseEntity<byte[]> exportResponse(String serviceId, String format, byte[] bytes) {
        MediaType mediaType = exportMediaType(format);
        ContentDisposition disposition = ContentDisposition.attachment().filename(exportFileName(serviceId, format), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(bytes);
    }

    /**
     * 规范化导出格式。
     *
     * @param format 导出格式
     * @return String 返回规范化格式
     */
    private String normalizeExportFormat(String format) {
        if (!StringUtils.hasText(format) || "md".equalsIgnoreCase(format)) {
            return "md";
        }
        if ("pdf".equalsIgnoreCase(format)) {
            return "pdf";
        }
        if ("openapi-json".equalsIgnoreCase(format) || "json".equalsIgnoreCase(format)) {
            return "openapi-json";
        }
        return null;
    }

    /**
     * 获取导出内容。
     *
     * @param serviceId 服务 ID
     * @param format    导出格式
     * @return Mono<byte[]> 返回导出内容
     */
    private Mono<byte[]> exportContent(String serviceId, String format) {
        if ("pdf".equals(format)) {
            return consoleService.exportPdf(serviceId);
        }
        if ("openapi-json".equals(format)) {
            return consoleService.exportOpenApiJson(serviceId);
        }
        return consoleService.exportMarkdown(serviceId);
    }

    /**
     * 获取导出响应类型。
     *
     * @param format 导出格式
     * @return MediaType 返回响应类型
     */
    private MediaType exportMediaType(String format) {
        if ("pdf".equals(format)) {
            return MediaType.APPLICATION_PDF;
        }
        if ("openapi-json".equals(format)) {
            return new MediaType("application", "json", StandardCharsets.UTF_8);
        }
        return new MediaType("text", "markdown", StandardCharsets.UTF_8);
    }

    /**
     * 获取导出文件名。
     *
     * @param serviceId 服务 ID
     * @param format    导出格式
     * @return String 返回文件名
     */
    private String exportFileName(String serviceId, String format) {
        if ("openapi-json".equals(format)) {
            return serviceId + ".openapi.json";
        }
        return serviceId + "." + format;
    }
}
