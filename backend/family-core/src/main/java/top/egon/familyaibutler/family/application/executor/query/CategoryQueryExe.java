package top.egon.familyaibutler.family.application.executor.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.common.pojo.PageResult;
import top.egon.familyaibutler.family.application.result.CategoryDTO;
import top.egon.familyaibutler.family.domain.category.gateway.CategoryGateway;
import top.egon.familyaibutler.family.domain.category.model.aggregate.Category;
import top.egon.familyaibutler.family.domain.common.model.valueobject.PageSlice;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.executor.query
 * @ClassName: CategoryQueryExe
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 分类查询应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class CategoryQueryExe {
    /**
     * Category 网关。
     */
    private final CategoryGateway categoryGateway;

    /**
     * 分页查询分类。
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    public PageResult<CategoryDTO> findAll(int pageNum, int pageSize) {
        PageSlice<Category> page = categoryGateway.findAll(pageNum, pageSize);
        return PageResult.success(page.records().stream().map(this::toDto).toList(), page.total(), page.pageNum(), page.pageSize());
    }

    /**
     * 按主键查询分类。
     *
     * @param id 主键
     * @return 分类 DTO
     */
    public Optional<CategoryDTO> findById(Long id) {
        return categoryGateway.findById(id).map(this::toDto);
    }

    /**
     * 领域模型转 DTO。
     *
     * @param category 分类领域模型
     * @return 分类 DTO
     */
    private CategoryDTO toDto(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .categoryName(category.getName())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .build();
    }
}
