/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @FileName: ApiDocConsoleSessionService.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:25
 * @Description: OpenAPI 调试文档控制台会话服务文件
 * @Version: 1.0
 */
package top.egon.openapi.console;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @ClassName: ApiDocConsoleSessionService
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:25
 * @Description: OpenAPI 调试文档控制台会话服务
 * @Version: 1.0
 */
@RequiredArgsConstructor
public class ApiDocConsoleSessionService {

    private static final String PASSWORD_SHA256_PREFIX = "{sha256}";

    private static final String PASSWORD_PLAIN_PREFIX = "{plain}";

    private final ApiDocConsoleProperties properties;

    private final Environment environment;

    /**
     * 判断文档平台是否开放
     *
     * @return boolean 返回 true 表示开放
     */
    public boolean isConsoleOpen() {
        return currentMode() != ApiDocConsoleProperties.Mode.OFF;
    }

    /**
     * 判断当前环境是否只读
     *
     * @return boolean 返回 true 表示只读
     */
    public boolean isReadOnly() {
        return currentMode() == ApiDocConsoleProperties.Mode.READ_ONLY;
    }

    /**
     * 计算当前文档平台开放模式
     *
     * @return ApiDocConsoleProperties.Mode 返回当前开放模式
     */
    public ApiDocConsoleProperties.Mode currentMode() {
        if (!properties.isEnabled()) {
            return ApiDocConsoleProperties.Mode.OFF;
        }
        if (properties.getMode() != ApiDocConsoleProperties.Mode.AUTO) {
            return properties.getMode();
        }
        if ("prod".equalsIgnoreCase(properties.getEnvironment()) || hasActiveProfile("prod")) {
            return ApiDocConsoleProperties.Mode.READ_ONLY;
        }
        return ApiDocConsoleProperties.Mode.FULL;
    }

    /**
     * 校验登录账号密码
     *
     * @param username 用户名
     * @param password 密码
     * @return boolean 返回 true 表示账号密码有效
     */
    public boolean validateLogin(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return false;
        }
        return constantEquals(properties.getAuth().getUsername(), username)
                && validatePassword(password, properties.getAuth().getPassword());
    }

    /**
     * 创建登录会话 Cookie
     *
     * @param username 用户名
     * @return ResponseCookie 返回登录会话 Cookie
     */
    public ResponseCookie createSessionCookie(String username) {
        long expiresAt = System.currentTimeMillis() + properties.getAuth().getTtl().toMillis();
        String token = createToken(username, expiresAt);
        return ResponseCookie.from(properties.getAuth().getCookieName(), token)
                .httpOnly(true)
                .secure(properties.getAuth().isSecureCookie())
                .sameSite(properties.getAuth().getSameSite())
                .path(normalizedBasePath())
                .maxAge(properties.getAuth().getTtl())
                .build();
    }

    /**
     * 创建清理登录会话 Cookie
     *
     * @return ResponseCookie 返回清理 Cookie
     */
    public ResponseCookie createLogoutCookie() {
        return ResponseCookie.from(properties.getAuth().getCookieName(), "")
                .httpOnly(true)
                .secure(properties.getAuth().isSecureCookie())
                .sameSite(properties.getAuth().getSameSite())
                .path(normalizedBasePath())
                .maxAge(Duration.ZERO)
                .build();
    }

    /**
     * 从请求中解析登录会话
     *
     * @param exchange WebFlux 请求上下文
     * @return Optional<UserSession> 返回登录会话
     */
    public Optional<ApiDocConsolePayloads.UserSession> resolveSession(ServerWebExchange exchange) {
        var cookie = exchange.getRequest().getCookies().getFirst(properties.getAuth().getCookieName());
        if (cookie == null || !StringUtils.hasText(cookie.getValue())) {
            return Optional.empty();
        }
        return parseToken(cookie.getValue());
    }

    /**
     * 获取会话过期时间
     *
     * @return long 返回过期时间戳
     */
    public long nextExpiresAt() {
        return System.currentTimeMillis() + properties.getAuth().getTtl().toMillis();
    }

    /**
     * 创建会话令牌
     *
     * @param username  用户名
     * @param expiresAt 过期时间
     * @return String 返回会话令牌
     */
    private String createToken(String username, long expiresAt) {
        String payload = username + ":" + expiresAt + ":" + UUID.randomUUID();
        String payloadValue = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return payloadValue + "." + hmacSha256(payloadValue);
    }

    /**
     * 解析会话令牌
     *
     * @param token 会话令牌
     * @return Optional<UserSession> 返回登录会话
     */
    private Optional<ApiDocConsolePayloads.UserSession> parseToken(String token) {
        String[] parts = token.split("\\.", 2);
        if (parts.length != 2 || !constantEquals(hmacSha256(parts[0]), parts[1])) {
            return Optional.empty();
        }
        String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String[] values = payload.split(":", 3);
        if (values.length != 3) {
            return Optional.empty();
        }
        long expiresAt = Long.parseLong(values[1]);
        if (expiresAt < System.currentTimeMillis()) {
            return Optional.empty();
        }
        ApiDocConsolePayloads.UserSession session = new ApiDocConsolePayloads.UserSession();
        session.setUsername(values[0]);
        session.setExpiresAt(expiresAt);
        return Optional.of(session);
    }

    /**
     * 校验密码
     *
     * @param inputPassword      输入密码
     * @param configuredPassword 配置密码
     * @return boolean 返回 true 表示密码匹配
     */
    private boolean validatePassword(String inputPassword, String configuredPassword) {
        if (!StringUtils.hasText(configuredPassword)) {
            return false;
        }
        if (configuredPassword.startsWith(PASSWORD_SHA256_PREFIX)) {
            return constantEquals(sha256Hex(inputPassword), configuredPassword.substring(PASSWORD_SHA256_PREFIX.length()));
        }
        if (configuredPassword.startsWith(PASSWORD_PLAIN_PREFIX)) {
            return constantEquals(inputPassword, configuredPassword.substring(PASSWORD_PLAIN_PREFIX.length()));
        }
        return constantEquals(inputPassword, configuredPassword);
    }

    /**
     * 判断指定 Profile 是否启用
     *
     * @param profile Profile 名称
     * @return boolean 返回 true 表示启用
     */
    private boolean hasActiveProfile(String profile) {
        return Arrays.stream(environment.getActiveProfiles()).anyMatch(profile::equalsIgnoreCase);
    }

    /**
     * 生成 HmacSHA256 摘要
     *
     * @param value 原文
     * @return String 返回 URL 安全摘要
     */
    private String hmacSha256(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(properties.getAuth().getSessionSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("接口文档平台会话签名失败", e);
        }
    }

    /**
     * 生成 SHA256 十六进制摘要
     *
     * @param value 原文
     * @return String 返回十六进制摘要
     */
    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte item : bytes) {
                builder.append(String.format("%02x", item));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 算法不存在", e);
        }
    }

    /**
     * 常量时间字符串比较
     *
     * @param left  左侧字符串
     * @param right 右侧字符串
     * @return boolean 返回 true 表示相等
     */
    private boolean constantEquals(String left, String right) {
        if (left == null || right == null) {
            return false;
        }
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 获取规范化基础路径
     *
     * @return String 返回基础路径
     */
    private String normalizedBasePath() {
        return StringUtils.hasText(properties.getBasePath()) ? properties.getBasePath() : "/";
    }
}
