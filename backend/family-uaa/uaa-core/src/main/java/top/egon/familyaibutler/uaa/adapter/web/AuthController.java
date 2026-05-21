/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @FileName: AuthController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 认证 Web 控制器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.uaa.application.manage.AuthManage;
import top.egon.familyaibutler.uaa.facade.dto.auth.LogoutRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.PasswordLoginRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.PasswordRecoveryRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.ResetPasswordRequest;
import top.egon.familyaibutler.uaa.facade.dto.auth.VerifyCodeLoginRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenPairResponse;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @ClassName: AuthController
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 认证 Web 控制器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthManage authService;

    /**
     * 创建认证 Web 控制器。
     *
     * @param authService 认证应用服务
     */
    public AuthController(AuthManage authService) {
        this.authService = authService;
    }

    /**
     * 密码登录。
     *
     * @param request 密码登录请求
     * @return 令牌对
     */
    @PostMapping("/login/password")
    public Result<TokenPairResponse> loginByPassword(@RequestBody @Valid PasswordLoginRequest request) {
        return Result.success(authService.loginByPassword(request));
    }

    /**
     * 邮箱验证码登录。
     *
     * @param request 验证码登录请求
     * @return 令牌对
     */
    @PostMapping("/login/email-code")
    public Result<TokenPairResponse> loginByEmailCode(@RequestBody @Valid VerifyCodeLoginRequest request) {
        return Result.success(authService.loginByEmailCode(request));
    }

    /**
     * 短信验证码登录。
     *
     * @param request 验证码登录请求
     * @return 令牌对
     */
    @PostMapping("/login/sms-code")
    public Result<TokenPairResponse> loginBySmsCode(@RequestBody @Valid VerifyCodeLoginRequest request) {
        return Result.success(authService.loginBySmsCode(request));
    }

    /**
     * 请求找回密码验证码。
     *
     * @param request 找回密码请求
     * @return 找回密码挑战 ID
     */
    @PostMapping("/password/recovery")
    public Result<String> requestPasswordRecovery(@RequestBody @Valid PasswordRecoveryRequest request) {
        return Result.success(authService.requestPasswordRecovery(request));
    }

    /**
     * 重置密码。
     *
     * @param request 重置密码请求
     * @return true 表示重置成功
     */
    @PostMapping("/password/reset")
    public Result<Boolean> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        return Result.success(authService.resetPassword(request));
    }

    /**
     * 退出当前会话。
     *
     * @param request 退出请求
     * @return true 表示退出成功
     */
    @PostMapping("/logout/current")
    public Result<Boolean> logoutCurrentSession(@RequestBody LogoutRequest request) {
        return Result.success(authService.logoutCurrentSession(request));
    }

    /**
     * 退出全部会话。
     *
     * @param request 退出请求
     * @return true 表示退出成功
     */
    @PostMapping("/logout/all")
    public Result<Boolean> logoutAllSessions(@RequestBody LogoutRequest request) {
        return Result.success(authService.logoutAllSessions(request));
    }
}
