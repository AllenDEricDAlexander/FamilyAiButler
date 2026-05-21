/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.auth.service
 * @FileName: TokenDomainService.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: Token 领域服务文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.auth.service;

import top.egon.familyaibutler.uaa.domain.auth.model.aggregate.AuthSession;
import top.egon.familyaibutler.uaa.domain.auth.model.entity.Device;
import top.egon.familyaibutler.uaa.domain.auth.model.entity.TokenRecord;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.auth.service
 * @ClassName: TokenDomainService
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Token 领域服务
 * @Version: 1.0
 */
public class TokenDomainService {

    /**
     * 创建刷新令牌记录。
     *
     * @param session      认证会话
     * @param device       登录设备
     * @param refreshToken 刷新令牌明文
     * @return Token 记录
     */
    public TokenRecord createRefreshTokenRecord(AuthSession session, Device device, String refreshToken) {
        return new TokenRecord(session.getAccountId(), session.getSessionId(), device.getDeviceId(), session.getClientId(), hashToken(refreshToken));
    }

    /**
     * 计算 Token 哈希。
     *
     * @param token Token 明文
     * @return Token 哈希
     */
    public String hashToken(String token) {
        return sha256(token);
    }

    /**
     * 计算 SHA-256 哈希。
     *
     * @param value 明文
     * @return 哈希值
     */
    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", exception);
        }
    }
}
