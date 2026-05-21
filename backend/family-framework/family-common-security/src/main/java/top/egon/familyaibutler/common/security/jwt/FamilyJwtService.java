/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.security.jwt
 * @FileName: FamilyJwtService.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-23:50
 * @Description: 统一 JWT 签发与校验服务文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.common.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.security.jwt
 * @ClassName: FamilyJwtService
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-23:50
 * @Description: 统一 JWT 签发与校验服务
 * @Version: 1.0
 */
public class FamilyJwtService {

    private static final int HS512_MIN_KEY_BYTES = 64;

    private static final String ALG_RS256 = "RS256";

    private static final String ALG_HS512 = "HS512";

    private static final String AUTHORITIES_CLAIM = "authorities";

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String PROFILE_ID_CLAIM = "profile_id";

    private static final String CLIENT_ID_CLAIM = "client_id";

    private static final String SESSION_ID_CLAIM = "session_id";

    private static final String DEVICE_ID_CLAIM = "device_id";

    private static final String AUTH_VERSION_CLAIM = "auth_version";

    private static final String ENTITLEMENT_VERSION_CLAIM = "entitlement_version";

    private static final String RISK_LEVEL_CLAIM = "risk_level";

    private final FamilyJwtProperties properties;

    private final Key accessSigningKey;

    private final Key accessVerificationKey;

    private final Key refreshKey;

    private final SignatureAlgorithm accessSignatureAlgorithm;

    /**
     * 创建统一 JWT 服务
     *
     * @param properties JWT 配置
     */
    public FamilyJwtService(FamilyJwtProperties properties) {
        this.properties = properties;
        String algorithm = StringUtils.hasText(properties.getAlgorithm()) ? properties.getAlgorithm().trim().toUpperCase(Locale.ROOT) : ALG_HS512;
        if (ALG_RS256.equals(algorithm)) {
            this.accessSignatureAlgorithm = SignatureAlgorithm.RS256;
            this.accessSigningKey = buildRsaPrivateKey(properties.getAccessPrivateKey());
            this.accessVerificationKey = buildRsaPublicKey(properties.getAccessPublicKey());
        } else if (ALG_HS512.equals(algorithm)) {
            this.accessSignatureAlgorithm = SignatureAlgorithm.HS512;
            this.accessSigningKey = buildHs512Key(properties.getAccessKey(), "access-key");
            this.accessVerificationKey = this.accessSigningKey;
        } else {
            throw new IllegalStateException("不支持的 JWT 签名算法: " + algorithm);
        }
        this.refreshKey = buildHs512Key(properties.getRefreshKey(), "refresh-key");
    }

    /**
     * 签发访问令牌
     *
     * @param username    用户名
     * @param authorities 权限列表
     * @return String 返回访问令牌
     */
    public String createAccessToken(String username, Collection<String> authorities) {
        return createToken(username, authorities, properties.getAccessTokenExpireTime(), accessSigningKey, accessSignatureAlgorithm);
    }

    /**
     * 签发 UAA 访问令牌。
     *
     * @param claims UAA 身份声明
     * @return String 返回访问令牌
     */
    public String createAccessToken(FamilyJwtClaims claims) {
        long now = System.currentTimeMillis();
        Instant expiresAt = claims.expiresAt() == null
                ? Instant.ofEpochMilli(now + properties.getAccessTokenExpireTime())
                : claims.expiresAt();
        return Jwts.builder()
                .setHeaderParam("kid", properties.getKeyId())
                .setId(StringUtils.hasText(claims.tokenId()) ? claims.tokenId() : UUID.randomUUID().toString())
                .setSubject(claims.accountId())
                .setIssuer(claims.issuer())
                .setAudience(claims.audience())
                .claim(PROFILE_ID_CLAIM, claims.profileId())
                .claim(CLIENT_ID_CLAIM, claims.clientId())
                .claim(SESSION_ID_CLAIM, claims.sessionId())
                .claim(DEVICE_ID_CLAIM, claims.deviceId())
                .claim(AUTH_VERSION_CLAIM, claims.authVersion())
                .claim(ENTITLEMENT_VERSION_CLAIM, claims.entitlementVersion())
                .claim(RISK_LEVEL_CLAIM, claims.riskLevel())
                .setIssuedAt(new Date(now))
                .setExpiration(Date.from(expiresAt))
                .signWith(accessSigningKey, accessSignatureAlgorithm)
                .compact();
    }

    /**
     * 签发刷新令牌
     *
     * @param username    用户名
     * @param authorities 权限列表
     * @return String 返回刷新令牌
     */
    public String createRefreshToken(String username, Collection<String> authorities) {
        return createToken(username, authorities, properties.getRefreshTokenExpireTime(), refreshKey, SignatureAlgorithm.HS512);
    }

    /**
     * 校验访问令牌
     *
     * @param token JWT 令牌
     * @return boolean 返回 true 表示令牌有效
     */
    public boolean validateAccessToken(String token) {
        return parseAccessClaims(token).isPresent();
    }

    /**
     * 校验刷新令牌
     *
     * @param token JWT 令牌
     * @return boolean 返回 true 表示令牌有效
     */
    public boolean validateRefreshToken(String token) {
        return parseRefreshClaims(token).isPresent();
    }

    /**
     * 解析访问令牌 Claims
     *
     * @param token JWT 令牌
     * @return Optional<Claims> 返回访问令牌 Claims
     */
    public Optional<Claims> parseAccessClaims(String token) {
        return parseClaims(token, accessVerificationKey);
    }

    /**
     * 解析 UAA 访问令牌身份声明。
     *
     * @param token JWT 令牌
     * @return Optional<FamilyJwtClaims> 返回身份声明
     */
    public Optional<FamilyJwtClaims> parseAccessJwtClaims(String token) {
        return parseAccessClaims(token).map(this::toFamilyJwtClaims);
    }

    /**
     * 解析刷新令牌 Claims
     *
     * @param token JWT 令牌
     * @return Optional<Claims> 返回刷新令牌 Claims
     */
    public Optional<Claims> parseRefreshClaims(String token) {
        return parseClaims(token, refreshKey);
    }

    /**
     * 从 Authorization 头解析 JWT 令牌
     *
     * @param authorization Authorization 请求头
     * @return String 返回 JWT 令牌
     */
    public String resolveAuthorizationToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        String value = authorization.trim();
        if (value.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return value.substring(BEARER_PREFIX.length()).trim();
        }
        String configuredPrefix = properties.getTokenPrefix();
        if (StringUtils.hasText(configuredPrefix)) {
            String normalizedPrefix = configuredPrefix.endsWith(" ") ? configuredPrefix : configuredPrefix + " ";
            if (value.regionMatches(true, 0, normalizedPrefix, 0, normalizedPrefix.length())) {
                return value.substring(normalizedPrefix.length()).trim();
            }
        }
        return value;
    }

    /**
     * 获取 Authorization Header 名称
     *
     * @return String 返回 Header 名称
     */
    public String authorizationHeader() {
        return properties.getAuthorizationHeader();
    }

    /**
     * 获取访问令牌有效期
     *
     * @return long 返回访问令牌有效期毫秒数
     */
    public long accessTokenExpireTime() {
        return properties.getAccessTokenExpireTime();
    }

    /**
     * 导出公开 JWK Set。
     *
     * @return Optional<Map<String, Object>> 返回 JWK Set
     */
    public Optional<Map<String, Object>> publicJwkSet() {
        if (!(accessVerificationKey instanceof RSAPublicKey rsaPublicKey)) {
            return Optional.empty();
        }
        Map<String, Object> jwk = new LinkedHashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("use", "sig");
        jwk.put("kid", properties.getKeyId());
        jwk.put("alg", ALG_RS256);
        jwk.put("n", Base64.getUrlEncoder().withoutPadding().encodeToString(stripLeadingZero(rsaPublicKey.getModulus().toByteArray())));
        jwk.put("e", Base64.getUrlEncoder().withoutPadding().encodeToString(stripLeadingZero(rsaPublicKey.getPublicExponent().toByteArray())));
        return Optional.of(Map.of("keys", List.of(jwk)));
    }

    /**
     * 创建 JWT 令牌
     *
     * @param username     用户名
     * @param authorities  权限列表
     * @param expireMillis 有效期毫秒数
     * @param key          签名密钥
     * @param algorithm    签名算法
     * @return String 返回 JWT 令牌
     */
    private String createToken(String username, Collection<String> authorities, long expireMillis, Key key,
                               SignatureAlgorithm algorithm) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setHeaderParam("kid", properties.getKeyId())
                .setId(UUID.randomUUID().toString())
                .setSubject(username)
                .claim(AUTHORITIES_CLAIM, authorities)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expireMillis))
                .signWith(key, algorithm)
                .compact();
    }

    /**
     * 解析 JWT Claims
     *
     * @param token JWT 令牌
     * @param key   签名密钥
     * @return Optional<Claims> 返回 JWT Claims
     */
    private Optional<Claims> parseClaims(String token, Key key) {
        try {
            String resolvedToken = resolveAuthorizationToken(token);
            if (!StringUtils.hasText(resolvedToken)) {
                return Optional.empty();
            }
            return Optional.of(Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(resolvedToken).getBody());
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    /**
     * 转换为 UAA 身份声明。
     *
     * @param claims JWT Claims
     * @return FamilyJwtClaims 返回 UAA 身份声明
     */
    private FamilyJwtClaims toFamilyJwtClaims(Claims claims) {
        return new FamilyJwtClaims(claims.getId(), claims.getSubject(), claims.get(PROFILE_ID_CLAIM, String.class),
                claims.get(CLIENT_ID_CLAIM, String.class), claims.get(SESSION_ID_CLAIM, String.class),
                claims.get(DEVICE_ID_CLAIM, String.class), getLongClaim(claims, AUTH_VERSION_CLAIM),
                getLongClaim(claims, ENTITLEMENT_VERSION_CLAIM), claims.get(RISK_LEVEL_CLAIM, String.class),
                claims.getIssuer(), claims.getAudience(), claims.getExpiration().toInstant());
    }

    /**
     * 获取 long 类型声明。
     *
     * @param claims    JWT Claims
     * @param claimName 声明名称
     * @return long 返回声明值
     */
    private long getLongClaim(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && StringUtils.hasText(text)) {
            return Long.parseLong(text);
        }
        return 0L;
    }

    /**
     * 创建 HS512 签名密钥
     *
     * @param value Base64 密钥
     * @param name  密钥名称
     * @return Key 返回签名密钥
     */
    private Key buildHs512Key(String value, String name) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("JWT " + name + " 不能为空");
        }
        byte[] bytes = Base64.getDecoder().decode(value);
        if (bytes.length < HS512_MIN_KEY_BYTES) {
            throw new IllegalStateException("JWT " + name + " 至少需要 64 字节 Base64 解码密钥");
        }
        return new SecretKeySpec(bytes, SignatureAlgorithm.HS512.getJcaName());
    }

    /**
     * 创建 RSA 私钥。
     *
     * @param value Base64 PKCS8 私钥
     * @return PrivateKey 返回 RSA 私钥
     */
    private PrivateKey buildRsaPrivateKey(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("JWT access-private-key 不能为空");
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(value);
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT access-private-key 解析失败", exception);
        }
    }

    /**
     * 创建 RSA 公钥。
     *
     * @param value Base64 X509 公钥
     * @return PublicKey 返回 RSA 公钥
     */
    private PublicKey buildRsaPublicKey(String value) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException("JWT access-public-key 不能为空");
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(value);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
        } catch (Exception exception) {
            throw new IllegalStateException("JWT access-public-key 解析失败", exception);
        }
    }

    /**
     * 去除 BigInteger 字节数组的符号前导零。
     *
     * @param bytes 原始字节数组
     * @return byte[] 规范字节数组
     */
    private byte[] stripLeadingZero(byte[] bytes) {
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] stripped = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, stripped, 0, stripped.length);
            return stripped;
        }
        return bytes;
    }
}
