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

import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.auth
 * @ClassName: StepUpChallengeRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 二次验证请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeStepUpChallengeRequest", description = "认证授权二次验证请求")
public record StepUpChallengeRequest(
        @DocField(description = "账号 ID", example = "account-001")
        String accountId,
        @DocField(description = "需要二次验证的动作", example = "RESET_PASSWORD")
        String action,
        @DocField(description = "二次验证挑战 ID", example = "challenge-001")
        String challengeId,
        @DocField(description = "验证码", example = "123456")
        String verifyCode
) {
}
