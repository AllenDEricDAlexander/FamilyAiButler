package top.egon.familyaibutler.family.infrastructure.persistence.jpa.converter;

import org.springframework.stereotype.Component;
import top.egon.familyaibutler.family.domain.category.model.aggregate.Category;
import top.egon.familyaibutler.family.domain.category.model.entity.CategoryType;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.dataobject.CategoryPo;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.dataobject.CategoryTypePo;

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

    /**
     * 领域模型转 JPA Entity。
     *
     * @param category 分类领域模型
     * @return 分类 JPA Entity
     */
    public CategoryPo toDataObject(Category category) {
        return CategoryPo.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParentId())
                .createTime(category.getCreateTime())
                .updateTime(category.getUpdateTime())
                .build();
    }

    /**
     * JPA Entity 转分类类型领域实体。
     *
     * @param categoryTypePo 分类类型 JPA Entity
     * @return 分类类型领域实体
     */
    public CategoryType toTypeDomain(CategoryTypePo categoryTypePo) {
        CategoryType categoryType = new CategoryType();
        categoryType.setId(categoryTypePo.getId());
        categoryType.setTypeName(categoryTypePo.getTypeName());
        categoryType.setDescription(categoryTypePo.getDescription());
        categoryType.setCreateTime(categoryTypePo.getCreateTime());
        categoryType.setUpdateTime(categoryTypePo.getUpdateTime());
        return categoryType;
    }

    /**
     * 分类类型领域实体转 JPA Entity。
     *
     * @param categoryType 分类类型领域实体
     * @return 分类类型 JPA Entity
     */
    public CategoryTypePo toTypeDataObject(CategoryType categoryType) {
        return CategoryTypePo.builder()
                .id(categoryType.getId())
                .typeName(categoryType.getTypeName())
                .description(categoryType.getDescription())
                .createTime(categoryType.getCreateTime())
                .updateTime(categoryType.getUpdateTime())
                .build();
    }
}
