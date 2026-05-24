/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @FileName: SessionController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 会话 Web 控制器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.uaa.application.manage.SessionManage;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocParam;
import top.egon.openapi.console.annotation.DocParamIn;
import top.egon.openapi.console.annotation.DocParameter;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;
import top.egon.openapi.console.annotation.DocWrapper;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @ClassName: SessionController
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 会话 Web 控制器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/session")
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-session",
        serviceName = "会话服务", serviceDescription = "登录会话管理能力", protocol = DocProtocol.HTTP)
public class SessionController {
    private final SessionManage sessionService;

    /**
     * 创建会话 Web 控制器。
     *
     * @param sessionService 会话应用服务
     */
    public SessionController(SessionManage sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * 撤销会话。
     *
     * @param sessionId 会话 ID
     * @return true 表示撤销成功
     */
    @DeleteMapping("/{sessionId}")
    @DocOperation(summary = "撤销会话", description = "按会话 ID 撤销登录会话",
            request = @DocRequest(params = {
                    @DocParameter(name = "sessionId", in = DocParamIn.PATH, description = "会话 ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.STRING), example = "session-001")
            }),
            response = @DocResponse(description = "撤销成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<Boolean> revokeSession(@PathVariable @DocParam(description = "会话 ID", required = true) String sessionId) {
        return Result.success(sessionService.revokeSession(sessionId));
    }
}
