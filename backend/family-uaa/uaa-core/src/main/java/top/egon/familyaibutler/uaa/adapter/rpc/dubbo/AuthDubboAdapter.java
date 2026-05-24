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
import top.egon.openapi.console.annotation.DocBody;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;

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
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-auth-dubbo",
        serviceName = "认证 Dubbo 服务", serviceDescription = "登录、退出、密码找回和二次验证 RPC 能力", protocol = DocProtocol.DUBBO_TRIPLE)
public class AuthDubboAdapter implements AuthFacade {
    private final AuthManage authService;

    /**
     * 密码登录。
     *
     * @param request 密码登录请求
     * @return 令牌对
     */
    @Override
    @DocOperation(summary = "密码登录", description = "使用账号密码完成登录并签发令牌",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = PasswordLoginRequest.class))),
            response = @DocResponse(description = "登录成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = TokenPairResponse.class)))
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
    @DocOperation(summary = "邮箱验证码登录", description = "使用邮箱验证码完成登录并签发令牌",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = VerifyCodeLoginRequest.class))),
            response = @DocResponse(description = "登录成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = TokenPairResponse.class)))
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
    @DocOperation(summary = "短信验证码登录", description = "使用短信验证码完成登录并签发令牌",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = VerifyCodeLoginRequest.class))),
            response = @DocResponse(description = "登录成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = TokenPairResponse.class)))
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
    @DocOperation(summary = "请求找回密码验证码", description = "为指定账号标识发起找回密码挑战",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = PasswordRecoveryRequest.class))),
            response = @DocResponse(description = "返回找回密码挑战 ID",
                    dataType = @DocDataType(kind = DocDataKind.STRING)))
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
    @DocOperation(summary = "重置密码", description = "使用验证码重置账号密码",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = ResetPasswordRequest.class))),
            response = @DocResponse(description = "重置成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN)))
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
    @DocOperation(summary = "请求高危操作二次验证", description = "为高危操作发起二次验证挑战",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = StepUpChallengeRequest.class))),
            response = @DocResponse(description = "返回二次验证挑战 ID",
                    dataType = @DocDataType(kind = DocDataKind.STRING)))
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
    @DocOperation(summary = "验证高危操作二次验证", description = "验证高危操作二次验证挑战",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = StepUpChallengeRequest.class))),
            response = @DocResponse(description = "验证成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN)))
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
    @DocOperation(summary = "退出当前会话", description = "撤销当前登录会话",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = LogoutRequest.class))),
            response = @DocResponse(description = "退出成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN)))
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
    @DocOperation(summary = "退出全部会话", description = "撤销账号下全部登录会话",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = LogoutRequest.class))),
            response = @DocResponse(description = "退出成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN)))
    public boolean logoutAllSessions(LogoutRequest request) {
        return authService.logoutAllSessions(request);
    }
}
