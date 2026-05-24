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
import top.egon.openapi.console.annotation.DocBody;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;
import top.egon.openapi.console.annotation.DocWrapper;

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
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-auth",
        serviceName = "认证服务", serviceDescription = "登录、退出和密码找回能力", protocol = DocProtocol.HTTP)
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
    @DocOperation(summary = "密码登录", description = "使用账号密码完成登录并签发令牌",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = PasswordLoginRequest.class))),
            response = @DocResponse(description = "登录成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = TokenPairResponse.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
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
    @DocOperation(summary = "邮箱验证码登录", description = "使用邮箱验证码完成登录并签发令牌",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = VerifyCodeLoginRequest.class))),
            response = @DocResponse(description = "登录成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = TokenPairResponse.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
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
    @DocOperation(summary = "短信验证码登录", description = "使用短信验证码完成登录并签发令牌",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = VerifyCodeLoginRequest.class))),
            response = @DocResponse(description = "登录成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = TokenPairResponse.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
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
    @DocOperation(summary = "请求找回密码验证码", description = "为指定账号标识发起找回密码挑战",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = PasswordRecoveryRequest.class))),
            response = @DocResponse(description = "返回找回密码挑战 ID",
                    dataType = @DocDataType(kind = DocDataKind.STRING),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
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
    @DocOperation(summary = "重置密码", description = "使用验证码重置账号密码",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = ResetPasswordRequest.class))),
            response = @DocResponse(description = "重置成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
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
    @DocOperation(summary = "退出当前会话", description = "撤销当前登录会话",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = LogoutRequest.class))),
            response = @DocResponse(description = "退出成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
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
    @DocOperation(summary = "退出全部会话", description = "撤销账号下全部登录会话",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = LogoutRequest.class))),
            response = @DocResponse(description = "退出成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<Boolean> logoutAllSessions(@RequestBody LogoutRequest request) {
        return Result.success(authService.logoutAllSessions(request));
    }
}
