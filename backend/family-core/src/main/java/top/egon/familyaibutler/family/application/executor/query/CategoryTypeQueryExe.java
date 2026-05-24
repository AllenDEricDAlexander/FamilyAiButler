package top.egon.familyaibutler.family.application.executor.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.common.pojo.PageResult;
import top.egon.familyaibutler.family.application.result.CategoryTypeDTO;
import top.egon.familyaibutler.family.domain.category.gateway.CategoryTypeGateway;
import top.egon.familyaibutler.family.domain.category.model.entity.CategoryType;
import top.egon.familyaibutler.family.domain.common.model.valueobject.PageSlice;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.executor.query
 * @ClassName: CategoryTypeQueryExe
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 分类类型查询应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class CategoryTypeQueryExe {
    /**
     * Category Type 网关。
     */
    private final CategoryTypeGateway categoryTypeGateway;

    /**
     * 分页查询分类类型。
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    public PageResult<CategoryTypeDTO> findAll(int pageNum, int pageSize) {
        PageSlice<CategoryType> page = categoryTypeGateway.findAll(pageNum, pageSize);
        return PageResult.success(page.records().stream().map(this::toDto).toList(), page.total(), page.pageNum(), page.pageSize());
    }

    /**
     * 按主键查询分类类型。
     *
     * @param id 主键
     * @return 分类类型 DTO
     */
    public Optional<CategoryTypeDTO> findById(Long id) {
        return categoryTypeGateway.findById(id).map(this::toDto);
    }

    /**
     * 领域实体转 DTO。
     *
     * @param categoryType 分类类型领域实体
     * @return 分类类型 DTO
     */
    private CategoryTypeDTO toDto(CategoryType categoryType) {
        return CategoryTypeDTO.builder()
                .id(categoryType.getId())
                .typeName(categoryType.getTypeName())
                .description(categoryType.getDescription())
                .createTime(categoryType.getCreateTime())
                .updateTime(categoryType.getUpdateTime())
                .build();
    }
}
