/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.application.dto
 * @FileName: GatewayAccessDecision.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-22:22
 * @Description: 网关访问决策响应文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.application.dto;

import top.egon.familyaibutler.common.security.jwt.FamilyJwtClaims;

/**
 * @param authenticated 是否已认证
 * @param allowed       是否允许访问
 * @param reason        决策原因
 * @param claims        JWT 身份声明
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.application.dto
 * @ClassName: GatewayAccessDecision
 * @Author: atluofu
 * @CreateTime: 2026-05-20 22:22
 * @Description: 网关访问决策响应
 * @Version: 1.0
 */
public record GatewayAccessDecision(boolean authenticated, boolean allowed, String reason, FamilyJwtClaims claims) {

    /**
     * 创建未认证决策。
     *
     * @param reason 决策原因
     * @return GatewayAccessDecision 返回未认证决策
     */
    public static GatewayAccessDecision unauthenticated(String reason) {
        return new GatewayAccessDecision(false, false, reason, null);
    }

    /**
     * 创建拒绝访问决策。
     *
     * @param reason 决策原因
     * @param claims JWT 身份声明
     * @return GatewayAccessDecision 返回拒绝访问决策
     */
    public static GatewayAccessDecision forbidden(String reason, FamilyJwtClaims claims) {
        return new GatewayAccessDecision(true, false, reason, claims);
    }

    /**
     * 创建允许访问决策。
     *
     * @param claims JWT 身份声明
     * @return GatewayAccessDecision 返回允许访问决策
     */
    public static GatewayAccessDecision allowed(FamilyJwtClaims claims) {
        return new GatewayAccessDecision(true, true, "ALLOW", claims);
    }
}
