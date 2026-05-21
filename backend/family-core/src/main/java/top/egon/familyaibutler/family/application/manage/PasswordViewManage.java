package top.egon.familyaibutler.family.application.manage;

import top.egon.familyaibutler.common.pojo.PageResult;
import top.egon.familyaibutler.family.application.command.CreatePasswordViewCommand;
import top.egon.familyaibutler.family.application.command.UpdatePasswordViewCommand;
import top.egon.familyaibutler.family.application.query.PasswordViewPageQuery;
import top.egon.familyaibutler.family.application.result.PasswordViewDTO;
import top.egon.familyaibutler.family.domain.passwordview.model.valueobject.StrengthDTO;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.manage
 * @ClassName: PasswordViewManage
 * @Author: atluofu
 * @CreateTime: 2026-05-20 11:05
 * @Description: 账号密码 COLA 客户端接口
 * @Version: 1.0
 */
public interface PasswordViewManage {

    /**
     * 创建账号密码。
     *
     * @param command 创建命令
     * @return 是否创建成功
     */
    boolean create(CreatePasswordViewCommand command);

    /**
     * 修改账号密码。
     *
     * @param command 修改命令
     * @return 是否修改成功
     */
    boolean update(UpdatePasswordViewCommand command);

    /**
     * 删除账号密码。
     *
     * @param idList 主键列表
     * @return 是否删除成功
     */
    boolean delete(List<Long> idList);

    /**
     * 生成随机密码。
     *
     * @param length                密码长度
     * @param needSpecialCharacters 是否需要特殊字符
     * @param specialCharacters     特殊字符集合
     * @return 随机密码
     */
    String generatePassword(int length, boolean needSpecialCharacters, String specialCharacters);

    /**
     * 检查密码强度详情。
     *
     * @param password 密码
     * @return 密码强度详情
     */
    StrengthDTO checkStrength(String password);

    /**
     * 判断密码是否符合强度规则。
     *
     * @param password 密码
     * @return 是否符合强度规则
     */
    boolean checkValid(String password);

    /**
     * 按业务主键查询账号密码。
     *
     * @param businessId 业务主键
     * @return 账号密码 DTO
     */
    PasswordViewDTO selectByBusinessId(String businessId);

    /**
     * 按主键查询账号密码。
     *
     * @param id 主键
     * @return 账号密码 DTO
     */
    PasswordViewDTO selectById(Long id);

    /**
     * 分页查询账号密码。
     *
     * @param query 分页查询对象
     * @return 分页结果
     */
    PageResult<PasswordViewDTO> page(PasswordViewPageQuery query);
}
