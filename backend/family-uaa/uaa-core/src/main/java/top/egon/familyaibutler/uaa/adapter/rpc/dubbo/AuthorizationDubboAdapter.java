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
public class AuthorizationDubboAdapter implements AuthorizationFacade {
    private final AuthorizationManage authorizationService;

    /**
     * 执行资源访问授权决策。
     *
     * @param request 授权决策请求
     * @return 授权决策响应
     */
    @Override
    public AuthorizationDecisionResponse decide(AuthorizationDecisionRequest request) {
        return authorizationService.decide(request);
    }
}
