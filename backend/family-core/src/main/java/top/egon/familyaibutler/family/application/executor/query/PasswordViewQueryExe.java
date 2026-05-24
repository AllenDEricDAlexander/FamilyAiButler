package top.egon.familyaibutler.family.application.executor.query;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.common.pojo.PageResult;
import top.egon.familyaibutler.family.application.query.PasswordViewPageQuery;
import top.egon.familyaibutler.family.application.result.PasswordViewDTO;
import top.egon.familyaibutler.family.domain.common.model.valueobject.PageSlice;
import top.egon.familyaibutler.family.domain.passwordview.gateway.PasswordViewGateway;
import top.egon.familyaibutler.family.domain.passwordview.model.aggregate.PasswordView;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.executor.query
 * @ClassName: PasswordViewQueryExe
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 账号密码查询应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class PasswordViewQueryExe {
    /**
     * Password View 网关。
     */
    private final PasswordViewGateway passwordViewGateway;

    /**
     * 按业务主键查询账号密码。
     *
     * @param businessId 业务主键
     * @return 账号密码 DTO
     */
    public PasswordViewDTO selectByBusinessId(String businessId) {
        return passwordViewGateway.findByBusinessId(businessId).map(this::toDto).orElse(null);
    }

    /**
     * 按主键查询账号密码。
     *
     * @param id 主键
     * @return 账号密码 DTO
     */
    public PasswordViewDTO selectById(Long id) {
        return passwordViewGateway.find(id).map(this::toDto).orElse(null);
    }

    /**
     * 分页查询账号密码。
     *
     * @param query 分页查询对象
     * @return 分页结果
     */
    public PageResult<PasswordViewDTO> page(PasswordViewPageQuery query) {
        int realPageNum = ObjectUtils.isNotEmpty(query.pageNum()) ? query.pageNum() : 1;
        int realPageSize = ObjectUtils.isNotEmpty(query.pageSize()) ? query.pageSize() : 10;
        PageSlice<PasswordView> page = passwordViewGateway.page(realPageNum, realPageSize, toCondition(query));
        return PageResult.success(page.records().stream().map(this::toDto).toList(), page.total(), page.pageNum(), page.pageSize());
    }

    /**
     * 查询对象转换为领域条件。
     *
     * @param query 查询对象
     * @return 领域条件
     */
    private PasswordView toCondition(PasswordViewPageQuery query) {
        PasswordView condition = new PasswordView();
        condition.setName(query.name());
        condition.setPassword(query.password());
        condition.setDescription(query.description());
        condition.setAccountNumber(query.accountNumber());
        condition.setWebsit(query.websit());
        condition.setLikeStatus(Boolean.TRUE.equals(query.likeStatus()));
        condition.setCategory(query.category());
        return condition;
    }

    /**
     * 领域模型转换为 DTO。
     *
     * @param passwordView 账号密码领域模型
     * @return 账号密码 DTO
     */
    private PasswordViewDTO toDto(PasswordView passwordView) {
        return PasswordViewDTO.builder()
                .id(passwordView.getId())
                .name(passwordView.getName())
                .password(passwordView.getPassword())
                .description(passwordView.getDescription())
                .accountNumber(passwordView.getAccountNumber())
                .websit(passwordView.getWebsit())
                .likeStatus(Boolean.TRUE.equals(passwordView.getLikeStatus()))
                .category(passwordView.getCategory())
                .build();
    }
}
