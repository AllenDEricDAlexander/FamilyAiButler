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
    public Result<Boolean> revokeSession(@PathVariable String sessionId) {
        return Result.success(sessionService.revokeSession(sessionId));
    }
}
