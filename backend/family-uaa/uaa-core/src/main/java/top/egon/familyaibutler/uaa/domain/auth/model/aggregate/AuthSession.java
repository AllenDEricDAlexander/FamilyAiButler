/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.auth.model.aggregate
 * @FileName: AuthSession.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 认证会话聚合文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.auth.model.aggregate;

import top.egon.familyaibutler.uaa.domain.auth.model.enums.SessionStatus;

import java.util.UUID;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.auth.model.aggregate
 * @ClassName: AuthSession
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 认证会话聚合
 * @Version: 1.0
 */
public class AuthSession {
    private final String sessionId;
    private final String accountId;
    private final String profileId;
    private final String deviceId;
    private final String clientId;
    private SessionStatus status;

    private AuthSession(String sessionId, String accountId, String profileId, String deviceId, String clientId) {
        this.sessionId = sessionId;
        this.accountId = accountId;
        this.profileId = profileId;
        this.deviceId = deviceId;
        this.clientId = clientId;
        this.status = SessionStatus.ACTIVE;
    }

    /**
     * 创建认证会话。
     *
     * @param accountId 账号 ID
     * @param profileId Profile ID
     * @param deviceId  设备 ID
     * @param clientId  客户端 ID
     * @return 认证会话
     */
    public static AuthSession create(String accountId, String profileId, String deviceId, String clientId) {
        return new AuthSession("sess_" + UUID.randomUUID(), accountId, profileId, deviceId, clientId);
    }

    /**
     * 还原认证会话。
     *
     * @param sessionId 会话 ID
     * @param accountId 账号 ID
     * @param profileId Profile ID
     * @param deviceId  设备 ID
     * @param clientId  客户端 ID
     * @param status    会话状态
     * @return 认证会话
     */
    public static AuthSession restore(String sessionId, String accountId, String profileId, String deviceId,
                                      String clientId, SessionStatus status) {
        AuthSession session = new AuthSession(sessionId, accountId, profileId, deviceId, clientId);
        session.status = status;
        return session;
    }

    /**
     * 撤销会话。
     */
    public void revoke() {
        this.status = SessionStatus.REVOKED;
    }

    /**
     * 获取会话 ID。
     *
     * @return 会话 ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 获取账号 ID。
     *
     * @return 账号 ID
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * 获取 Profile ID。
     *
     * @return Profile ID
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * 获取设备 ID。
     *
     * @return 设备 ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 获取客户端 ID。
     *
     * @return 客户端 ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * 获取会话状态。
     *
     * @return 会话状态
     */
    public SessionStatus getStatus() {
        return status;
    }
}
