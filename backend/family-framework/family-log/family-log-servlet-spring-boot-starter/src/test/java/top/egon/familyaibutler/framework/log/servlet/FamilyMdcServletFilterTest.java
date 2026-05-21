/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.servlet
 * @FileName: FamilyMdcServletFilterTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:10
 * @Description: Servlet MDC 过滤器测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.servlet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import top.egon.familyaibutler.framework.log.core.FamilyLogMdcKeys;
import top.egon.familyaibutler.framework.log.core.FamilyLogProperties;
import top.egon.familyaibutler.framework.log.core.FamilyLogUtil;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.servlet
 * @ClassName: FamilyMdcServletFilterTest
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:10
 * @Description: Servlet MDC 过滤器测试
 * @Version: 1.0
 */
class FamilyMdcServletFilterTest {

    /**
     * 清理测试线程 MDC。
     */
    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    /**
     * 校验 Servlet 过滤器会读取请求头、补齐响应头并恢复原线程上下文。
     *
     * @throws Exception 过滤器执行异常
     */
    @Test
    void shouldBindHeadersIntoMdcAndRestoreContextAfterChain() throws Exception {
        FamilyLogProperties properties = new FamilyLogProperties();
        FamilyMdcServletFilter filter = new FamilyMdcServletFilter(properties);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/password/list");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Map<String, String>> requestContext = new AtomicReference<>();
        MDC.put("customKey", "customValue");
        request.addHeader(FamilyLogMdcKeys.HEADER_TRACE_ID, "trace_servlet");
        request.addHeader(FamilyLogMdcKeys.HEADER_ACCOUNT_ID, "account_1");
        request.setRemoteAddr("127.0.0.1");

        filter.doFilter(request, response, (req, resp) -> requestContext.set(FamilyLogUtil.copyFamilyContext()));

        assertThat(requestContext.get().get(FamilyLogMdcKeys.TRACE_ID)).isEqualTo("trace_servlet");
        assertThat(requestContext.get().get(FamilyLogMdcKeys.ACCOUNT_ID)).isEqualTo("account_1");
        assertThat(requestContext.get().get(FamilyLogMdcKeys.REQUEST_METHOD)).isEqualTo("GET");
        assertThat(response.getHeader(FamilyLogMdcKeys.HEADER_TRACE_ID)).isEqualTo("trace_servlet");
        assertThat(response.getHeader(FamilyLogMdcKeys.HEADER_REQUEST_ID)).isNotBlank();
        assertThat(MDC.get("customKey")).isEqualTo("customValue");
        assertThat(MDC.get(FamilyLogMdcKeys.TRACE_ID)).isNull();
    }
}
