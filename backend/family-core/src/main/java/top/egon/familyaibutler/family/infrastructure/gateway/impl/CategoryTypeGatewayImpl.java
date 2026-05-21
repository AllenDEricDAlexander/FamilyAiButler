/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.gateway.impl
 * @FileName: CategoryTypeGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:12
 * @Description: 分类类型领域网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.family.infrastructure.gateway.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import top.egon.familyaibutler.family.domain.category.gateway.CategoryTypeGateway;
import top.egon.familyaibutler.family.domain.category.model.entity.CategoryType;
import top.egon.familyaibutler.family.domain.common.model.valueobject.PageSlice;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.converter.CategoryJpaConverter;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.dataobject.CategoryTypePo;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.service.CategoryTypeService;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.gateway.impl
 * @ClassName: CategoryTypeGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:12
 * @Description: 分类类型领域网关实现
 * @Version: 1.0
 */
@Repository
@RequiredArgsConstructor
public class CategoryTypeGatewayImpl implements CategoryTypeGateway {
    private final CategoryTypeService categoryTypeService;
    private final CategoryJpaConverter categoryJpaConverter;

    /**
     * 保存分类类型。
     *
     * @param categoryType 分类类型领域实体
     * @return 保存后的分类类型
     */
    @Override
    public CategoryType save(CategoryType categoryType) {
        return categoryJpaConverter.toTypeDomain(categoryTypeService.save(categoryJpaConverter.toTypeDataObject(categoryType)));
    }

    /**
     * 修改分类类型。
     *
     * @param categoryType 分类类型领域实体
     * @return 修改后的分类类型
     */
    @Override
    public CategoryType update(CategoryType categoryType) {
        return categoryJpaConverter.toTypeDomain(categoryTypeService.update(categoryJpaConverter.toTypeDataObject(categoryType)));
    }

    /**
     * 删除分类类型。
     *
     * @param id 主键
     * @return true 表示删除成功
     */
    @Override
    public Boolean delete(Long id) {
        return categoryTypeService.delete(id);
    }

    /**
     * 分页查询分类类型。
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 分类类型分页切片
     */
    @Override
    public PageSlice<CategoryType> findAll(int pageNum, int pageSize) {
        Page<CategoryTypePo> page = categoryTypeService.findAll(PageRequest.of(pageNum, pageSize));
        return new PageSlice<>(page.getContent().stream().map(categoryJpaConverter::toTypeDomain).toList(),
                page.getTotalElements(), page.getNumber(), page.getSize());
    }

    /**
     * 按主键查询分类类型。
     *
     * @param id 主键
     * @return 分类类型领域实体
     */
    @Override
    public Optional<CategoryType> findById(Long id) {
        return categoryTypeService.findById(id).map(categoryJpaConverter::toTypeDomain);
    }
}
