/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @FileName: OAuthClientController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-16:45
 * @Description: OAuth Client Web 控制器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.uaa.application.manage.OAuthClientManage;
import top.egon.familyaibutler.uaa.facade.dto.oauthclient.CreateOAuthClientRequest;
import top.egon.familyaibutler.uaa.facade.dto.oauthclient.OAuthClientResponse;
import top.egon.openapi.console.annotation.DocBody;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocModel;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocParam;
import top.egon.openapi.console.annotation.DocParamIn;
import top.egon.openapi.console.annotation.DocParameter;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;
import top.egon.openapi.console.annotation.DocTypeReference;
import top.egon.openapi.console.annotation.DocWrapper;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @ClassName: OAuthClientController
 * @Author: atluofu
 * @CreateTime: 2026-05-20 16:45
 * @Description: OAuth Client Web 控制器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/oauth-clients")
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-oauth-client",
        serviceName = "OAuth Client 服务", serviceDescription = "OAuth Client 创建与查询能力", protocol = DocProtocol.HTTP)
public class OAuthClientController {
    /**
     * OAuth Client 应用服务。
     */
    private final OAuthClientManage oAuthClientService;

    /**
     * 创建 OAuth Client Web 控制器。
     *
     * @param oAuthClientService OAuth Client 应用服务
     */
    public OAuthClientController(OAuthClientManage oAuthClientService) {
        this.oAuthClientService = oAuthClientService;
    }

    /**
     * 创建 OAuth Client。
     *
     * @param request 创建请求
     * @return OAuth Client 响应
     */
    @PostMapping
    @DocOperation(summary = "创建 OAuth Client", description = "创建 OAuth Client 配置",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = CreateOAuthClientRequest.class))),
            response = @DocResponse(description = "创建成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = OAuthClientResponse.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
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
    @DocOperation(summary = "按客户端 ID 查询 OAuth Client", description = "根据客户端 ID 查询 OAuth Client 配置",
            request = @DocRequest(params = {
                    @DocParameter(name = "clientId", in = DocParamIn.PATH, description = "客户端 ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.STRING), example = "family-web")
            }),
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = OAuthClientResponse.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<OAuthClientResponse> get(@PathVariable @DocParam(description = "客户端 ID", required = true) String clientId) {
        return Result.success(oAuthClientService.get(clientId));
    }

    /**
     * 查询 OAuth Client 列表。
     *
     * @return OAuth Client 列表
     */
    @GetMapping
    @DocOperation(summary = "查询 OAuth Client 列表", description = "查询全部 OAuth Client 配置",
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.GENERIC, ref = OAuthClientListDataType.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<List<OAuthClientResponse>> list() {
        return Result.success(oAuthClientService.list());
    }

    /**
     * OAuth Client 列表响应数据类型引用。
     */
    @DocModel(name = "UaaWebOAuthClientListDataType", description = "UAA Web OAuth Client 列表响应数据类型引用")
    public static final class OAuthClientListDataType extends DocTypeReference<List<OAuthClientResponse>> {
    }
}
