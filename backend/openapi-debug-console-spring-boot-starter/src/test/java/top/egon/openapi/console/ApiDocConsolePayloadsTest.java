/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @FileName: ApiDocConsolePayloadsTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-19:10
 * @Description: OpenAPI 调试文档控制台响应对象测试文件
 * @Version: 1.0
 */
package top.egon.openapi.console;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @ClassName: ApiDocConsolePayloadsTest
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-19:10
 * @Description: OpenAPI 调试文档控制台响应对象测试
 * @Version: 1.0
 */
class ApiDocConsolePayloadsTest {

    /**
     * 测试压测响应保留请求样本
     */
    @Test
    void testLoadTestResultSamples() {
        ApiDocConsolePayloads.LoadTestSample sample = new ApiDocConsolePayloads.LoadTestSample();
        sample.setIndex(1);
        sample.setStatus(200);
        sample.setDurationMillis(123);

        ApiDocConsolePayloads.LoadTestResult result = new ApiDocConsolePayloads.LoadTestResult();
        result.getSamples().add(sample);

        Assertions.assertEquals(1, result.getSamples().size());
        Assertions.assertTrue(result.getSamples().get(0).success());
    }
}
