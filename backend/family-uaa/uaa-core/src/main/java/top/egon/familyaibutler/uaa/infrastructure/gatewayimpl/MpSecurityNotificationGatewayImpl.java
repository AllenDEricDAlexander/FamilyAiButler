/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @FileName: MpSecurityNotificationGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:45
 * @Description: MyBatis Plus 安全通知网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gatewayimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import top.egon.familyaibutler.uaa.domain.gateway.SecurityNotificationGateway;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.SecurityChallengePO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper.SecurityChallengeMapper;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @ClassName: MpSecurityNotificationGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:45
 * @Description: MyBatis Plus 安全通知网关实现
 * @Version: 1.0
 */
@Repository
public class MpSecurityNotificationGatewayImpl implements SecurityNotificationGateway {
    private static final String PASSWORD_RECOVERY = "PASSWORD_RECOVERY";
    private final SecurityChallengeMapper securityChallengeMapper;

    /**
     * 创建 MyBatis Plus 安全通知网关实现。
     *
     * @param securityChallengeMapper 安全挑战 Mapper
     */
    public MpSecurityNotificationGatewayImpl(SecurityChallengeMapper securityChallengeMapper) {
        this.securityChallengeMapper = securityChallengeMapper;
    }

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
        SecurityChallengePO challengePO = SecurityChallengePO.builder()
                .challengeId(challengeId)
                .accountId(accountId)
                .principal(principal)
                .challengeType(PASSWORD_RECOVERY)
                .verificationCode(challengeId)
                .used(false)
                .expiresAt(Date.from(Instant.now().plusSeconds(600L)))
                .deleted(false)
                .build();
        securityChallengeMapper.insert(challengePO);
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
        LambdaQueryWrapper<SecurityChallengePO> wrapper = new LambdaQueryWrapper<SecurityChallengePO>()
                .eq(SecurityChallengePO::getPrincipal, principal)
                .eq(SecurityChallengePO::getChallengeType, PASSWORD_RECOVERY)
                .eq(SecurityChallengePO::getVerificationCode, verificationCode)
                .eq(SecurityChallengePO::getUsed, false)
                .gt(SecurityChallengePO::getExpiresAt, new Date())
                .orderByDesc(SecurityChallengePO::getCreateTime)
                .last("limit 1");
        SecurityChallengePO challengePO = securityChallengeMapper.selectOne(wrapper);
        if (challengePO == null) {
            return false;
        }
        challengePO.setUsed(true);
        securityChallengeMapper.updateById(challengePO);
        return true;
    }
}
