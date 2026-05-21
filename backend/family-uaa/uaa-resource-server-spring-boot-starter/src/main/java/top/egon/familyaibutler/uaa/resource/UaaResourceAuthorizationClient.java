/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource
 * @FileName: UaaResourceAuthorizationClient.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:45
 * @Description: UAA 资源授权客户端接口文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.resource;

import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionRequest;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionResponse;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.resource
 * @ClassName: UaaResourceAuthorizationClient
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:45
 * @Description: UAA 资源授权客户端接口
 * @Version: 1.0
 */
public interface UaaResourceAuthorizationClient {

    /**
     * 调用 UAA 授权决策。
     *
     * @param request 授权决策请求
     * @return 授权决策响应
     */
    AuthorizationDecisionResponse decide(AuthorizationDecisionRequest request);
}
