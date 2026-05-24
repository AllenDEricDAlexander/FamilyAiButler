/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @FileName: AuthorizationDubboAdapter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:20
 * @Description: 授权决策 facade 适配器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.rpc.dubbo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.uaa.application.manage.AuthorizationManage;
import top.egon.familyaibutler.uaa.facade.AuthorizationFacade;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionRequest;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionResponse;
import top.egon.openapi.console.annotation.DocBody;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @ClassName: AuthorizationDubboAdapter
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:20
 * @Description: 授权决策 facade 适配器
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-authorization-dubbo",
        serviceName = "授权决策 Dubbo 服务", serviceDescription = "资源访问授权决策 RPC 能力", protocol = DocProtocol.DUBBO_TRIPLE)
public class AuthorizationDubboAdapter implements AuthorizationFacade {
    private final AuthorizationManage authorizationService;

    /**
     * 执行资源访问授权决策。
     *
     * @param request 授权决策请求
     * @return 授权决策响应
     */
    @Override
    @DocOperation(summary = "执行资源访问授权决策", description = "根据访问令牌和资源信息判断是否允许访问",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = AuthorizationDecisionRequest.class))),
            response = @DocResponse(description = "返回授权决策",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = AuthorizationDecisionResponse.class)))
    public AuthorizationDecisionResponse decide(AuthorizationDecisionRequest request) {
        return authorizationService.decide(request);
    }
}
