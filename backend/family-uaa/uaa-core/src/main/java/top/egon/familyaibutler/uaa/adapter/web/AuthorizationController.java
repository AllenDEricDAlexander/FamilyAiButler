/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @FileName: AuthorizationController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:20
 * @Description: 授权决策 Web 控制器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.uaa.application.manage.AuthorizationManage;
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
import top.egon.openapi.console.annotation.DocWrapper;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @ClassName: AuthorizationController
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:20
 * @Description: 授权决策 Web 控制器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/authorization")
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-authorization",
        serviceName = "授权决策服务", serviceDescription = "资源访问授权决策能力", protocol = DocProtocol.HTTP)
public class AuthorizationController {
    /**
     * Authorization 应用服务。
     */
    private final AuthorizationManage authorizationService;

    /**
     * 创建授权决策 Web 控制器。
     *
     * @param authorizationService 授权决策应用服务
     */
    public AuthorizationController(AuthorizationManage authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * 执行资源访问授权决策。
     *
     * @param request 授权决策请求
     * @return 授权决策响应
     */
    @PostMapping("/decide")
    @DocOperation(summary = "执行资源访问授权决策", description = "根据访问令牌和资源信息判断是否允许访问",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = AuthorizationDecisionRequest.class))),
            response = @DocResponse(description = "返回授权决策",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = AuthorizationDecisionResponse.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<AuthorizationDecisionResponse> decide(@RequestBody AuthorizationDecisionRequest request) {
        return Result.success(authorizationService.decide(request));
    }
}
