/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.http
 * @FileName: FamilyMdcRestClientCustomizerTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:10
 * @Description: RestClient MDC 自定义器测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import top.egon.familyaibutler.framework.log.core.FamilyLogMdcKeys;
import top.egon.familyaibutler.framework.log.core.FamilyLogProperties;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.http
 * @ClassName: FamilyMdcRestClientCustomizerTest
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:10
 * @Description: RestClient MDC 自定义器测试
 * @Version: 1.0
 */
class FamilyMdcRestClientCustomizerTest {

    /**
     * 清理测试线程 MDC。
     */
    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    /**
     * 校验 RestClient 出站请求会透传 trace 与 requestId 请求头。
     */
    @Test
    void shouldPropagateHeadersForRestClient() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        new FamilyMdcRestClientCustomizer(new FamilyLogProperties()).customize(builder);
        MDC.put(FamilyLogMdcKeys.TRACE_ID, "trace_rest");
        MDC.put(FamilyLogMdcKeys.REQUEST_ID, "request_rest");
        RestClient restClient = builder.baseUrl("http://uaa").build();
        server.expect(requestTo("http://uaa/authorization/decide"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(FamilyLogMdcKeys.HEADER_TRACE_ID, "trace_rest"))
                .andExpect(header(FamilyLogMdcKeys.HEADER_REQUEST_ID, "request_rest"))
                .andExpect(headerDoesNotExist(FamilyLogMdcKeys.HEADER_ACCOUNT_ID))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        restClient.get().uri("/authorization/decide").retrieve().toBodilessEntity();

        server.verify();
    }
}
