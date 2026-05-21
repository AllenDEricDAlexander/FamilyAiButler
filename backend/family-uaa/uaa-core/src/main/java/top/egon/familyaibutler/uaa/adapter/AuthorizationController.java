/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter
 * @FileName: AuthorizationController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:20
 * @Description: 授权决策 Web 控制器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.uaa.application.AuthorizationServiceI;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionRequest;
import top.egon.familyaibutler.uaa.facade.dto.authorization.AuthorizationDecisionResponse;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter
 * @ClassName: AuthorizationController
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:20
 * @Description: 授权决策 Web 控制器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/authorization")
public class AuthorizationController {
    private final AuthorizationServiceI authorizationService;

    /**
     * 创建授权决策 Web 控制器。
     *
     * @param authorizationService 授权决策应用服务
     */
    public AuthorizationController(AuthorizationServiceI authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * 执行资源访问授权决策。
     *
     * @param request 授权决策请求
     * @return 授权决策响应
     */
    @PostMapping("/decide")
    public Result<AuthorizationDecisionResponse> decide(@RequestBody AuthorizationDecisionRequest request) {
        return Result.success(authorizationService.decide(request));
    }
}
