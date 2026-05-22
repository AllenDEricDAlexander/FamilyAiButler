/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.openapi
 * @FileName: ApiDocOpenApiController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:45
 * @Description: OpenAPI JSON 控制器文件
 * @Version: 1.0
 */
package top.egon.openapi.console.openapi;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.openapi
 * @ClassName: ApiDocOpenApiController
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:45
 * @Description: OpenAPI JSON 控制器
 * @Version: 1.0
 */
@RestController
@RequiredArgsConstructor
public class ApiDocOpenApiController {

    private final ApiDocSpringMvcOpenApiGenerator openApiGenerator;

    /**
     * 获取 REST OpenAPI JSON
     *
     * @return Map<String, Object> 返回 REST OpenAPI JSON
     */
    @GetMapping("/v3/api-docs")
    public Map<String, Object> apiDocs() {
        return openApiGenerator.generate();
    }
}
