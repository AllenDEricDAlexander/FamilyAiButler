/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @FileName: StepUpChallengeRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 二次验证请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.auth;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @ClassName: StepUpChallengeRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 二次验证请求
 * @Version: 1.0
 */
public record StepUpChallengeRequest(
        String accountId,
        String action,
        String challengeId,
        String verifyCode
) {
}
