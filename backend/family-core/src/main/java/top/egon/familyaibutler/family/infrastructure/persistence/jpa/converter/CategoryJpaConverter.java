package top.egon.familyaibutler.family.infrastructure.persistence.jpa.converter;

import org.springframework.stereotype.Component;
import top.egon.familyaibutler.family.domain.model.aggregate.Category;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.entity.CategoryPo;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.persistence.jpa.converter
 * @ClassName: CategoryJpaConverter
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 分类 JPA Entity 与领域模型转换器
 * @Version: 1.0
 */
@Component
public class CategoryJpaConverter {

    /**
     * JPA Entity 转领域模型。
     *
     * @param categoryPo 分类 JPA Entity
     * @return 分类领域模型
     */
    public Category toDomain(CategoryPo categoryPo) {
        Category category = new Category();
        category.setId(categoryPo.getId());
        category.setName(categoryPo.getName());
        category.setDescription(categoryPo.getDescription());
        category.setParentId(categoryPo.getParentId());
        category.setCreateTime(categoryPo.getCreateTime());
        category.setUpdateTime(categoryPo.getUpdateTime());
        return category;
    }
}
