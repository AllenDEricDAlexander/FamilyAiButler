/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.core
 * @FileName: ApiDocConsoleSessionService.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:25
 * @Description: OpenAPI 调试文档控制台会话服务文件
 * @Version: 1.0
 */
package top.egon.openapi.console.core;

import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import top.egon.openapi.console.ApiDocConsolePayloads;
import top.egon.openapi.console.ApiDocConsoleProperties;
import top.egon.openapi.console.security.ApiDocConsoleCryptoSupport;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.core
 * @ClassName: ApiDocConsoleSessionService
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:25
 * @Description: OpenAPI 调试文档控制台会话服务
 * @Version: 1.0
 */
public class ApiDocConsoleSessionService {

    private static final String PASSWORD_SHA256_PREFIX = "{sha256}";

    private static final String PASSWORD_PLAIN_PREFIX = "{plain}";

    private static final String LOGIN_CHALLENGE_ALGORITHM = "SHA-256+HMAC-SHA256";

    private static final String REQUEST_SIGNING_KEY_PURPOSE = "openapi-console-request-signing";

    private static final String ENCRYPTED_TOKEN_PREFIX = "v2enc:";

    private static final String DEFAULT_PASSWORD = "OpenApi@123456";

    private static final String DEFAULT_PASSWORD_SHA256 = "26f14356ce3643fcbe7facfd45819669f3f044f42360b009c740a10ceddb2fd2";

    private static final String DEFAULT_SESSION_SECRET = "OpenApiDebugConsoleSessionSecretChangeMe";

    private static final String CLIENT_UNKNOWN = "unknown";

    private final ApiDocConsoleProperties properties;

    private final Environment environment;

    private final Map<String, LoginFailure> loginFailureMap = new ConcurrentHashMap<>();

    private final Map<String, LoginChallenge> loginChallengeMap = new ConcurrentHashMap<>();

    private final Map<String, Long> requestNonceMap = new ConcurrentHashMap<>();

    /**
     * 创建接口文档平台会话服务
     *
     * @param properties  接口文档平台配置
     * @param environment Spring 环境对象
     */
    public ApiDocConsoleSessionService(ApiDocConsoleProperties properties, Environment environment) {
        this.properties = properties;
        this.environment = environment;
        validateAuthConfiguration();
    }

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
        return validateLogin(username, password, CLIENT_UNKNOWN);
    }

    /**
     * 校验登录账号密码并记录失败次数
     *
     * @param username 用户名
     * @param password 密码
     * @param clientId 客户端标识
     * @return boolean 返回 true 表示账号密码有效
     */
    public boolean validateLogin(String username, String password, String clientId) {
        String failureKey = loginFailureKey(username, clientId);
        if (isLoginLocked(failureKey)) {
            return false;
        }
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            recordLoginFailure(failureKey);
            return false;
        }
        boolean matched = ApiDocConsoleCryptoSupport.constantEquals(properties.getAuth().getUsername(), username)
                && validatePassword(password, properties.getAuth().getPassword());
        if (matched) {
            loginFailureMap.remove(failureKey);
            return true;
        }
        recordLoginFailure(failureKey);
        return false;
    }

    /**
     * 创建登录挑战，避免前端提交明文密码。
     *
     * @param username 用户名
     * @param clientId 客户端标识
     * @return LoginChallengeResponse 返回登录挑战
     */
    public ApiDocConsolePayloads.LoginChallengeResponse createLoginChallenge(String username, String clientId) {
        cleanupExpiredSecurityState();
        long expiresAt = System.currentTimeMillis() + properties.getAuth().getChallengeTtl().toMillis();
        LoginChallenge challenge = new LoginChallenge();
        challenge.username = username;
        challenge.clientId = StringUtils.hasText(clientId) ? clientId : CLIENT_UNKNOWN;
        challenge.nonce = ApiDocConsoleCryptoSupport.randomToken(24);
        challenge.expiresAt = expiresAt;
        String challengeId = UUID.randomUUID().toString();
        loginChallengeMap.put(challengeId, challenge);

        ApiDocConsolePayloads.LoginChallengeResponse response = new ApiDocConsolePayloads.LoginChallengeResponse();
        response.setChallengeId(challengeId);
        response.setNonce(challenge.nonce);
        response.setAlgorithm(LOGIN_CHALLENGE_ALGORITHM);
        response.setExpiresAt(expiresAt);
        return response;
    }

    /**
     * 校验登录挑战证明并生成控制台请求签名密钥。
     *
     * @param request  登录请求
     * @param clientId 客户端标识
     * @return Optional<LoginValidation> 返回登录校验结果
     */
    public Optional<LoginValidation> validateLoginProof(ApiDocConsolePayloads.LoginRequest request, String clientId) {
        String username = request == null ? null : request.getUsername();
        String failureKey = loginFailureKey(username, clientId);
        if (isLoginLocked(failureKey)) {
            return Optional.empty();
        }
        if (request == null || !StringUtils.hasText(username) || !StringUtils.hasText(request.getChallengeId())
                || !StringUtils.hasText(request.getProof())) {
            recordLoginFailure(failureKey);
            return Optional.empty();
        }
        LoginChallenge challenge = loginChallengeMap.remove(request.getChallengeId());
        if (challenge == null || challenge.expiresAt < System.currentTimeMillis()
                || !ApiDocConsoleCryptoSupport.constantEquals(challenge.username, username)
                || !ApiDocConsoleCryptoSupport.constantEquals(challenge.clientId, StringUtils.hasText(clientId) ? clientId : CLIENT_UNKNOWN)) {
            recordLoginFailure(failureKey);
            return Optional.empty();
        }
        if (!ApiDocConsoleCryptoSupport.constantEquals(properties.getAuth().getUsername(), username) || !timestampInWindow(request.getTimestamp(), properties.getAuth().getChallengeTtl())) {
            recordLoginFailure(failureKey);
            return Optional.empty();
        }
        byte[] passwordDigest = configuredPasswordDigest();
        String proofPayload = loginProofPayload(request.getChallengeId(), challenge.nonce, username, request.getTimestamp());
        String expectedProof = ApiDocConsoleCryptoSupport.hmacSha256Hex(passwordDigest, proofPayload);
        if (!ApiDocConsoleCryptoSupport.constantEquals(expectedProof, request.getProof())) {
            recordLoginFailure(failureKey);
            return Optional.empty();
        }
        loginFailureMap.remove(failureKey);
        String requestSigningSecret = ApiDocConsoleCryptoSupport.base64Url(ApiDocConsoleCryptoSupport.hmacSha256Bytes(passwordDigest, REQUEST_SIGNING_KEY_PURPOSE + "\n" + proofPayload));
        return Optional.of(new LoginValidation(username, requestSigningSecret));
    }

    /**
     * 创建登录会话 Cookie
     *
     * @param username 用户名
     * @return ResponseCookie 返回登录会话 Cookie
     */
    public ResponseCookie createSessionCookie(String username) {
        return createSessionCookie(username, ApiDocConsoleCryptoSupport.randomToken(32));
    }

    /**
     * 创建带控制台请求签名密钥的登录会话 Cookie
     *
     * @param username             用户名
     * @param requestSigningSecret 控制台请求签名密钥
     * @return ResponseCookie 返回登录会话 Cookie
     */
    public ResponseCookie createSessionCookie(String username, String requestSigningSecret) {
        long expiresAt = System.currentTimeMillis() + properties.getAuth().getTtl().toMillis();
        String token = createToken(username, expiresAt, requestSigningSecret);
        return ResponseCookie.from(properties.getAuth().getCookieName(), token)
                .httpOnly(true)
                .secure(shouldUseSecureCookie())
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
                .secure(shouldUseSecureCookie())
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
        return resolveSession(cookie.getValue());
    }

    /**
     * 从会话 Cookie 值中解析登录会话
     *
     * @param cookieValue 会话 Cookie 值
     * @return Optional<UserSession> 返回登录会话
     */
    public Optional<ApiDocConsolePayloads.UserSession> resolveSession(String cookieValue) {
        if (!StringUtils.hasText(cookieValue)) {
            return Optional.empty();
        }
        return parseToken(cookieValue);
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
     * 校验控制台 API 请求签名
     *
     * @param session       登录会话
     * @param method        HTTP 方法
     * @param pathWithQuery 请求路径和查询串
     * @param body          请求体
     * @param timestamp     签名时间戳
     * @param nonce         签名随机数
     * @param signature     请求签名
     * @return boolean 返回 true 表示签名有效
     */
    public boolean validateRequestSignature(ApiDocConsolePayloads.UserSession session,
                                            String method,
                                            String pathWithQuery,
                                            String body,
                                            String timestamp,
                                            String nonce,
                                            String signature) {
        if (!properties.getAuth().isRequestSigningEnabled()) {
            return true;
        }
        if (session == null || !StringUtils.hasText(session.getSessionId()) || !StringUtils.hasText(session.getRequestSigningSecret())
                || !StringUtils.hasText(method) || !StringUtils.hasText(pathWithQuery)
                || !StringUtils.hasText(timestamp) || !StringUtils.hasText(nonce) || !StringUtils.hasText(signature)) {
            return false;
        }
        long requestTimestamp;
        try {
            requestTimestamp = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            return false;
        }
        if (!timestampInWindow(requestTimestamp, properties.getAuth().getRequestSignatureTtl())) {
            return false;
        }
        byte[] signingKey;
        try {
            signingKey = Base64.getUrlDecoder().decode(session.getRequestSigningSecret());
        } catch (IllegalArgumentException e) {
            return false;
        }
        String canonical = canonicalRequest(method, pathWithQuery, body, timestamp, nonce);
        String expectedSignature = ApiDocConsoleCryptoSupport.hmacSha256Hex(signingKey, canonical);
        if (!ApiDocConsoleCryptoSupport.constantEquals(expectedSignature, signature)) {
            return false;
        }
        cleanupExpiredSecurityState();
        String nonceKey = session.getSessionId() + ":" + nonce;
        Long oldValue = requestNonceMap.putIfAbsent(nonceKey, System.currentTimeMillis() + properties.getAuth().getRequestSignatureTtl().toMillis());
        return oldValue == null;
    }

    /**
     * 创建会话令牌
     *
     * @param username             用户名
     * @param expiresAt            过期时间
     * @param requestSigningSecret 控制台请求签名密钥
     * @return String 返回会话令牌
     */
    private String createToken(String username, long expiresAt, String requestSigningSecret) {
        String usernameValue = ApiDocConsoleCryptoSupport.base64Url(username.getBytes(StandardCharsets.UTF_8));
        String payload = "v2:" + usernameValue + ":" + expiresAt + ":" + UUID.randomUUID() + ":" + requestSigningSecret;
        String payloadValue = encryptTokenPayload(payload);
        return payloadValue + "." + ApiDocConsoleCryptoSupport.hmacSha256Base64Url(properties.getAuth().getSessionSecret(), payloadValue);
    }

    /**
     * 解析会话令牌
     *
     * @param token 会话令牌
     * @return Optional<UserSession> 返回登录会话
     */
    private Optional<ApiDocConsolePayloads.UserSession> parseToken(String token) {
        String[] parts = token.split("\\.", 2);
        if (parts.length != 2 || !ApiDocConsoleCryptoSupport.constantEquals(ApiDocConsoleCryptoSupport.hmacSha256Base64Url(properties.getAuth().getSessionSecret(), parts[0]), parts[1])) {
            return Optional.empty();
        }
        String payload = decodeTokenPayload(parts[0]);
        if (!StringUtils.hasText(payload)) {
            return Optional.empty();
        }
        String[] values = payload.split(":");
        if (values.length != 3 && values.length != 5) {
            return Optional.empty();
        }
        boolean version2 = "v2".equals(values[0]);
        int expiresIndex = version2 ? 2 : 1;
        long expiresAt;
        try {
            expiresAt = Long.parseLong(values[expiresIndex]);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        if (expiresAt < System.currentTimeMillis()) {
            return Optional.empty();
        }
        ApiDocConsolePayloads.UserSession session = new ApiDocConsolePayloads.UserSession();
        try {
            session.setUsername(version2 ? new String(Base64.getUrlDecoder().decode(values[1]), StandardCharsets.UTF_8) : values[0]);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        session.setSessionId(version2 ? values[3] : values[2]);
        if (version2) {
            session.setRequestSigningSecret(values[4]);
        }
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
            return ApiDocConsoleCryptoSupport.constantEquals(ApiDocConsoleCryptoSupport.sha256Hex(inputPassword), configuredPassword.substring(PASSWORD_SHA256_PREFIX.length()));
        }
        if (configuredPassword.startsWith(PASSWORD_PLAIN_PREFIX)) {
            return ApiDocConsoleCryptoSupport.constantEquals(inputPassword, configuredPassword.substring(PASSWORD_PLAIN_PREFIX.length()));
        }
        return ApiDocConsoleCryptoSupport.constantEquals(inputPassword, configuredPassword);
    }

    /**
     * 获取当前配置密码的 SHA-256 摘要字节
     *
     * @return byte[] 返回密码摘要字节
     */
    private byte[] configuredPasswordDigest() {
        String configuredPassword = properties.getAuth().getPassword();
        if (!StringUtils.hasText(configuredPassword)) {
            return new byte[0];
        }
        if (configuredPassword.startsWith(PASSWORD_SHA256_PREFIX)) {
            return ApiDocConsoleCryptoSupport.hexToBytes(configuredPassword.substring(PASSWORD_SHA256_PREFIX.length()));
        }
        if (configuredPassword.startsWith(PASSWORD_PLAIN_PREFIX)) {
            return ApiDocConsoleCryptoSupport.sha256Bytes(configuredPassword.substring(PASSWORD_PLAIN_PREFIX.length()));
        }
        return ApiDocConsoleCryptoSupport.sha256Bytes(configuredPassword);
    }

    /**
     * 构造登录挑战证明载荷
     *
     * @param challengeId 挑战 ID
     * @param nonce       随机数
     * @param username    用户名
     * @param timestamp   时间戳
     * @return String 返回证明载荷
     */
    private String loginProofPayload(String challengeId, String nonce, String username, long timestamp) {
        return challengeId + "\n" + nonce + "\n" + username + "\n" + timestamp;
    }

    /**
     * 构造控制台 API 请求签名载荷
     *
     * @param method        HTTP 方法
     * @param pathWithQuery 请求路径和查询串
     * @param body          请求体
     * @param timestamp     时间戳
     * @param nonce         随机数
     * @return String 返回签名载荷
     */
    private String canonicalRequest(String method, String pathWithQuery, String body, String timestamp, String nonce) {
        return method.toUpperCase() + "\n" + pathWithQuery + "\n" + timestamp + "\n" + nonce + "\n" + ApiDocConsoleCryptoSupport.sha256Hex(body == null ? "" : body);
    }

    /**
     * 判断请求时间戳是否在允许窗口内
     *
     * @param timestamp 时间戳
     * @param window    允许窗口
     * @return boolean 返回 true 表示时间戳有效
     */
    private boolean timestampInWindow(long timestamp, Duration window) {
        long windowMillis = Math.max(1, window == null ? Duration.ofMinutes(2).toMillis() : window.toMillis());
        long now = System.currentTimeMillis();
        return timestamp > 0 && Math.abs(now - timestamp) <= windowMillis;
    }

    /**
     * 清理过期安全状态
     */
    private void cleanupExpiredSecurityState() {
        long now = System.currentTimeMillis();
        loginChallengeMap.entrySet().removeIf(entry -> entry.getValue().expiresAt <= now);
        requestNonceMap.entrySet().removeIf(entry -> entry.getValue() <= now);
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
     * 校验认证配置安全性
     */
    private void validateAuthConfiguration() {
        if (!properties.isEnabled() || !properties.getAuth().isRejectDefaultCredentials()) {
            return;
        }
        String password = properties.getAuth().getPassword();
        String sessionSecret = properties.getAuth().getSessionSecret();
        if (!StringUtils.hasText(password)
                || DEFAULT_PASSWORD.equals(password)
                || ("{plain}" + DEFAULT_PASSWORD).equals(password)
                || (PASSWORD_SHA256_PREFIX + DEFAULT_PASSWORD_SHA256).equalsIgnoreCase(password)) {
            throw new IllegalStateException("OpenAPI 控制台开启时禁止使用默认密码");
        }
        if (!StringUtils.hasText(sessionSecret)
                || DEFAULT_SESSION_SECRET.equals(sessionSecret)
                || sessionSecret.contains("ChangeMe")
                || sessionSecret.length() < 32) {
            throw new IllegalStateException("OpenAPI 控制台开启时必须配置非默认高强度 session-secret");
        }
    }

    /**
     * 判断登录来源是否被锁定
     *
     * @param failureKey 失败记录键
     * @return boolean 返回 true 表示已锁定
     */
    private boolean isLoginLocked(String failureKey) {
        LoginFailure failure = loginFailureMap.get(failureKey);
        if (failure == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (failure.lockedUntil > now) {
            return true;
        }
        if (failure.lockedUntil > 0) {
            loginFailureMap.remove(failureKey);
        }
        return false;
    }

    /**
     * 记录登录失败
     *
     * @param failureKey 失败记录键
     */
    private void recordLoginFailure(String failureKey) {
        int maxFailures = Math.max(1, properties.getAuth().getMaxLoginFailures());
        Duration lockDuration = properties.getAuth().getLoginLockDuration() == null ? Duration.ofMinutes(10) : properties.getAuth().getLoginLockDuration();
        long lockMillis = Math.max(1, lockDuration.toMillis());
        loginFailureMap.compute(failureKey, (key, oldFailure) -> {
            LoginFailure failure = oldFailure == null ? new LoginFailure() : oldFailure;
            long now = System.currentTimeMillis();
            if (failure.lockedUntil > 0 && failure.lockedUntil <= now) {
                failure.failureCount = 0;
                failure.lockedUntil = 0;
            }
            failure.failureCount++;
            if (failure.failureCount >= maxFailures) {
                failure.lockedUntil = now + lockMillis;
            }
            return failure;
        });
    }

    /**
     * 构造登录失败记录键
     *
     * @param username 用户名
     * @param clientId 客户端标识
     * @return String 返回失败记录键
     */
    private String loginFailureKey(String username, String clientId) {
        String safeUsername = StringUtils.hasText(username) ? username : CLIENT_UNKNOWN;
        String safeClientId = StringUtils.hasText(clientId) ? clientId : CLIENT_UNKNOWN;
        return safeClientId + ":" + safeUsername;
    }

    /**
     * 判断是否使用 Secure Cookie
     *
     * @return boolean 返回 true 表示启用 Secure Cookie
     */
    private boolean shouldUseSecureCookie() {
        return properties.getAuth().isSecureCookie()
                || "prod".equalsIgnoreCase(properties.getEnvironment())
                || hasActiveProfile("prod");
    }

    /**
     * 加密会话令牌载荷
     *
     * @param payload 明文载荷
     * @return String 返回加密载荷
     */
    private String encryptTokenPayload(String payload) {
        return ENCRYPTED_TOKEN_PREFIX + ApiDocConsoleCryptoSupport.encryptPayload(properties.getAuth().getSessionSecret(), payload);
    }

    /**
     * 解码会话令牌载荷
     *
     * @param payloadValue 载荷值
     * @return String 返回明文载荷
     */
    private String decodeTokenPayload(String payloadValue) {
        try {
            if (payloadValue.startsWith(ENCRYPTED_TOKEN_PREFIX)) {
                return ApiDocConsoleCryptoSupport.decryptPayload(properties.getAuth().getSessionSecret(), payloadValue.substring(ENCRYPTED_TOKEN_PREFIX.length()));
            }
            return new String(Base64.getUrlDecoder().decode(payloadValue), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取规范化基础路径
     *
     * @return String 返回基础路径
     */
    private String normalizedBasePath() {
        return StringUtils.hasText(properties.getBasePath()) ? properties.getBasePath() : "/";
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console.core
     * @ClassName: LoginValidation
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-20Day-13:35
     * @Description: 登录挑战校验结果
     * @Version: 1.0
     */
    public static class LoginValidation {

        private final String username;

        private final String requestSigningSecret;

        /**
         * 创建登录挑战校验结果
         *
         * @param username             用户名
         * @param requestSigningSecret 控制台请求签名密钥
         */
        public LoginValidation(String username, String requestSigningSecret) {
            this.username = username;
            this.requestSigningSecret = requestSigningSecret;
        }

        /**
         * 获取用户名
         *
         * @return String 返回用户名
         */
        public String getUsername() {
            return username;
        }

        /**
         * 获取控制台请求签名密钥
         *
         * @return String 返回控制台请求签名密钥
         */
        public String getRequestSigningSecret() {
            return requestSigningSecret;
        }
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console.core
     * @ClassName: LoginChallenge
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-20Day-13:35
     * @Description: 登录挑战记录
     * @Version: 1.0
     */
    private static class LoginChallenge {

        private String username;

        private String clientId;

        private String nonce;

        private long expiresAt;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console.core
     * @ClassName: LoginFailure
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-23:15
     * @Description: 登录失败计数记录
     * @Version: 1.0
     */
    private static class LoginFailure {

        private int failureCount;

        private long lockedUntil;
    }
}
