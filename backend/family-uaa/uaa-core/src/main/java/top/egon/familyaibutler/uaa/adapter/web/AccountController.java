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
public class AccountController {
    private final AccountManage accountService;
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
    public Result<AccountResponse> register(@RequestBody @Valid RegisterAccountRequest request) {
        return Result.success(accountService.registerByUsername(accountWebAssembler.toRegisterCommand(request)));
    }
}
