package top.egon.familyaibutler.gateway.util;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.util
 * @ClassName: JwtUtil
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-15Day-20:19
 * @Description: 网关 JWT 统一工具适配器
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final FamilyJwtService familyJwtService;

    /**
     * 创建访问令牌
     *
     * @param userDetails 用户信息
     * @return String 返回访问令牌
     */
    public String createAccessToken(Map<String, Object> userDetails) {
        return familyJwtService.createAccessToken(userDetails.get("username").toString(), authorities(userDetails));
    }

    /**
     * 创建刷新令牌
     *
     * @param userDetails 用户信息
     * @return String 返回刷新令牌
     */
    public String createRefreshToken(Map<String, Object> userDetails) {
        return familyJwtService.createRefreshToken(userDetails.get("username").toString(), authorities(userDetails));
    }

    /**
     * 校验访问令牌
     *
     * @param jwtToken JWT 访问令牌
     * @return boolean 返回 true 表示有效
     */
    public boolean validateAccessToken(String jwtToken) {
        return familyJwtService.validateAccessToken(jwtToken);
    }

    /**
     * 校验刷新令牌
     *
     * @param jwtToken JWT 刷新令牌
     * @return boolean 返回 true 表示有效
     */
    public boolean validateRefreshToken(String jwtToken) {
        return familyJwtService.validateRefreshToken(jwtToken);
    }

    /**
     * 解析访问令牌
     *
     * @param jwtToken JWT 访问令牌
     * @return Optional<Claims> 返回访问令牌 Claims
     */
    public Optional<Claims> parseAccessClaims(String jwtToken) {
        return familyJwtService.parseAccessClaims(jwtToken);
    }

    /**
     * 从 Authorization 头解析 JWT 令牌
     *
     * @param authorization Authorization 请求头
     * @return String 返回 JWT 令牌
     */
    public String resolveAuthorizationToken(String authorization) {
        return familyJwtService.resolveAuthorizationToken(authorization);
    }

    /**
     * 获取 Authorization Header 名称
     *
     * @return String 返回 Header 名称
     */
    public String authorizationHeader() {
        return familyJwtService.authorizationHeader();
    }

    /**
     * 转换权限列表
     *
     * @param userDetails 用户信息
     * @return Collection<String> 返回权限列表
     */
    private Collection<String> authorities(Map<String, Object> userDetails) {
        Object value = userDetails.get("authorities");
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
