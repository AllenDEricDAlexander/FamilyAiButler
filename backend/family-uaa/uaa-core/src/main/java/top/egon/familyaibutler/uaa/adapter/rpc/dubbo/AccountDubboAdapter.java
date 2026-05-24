/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @FileName: AccountDubboAdapter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:20
 * @Description: 账号 facade 适配器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.rpc.dubbo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.uaa.application.manage.AccountManage;
import top.egon.familyaibutler.uaa.facade.AccountFacade;
import top.egon.familyaibutler.uaa.facade.dto.account.AccountSummaryResponse;
import top.egon.familyaibutler.uaa.facade.dto.account.ChangeAccountStatusRequest;
import top.egon.familyaibutler.uaa.facade.dto.account.DeleteAccountRequest;
import top.egon.familyaibutler.uaa.facade.dto.account.RegisterAccountRequest;
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
 * @ClassName: AccountDubboAdapter
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:20
 * @Description: 账号 facade 适配器
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-account-dubbo",
        serviceName = "账号 Dubbo 服务", serviceDescription = "账号注册、查询、状态变更和注销 RPC 能力", protocol = DocProtocol.DUBBO_TRIPLE)
public class AccountDubboAdapter implements AccountFacade {
    private final AccountManage accountService;

    /**
     * 通过用户名注册账号。
     *
     * @param request 注册请求
     * @return 账号摘要
     */
    @Override
    @DocOperation(summary = "通过用户名注册账号", description = "通过用户名注册账号",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = RegisterAccountRequest.class))),
            response = @DocResponse(description = "注册成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = AccountSummaryResponse.class)))
    public AccountSummaryResponse registerByUsername(RegisterAccountRequest request) {
        return accountService.registerByUsername(request);
    }

    /**
     * 通过邮箱注册账号。
     *
     * @param request 注册请求
     * @return 账号摘要
     */
    @Override
    @DocOperation(summary = "通过邮箱注册账号", description = "通过邮箱注册账号",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = RegisterAccountRequest.class))),
            response = @DocResponse(description = "注册成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = AccountSummaryResponse.class)))
    public AccountSummaryResponse registerByEmail(RegisterAccountRequest request) {
        return accountService.registerByEmail(request);
    }

    /**
     * 通过手机号注册账号。
     *
     * @param request 注册请求
     * @return 账号摘要
     */
    @Override
    @DocOperation(summary = "通过手机号注册账号", description = "通过手机号注册账号",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = RegisterAccountRequest.class))),
            response = @DocResponse(description = "注册成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = AccountSummaryResponse.class)))
    public AccountSummaryResponse registerByPhone(RegisterAccountRequest request) {
        return accountService.registerByPhone(request);
    }

    /**
     * 查询账号摘要。
     *
     * @param accountId 账号 ID
     * @return 账号摘要
     */
    @Override
    @DocOperation(summary = "查询账号摘要", description = "按账号 ID 查询账号摘要",
            request = @DocRequest(params = {
                    @DocParameter(name = "accountId", in = DocParamIn.AUTO, description = "账号 ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.STRING), example = "account-001")
            }),
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = AccountSummaryResponse.class)))
    public AccountSummaryResponse findAccountSummary(String accountId) {
        return accountService.findAccountSummary(accountId);
    }

    /**
     * 修改账号状态。
     *
     * @param request 状态修改请求
     * @return 账号摘要
     */
    @Override
    @DocOperation(summary = "修改账号状态", description = "修改账号状态",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = ChangeAccountStatusRequest.class))),
            response = @DocResponse(description = "修改成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = AccountSummaryResponse.class)))
    public AccountSummaryResponse changeAccountStatus(ChangeAccountStatusRequest request) {
        return accountService.changeAccountStatus(request);
    }

    /**
     * 申请注销账号。
     *
     * @param request 注销请求
     * @return 账号摘要
     */
    @Override
    @DocOperation(summary = "申请注销账号", description = "申请注销账号",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = DeleteAccountRequest.class))),
            response = @DocResponse(description = "申请成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = AccountSummaryResponse.class)))
    public AccountSummaryResponse requestAccountDeletion(DeleteAccountRequest request) {
        return accountService.requestAccountDeletion(request);
    }

    /**
     * 确认注销账号。
     *
     * @param request 注销请求
     * @return 账号摘要
     */
    @Override
    @DocOperation(summary = "确认注销账号", description = "确认注销账号",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = DeleteAccountRequest.class))),
            response = @DocResponse(description = "注销成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = AccountSummaryResponse.class)))
    public AccountSummaryResponse confirmAccountDeletion(DeleteAccountRequest request) {
        return accountService.confirmAccountDeletion(request);
    }
}
