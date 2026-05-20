/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.client
 * @FileName: ApiDocConsoleHttpResponse.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:45
 * @Description: OpenAPI 调试文档控制台 HTTP 响应对象文件
 * @Version: 1.0
 */
package top.egon.openapi.console.client;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.client
 * @ClassName: ApiDocConsoleHttpResponse
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:45
 * @Description: OpenAPI 调试文档控制台 HTTP 响应对象
 * @Version: 1.0
 */
@Getter
@Setter
public class ApiDocConsoleHttpResponse {

    private int status;

    private long durationMillis;

    private Map<String, String> headers = new LinkedHashMap<>();

    private String body = "";
}
