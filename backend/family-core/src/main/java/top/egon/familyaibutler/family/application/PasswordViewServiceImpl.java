package top.egon.familyaibutler.family.application;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.application.dto.CreatePasswordViewCommand;
import top.egon.familyaibutler.family.application.dto.UpdatePasswordViewCommand;
import top.egon.familyaibutler.family.application.dto.PasswordViewPageQuery;
import top.egon.familyaibutler.family.domain.model.valueobject.StrengthDTO;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.dataobject.PasswordViewPO;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application
 * @ClassName: PasswordViewServiceImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 11:05
 * @Description: 账号密码 COLA 应用服务
 * @Version: 1.0
 */
@Service("passwordViewApplicationService")
@RequiredArgsConstructor
public class PasswordViewServiceImpl implements PasswordViewServiceI {
    private final PasswordViewCommandService passwordViewCommandService;
    private final PasswordViewQueryService passwordViewQueryService;

    /**
     * 创建账号密码。
     *
     * @param command 创建命令
     * @return 是否创建成功
     */
    @Override
    public boolean create(CreatePasswordViewCommand command) {
        return passwordViewCommandService.create(command);
    }

    /**
     * 修改账号密码。
     *
     * @param command 修改命令
     * @return 是否修改成功
     */
    @Override
    public boolean update(UpdatePasswordViewCommand command) {
        return passwordViewCommandService.update(command);
    }

    /**
     * 删除账号密码。
     *
     * @param idList 主键列表
     * @return 是否删除成功
     */
    @Override
    public boolean delete(List<Long> idList) {
        return passwordViewCommandService.delete(idList);
    }

    /**
     * 生成随机密码。
     *
     * @param length                密码长度
     * @param needSpecialCharacters 是否需要特殊字符
     * @param specialCharacters     特殊字符集合
     * @return 随机密码
     */
    @Override
    public String generatePassword(int length, boolean needSpecialCharacters, String specialCharacters) {
        return passwordViewCommandService.generatePassword(length, needSpecialCharacters, specialCharacters);
    }

    /**
     * 检查密码强度详情。
     *
     * @param password 密码
     * @return 密码强度详情
     */
    @Override
    public StrengthDTO checkStrength(String password) {
        return passwordViewCommandService.checkStrength(password);
    }

    /**
     * 判断密码是否符合强度规则。
     *
     * @param password 密码
     * @return 是否符合强度规则
     */
    @Override
    public boolean checkValid(String password) {
        return passwordViewCommandService.checkValid(password);
    }

    /**
     * 按业务主键查询账号密码。
     *
     * @param businessId 业务主键
     * @return 账号密码数据对象
     */
    @Override
    public PasswordViewPO selectByBusinessId(String businessId) {
        return passwordViewQueryService.selectByBusinessId(businessId);
    }

    /**
     * 按主键查询账号密码。
     *
     * @param id 主键
     * @return 账号密码数据对象
     */
    @Override
    public PasswordViewPO selectById(Long id) {
        return passwordViewQueryService.selectById(id);
    }

    /**
     * 分页查询账号密码。
     *
     * @param query 分页查询对象
     * @return 分页结果
     */
    @Override
    public Page<PasswordViewPO> page(PasswordViewPageQuery query) {
        return passwordViewQueryService.page(query);
    }
}
