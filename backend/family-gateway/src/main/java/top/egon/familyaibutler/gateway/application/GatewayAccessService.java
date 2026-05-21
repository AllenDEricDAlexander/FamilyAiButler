/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.application
 * @FileName: GatewayAccessService.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-22:23
 * @Description: 网关访问授权应用服务文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.application;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtClaims;
import top.egon.familyaibutler.gateway.application.dto.GatewayAccessDecision;
import top.egon.familyaibutler.gateway.domain.gateway.AuthorizationDecisionGateway;
import top.egon.familyaibutler.gateway.domain.gateway.TokenGateway;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionRequest;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.application
 * @ClassName: GatewayAccessService
 * @Author: atluofu
 * @CreateTime: 2026-05-20 22:23
 * @Description: 网关访问授权应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class GatewayAccessService {

    private final TokenGateway tokenGateway;
    private final AuthorizationDecisionGateway authorizationDecisionGateway;

    /**
     * 获取认证请求头名称。
     *
     * @return String 返回认证请求头名称
     */
    public String authorizationHeader() {
        return tokenGateway.authorizationHeader();
    }

    /**
     * 执行网关访问授权决策。
     *
     * @param authorization Authorization 请求头
     * @param path          请求路径
     * @param action        请求动作
     * @return Mono<GatewayAccessDecision> 返回网关访问决策
     */
    public Mono<GatewayAccessDecision> authorize(String authorization, String path, String action) {
        String token = tokenGateway.resolveAuthorizationToken(authorization);
        if (StringUtils.isBlank(token)) {
            return Mono.just(GatewayAccessDecision.unauthenticated("请先认证"));
        }
        if (!tokenGateway.validateAccessToken(token)) {
            return Mono.just(GatewayAccessDecision.unauthenticated("token已过期"));
        }
        return tokenGateway.parseAccessJwtClaims(token)
                .map(claims -> decideByUaa(authorization, path, action, claims))
                .orElseGet(() -> Mono.just(GatewayAccessDecision.unauthenticated("token解析失败")));
    }

    /**
     * 请求 UAA 执行资源访问决策。
     *
     * @param authorization Authorization 请求头
     * @param path          请求路径
     * @param action        请求动作
     * @param claims        JWT 身份声明
     * @return Mono<GatewayAccessDecision> 返回网关访问决策
     */
    private Mono<GatewayAccessDecision> decideByUaa(String authorization, String path, String action,
                                                    FamilyJwtClaims claims) {
        AuthorizationDecisionRequest request = new AuthorizationDecisionRequest(authorization,
                resolveResourceService(path), resolveResourcePath(path), action);
        return authorizationDecisionGateway.decide(request)
                .map(decision -> {
                    if (decision == null || !decision.allowed()) {
                        String reason = decision == null ? "授权服务未返回决策" : decision.reason();
                        return GatewayAccessDecision.forbidden(reason, claims);
                    }
                    return GatewayAccessDecision.allowed(claims);
                })
                .onErrorReturn(GatewayAccessDecision.forbidden("授权服务不可用", claims));
    }

    /**
     * 解析资源服务名称。
     *
     * @param path 请求路径
     * @return 资源服务名称
     */
    private String resolveResourceService(String path) {
        String firstSegment = firstSegment(path);
        return switch (firstSegment) {
            case "base" -> "family-core";
            case "ai" -> "family-ai-qwen";
            case "uaa" -> "family-uaa";
            default -> firstSegment;
        };
    }

    /**
     * 解析资源路径。
     *
     * @param path 请求路径
     * @return 资源路径
     */
    private String resolveResourcePath(String path) {
        if (StringUtils.isBlank(path) || "/".equals(path)) {
            return "/";
        }
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        int nextSlashIndex = normalizedPath.indexOf('/', 1);
        return nextSlashIndex < 0 ? "/" : normalizedPath.substring(nextSlashIndex);
    }

    /**
     * 解析路径首段。
     *
     * @param path 请求路径
     * @return 路径首段
     */
    private String firstSegment(String path) {
        if (StringUtils.isBlank(path) || "/".equals(path)) {
            return "";
        }
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        int slashIndex = normalizedPath.indexOf('/');
        return slashIndex < 0 ? normalizedPath : normalizedPath.substring(0, slashIndex);
    }
}
