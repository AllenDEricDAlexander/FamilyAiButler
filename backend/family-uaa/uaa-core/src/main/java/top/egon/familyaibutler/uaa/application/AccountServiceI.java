/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @FileName: AccountServiceI.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号应用服务接口文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application;

import top.egon.familyaibutler.uaa.application.dto.account.AccountResponse;
import top.egon.familyaibutler.uaa.application.dto.account.RegisterAccountCommand;
import top.egon.familyaibutler.uaa.domain.model.enums.AccountStatus;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @ClassName: AccountServiceI
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 账号应用服务接口
 * @Version: 1.0
 */
public interface AccountServiceI {

    /**
     * 通过用户名注册账号。
     *
     * @param command 注册账号命令
     * @return 账号响应
     */
    AccountResponse registerByUsername(RegisterAccountCommand command);

    /**
     * 修改账号状态。
     *
     * @param accountId 账号 ID
     * @param status    账号状态
     * @return 账号响应
     */
    AccountResponse changeAccountStatus(String accountId, AccountStatus status);

    /**
     * 申请注销账号。
     *
     * @param accountId 账号 ID
     * @return 账号响应
     */
    AccountResponse requestDeletion(String accountId);

    /**
     * 确认注销账号。
     *
     * @param accountId        账号 ID
     * @param verificationCode 二次验证码
     * @return 账号响应
     */
    AccountResponse confirmDeletion(String accountId, String verificationCode);
}
