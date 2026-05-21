/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource
 * @FileName: UaaResourceAuthorizationFilterTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:50
 * @Description: UAA 资源授权过滤器测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.resource;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionRequest;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionResponse;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource
 * @ClassName: UaaResourceAuthorizationFilterTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:50
 * @Description: UAA 资源授权过滤器测试
 * @Version: 1.0
 */
class UaaResourceAuthorizationFilterTest {

    /**
     * 校验过滤器会调用 UAA 授权决策并在允许后放行。
     *
     * @throws Exception 过滤异常
     */
    @Test
    void shouldCallUaaDecisionAndContinueWhenAllowed() throws Exception {
        UaaResourceServerProperties properties = new UaaResourceServerProperties();
        properties.setServiceName("family-core");
        AtomicReference<AuthorizationDecisionRequest> decisionRequest = new AtomicReference<>();
        UaaResourceAuthorizationFilter filter = new UaaResourceAuthorizationFilter(properties, request -> {
            decisionRequest.set(request);
            return new AuthorizationDecisionResponse(true, "ALLOW", "acc_1", "prof_1",
                    "family-web", "sess_1", "dev_1");
        }, new MockEnvironment());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/password/view/list");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean continued = new AtomicBoolean(false);

        filter.doFilter(request, response, (ServletRequest servletRequest, ServletResponse servletResponse) -> continued.set(true));

        assertThat(continued).isTrue();
        assertThat(decisionRequest.get().accessToken()).isEqualTo("Bearer jwt-token");
        assertThat(decisionRequest.get().resourceService()).isEqualTo("family-core");
        assertThat(decisionRequest.get().resourcePath()).isEqualTo("/password/view/list");
        assertThat(decisionRequest.get().action()).isEqualTo("GET");
    }

    /**
     * 校验过滤器在 UAA 授权拒绝后返回 403。
     *
     * @throws Exception 过滤异常
     */
    @Test
    void shouldRejectWhenUaaDecisionDenied() throws Exception {
        UaaResourceServerProperties properties = new UaaResourceServerProperties();
        properties.setServiceName("family-core");
        UaaResourceAuthorizationFilter filter = new UaaResourceAuthorizationFilter(properties, request ->
                new AuthorizationDecisionResponse(false, "RBAC_DENIED", null, null, null, null, null), new MockEnvironment());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/password/view/list");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer jwt-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean continued = new AtomicBoolean(false);

        filter.doFilter(request, response, (ServletRequest servletRequest, ServletResponse servletResponse) -> continued.set(true));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(continued).isFalse();
    }

    /**
     * 校验放行路径不调用 UAA。
     *
     * @throws Exception 过滤异常
     */
    @Test
    void shouldSkipPermitPatterns() throws Exception {
        UaaResourceServerProperties properties = new UaaResourceServerProperties();
        properties.setPermitPatterns(Set.of("/actuator/**"));
        UaaResourceAuthorizationFilter filter = new UaaResourceAuthorizationFilter(properties, request -> {
            throw new IllegalStateException("不应调用 UAA");
        }, new MockEnvironment());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean continued = new AtomicBoolean(false);

        filter.doFilter(request, response, (ServletRequest servletRequest, ServletResponse servletResponse) -> continued.set(true));

        assertThat(continued).isTrue();
    }
}
