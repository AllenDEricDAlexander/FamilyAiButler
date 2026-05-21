package top.egon.familyaibutler.family.application;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.infrastructure.configuration.CacheService;
import top.egon.familyaibutler.family.application.dto.CreatePasswordViewCommand;
import top.egon.familyaibutler.family.application.dto.UpdatePasswordViewCommand;
import top.egon.familyaibutler.family.domain.model.valueobject.StrengthDTO;
import top.egon.familyaibutler.family.domain.service.PasswordDomainService;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.converter.PasswordViewMpConverter;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.dataobject.PasswordViewPO;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.service.PasswordViewService;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application
 * @ClassName: PasswordViewCommandService
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 账号密码命令应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class PasswordViewCommandService {
    private final PasswordViewService passwordViewService;
    private final PasswordDomainService passwordDomainService;
    private final PasswordViewMpConverter passwordViewMpConverter;
    private final CacheService cacheService;

    /**
     * 创建账号密码。
     *
     * @param command 创建命令
     * @return 是否创建成功
     */
    public boolean create(CreatePasswordViewCommand command) {
        PasswordViewPO passwordView = passwordViewMpConverter.toDataObject(command);
        return passwordViewService.save(passwordView);
    }

    /**
     * 修改账号密码。
     *
     * @param command 修改命令
     * @return 是否修改成功
     */
    public boolean update(UpdatePasswordViewCommand command) {
        PasswordViewPO byId = passwordViewService.getById(command.id());
        if (byId == null) {
            return false;
        }
        passwordViewMpConverter.apply(command, byId);
        boolean updated = passwordViewService.updateById(byId);
        if (updated && ObjectUtils.isNotEmpty(byId.getBusinessId())) {
            cacheService.put(byId.getBusinessId(), byId, 60L * 60 * 24);
        }
        return updated;
    }

    /**
     * 删除账号密码。
     *
     * @param idList 主键列表
     * @return 是否删除成功
     */
    public boolean delete(List<Long> idList) {
        idList.forEach(id -> cacheService.evict(String.valueOf(id)));
        return passwordViewService.removeByIds(idList);
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
}
