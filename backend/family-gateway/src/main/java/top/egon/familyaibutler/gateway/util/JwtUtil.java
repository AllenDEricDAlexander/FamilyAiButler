package top.egon.familyaibutler.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.gateway.config.FamilyButlerGateWayProperties;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.util
 * @ClassName: JwtUtil
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-15Day-20:19
 * @Description: JWT 工具类
 * @Version: 1.0
 */
@Slf4j
@Component
@Getter
public class JwtUtil {

    private final Key accessKey;

    private final Key refreshKey;

    private final long accessTokenExpireTime;

    private final long refreshTokenExpireTime;

    private final FamilyButlerGateWayProperties familyButlerGatewayProperties;

    public JwtUtil(FamilyButlerGateWayProperties familyButlerGatewayProperties) {
        this.familyButlerGatewayProperties = familyButlerGatewayProperties;
        this.accessTokenExpireTime = familyButlerGatewayProperties.getJwt().getAccessTokenExpireTime();
        this.refreshTokenExpireTime = familyButlerGatewayProperties.getJwt().getRefreshTokenExpireTime();
        this.accessKey = new SecretKeySpec(Base64.getDecoder().decode(familyButlerGatewayProperties.getJwt().getAccessKey()), SignatureAlgorithm.HS512.getJcaName());
        this.refreshKey = new SecretKeySpec(Base64.getDecoder().decode(familyButlerGatewayProperties.getJwt().getRefreshKey()), SignatureAlgorithm.HS512.getJcaName());
    }

    public String createJWTToken(Map<String, Object> userDetails, long timeToExpire, String key) {
        return createJWTToken(userDetails, timeToExpire, new SecretKeySpec(Base64.getDecoder().decode(key), SignatureAlgorithm.HS512.getJcaName()));
    }

    public String createAccessToken(Map<String, Object> userDetails) {
        return createJWTToken(userDetails, this.getAccessTokenExpireTime(), this.accessKey);
    }

    public String createRefreshToken(Map<String, Object> userDetails) {
        return createJWTToken(userDetails, this.getRefreshTokenExpireTime(), this.refreshKey);
    }

    public boolean validateAccessToken(String jwtToken) {
        return validateToken(jwtToken, this.accessKey);
    }

    public boolean validateRefreshToken(String jwtToken) {
        return validateToken(jwtToken, this.refreshKey);
    }

    public static boolean validateToken(String jwtToken, Key signKey) {
        return parseClaims(jwtToken, signKey).isPresent();
    }

    public static Optional<Claims> parseClaims(String jwtToken, Key signKey) {
        return Optional.ofNullable(Jwts.parserBuilder().setSigningKey(signKey).build().parseClaimsJws(jwtToken).getBody());
    }

    public boolean validateAccessTokenWithoutExpiration(String jwtToken) {
        try {
            Jwts.parserBuilder().setSigningKey(this.accessKey).build().parseClaimsJws(jwtToken);
            return true;
        } catch (ExpiredJwtException | SignatureException | MalformedJwtException | UnsupportedJwtException |
                 IllegalArgumentException e) {
            if (e instanceof ExpiredJwtException) {
                return true;
            }
        }
        return false;
    }

    public String refreshJWTToken(String oldToken, long timeToExpire, Key signKey) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signKey)
                .build()
                .parseClaimsJws(oldToken)
                .getBody();

        Date newExpirationDate = new Date(System.currentTimeMillis() + timeToExpire);

        return Jwts.builder()
                .setId(claims.getId())
                .setSubject(claims.getSubject())
                .claim("authorities", claims.get("authorities"))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(newExpirationDate)
                .signWith(signKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String createJWTToken(Map<String, Object> userDetails, long timeToExpire, Key signKey) {
        return Jwts
                .builder()
                // todo uuidv7
                .setId("ycyd")
                .setSubject(userDetails.get("username").toString())
                .claim("authorities", userDetails.get("authorities"))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + timeToExpire))
                .signWith(signKey, SignatureAlgorithm.HS512).compact();
    }

}