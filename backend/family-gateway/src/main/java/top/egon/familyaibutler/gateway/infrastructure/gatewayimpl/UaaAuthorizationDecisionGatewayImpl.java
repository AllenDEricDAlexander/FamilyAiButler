/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.infrastructure.gatewayimpl
 * @FileName: UaaAuthorizationDecisionGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-22:24
 * @Description: UAA 授权决策领域网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.infrastructure.gatewayimpl;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.gateway.domain.gateway.AuthorizationDecisionGateway;
import top.egon.familyaibutler.gateway.infrastructure.configuration.FamilyButlerGateWayProperties;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionRequest;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionResponse;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.infrastructure.gatewayimpl
 * @ClassName: UaaAuthorizationDecisionGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 22:24
 * @Description: UAA 授权决策领域网关实现
 * @Version: 1.0
 */
@Component
public class UaaAuthorizationDecisionGatewayImpl implements AuthorizationDecisionGateway {
    private final WebClient webClient;

    /**
     * 创建 UAA 授权决策领域网关实现。
     *
     * @param properties       网关配置
     * @param webClientBuilder WebClient 构建器
     */
    public UaaAuthorizationDecisionGatewayImpl(FamilyButlerGateWayProperties properties, WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(properties.getUaa().getBaseUrl()).build();
    }

    /**
     * 请求 UAA 执行授权决策。
     *
     * @param request 授权决策请求
     * @return Mono<AuthorizationDecisionResponse> 返回授权决策响应
     */
    @Override
    public Mono<AuthorizationDecisionResponse> decide(AuthorizationDecisionRequest request) {
        return webClient.post()
                .uri("/authorization/decide")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<AuthorizationDecisionResponse>>() {
                })
                .map(Result::getData);
    }
}
