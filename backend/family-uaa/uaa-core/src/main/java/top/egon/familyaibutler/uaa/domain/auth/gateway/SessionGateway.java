/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.auth.gateway
 * @FileName: SessionGateway.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: 会话领域网关文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.auth.gateway;

import top.egon.familyaibutler.uaa.domain.auth.model.aggregate.AuthSession;

import java.util.List;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.auth.gateway
 * @ClassName: SessionGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: 会话领域网关
 * @Version: 1.0
 */
public interface SessionGateway {

    /**
     * 保存会话。
     *
     * @param session 认证会话
     * @return 保存后的认证会话
     */
    AuthSession save(AuthSession session);

    /**
     * 按会话 ID 查询会话。
     *
     * @param sessionId 会话 ID
     * @return 认证会话
     */
    Optional<AuthSession> findBySessionId(String sessionId);

    /**
     * 按账号查询会话列表。
     *
     * @param accountId 账号 ID
     * @return 会话列表
     */
    List<AuthSession> findByAccountId(String accountId);

    /**
     * 按设备查询会话列表。
     *
     * @param deviceId 设备 ID
     * @return 会话列表
     */
    List<AuthSession> findByDeviceId(String deviceId);
}
