/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.domain.gateway
 * @FileName: AuthorizationDecisionGateway.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-22:20
 * @Description: 网关授权决策领域网关文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.domain.gateway;

import reactor.core.publisher.Mono;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionRequest;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionResponse;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.domain.gateway
 * @ClassName: AuthorizationDecisionGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 22:20
 * @Description: 网关授权决策领域网关
 * @Version: 1.0
 */
public interface AuthorizationDecisionGateway {

    /**
     * 请求统一账号认证服务执行授权决策。
     *
     * @param request 授权决策请求
     * @return Mono<AuthorizationDecisionResponse> 返回授权决策响应
     */
    Mono<AuthorizationDecisionResponse> decide(AuthorizationDecisionRequest request);
}
