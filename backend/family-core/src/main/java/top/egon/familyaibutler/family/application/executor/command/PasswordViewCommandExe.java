package top.egon.familyaibutler.family.application.executor.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.application.command.CreatePasswordViewCommand;
import top.egon.familyaibutler.family.application.command.UpdatePasswordViewCommand;
import top.egon.familyaibutler.family.domain.passwordview.gateway.PasswordViewGateway;
import top.egon.familyaibutler.family.domain.passwordview.model.aggregate.PasswordView;
import top.egon.familyaibutler.family.domain.passwordview.model.valueobject.StrengthDTO;
import top.egon.familyaibutler.family.domain.passwordview.service.PasswordDomainService;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.executor.command
 * @ClassName: PasswordViewCommandExe
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 账号密码命令应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class PasswordViewCommandExe {
    private final PasswordViewGateway passwordViewGateway;
    private final PasswordDomainService passwordDomainService;

    /**
     * 创建账号密码。
     *
     * @param command 创建命令
     * @return 是否创建成功
     */
    public boolean create(CreatePasswordViewCommand command) {
        passwordViewGateway.save(toDomain(command));
        return true;
    }

    /**
     * 修改账号密码。
     *
     * @param command 修改命令
     * @return 是否修改成功
     */
    public boolean update(UpdatePasswordViewCommand command) {
        PasswordView passwordView = new PasswordView();
        passwordView.setId(command.id());
        passwordView.setName(command.name());
        passwordView.setPassword(command.password());
        passwordView.setDescription(command.description());
        passwordView.setAccountNumber(command.accountNumber());
        passwordView.setWebsit(command.websit());
        passwordView.setLikeStatus(Boolean.TRUE.equals(command.likeStatus()));
        passwordView.setCategory(command.category());
        return passwordViewGateway.update(passwordView);
    }

    /**
     * 删除账号密码。
     *
     * @param idList 主键列表
     * @return 是否删除成功
     */
    public boolean delete(List<Long> idList) {
        return passwordViewGateway.delete(idList);
    }

    /**
     * 生成随机密码。
     *
     * @param length                密码长度
     * @param needSpecialCharacters 是否需要特殊字符
     * @param specialCharacters     特殊字符集合
     * @return 随机密码
     */
    public String generatePassword(int length, boolean needSpecialCharacters, String specialCharacters) {
        return passwordDomainService.generatePassword(length, needSpecialCharacters, specialCharacters);
    }

    /**
     * 检查密码强度详情。
     *
     * @param password 密码
     * @return 密码强度详情
     */
    public StrengthDTO checkStrength(String password) {
        return passwordDomainService.checkStrength(password);
    }

    /**
     * 判断密码是否符合强度规则。
     *
     * @param password 密码
     * @return 是否符合强度规则
     */
    public boolean checkValid(String password) {
        return passwordDomainService.isValidPassword(password);
    }

    /**
     * 创建命令转换为领域模型。
     *
     * @param command 创建命令
     * @return 账号密码领域模型
     */
    private PasswordView toDomain(CreatePasswordViewCommand command) {
        PasswordView passwordView = new PasswordView();
        passwordView.setName(command.name());
        passwordView.setPassword(command.password());
        passwordView.setDescription(command.description());
        passwordView.setAccountNumber(command.accountNumber());
        passwordView.setWebsit(command.websit());
        passwordView.setLikeStatus(Boolean.TRUE.equals(command.likeStatus()));
        passwordView.setCategory(command.category());
        return passwordView;
    }
}
