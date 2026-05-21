/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource
 * @FileName: RestUaaResourceAuthorizationClientTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:50
 * @Description: REST UAA 资源授权客户端测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.resource;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionRequest;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource
 * @ClassName: RestUaaResourceAuthorizationClientTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:50
 * @Description: REST UAA 资源授权客户端测试
 * @Version: 1.0
 */
class RestUaaResourceAuthorizationClientTest {

    /**
     * 校验 REST 客户端调用 UAA 授权决策接口。
     */
    @Test
    void decideShouldCallUaaAuthorizationEndpoint() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        UaaResourceServerProperties properties = new UaaResourceServerProperties();
        properties.setAuthorizationBaseUrl("http://uaa");
        RestUaaResourceAuthorizationClient client = new RestUaaResourceAuthorizationClient(properties, restClientBuilder);
        server.expect(requestTo("http://uaa/authorization/decide"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"code":10000,"message":"success","success":true,"timestamp":1,
                        "data":{"allowed":true,"reason":"ALLOW","accountId":"acc_1","profileId":"prof_1",
                        "clientId":"family-web","sessionId":"sess_1","deviceId":"dev_1"}}
                        """, MediaType.APPLICATION_JSON));

        AuthorizationDecisionResponse response = client.decide(new AuthorizationDecisionRequest(
                "jwt-token", "family-core", "/password/view/list", "GET"));

        assertThat(response.allowed()).isTrue();
        assertThat(response.accountId()).isEqualTo("acc_1");
        server.verify();
    }
}
