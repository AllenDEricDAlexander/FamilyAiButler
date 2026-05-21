package top.egon.familyaibutler.family.domain.passwordview.gateway;

import top.egon.familyaibutler.family.domain.common.model.valueobject.PageSlice;
import top.egon.familyaibutler.family.domain.passwordview.model.aggregate.PasswordView;

import java.util.List;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.passwordview.gateway
 * @ClassName: PasswordViewGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 账号密码写模型网关
 * @Version: 1.0
 */
public interface PasswordViewGateway {

    /**
     * 按业务主键查找账号密码聚合。
     *
     * @param businessId 业务主键
     * @return 账号密码聚合
     */
    Optional<PasswordView> findByBusinessId(String businessId);

    /**
     * 按主键查找账号密码聚合。
     *
     * @param id 聚合主键
     * @return 账号密码聚合
     */
    Optional<PasswordView> find(Long id);

    /**
     * 保存账号密码聚合。
     *
     * @param aggregate 账号密码聚合
     * @return 保存后的聚合
     */
    PasswordView save(PasswordView aggregate);

    /**
     * 修改账号密码聚合。
     *
     * @param aggregate 账号密码聚合
     * @return true 表示修改成功
     */
    boolean update(PasswordView aggregate);

    /**
     * 批量删除账号密码聚合。
     *
     * @param idList 主键列表
     * @return true 表示删除成功
     */
    boolean delete(List<Long> idList);

    /**
     * 分页查询账号密码聚合。
     *
     * @param pageNum   页码
     * @param pageSize  页大小
     * @param condition 查询条件
     * @return 分页切片
     */
    PageSlice<PasswordView> page(int pageNum, int pageSize, PasswordView condition);
}
