package top.egon.familyaibutler.family.infrastructure.persistence.mp.service.impl;

import org.springframework.stereotype.Service;
import top.egon.familyaibutler.common.mybatis.extention.IEgonServiceImpl;
import top.egon.familyaibutler.family.domain.model.valueobject.StrengthDTO;
import top.egon.familyaibutler.family.domain.service.PasswordDomainService;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.mapper.PasswordViewMapper;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.dataobject.PasswordViewPO;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.service.PasswordViewService;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.persistence.mp.service.impl
 * @Author: atluofu
 * @CreateTime: 2025-08-03 09:40:09
 * @Description: (PasswordView)表服务实现类
 * @Version: 1.0
 */
@Service("passwordViewService")
public class PasswordViewServiceImpl extends IEgonServiceImpl<PasswordViewMapper, PasswordViewPO> implements PasswordViewService {
    private static final PasswordDomainService PASSWORD_DOMAIN_SERVICE = new PasswordDomainService();

    /**
     * @description: 校验密码强度信息
     * @author: atluofu
     * @date: 2025/8/3 12:22
     * @param: password
     * @return: Strength
     **/
    public static StrengthDTO checkStrength(String password) {
        return PASSWORD_DOMAIN_SERVICE.checkStrength(password);
    }

    /**
     * @description: 综合验证密码强度（ZXCVBN+自定义补漏）
     * @author: atluofu
     * @date: 2025/8/3 10:40
     * @param: password
     * @return: boolean
     **/
    public static boolean isValidPassword(String password) {
        return PASSWORD_DOMAIN_SERVICE.isValidPassword(password);
    }

    /**
     * @description: 密码生成
     * @author: atluofu
     * @date: 2025/7/31 22:09
     * @param:
     * @return:
     **/
    public static String generatePassword(int length, boolean needSpecialCharacters, String realSpecialCharacters) {
        return PASSWORD_DOMAIN_SERVICE.generatePassword(length, needSpecialCharacters, realSpecialCharacters);
    }
}
