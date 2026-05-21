/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.gateway.impl
 * @FileName: PasswordViewGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:12
 * @Description: 账号密码领域网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.family.infrastructure.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;
import top.egon.familyaibutler.family.domain.common.model.valueobject.PageSlice;
import top.egon.familyaibutler.family.domain.passwordview.gateway.PasswordViewGateway;
import top.egon.familyaibutler.family.domain.passwordview.model.aggregate.PasswordView;
import top.egon.familyaibutler.family.infrastructure.configuration.CacheService;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.converter.PasswordViewMpConverter;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.dataobject.PasswordViewPO;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.service.PasswordViewService;

import java.util.List;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.gateway.impl
 * @ClassName: PasswordViewGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:12
 * @Description: 账号密码领域网关实现
 * @Version: 1.0
 */
@Repository
@RequiredArgsConstructor
public class PasswordViewGatewayImpl implements PasswordViewGateway {
    private final PasswordViewService passwordViewService;
    private final PasswordViewMpConverter passwordViewMpConverter;
    private final CacheService cacheService;

    /**
     * 按业务主键查找账号密码聚合。
     *
     * @param businessId 业务主键
     * @return 账号密码聚合
     */
    @Override
    public Optional<PasswordView> findByBusinessId(String businessId) {
        PasswordViewPO dataObject = cacheService.get(businessId, PasswordViewPO.class);
        if (ObjectUtils.isEmpty(dataObject)) {
            dataObject = passwordViewService.selectByBusinessId(businessId);
            cacheService.put(businessId, dataObject, 60L * 60 * 24);
        }
        return Optional.ofNullable(dataObject).map(passwordViewMpConverter::toDomain);
    }

    /**
     * 按主键查找账号密码聚合。
     *
     * @param id 聚合主键
     * @return 账号密码聚合
     */
    @Override
    public Optional<PasswordView> find(Long id) {
        String cacheKey = String.valueOf(id);
        PasswordViewPO dataObject = cacheService.get(cacheKey, PasswordViewPO.class);
        if (ObjectUtils.isEmpty(dataObject)) {
            dataObject = passwordViewService.getById(id);
            cacheService.put(cacheKey, dataObject, 60L * 60 * 24);
        }
        return Optional.ofNullable(dataObject).map(passwordViewMpConverter::toDomain);
    }

    /**
     * 保存账号密码聚合。
     *
     * @param aggregate 账号密码聚合
     * @return 保存后的聚合
     */
    @Override
    public PasswordView save(PasswordView aggregate) {
        PasswordViewPO dataObject = passwordViewMpConverter.toDataObject(aggregate);
        passwordViewService.save(dataObject);
        return passwordViewMpConverter.toDomain(dataObject);
    }

    /**
     * 修改账号密码聚合。
     *
     * @param aggregate 账号密码聚合
     * @return true 表示修改成功
     */
    @Override
    public boolean update(PasswordView aggregate) {
        PasswordViewPO dataObject = passwordViewService.getById(aggregate.getId());
        if (dataObject == null) {
            return false;
        }
        passwordViewMpConverter.apply(aggregate, dataObject);
        boolean updated = passwordViewService.updateById(dataObject);
        if (updated && ObjectUtils.isNotEmpty(dataObject.getBusinessId())) {
            cacheService.put(dataObject.getBusinessId(), dataObject, 60L * 60 * 24);
        }
        return updated;
    }

    /**
     * 批量删除账号密码聚合。
     *
     * @param idList 主键列表
     * @return true 表示删除成功
     */
    @Override
    public boolean delete(List<Long> idList) {
        idList.forEach(id -> cacheService.evict(String.valueOf(id)));
        return passwordViewService.removeByIds(idList);
    }

    /**
     * 分页查询账号密码聚合。
     *
     * @param pageNum   页码
     * @param pageSize  页大小
     * @param condition 查询条件
     * @return 分页切片
     */
    @Override
    public PageSlice<PasswordView> page(int pageNum, int pageSize, PasswordView condition) {
        PasswordViewPO dataObject = passwordViewMpConverter.toDataObject(condition);
        Page<PasswordViewPO> page = passwordViewService.page(new Page<>(pageNum, pageSize), new QueryWrapper<>(dataObject));
        return new PageSlice<>(page.getRecords().stream().map(passwordViewMpConverter::toDomain).toList(),
                page.getTotal(), page.getCurrent(), page.getSize());
    }
}
