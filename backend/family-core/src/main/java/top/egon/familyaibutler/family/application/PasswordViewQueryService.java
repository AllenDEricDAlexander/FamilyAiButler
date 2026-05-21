package top.egon.familyaibutler.family.application;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.application.dto.PasswordViewPageQuery;
import top.egon.familyaibutler.family.infrastructure.configuration.CacheService;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.converter.PasswordViewMpConverter;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.dataobject.PasswordViewPO;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.service.PasswordViewService;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application
 * @ClassName: PasswordViewQueryService
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 账号密码查询应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class PasswordViewQueryService {
    private final PasswordViewService passwordViewService;
    private final PasswordViewMpConverter passwordViewMpConverter;
    private final CacheService cacheService;

    /**
     * 按业务主键查询账号密码。
     *
     * @param businessId 业务主键
     * @return 账号密码数据对象
     */
    public PasswordViewPO selectByBusinessId(String businessId) {
        PasswordViewPO passwordView = cacheService.get(businessId, PasswordViewPO.class);
        if (ObjectUtils.isEmpty(passwordView)) {
            passwordView = passwordViewService.selectByBusinessId(businessId);
            cacheService.put(businessId, passwordView, 60L * 60 * 24);
        }
        return passwordView;
    }

    /**
     * 按主键查询账号密码。
     *
     * @param id 主键
     * @return 账号密码数据对象
     */
    public PasswordViewPO selectById(Long id) {
        String cacheKey = String.valueOf(id);
        PasswordViewPO passwordView = cacheService.get(cacheKey, PasswordViewPO.class);
        if (ObjectUtils.isEmpty(passwordView)) {
            passwordView = passwordViewService.getById(id);
            cacheService.put(cacheKey, passwordView, 60L * 60 * 24);
        }
        return passwordView;
    }

    /**
     * 分页查询账号密码。
     *
     * @param query 分页查询对象
     * @return 分页结果
     */
    public Page<PasswordViewPO> page(PasswordViewPageQuery query) {
        int realPageNum = ObjectUtils.isNotEmpty(query.pageNum()) ? query.pageNum() : 1;
        int realPageSize = ObjectUtils.isNotEmpty(query.pageSize()) ? query.pageSize() : 10;
        PasswordViewPO passwordView = passwordViewMpConverter.toDataObject(query);
        return passwordViewService.page(new Page<>(realPageNum, realPageSize), new QueryWrapper<>(passwordView));
    }
}
