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
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
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

    private final ObjectMapper objectMapper;

    /**
     * 创建登录挑战
     *
     * @param username       用户名
     * @param servletRequest Servlet 请求
     * @return ResponseEntity<LoginChallengeResponse> 返回登录挑战
     */
    @GetMapping("/login-challenge")
    public ResponseEntity<ApiDocConsolePayloads.LoginChallengeResponse> loginChallenge(@RequestParam(required = false) String username,
                                                                                       HttpServletRequest servletRequest) {
        if (!sessionService.isConsoleOpen()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sessionService.createLoginChallenge(username, clientId(servletRequest)));
    }

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
        Optional<ApiDocConsoleSessionService.LoginValidation> validation = sessionService.validateLoginProof(request, clientId(servletRequest));
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
     * @param servletRequest Servlet 请求
     * @return ResponseEntity<Void> 返回退出登录结果
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest servletRequest) {
        Optional<ApiDocConsolePayloads.UserSession> session = requireSession(servletRequest);
        if (session.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!validateConsoleSignature(session.get(), servletRequest, "")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
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
        resolveServletSession(servletRequest).ifPresent(session -> {
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
        Optional<ApiDocConsolePayloads.UserSession> session = requireSession(servletRequest);
        if (session.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!validateConsoleSignature(session.get(), servletRequest, "")) {
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
        Optional<ApiDocConsolePayloads.UserSession> session = requireSession(servletRequest);
        if (session.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!validateConsoleSignature(session.get(), servletRequest, "")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(block(consoleService.fetchOpenApi(serviceId)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorBody(e));
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
    public ResponseEntity<Object> execute(@RequestBody(required = false) String requestBody,
                                          HttpServletRequest servletRequest) {
        Optional<ApiDocConsolePayloads.UserSession> session = requireSession(servletRequest);
        if (session.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!validateConsoleSignature(session.get(), servletRequest, requestBody)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (sessionService.isReadOnly()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "当前环境只读，禁止执行调试请求"));
        }
        ApiDocConsolePayloads.ExecuteRequest request;
        try {
            request = objectMapper.readValue(StringUtils.hasText(requestBody) ? requestBody : "{}", ApiDocConsolePayloads.ExecuteRequest.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "请求体不是合法 JSON"));
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
    public ResponseEntity<Object> loadTest(@RequestBody(required = false) String requestBody,
                                           HttpServletRequest servletRequest) {
        Optional<ApiDocConsolePayloads.UserSession> session = requireSession(servletRequest);
        if (session.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!validateConsoleSignature(session.get(), servletRequest, requestBody)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (sessionService.isReadOnly() || !properties.getLoadTest().isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "当前环境禁止压测"));
        }
        ApiDocConsolePayloads.LoadTestRequest request;
        try {
            request = objectMapper.readValue(StringUtils.hasText(requestBody) ? requestBody : "{}", ApiDocConsolePayloads.LoadTestRequest.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "请求体不是合法 JSON"));
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
        Optional<ApiDocConsolePayloads.UserSession> session = requireSession(servletRequest);
        if (session.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!validateConsoleSignature(session.get(), servletRequest, "")) {
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
        return resolveServletSession(request);
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
     * @param session 登录会话
     * @param request Servlet 请求
     * @param body    请求体
     * @return boolean 返回 true 表示签名有效
     */
    private boolean validateConsoleSignature(ApiDocConsolePayloads.UserSession session, HttpServletRequest request, String body) {
        return sessionService.validateRequestSignature(
                session,
                request.getMethod(),
                pathWithQuery(request),
                body == null ? "" : body,
                request.getHeader("X-OpenAPI-Console-Timestamp"),
                request.getHeader("X-OpenAPI-Console-Nonce"),
                request.getHeader("X-OpenAPI-Console-Signature"));
    }

    /**
     * 获取请求路径和查询串
     *
     * @param request Servlet 请求
     * @return String 返回路径和查询串
     */
    private String pathWithQuery(HttpServletRequest request) {
        String query = request.getQueryString();
        return StringUtils.hasText(query) ? request.getRequestURI() + "?" + query : request.getRequestURI();
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
     * 从 Servlet 请求中解析登录会话
     *
     * @param request Servlet 请求
     * @return Optional<UserSession> 返回登录会话
     */
    private Optional<ApiDocConsolePayloads.UserSession> resolveServletSession(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (properties.getAuth().getCookieName().equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return sessionService.resolveSession(cookie.getValue());
            }
        }
        return Optional.empty();
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
