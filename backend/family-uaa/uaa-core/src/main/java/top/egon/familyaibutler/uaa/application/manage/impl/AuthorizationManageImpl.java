/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage.impl
 * @FileName: AuthorizationManageImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:20
 * @Description: 授权决策应用服务实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application.manage.impl;

import org.springframework.stereotype.Service;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtClaims;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtService;
import top.egon.familyaibutler.uaa.application.manage.AuthorizationManage;
import top.egon.familyaibutler.uaa.domain.account.gateway.AccountGateway;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Account;
import top.egon.familyaibutler.uaa.domain.auth.gateway.TokenGateway;
import top.egon.familyaibutler.uaa.domain.auth.model.valueobject.TokenClaims;
import top.egon.familyaibutler.uaa.domain.oauth.gateway.OAuthClientGateway;
import top.egon.familyaibutler.uaa.domain.oauth.model.aggregate.OAuthClient;
import top.egon.familyaibutler.uaa.domain.rbac.gateway.RbacGateway;
import top.egon.familyaibutler.uaa.domain.rbac.model.aggregate.PermissionResource;
import top.egon.familyaibutler.uaa.domain.rbac.model.enums.PermissionResourceType;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionRequest;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionResponse;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage.impl
 * @ClassName: AuthorizationManageImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:20
 * @Description: 授权决策应用服务实现
 * @Version: 1.0
 */
@Service
public class AuthorizationManageImpl implements AuthorizationManage {
    /**
     * Account 网关。
     */
    private final AccountGateway accountGateway;
    /**
     * Token 网关。
     */
    private final TokenGateway tokenGateway;
    /**
     * OAuth Client 网关。
     */
    private final OAuthClientGateway oAuthClientGateway;
    /**
     * RBAC 网关。
     */
    private final RbacGateway rbacGateway;
    /**
     * Family JWT 服务。
     */
    private final FamilyJwtService familyJwtService;

    /**
     * 创建授权决策应用服务实现。
     *
     * @param accountGateway     账号网关
     * @param tokenGateway       Token 网关
     * @param oAuthClientGateway OAuth Client 网关
     * @param rbacGateway        RBAC 网关
     * @param familyJwtService   统一 JWT 服务
     */
    public AuthorizationManageImpl(AccountGateway accountGateway, TokenGateway tokenGateway, OAuthClientGateway oAuthClientGateway,
                                   RbacGateway rbacGateway, FamilyJwtService familyJwtService) {
        this.accountGateway = accountGateway;
        this.tokenGateway = tokenGateway;
        this.oAuthClientGateway = oAuthClientGateway;
        this.rbacGateway = rbacGateway;
        this.familyJwtService = familyJwtService;
    }

    /**
     * 执行资源访问授权决策。
     *
     * @param request 授权决策请求
     * @return 授权决策响应
     */
    @Override
    public AuthorizationDecisionResponse decide(AuthorizationDecisionRequest request) {
        if (request.accessToken() == null || request.accessToken().isBlank()) {
            return deny("EMPTY_TOKEN", null, null, null);
        }
        String accessToken = familyJwtService.resolveAuthorizationToken(request.accessToken());
        FamilyJwtClaims jwtClaims = familyJwtService.parseAccessJwtClaims(accessToken).orElse(null);
        if (jwtClaims == null) {
            return deny("TOKEN_INVALID", null, null, null);
        }
        TokenClaims tokenClaims = tokenGateway.findAccessTokenClaims(accessToken).orElse(null);
        if (tokenClaims == null) {
            return deny("TOKEN_NOT_FOUND", jwtClaims, null, null);
        }
        if (tokenClaims.isExpired()) {
            return deny("TOKEN_EXPIRED", jwtClaims, tokenClaims, null);
        }
        Account account = accountGateway.findByAccountId(tokenClaims.accountId()).orElse(null);
        if (account == null) {
            return deny("ACCOUNT_NOT_FOUND", jwtClaims, tokenClaims, null);
        }
        if (!account.canLogin()) {
            return deny("ACCOUNT_DENIED", jwtClaims, tokenClaims, null);
        }
        if (account.getAuthVersion() != tokenClaims.authVersion()) {
            return deny("AUTH_VERSION_STALE", jwtClaims, tokenClaims, null);
        }
        if (account.getEntitlementVersion() != tokenClaims.entitlementVersion()) {
            return deny("ENTITLEMENT_VERSION_STALE", jwtClaims, tokenClaims, null);
        }
        if (isRiskDenied(tokenClaims.riskLevel())) {
            return deny("RISK_DENIED", jwtClaims, tokenClaims, null);
        }
        OAuthClient client = oAuthClientGateway.findByClientId(tokenClaims.clientId()).orElse(null);
        if (client == null || !client.isActive()) {
            return deny("CLIENT_DENIED", jwtClaims, tokenClaims, tokenClaims.clientId());
        }
        if (!hasResourceAccess(client, request.resourceService(), request.resourcePath())) {
            return deny("RESOURCE_DENIED", jwtClaims, tokenClaims, client.getClientId());
        }
        if (!hasApiPermission(tokenClaims.accountId(), request.resourceService(), request.resourcePath(), request.action())) {
            return deny("RBAC_DENIED", jwtClaims, tokenClaims, client.getClientId());
        }
        return new AuthorizationDecisionResponse(true, "ALLOW", tokenClaims.accountId(), tokenClaims.profileId(),
                client.getClientId(), tokenClaims.sessionId(), tokenClaims.deviceId());
    }

    /**
     * 判断账号是否拥有接口权限。
     *
     * @param accountId       账号 ID
     * @param resourceService 资源服务
     * @param resourcePath    资源路径
     * @param action          操作
     * @return true 表示允许访问
     */
    private boolean hasApiPermission(String accountId, String resourceService, String resourcePath, String action) {
        String service = resourceService == null ? "" : resourceService.trim();
        String path = normalizePath(resourcePath);
        for (PermissionResource resource : rbacGateway.findResourcesByAccountId(accountId)) {
            if (resource.getResourceType() == PermissionResourceType.API
                    && matchesService(resource.getResourceService(), service)
                    && matchesPattern(resource.getPathPattern(), path)
                    && matchesAction(resource.getAction(), action)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断风控等级是否拒绝访问。
     *
     * @param riskLevel 风控等级
     * @return true 表示拒绝
     */
    private boolean isRiskDenied(String riskLevel) {
        return "HIGH".equalsIgnoreCase(riskLevel) || "BLOCKED".equalsIgnoreCase(riskLevel);
    }

    /**
     * 判断接入方是否拥有资源访问范围。
     *
     * @param client          OAuth Client
     * @param resourceService 资源服务
     * @param resourcePath    资源路径
     * @return true 表示允许访问
     */
    private boolean hasResourceAccess(OAuthClient client, String resourceService, String resourcePath) {
        String service = resourceService == null ? "" : resourceService.trim();
        String path = normalizePath(resourcePath);
        String servicePath = service.isBlank() ? path : service + ":" + path;
        for (String pattern : client.getResourcePatterns()) {
            if (matchesPattern(pattern, path) || matchesPattern(pattern, servicePath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断服务是否匹配。
     *
     * @param patternService 配置服务
     * @param targetService  目标服务
     * @return true 表示命中
     */
    private boolean matchesService(String patternService, String targetService) {
        return patternService == null || patternService.isBlank() || "*".equals(patternService) || patternService.equals(targetService);
    }

    /**
     * 判断操作是否匹配。
     *
     * @param patternAction 配置操作
     * @param targetAction  目标操作
     * @return true 表示命中
     */
    private boolean matchesAction(String patternAction, String targetAction) {
        return patternAction == null || patternAction.isBlank() || "*".equals(patternAction)
                || patternAction.equalsIgnoreCase(targetAction == null ? "" : targetAction);
    }

    /**
     * 规范化资源路径。
     *
     * @param resourcePath 资源路径
     * @return 规范化路径
     */
    private String normalizePath(String resourcePath) {
        String path = resourcePath == null || resourcePath.isBlank() ? "/" : resourcePath.trim();
        return path.startsWith("/") ? path : "/" + path;
    }

    /**
     * 判断资源模式是否命中目标路径。
     *
     * @param pattern 资源模式
     * @param target  目标路径
     * @return true 表示命中
     */
    private boolean matchesPattern(String pattern, String target) {
        if (pattern == null || pattern.isBlank()) {
            return false;
        }
        if ("/**".equals(pattern) || "**".equals(pattern)) {
            return true;
        }
        if (pattern.endsWith("/**")) {
            return target.startsWith(pattern.substring(0, pattern.length() - 3));
        }
        return pattern.equals(target);
    }

    /**
     * 创建拒绝响应。
     *
     * @param reason      拒绝原因
     * @param jwtClaims   JWT 身份声明
     * @param tokenClaims Token 存储声明
     * @param clientId    客户端 ID
     * @return 授权决策响应
     */
    private AuthorizationDecisionResponse deny(String reason, FamilyJwtClaims jwtClaims, TokenClaims tokenClaims, String clientId) {
        String accountId = tokenClaims == null ? jwtClaims == null ? null : jwtClaims.accountId() : tokenClaims.accountId();
        String profileId = tokenClaims == null ? jwtClaims == null ? null : jwtClaims.profileId() : tokenClaims.profileId();
        String actualClientId = clientId == null ? jwtClaims == null ? null : jwtClaims.clientId() : clientId;
        String sessionId = tokenClaims == null ? jwtClaims == null ? null : jwtClaims.sessionId() : tokenClaims.sessionId();
        String deviceId = tokenClaims == null ? jwtClaims == null ? null : jwtClaims.deviceId() : tokenClaims.deviceId();
        return new AuthorizationDecisionResponse(false, reason, accountId, profileId, actualClientId, sessionId, deviceId);
    }
}
