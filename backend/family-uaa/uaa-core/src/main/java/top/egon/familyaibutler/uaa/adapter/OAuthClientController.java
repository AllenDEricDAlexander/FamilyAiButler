/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter
 * @FileName: OAuthClientController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-16:45
 * @Description: OAuth Client Web 控制器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.uaa.application.OAuthClientServiceI;
import top.egon.familyaibutler.uaa.facade.dto.oauthclient.CreateOAuthClientRequest;
import top.egon.familyaibutler.uaa.facade.dto.oauthclient.OAuthClientResponse;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter
 * @ClassName: OAuthClientController
 * @Author: atluofu
 * @CreateTime: 2026-05-20 16:45
 * @Description: OAuth Client Web 控制器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/oauth-clients")
public class OAuthClientController {
    private final OAuthClientServiceI oAuthClientService;

    /**
     * 创建 OAuth Client Web 控制器。
     *
     * @param oAuthClientService OAuth Client 应用服务
     */
    public OAuthClientController(OAuthClientServiceI oAuthClientService) {
        this.oAuthClientService = oAuthClientService;
    }

    /**
     * 创建 OAuth Client。
     *
     * @param request 创建请求
     * @return OAuth Client 响应
     */
    @PostMapping
    public Result<OAuthClientResponse> create(@RequestBody @Valid CreateOAuthClientRequest request) {
        return Result.success(oAuthClientService.create(request));
    }

    /**
     * 按客户端 ID 查询 OAuth Client。
     *
     * @param clientId 客户端 ID
     * @return OAuth Client 响应
     */
    @GetMapping("/{clientId}")
    public Result<OAuthClientResponse> get(@PathVariable String clientId) {
        return Result.success(oAuthClientService.get(clientId));
    }

    /**
     * 查询 OAuth Client 列表。
     *
     * @return OAuth Client 列表
     */
    @GetMapping
    public Result<List<OAuthClientResponse>> list() {
        return Result.success(oAuthClientService.list());
    }
}
