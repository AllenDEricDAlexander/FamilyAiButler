/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.web
 * @FileName: ApiDocConsoleMvcController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:55
 * @Description: OpenAPI 调试文档控制台 MVC 控制器文件
 * @Version: 1.0
 */
package top.egon.openapi.console.web;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
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
import reactor.core.publisher.Mono;
import top.egon.openapi.console.ApiDocConsolePayloads;
import top.egon.openapi.console.ApiDocConsoleProperties;
import top.egon.openapi.console.core.ApiDocConsoleService;
import top.egon.openapi.console.core.ApiDocConsoleSessionService;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.web
 * @ClassName: ApiDocConsoleMvcController
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:55
 * @Description: OpenAPI 调试文档控制台 MVC 控制器
 * @Version: 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("${egon.openapi.console.base-path:/openapi-console}/api")
public class ApiDocConsoleMvcController {

    private final ApiDocConsoleProperties properties;

    private final ApiDocConsoleSessionService sessionService;

    private final ApiDocConsoleService consoleService;

    /**
     * 登录控制台
     *
     * @param request        登录请求
     * @param servletRequest Servlet 请求
     * @return ResponseEntity<LoginResponse> 返回登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<ApiDocConsolePayloads.LoginResponse> login(@RequestBody ApiDocConsolePayloads.LoginRequest request,
                                                                     HttpServletRequest servletRequest) {
        if (!sessionService.isConsoleOpen()) {
            return ResponseEntity.notFound().build();
        }
        if (!sessionService.validateLogin(request.getUsername(), request.getPassword(), clientId(servletRequest))) {
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
     * @param servletRequest Servlet 请求
     * @return ResponseEntity<MeResponse> 返回当前登录用户
     */
    @GetMapping("/me")
    public ResponseEntity<ApiDocConsolePayloads.MeResponse> me(HttpServletRequest servletRequest) {
        ApiDocConsolePayloads.MeResponse response = new ApiDocConsolePayloads.MeResponse();
        response.setMode(sessionService.currentMode().name());
        response.setReadOnly(sessionService.isReadOnly());
        response.setCapabilities(consoleService.catalog(sessionService.currentMode()).getCapabilities());
        sessionService.resolveSession(servletRequest).ifPresent(session -> {
            response.setAuthenticated(true);
            response.setUsername(session.getUsername());
        });
        return ResponseEntity.ok(response);
    }

    /**
     * 查询服务目录
     *
     * @param servletRequest Servlet 请求
     * @return ResponseEntity<CatalogResponse> 返回服务目录
     */
    @GetMapping("/catalog")
    public ResponseEntity<ApiDocConsolePayloads.CatalogResponse> catalog(HttpServletRequest servletRequest) {
        if (requireSession(servletRequest).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(consoleService.catalog(sessionService.currentMode()));
    }

    /**
     * 查询服务 OpenAPI JSON
     *
     * @param serviceId      服务 ID
     * @param servletRequest Servlet 请求
     * @return ResponseEntity<JsonNode> 返回 OpenAPI JSON
     */
    @GetMapping("/openapi/{serviceId}")
    public ResponseEntity<JsonNode> openApi(@PathVariable String serviceId, HttpServletRequest servletRequest) {
        if (requireSession(servletRequest).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(block(consoleService.fetchOpenApi(serviceId)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    /**
     * 执行接口调试请求
     *
     * @param request        调试请求
     * @param servletRequest Servlet 请求
     * @return ResponseEntity<Object> 返回调试响应
     */
    @PostMapping("/execute")
    public ResponseEntity<Object> execute(@RequestBody ApiDocConsolePayloads.ExecuteRequest request,
                                          HttpServletRequest servletRequest) {
        if (requireSession(servletRequest).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (sessionService.isReadOnly()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "当前环境只读，禁止执行调试请求"));
        }
        try {
            return ResponseEntity.ok(block(consoleService.execute(request)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 执行轻量压测
     *
     * @param request        压测请求
     * @param servletRequest Servlet 请求
     * @return ResponseEntity<Object> 返回压测结果
     */
    @PostMapping("/load-test")
    public ResponseEntity<Object> loadTest(@RequestBody ApiDocConsolePayloads.LoadTestRequest request,
                                           HttpServletRequest servletRequest) {
        if (requireSession(servletRequest).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (sessionService.isReadOnly() || !properties.getLoadTest().isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "当前环境禁止压测"));
        }
        try {
            return ResponseEntity.ok(block(consoleService.loadTest(request)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 导出接口文档
     *
     * @param serviceId      服务 ID
     * @param format         导出格式
     * @param servletRequest Servlet 请求
     * @return ResponseEntity<byte[]> 返回导出文件
     */
    @GetMapping("/export/{serviceId}")
    public ResponseEntity<byte[]> export(@PathVariable String serviceId,
                                         @RequestParam(defaultValue = "md") String format,
                                         HttpServletRequest servletRequest) {
        if (requireSession(servletRequest).isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!properties.getExport().isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String normalizedFormat = "pdf".equalsIgnoreCase(format) ? "pdf" : "md";
        Mono<byte[]> content = "pdf".equals(normalizedFormat) ? consoleService.exportPdf(serviceId) : consoleService.exportMarkdown(serviceId);
        try {
            return exportResponse(serviceId, normalizedFormat, block(content));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }

    /**
     * 校验登录会话
     *
     * @param request Servlet 请求
     * @return Optional<UserSession> 返回登录会话
     */
    private Optional<ApiDocConsolePayloads.UserSession> requireSession(HttpServletRequest request) {
        if (!sessionService.isConsoleOpen()) {
            return Optional.empty();
        }
        return sessionService.resolveSession(request);
    }

    /**
     * 阻塞等待响应式结果
     *
     * @param mono 响应式结果
     * @param <T>  响应类型
     * @return T 返回结果
     */
    private <T> T block(Mono<T> mono) {
        Duration timeout = properties.getRequestTimeout().plusSeconds(1);
        return mono.block(timeout);
    }

    /**
     * 获取客户端标识
     *
     * @param request Servlet 请求
     * @return String 返回客户端标识
     */
    private String clientId(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",", 2)[0].trim();
        }
        return StringUtils.hasText(request.getRemoteAddr()) ? request.getRemoteAddr() : "unknown";
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
