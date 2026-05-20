/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.client
 * @FileName: ApiDocConsoleHttpRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:45
 * @Description: OpenAPI 调试文档控制台 HTTP 请求对象文件
 * @Version: 1.0
 */
package top.egon.openapi.console.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.time.Duration;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.client
 * @ClassName: ApiDocConsoleHttpRequest
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-00:45
 * @Description: OpenAPI 调试文档控制台 HTTP 请求对象
 * @Version: 1.0
 */
@Getter
@Setter
public class ApiDocConsoleHttpRequest {

    private HttpMethod method;

    private URI uri;

    private HttpHeaders headers = new HttpHeaders();

    private String body = "";

    private Duration timeout = Duration.ofSeconds(30);

    private int maxResponseSize = 3 * 1024 * 1024;

    private boolean readResponseBody = true;
}
