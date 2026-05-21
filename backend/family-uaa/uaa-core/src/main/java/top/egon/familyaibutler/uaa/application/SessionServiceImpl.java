/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @FileName: SessionServiceImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: 会话应用服务实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application;

import org.springframework.stereotype.Service;
import top.egon.familyaibutler.uaa.domain.gateway.SessionGateway;
import top.egon.familyaibutler.uaa.domain.gateway.TokenGateway;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @ClassName: SessionServiceImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: 会话应用服务实现
 * @Version: 1.0
 */
@Service
public class SessionServiceImpl implements SessionServiceI {
    private final SessionGateway sessionGateway;
    private final TokenGateway tokenGateway;

    /**
     * 创建会话应用服务实现。
     *
     * @param sessionGateway 会话网关
     * @param tokenGateway   Token 网关
     */
    public SessionServiceImpl(SessionGateway sessionGateway, TokenGateway tokenGateway) {
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
