/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @FileName: InMemorySecurityNotificationGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:00
 * @Description: 内存安全通知网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gateway.impl;

import top.egon.familyaibutler.uaa.domain.auth.gateway.SecurityNotificationGateway;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @ClassName: InMemorySecurityNotificationGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:00
 * @Description: 内存安全通知网关实现
 * @Version: 1.0
 */
public class InMemorySecurityNotificationGatewayImpl implements SecurityNotificationGateway {
    private final Map<String, String> recoveryCodes = new ConcurrentHashMap<>();

    /**
     * 发送找回密码验证码。
     *
     * @param accountId 账号 ID
     * @param principal 找回主体
     * @return 挑战 ID
     */
    @Override
    public String sendPasswordRecoveryCode(String accountId, String principal) {
        String challengeId = "pwd_recovery_" + UUID.randomUUID();
        recoveryCodes.put(principal, challengeId);
        return challengeId;
    }

    /**
     * 校验找回密码验证码。
     *
     * @param principal        找回主体
     * @param verificationCode 验证码
     * @return true 表示验证通过
     */
    @Override
    public boolean verifyPasswordRecoveryCode(String principal, String verificationCode) {
        return verificationCode != null && verificationCode.equals(recoveryCodes.remove(principal));
    }
}
