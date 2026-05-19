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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
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
@Component
@ConditionalOnProperty(prefix = "family.security.jwt", name = "enabled", havingValue = "true")
public class FamilyJwtService {

    private static final int HS512_MIN_KEY_BYTES = 64;

    private static final String AUTHORITIES_CLAIM = "authorities";

    private static final String BEARER_PREFIX = "Bearer ";

    private final FamilyJwtProperties properties;

    private final Key accessKey;

    private final Key refreshKey;

    /**
     * 创建统一 JWT 服务
     *
     * @param properties JWT 配置
     */
    public FamilyJwtService(FamilyJwtProperties properties) {
        this.properties = properties;
        this.accessKey = buildHs512Key(properties.getAccessKey(), "access-key");
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
        return createToken(username, authorities, properties.getAccessTokenExpireTime(), accessKey);
    }

    /**
     * 签发刷新令牌
     *
     * @param username    用户名
     * @param authorities 权限列表
     * @return String 返回刷新令牌
     */
    public String createRefreshToken(String username, Collection<String> authorities) {
        return createToken(username, authorities, properties.getRefreshTokenExpireTime(), refreshKey);
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
        return parseClaims(token, accessKey);
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
     * 创建 JWT 令牌
     *
     * @param username     用户名
     * @param authorities  权限列表
     * @param expireMillis 有效期毫秒数
     * @param key          签名密钥
     * @return String 返回 JWT 令牌
     */
    private String createToken(String username, Collection<String> authorities, long expireMillis, Key key) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(username)
                .claim(AUTHORITIES_CLAIM, authorities)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expireMillis))
                .signWith(key, SignatureAlgorithm.HS512)
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
}
