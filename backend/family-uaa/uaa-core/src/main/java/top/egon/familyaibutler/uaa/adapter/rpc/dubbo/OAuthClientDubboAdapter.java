/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @FileName: OAuthClientDubboAdapter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:20
 * @Description: OAuth Client facade 适配器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.rpc.dubbo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.uaa.application.manage.OAuthClientManage;
import top.egon.familyaibutler.uaa.facade.OAuthClientFacade;
import top.egon.familyaibutler.uaa.facade.dto.oauthclient.CreateOAuthClientRequest;
import top.egon.familyaibutler.uaa.facade.dto.oauthclient.OAuthClientResponse;
import top.egon.openapi.console.annotation.DocBody;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocParamIn;
import top.egon.openapi.console.annotation.DocParameter;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;
import top.egon.openapi.console.annotation.DocTypeReference;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @ClassName: OAuthClientDubboAdapter
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:20
 * @Description: OAuth Client facade 适配器
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-oauth-client-dubbo",
        serviceName = "OAuth Client Dubbo 服务", serviceDescription = "OAuth Client 创建与查询 RPC 能力", protocol = DocProtocol.DUBBO_TRIPLE)
public class OAuthClientDubboAdapter implements OAuthClientFacade {
    private final OAuthClientManage oAuthClientService;

    /**
     * 创建 OAuth Client。
     *
     * @param request 创建请求
     * @return OAuth Client 响应
     */
    @Override
    @DocOperation(summary = "创建 OAuth Client", description = "创建 OAuth Client 配置",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = CreateOAuthClientRequest.class))),
            response = @DocResponse(description = "创建成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = OAuthClientResponse.class)))
    public OAuthClientResponse create(CreateOAuthClientRequest request) {
        return oAuthClientService.create(request);
    }

    /**
     * 按客户端 ID 查询 OAuth Client。
     *
     * @param clientId 客户端 ID
     * @return OAuth Client 响应
     */
    @Override
    @DocOperation(summary = "按客户端 ID 查询 OAuth Client", description = "根据客户端 ID 查询 OAuth Client 配置",
            request = @DocRequest(params = {
                    @DocParameter(name = "clientId", in = DocParamIn.AUTO, description = "客户端 ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.STRING), example = "family-web")
            }),
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = OAuthClientResponse.class)))
    public OAuthClientResponse get(String clientId) {
        return oAuthClientService.get(clientId);
    }

    /**
     * 查询 OAuth Client 列表。
     *
     * @return OAuth Client 列表
     */
    @Override
    @DocOperation(summary = "查询 OAuth Client 列表", description = "查询全部 OAuth Client 配置",
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.GENERIC, ref = OAuthClientListDataType.class)))
    public List<OAuthClientResponse> list() {
        return oAuthClientService.list();
    }

    public static final class OAuthClientListDataType extends DocTypeReference<List<OAuthClientResponse>> {
    }
}
