/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.model.entity
 * @FileName: TokenRecord.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: Token 记录实体文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.model.entity;

import top.egon.familyaibutler.uaa.domain.model.enums.TokenStatus;

import java.util.UUID;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.model.entity
 * @ClassName: TokenRecord
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Token 记录实体
 * @Version: 1.0
 */
public class TokenRecord {
    private final String tokenId;
    private final String accountId;
    private final String sessionId;
    private final String deviceId;
    private final String clientId;
    private final String tokenHash;
    private TokenStatus status;

    /**
     * 创建 Token 记录。
     *
     * @param accountId 账号 ID
     * @param sessionId 会话 ID
     * @param deviceId  设备 ID
     * @param clientId  客户端 ID
     * @param tokenHash Token 哈希
     */
    public TokenRecord(String accountId, String sessionId, String deviceId, String clientId, String tokenHash) {
        this.tokenId = "tok_" + UUID.randomUUID();
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.deviceId = deviceId;
        this.clientId = clientId;
        this.tokenHash = tokenHash;
        this.status = TokenStatus.ACTIVE;
    }

    /**
     * 还原 Token 记录。
     *
     * @param tokenId   Token ID
     * @param accountId 账号 ID
     * @param sessionId 会话 ID
     * @param deviceId  设备 ID
     * @param clientId  客户端 ID
     * @param tokenHash Token 哈希
     * @param status    Token 状态
     * @return Token 记录
     */
    public static TokenRecord restore(String tokenId, String accountId, String sessionId, String deviceId,
                                      String clientId, String tokenHash, TokenStatus status) {
        return new TokenRecord(tokenId, accountId, sessionId, deviceId, clientId, tokenHash, status);
    }

    private TokenRecord(String tokenId, String accountId, String sessionId, String deviceId, String clientId,
                        String tokenHash, TokenStatus status) {
        this.tokenId = tokenId;
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.deviceId = deviceId;
        this.clientId = clientId;
        this.tokenHash = tokenHash;
        this.status = status;
    }

    /**
     * 撤销 Token。
     */
    public void revoke() {
        this.status = TokenStatus.REVOKED;
    }

    /**
     * 判断 Token 是否可用。
     *
     * @return true 表示 Token 可用
     */
    public boolean isActive() {
        return status == TokenStatus.ACTIVE;
    }

    /**
     * 获取 Token ID。
     *
     * @return Token ID
     */
    public String getTokenId() {
        return tokenId;
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
     * 获取会话 ID。
     *
     * @return 会话 ID
     */
    public String getSessionId() {
        return sessionId;
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
     * 获取 Token 哈希。
     *
     * @return Token 哈希
     */
    public String getTokenHash() {
        return tokenHash;
    }

    /**
     * 获取 Token 状态。
     *
     * @return Token 状态
     */
    public TokenStatus getStatus() {
        return status;
    }
}
