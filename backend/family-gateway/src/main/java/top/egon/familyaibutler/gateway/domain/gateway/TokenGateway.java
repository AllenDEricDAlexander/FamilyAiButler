/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.domain.gateway
 * @FileName: TokenGateway.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-22:21
 * @Description: 网关 Token 领域网关文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.domain.gateway;

import top.egon.familyaibutler.common.security.jwt.FamilyJwtClaims;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.domain.gateway
 * @ClassName: TokenGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 22:21
 * @Description: 网关 Token 领域网关
 * @Version: 1.0
 */
public interface TokenGateway {

    /**
     * 校验访问令牌。
     *
     * @param jwtToken JWT 访问令牌
     * @return boolean 返回 true 表示令牌有效
     */
    boolean validateAccessToken(String jwtToken);

    /**
     * 解析访问令牌身份声明。
     *
     * @param jwtToken JWT 访问令牌
     * @return Optional<FamilyJwtClaims> 返回身份声明
     */
    Optional<FamilyJwtClaims> parseAccessJwtClaims(String jwtToken);

    /**
     * 从 Authorization 请求头解析令牌。
     *
     * @param authorization Authorization 请求头
     * @return String 返回令牌
     */
    String resolveAuthorizationToken(String authorization);

    /**
     * 获取 Authorization 请求头名称。
     *
     * @return String 返回请求头名称
     */
    String authorizationHeader();
}
