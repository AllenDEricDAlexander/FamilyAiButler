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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import top.egon.openapi.console.ApiDocConsolePayloads;
import top.egon.openapi.console.ApiDocConsoleProperties;
import top.egon.openapi.console.core.ApiDocConsoleService;
import top.egon.openapi.console.core.ApiDocConsoleSessionService;

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
        if (!sessionService.validateLogin(request.getUsername(), request.getPassword(), clientId(exchange))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        ApiDocConsolePayloads.LoginResponse response = new ApiDocConsolePayloads.LoginResponse();
        response.setUsername(request.getUsername());
        response.setMode(sessionService.currentMode().name());
        response.setReadOnly(sessionService.isReadOnly());
        response.setExpiresAt(sessionService.nextExpiresAt());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionService.createSessionCookie(request.getUsername()).toString())
                .body(response);
    }

    /**
     * 退出登录
     *
     * @return ResponseEntity<Void> 返回退出登录结果
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
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
        if (requireSession(exchange).isEmpty()) {
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
        if (requireSession(exchange).isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        return consoleService.fetchOpenApi(serviceId)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY).build()));
    }

    /**
     * 执行接口调试请求
     *
     * @param request  调试请求
     * @param exchange WebFlux 请求上下文
     * @return Mono<ResponseEntity<Object>> 返回调试响应
     */
    @PostMapping("/execute")
    public Mono<ResponseEntity<Object>> execute(@RequestBody ApiDocConsolePayloads.ExecuteRequest request,
                                                ServerWebExchange exchange) {
        Optional<ApiDocConsolePayloads.UserSession> session = requireSession(exchange);
        if (session.isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        if (sessionService.isReadOnly()) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "当前环境只读，禁止执行调试请求")));
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
    public Mono<ResponseEntity<Object>> loadTest(@RequestBody ApiDocConsolePayloads.LoadTestRequest request,
                                                 ServerWebExchange exchange) {
        if (requireSession(exchange).isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        if (sessionService.isReadOnly() || !properties.getLoadTest().isEnabled()) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "当前环境禁止压测")));
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
                                               ServerWebExchange exchange) {
        if (requireSession(exchange).isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
        if (!properties.getExport().isEnabled()) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        }
        String normalizedFormat = "pdf".equalsIgnoreCase(format) ? "pdf" : "md";
        Mono<byte[]> content = "pdf".equals(normalizedFormat) ? consoleService.exportPdf(serviceId) : consoleService.exportMarkdown(serviceId);
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
        MediaType mediaType = "pdf".equals(format) ? MediaType.APPLICATION_PDF : new MediaType("text", "markdown", StandardCharsets.UTF_8);
        ContentDisposition disposition = ContentDisposition.attachment().filename(serviceId + "." + format, StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(bytes);
    }
}
