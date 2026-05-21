/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.gateway
 * @FileName: SecurityNotificationGateway.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:00
 * @Description: 安全通知领域网关文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.gateway;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.gateway
 * @ClassName: SecurityNotificationGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:00
 * @Description: 安全通知领域网关
 * @Version: 1.0
 */
public interface SecurityNotificationGateway {

    /**
     * 发送找回密码验证码。
     *
     * @param accountId 账号 ID
     * @param principal 找回主体
     * @return 挑战 ID
     */
    String sendPasswordRecoveryCode(String accountId, String principal);

    /**
     * 校验找回密码验证码。
     *
     * @param principal        找回主体
     * @param verificationCode 验证码
     * @return true 表示验证通过
     */
    boolean verifyPasswordRecoveryCode(String principal, String verificationCode);
}
