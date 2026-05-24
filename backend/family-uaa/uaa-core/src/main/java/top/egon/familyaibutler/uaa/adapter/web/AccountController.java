/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @FileName: AccountController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号 Web 控制器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.uaa.adapter.web.assembler.AccountWebAssembler;
import top.egon.familyaibutler.uaa.application.manage.AccountManage;
import top.egon.familyaibutler.uaa.application.result.account.AccountResponse;
import top.egon.familyaibutler.uaa.facade.dto.account.RegisterAccountRequest;
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
 * @ClassName: AccountController
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号 Web 控制器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/account")
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-account",
        serviceName = "账号服务", serviceDescription = "账号注册与账号资料查询能力", protocol = DocProtocol.HTTP)
public class AccountController {
    /**
     * Account 应用服务。
     */
    private final AccountManage accountService;
    /**
     * Account Web 对象转换器。
     */
    private final AccountWebAssembler accountWebAssembler;

    /**
     * 创建账号 Web 控制器。
     *
     * @param accountService      账号应用服务
     * @param accountWebAssembler 账号 Web 装配器
     */
    public AccountController(AccountManage accountService, AccountWebAssembler accountWebAssembler) {
        this.accountService = accountService;
        this.accountWebAssembler = accountWebAssembler;
    }

    /**
     * 注册账号。
     *
     * @param request 注册请求
     * @return 账号响应
     */
    @PostMapping("/register")
    @DocOperation(summary = "注册账号", description = "使用账号注册请求创建账号",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = RegisterAccountRequest.class))),
            response = @DocResponse(description = "注册成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = AccountResponse.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<AccountResponse> register(@RequestBody @Valid RegisterAccountRequest request) {
        return Result.success(accountService.registerByUsername(accountWebAssembler.toRegisterCommand(request)));
    }
}
