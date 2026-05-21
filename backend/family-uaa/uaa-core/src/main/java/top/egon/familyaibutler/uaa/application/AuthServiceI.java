/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @FileName: AuthServiceI.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 认证应用服务接口文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application;

import top.egon.familyaibutler.uaa.facade.dto.auth.PasswordLoginRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.PasswordRecoveryRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.ResetPasswordRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.LogoutRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.VerifyCodeLoginRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenPairResponse;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @ClassName: AuthServiceI
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 认证应用服务接口
 * @Version: 1.0
 */
public interface AuthServiceI {

    /**
     * 密码登录。
     *
     * @param request 密码登录请求
     * @return 令牌对
     */
    TokenPairResponse loginByPassword(PasswordLoginRequest request);

    /**
     * 邮箱验证码登录。
     *
     * @param request 验证码登录请求
     * @return 令牌对
     */
    TokenPairResponse loginByEmailCode(VerifyCodeLoginRequest request);

    /**
     * 短信验证码登录。
     *
     * @param request 验证码登录请求
     * @return 令牌对
     */
    TokenPairResponse loginBySmsCode(VerifyCodeLoginRequest request);

    /**
     * 请求找回密码验证码。
     *
     * @param request 找回密码请求
     * @return 找回密码挑战 ID
     */
    String requestPasswordRecovery(PasswordRecoveryRequest request);

    /**
     * 重置密码。
     *
     * @param request 重置密码请求
     * @return true 表示重置成功
     */
    boolean resetPassword(ResetPasswordRequest request);

    /**
     * 退出当前会话。
     *
     * @param request 退出请求
     * @return true 表示退出成功
     */
    boolean logoutCurrentSession(LogoutRequest request);

    /**
     * 退出全部会话。
     *
     * @param request 退出请求
     * @return true 表示退出成功
     */
    boolean logoutAllSessions(LogoutRequest request);
}
