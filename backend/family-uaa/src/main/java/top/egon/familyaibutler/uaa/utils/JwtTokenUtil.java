package top.egon.familyaibutler.uaa.utils;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtService;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common
 * @ClassName: JwtTokenUtil
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-13Day-10:53
 * @Description: JWT 工具类 todo 替换
 * @Version: 1.0
 */
@Component
public class JwtTokenUtil {

    private final FamilyJwtService familyJwtService;

    /**
     * 创建 JWT 工具类
     *
     * @param familyJwtService 统一 JWT 服务
     */
    public JwtTokenUtil(FamilyJwtService familyJwtService) {
        this.familyJwtService = familyJwtService;
    }

    /**
     * 生成 JWT 访问令牌
     *
     * @param username    用户名
     * @param authorities 权限列表
     * @return String 返回访问令牌
     */
    public String generateToken(String username, Collection<? extends GrantedAuthority> authorities) {
        return familyJwtService.createAccessToken(username, authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
    }

    /**
     * 解析 JWT 访问令牌
     *
     * @param token JWT 访问令牌
     * @return Claims 返回 JWT Claims
     */
    public Claims parseToken(String token) {
        return familyJwtService.parseAccessClaims(token).orElseThrow();
    }

    /**
     * 验证访问令牌是否有效
     *
     * @param token JWT 访问令牌
     * @return boolean 返回 true 表示有效
     */
    public boolean validateToken(String token) {
        return familyJwtService.validateAccessToken(token);
    }

    /**
     * 获取 Authorization Header 名称
     *
     * @return String 返回 Header 名称
     */
    public String authorizationHeader() {
        return familyJwtService.authorizationHeader();
    }
}
