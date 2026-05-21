package top.egon.familyaibutler.family.application.executor.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.application.result.CategoryDTO;
import top.egon.familyaibutler.family.domain.category.gateway.CategoryGateway;
import top.egon.familyaibutler.family.domain.category.model.aggregate.Category;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.executor.command
 * @ClassName: CategoryCommandExe
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 分类命令应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class CategoryCommandExe {
    private final CategoryGateway categoryGateway;

    /**
     * 新增分类。
     *
     * @param category 分类 DTO
     * @return 保存后的分类
     */
    public CategoryDTO save(CategoryDTO category) {
        return toDto(categoryGateway.save(toDomain(category)));
    }

    /**
     * 修改分类。
     *
     * @param category 分类 DTO
     * @return 保存后的分类
     */
    public CategoryDTO update(CategoryDTO category) {
        return toDto(categoryGateway.update(toDomain(category)));
    }

    /**
     * 删除分类。
     *
     * @param id 主键
     * @return 是否删除成功
     */
    public Boolean delete(Long id) {
        return categoryGateway.delete(id);
    }

    /**
     * DTO 转领域模型。
     *
     * @param categoryDTO 分类 DTO
     * @return 分类领域模型
     */
    private Category toDomain(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setId(categoryDTO.getId());
        category.setName(categoryDTO.getCategoryName());
        category.setDescription(categoryDTO.getDescription());
        category.setParentId(categoryDTO.getParentId());
        return category;
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
