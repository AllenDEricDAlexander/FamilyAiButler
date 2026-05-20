/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.client
 * @FileName: ApiDocConsoleHttpClient.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:45
 * @Description: OpenAPI 调试文档控制台 HTTP 客户端接口文件
 * @Version: 1.0
 */
package top.egon.openapi.console.client;

import reactor.core.publisher.Mono;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.client
 * @ClassName: ApiDocConsoleHttpClient
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:45
 * @Description: OpenAPI 调试文档控制台 HTTP 客户端接口
 * @Version: 1.0
 */
public interface ApiDocConsoleHttpClient extends AutoCloseable {

    /**
     * 执行代理 HTTP 请求
     *
     * @param request HTTP 请求
     * @return Mono<ApiDocConsoleHttpResponse> 返回 HTTP 响应
     */
    Mono<ApiDocConsoleHttpResponse> execute(ApiDocConsoleHttpRequest request);

    /**
     * 关闭 HTTP 客户端
     */
    @Override
    default void close() {
    }
}
