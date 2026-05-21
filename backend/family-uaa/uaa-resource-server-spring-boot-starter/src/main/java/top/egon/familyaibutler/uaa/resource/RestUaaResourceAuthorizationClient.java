/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource
 * @FileName: RestUaaResourceAuthorizationClient.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:45
 * @Description: REST UAA 资源授权客户端文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionRequest;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionResponse;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource
 * @ClassName: RestUaaResourceAuthorizationClient
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:45
 * @Description: REST UAA 资源授权客户端
 * @Version: 1.0
 */
@Slf4j
public class RestUaaResourceAuthorizationClient implements UaaResourceAuthorizationClient {
    private static final ParameterizedTypeReference<Result<AuthorizationDecisionResponse>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    /**
     * 创建 REST UAA 资源授权客户端。
     *
     * @param properties        UAA 资源服务配置
     * @param restClientBuilder REST 客户端构造器
     */
    public RestUaaResourceAuthorizationClient(UaaResourceServerProperties properties, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(properties.getAuthorizationBaseUrl()).build();
    }

    /**
     * 调用 UAA 授权决策。
     *
     * @param request 授权决策请求
     * @return 授权决策响应
     */
    @Override
    public AuthorizationDecisionResponse decide(AuthorizationDecisionRequest request) {
        try {
            Result<AuthorizationDecisionResponse> result = restClient.post()
                    .uri("/authorization/decide")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(RESPONSE_TYPE);
            if (result != null && Boolean.TRUE.equals(result.getSuccess()) && result.getData() != null) {
                return result.getData();
            }
        } catch (RestClientException exception) {
            log.warn("调用 UAA 授权决策失败: {}", exception.getMessage());
        }
        return new AuthorizationDecisionResponse(false, "UAA_DECISION_UNAVAILABLE", null, null, null, null, null);
    }
}
