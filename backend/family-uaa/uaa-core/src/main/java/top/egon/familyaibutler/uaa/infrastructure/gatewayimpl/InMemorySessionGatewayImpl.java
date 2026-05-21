/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @FileName: InMemorySessionGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: 内存会话网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gatewayimpl;

import top.egon.familyaibutler.uaa.domain.gateway.SessionGateway;
import top.egon.familyaibutler.uaa.domain.model.aggregate.AuthSession;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @ClassName: InMemorySessionGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: 内存会话网关实现
 * @Version: 1.0
 */
public class InMemorySessionGatewayImpl implements SessionGateway {
    private final Map<String, AuthSession> sessions = new ConcurrentHashMap<>();

    /**
     * 保存会话。
     *
     * @param session 认证会话
     * @return 保存后的认证会话
     */
    @Override
    public AuthSession save(AuthSession session) {
        sessions.put(session.getSessionId(), session);
        return session;
    }

    /**
     * 按会话 ID 查询会话。
     *
     * @param sessionId 会话 ID
     * @return 认证会话
     */
    @Override
    public Optional<AuthSession> findBySessionId(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    /**
     * 按账号查询会话列表。
     *
     * @param accountId 账号 ID
     * @return 会话列表
     */
    @Override
    public List<AuthSession> findByAccountId(String accountId) {
        return sessions.values().stream()
                .filter(session -> session.getAccountId().equals(accountId))
                .toList();
    }

    /**
     * 按设备查询会话列表。
     *
     * @param deviceId 设备 ID
     * @return 会话列表
     */
    @Override
    public List<AuthSession> findByDeviceId(String deviceId) {
        return sessions.values().stream()
                .filter(session -> session.getDeviceId().equals(deviceId))
                .toList();
    }
}
