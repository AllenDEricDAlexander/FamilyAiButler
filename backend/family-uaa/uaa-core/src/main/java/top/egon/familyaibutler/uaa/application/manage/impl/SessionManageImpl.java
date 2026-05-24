/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage.impl
 * @FileName: SessionManageImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: 会话应用服务实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application.manage.impl;

import org.springframework.stereotype.Service;
import top.egon.familyaibutler.uaa.application.manage.SessionManage;
import top.egon.familyaibutler.uaa.domain.auth.gateway.SessionGateway;
import top.egon.familyaibutler.uaa.domain.auth.gateway.TokenGateway;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage.impl
 * @ClassName: SessionManageImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: 会话应用服务实现
 * @Version: 1.0
 */
@Service
public class SessionManageImpl implements SessionManage {
    /**
     * Session 网关。
     */
    private final SessionGateway sessionGateway;
    /**
     * Token 网关。
     */
    private final TokenGateway tokenGateway;

    /**
     * 创建会话应用服务实现。
     *
     * @param sessionGateway 会话网关
     * @param tokenGateway   Token 网关
     */
    public SessionManageImpl(SessionGateway sessionGateway, TokenGateway tokenGateway) {
        this.sessionGateway = sessionGateway;
        this.tokenGateway = tokenGateway;
    }

    /**
     * 撤销会话。
     *
     * @param sessionId 会话 ID
     * @return true 表示撤销成功
     */
    @Override
    public boolean revokeSession(String sessionId) {
        return sessionGateway.findBySessionId(sessionId)
                .map(session -> {
                    session.revoke();
                    sessionGateway.save(session);
                    tokenGateway.revokeBySessionId(sessionId);
                    return true;
                })
                .orElse(false);
    }
}
