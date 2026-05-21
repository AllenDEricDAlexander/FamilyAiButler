/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.assembler
 * @FileName: AccountWebAssembler.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号 Web 装配器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.assembler;

import org.springframework.stereotype.Component;
import top.egon.familyaibutler.uaa.application.dto.account.RegisterAccountCommand;
import top.egon.familyaibutler.uaa.facade.dto.account.RegisterAccountRequest;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.assembler
 * @ClassName: AccountWebAssembler
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号 Web 装配器
 * @Version: 1.0
 */
@Component
public class AccountWebAssembler {

    /**
     * 注册请求转换为应用命令。
     *
     * @param request 注册请求
     * @return 注册命令
     */
    public RegisterAccountCommand toRegisterCommand(RegisterAccountRequest request) {
        return new RegisterAccountCommand(request.username(), request.email(), request.phone(), request.password());
    }
}
