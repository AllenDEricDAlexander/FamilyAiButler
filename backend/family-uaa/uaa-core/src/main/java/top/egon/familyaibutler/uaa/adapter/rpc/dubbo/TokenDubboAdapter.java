/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @FileName: TokenDubboAdapter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:20
 * @Description: Token facade 适配器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.rpc.dubbo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.uaa.application.manage.TokenManage;
import top.egon.familyaibutler.uaa.facade.TokenFacade;
import top.egon.familyaibutler.uaa.facade.dto.token.RefreshTokenRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.RevokeTokenRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenPairResponse;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenValidationRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenValidationResponse;
import top.egon.openapi.console.annotation.DocBody;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocParamIn;
import top.egon.openapi.console.annotation.DocParameter;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @ClassName: TokenDubboAdapter
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:20
 * @Description: Token facade 适配器
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-token-dubbo",
        serviceName = "Token Dubbo 服务", serviceDescription = "令牌刷新、校验和撤销 RPC 能力", protocol = DocProtocol.DUBBO_TRIPLE)
public class TokenDubboAdapter implements TokenFacade {
    /**
     * Token 应用服务。
     */
    private final TokenManage tokenService;

    /**
     * 刷新访问令牌。
     *
     * @param request 刷新请求
     * @return 新令牌对
     */
    @Override
    @DocOperation(summary = "刷新访问令牌", description = "使用刷新令牌签发新的令牌对",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = RefreshTokenRequest.class))),
            response = @DocResponse(description = "刷新成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = TokenPairResponse.class)))
    public TokenPairResponse refreshAccessToken(RefreshTokenRequest request) {
        return tokenService.refreshAccessToken(request);
    }

    /**
     * 校验访问令牌。
     *
     * @param request 校验请求
     * @return 校验结果
     */
    @Override
    @DocOperation(summary = "校验访问令牌", description = "校验访问令牌并返回令牌上下文",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = TokenValidationRequest.class))),
            response = @DocResponse(description = "校验成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = TokenValidationResponse.class)))
    public TokenValidationResponse validateAccessToken(TokenValidationRequest request) {
        return tokenService.validateAccessToken(request);
    }

    /**
     * 撤销单个令牌。
     *
     * @param request 撤销请求
     * @return true 表示撤销成功
     */
    @Override
    @DocOperation(summary = "撤销单个令牌", description = "撤销指定令牌",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = RevokeTokenRequest.class))),
            response = @DocResponse(description = "撤销成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN)))
    public boolean revokeToken(RevokeTokenRequest request) {
        return tokenService.revokeToken(request);
    }

    /**
     * 按账号撤销令牌。
     *
     * @param accountId 账号 ID
     * @return true 表示撤销成功
     */
    @Override
    @DocOperation(summary = "按账号撤销令牌", description = "撤销账号下的令牌",
            request = @DocRequest(params = {
                    @DocParameter(name = "accountId", in = DocParamIn.AUTO, description = "账号 ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.STRING), example = "account-001")
            }),
            response = @DocResponse(description = "撤销成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN)))
    public boolean revokeAccountTokens(String accountId) {
        return tokenService.revokeAccountTokens(accountId);
    }

    /**
     * 按设备撤销令牌。
     *
     * @param deviceId 设备 ID
     * @return true 表示撤销成功
     */
    @Override
    @DocOperation(summary = "按设备撤销令牌", description = "撤销设备下的令牌",
            request = @DocRequest(params = {
                    @DocParameter(name = "deviceId", in = DocParamIn.AUTO, description = "设备 ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.STRING), example = "device-001")
            }),
            response = @DocResponse(description = "撤销成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN)))
    public boolean revokeDeviceTokens(String deviceId) {
        return tokenService.revokeDeviceTokens(deviceId);
    }
}
