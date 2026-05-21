/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @FileName: AuthorizationServiceI.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:20
 * @Description: 授权决策应用服务接口文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application;

import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionRequest;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionResponse;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @ClassName: AuthorizationServiceI
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:20
 * @Description: 授权决策应用服务接口
 * @Version: 1.0
 */
public interface AuthorizationServiceI {

    /**
     * 执行资源访问授权决策。
     *
     * @param request 授权决策请求
     * @return 授权决策响应
     */
    AuthorizationDecisionResponse decide(AuthorizationDecisionRequest request);
}
