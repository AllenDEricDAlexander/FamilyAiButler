/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @FileName: AuthDubboAdapter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:20
 * @Description: 认证 facade 适配器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.rpc.dubbo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.uaa.application.manage.AuthManage;
import top.egon.familyaibutler.uaa.facade.AuthFacade;
import top.egon.familyaibutler.uaa.facade.dto.auth.LogoutRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.PasswordLoginRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.PasswordRecoveryRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.ResetPasswordRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.StepUpChallengeRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.VerifyCodeLoginRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenPairResponse;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @ClassName: AuthDubboAdapter
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:20
 * @Description: 认证 facade 适配器
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
public class AuthDubboAdapter implements AuthFacade {
    private final AuthManage authService;

    /**
     * 密码登录。
     *
     * @param request 密码登录请求
     * @return 令牌对
     */
    @Override
    public TokenPairResponse loginByPassword(PasswordLoginRequest request) {
        return authService.loginByPassword(request);
    }

    /**
     * 邮箱验证码登录。
     *
     * @param request 验证码登录请求
     * @return 令牌对
     */
    @Override
    public TokenPairResponse loginByEmailCode(VerifyCodeLoginRequest request) {
        return authService.loginByEmailCode(request);
    }

    /**
     * 短信验证码登录。
     *
     * @param request 验证码登录请求
     * @return 令牌对
     */
    @Override
    public TokenPairResponse loginBySmsCode(VerifyCodeLoginRequest request) {
        return authService.loginBySmsCode(request);
    }

    /**
     * 请求找回密码验证码。
     *
     * @param request 找回密码请求
     * @return 找回密码挑战 ID
     */
    @Override
    public String requestPasswordRecovery(PasswordRecoveryRequest request) {
        return authService.requestPasswordRecovery(request);
    }

    /**
     * 重置密码。
     *
     * @param request 重置密码请求
     * @return true 表示重置成功
     */
    @Override
    public boolean resetPassword(ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    /**
     * 请求高危操作二次验证。
     *
     * @param request 二次验证请求
     * @return 挑战 ID
     */
    @Override
    public String requestStepUpChallenge(StepUpChallengeRequest request) {
        return authService.requestStepUpChallenge(request);
    }

    /**
     * 验证高危操作二次验证。
     *
     * @param request 二次验证请求
     * @return true 表示验证通过
     */
    @Override
    public boolean verifyStepUpChallenge(StepUpChallengeRequest request) {
        return authService.verifyStepUpChallenge(request);
    }

    /**
     * 退出当前会话。
     *
     * @param request 退出请求
     * @return true 表示退出成功
     */
    @Override
    public boolean logoutCurrentSession(LogoutRequest request) {
        return authService.logoutCurrentSession(request);
    }

    /**
     * 退出全部会话。
     *
     * @param request 退出请求
     * @return true 表示退出成功
     */
    @Override
    public boolean logoutAllSessions(LogoutRequest request) {
        return authService.logoutAllSessions(request);
    }
}
