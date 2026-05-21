/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.infrastructure.security
 * @FileName: FamilyJwtTokenGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-22:25
 * @Description: Family JWT Token 领域网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtClaims;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtService;
import top.egon.familyaibutler.gateway.domain.gateway.TokenGateway;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.infrastructure.security
 * @ClassName: FamilyJwtTokenGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 22:25
 * @Description: Family JWT Token 领域网关实现
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
public class FamilyJwtTokenGatewayImpl implements TokenGateway {

    private final FamilyJwtService familyJwtService;

    /**
     * 校验访问令牌。
     *
     * @param jwtToken JWT 访问令牌
     * @return boolean 返回 true 表示令牌有效
     */
    @Override
    public boolean validateAccessToken(String jwtToken) {
        return familyJwtService.validateAccessToken(jwtToken);
    }

    /**
     * 解析访问令牌身份声明。
     *
     * @param jwtToken JWT 访问令牌
     * @return Optional<FamilyJwtClaims> 返回身份声明
     */
    @Override
    public Optional<FamilyJwtClaims> parseAccessJwtClaims(String jwtToken) {
        return familyJwtService.parseAccessJwtClaims(jwtToken);
    }

    /**
     * 从 Authorization 请求头解析令牌。
     *
     * @param authorization Authorization 请求头
     * @return String 返回令牌
     */
    @Override
    public String resolveAuthorizationToken(String authorization) {
        return familyJwtService.resolveAuthorizationToken(authorization);
    }

    /**
     * 获取 Authorization 请求头名称。
     *
     * @return String 返回请求头名称
     */
    @Override
    public String authorizationHeader() {
        return familyJwtService.authorizationHeader();
    }
}
