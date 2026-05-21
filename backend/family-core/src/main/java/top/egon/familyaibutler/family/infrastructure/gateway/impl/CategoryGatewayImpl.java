/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.gateway.impl
 * @FileName: CategoryGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:12
 * @Description: 分类领域网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.family.infrastructure.gateway.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import top.egon.familyaibutler.family.domain.category.gateway.CategoryGateway;
import top.egon.familyaibutler.family.domain.category.model.aggregate.Category;
import top.egon.familyaibutler.family.domain.common.model.valueobject.PageSlice;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.converter.CategoryJpaConverter;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.dataobject.CategoryPo;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.service.CategoryService;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.gateway.impl
 * @ClassName: CategoryGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:12
 * @Description: 分类领域网关实现
 * @Version: 1.0
 */
@Repository
@RequiredArgsConstructor
public class CategoryGatewayImpl implements CategoryGateway {
    private final CategoryService categoryService;
    private final CategoryJpaConverter categoryJpaConverter;

    /**
     * 保存分类。
     *
     * @param category 分类领域模型
     * @return 保存后的分类
     */
    @Override
    public Category save(Category category) {
        return categoryJpaConverter.toDomain(categoryService.save(categoryJpaConverter.toDataObject(category)));
    }

    /**
     * 修改分类。
     *
     * @param category 分类领域模型
     * @return 修改后的分类
     */
    @Override
    public Category update(Category category) {
        return categoryJpaConverter.toDomain(categoryService.update(categoryJpaConverter.toDataObject(category)));
    }

    /**
     * 删除分类。
     *
     * @param id 主键
     * @return true 表示删除成功
     */
    @Override
    public Boolean delete(Long id) {
        return categoryService.delete(id);
    }

    /**
     * 分页查询分类。
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 分类分页切片
     */
    @Override
    public PageSlice<Category> findAll(int pageNum, int pageSize) {
        Page<CategoryPo> page = categoryService.findAll(PageRequest.of(pageNum, pageSize));
        return new PageSlice<>(page.getContent().stream().map(categoryJpaConverter::toDomain).toList(),
                page.getTotalElements(), page.getNumber(), page.getSize());
    }

    /**
     * 按主键查询分类。
     *
     * @param id 主键
     * @return 分类领域模型
     */
    @Override
    public Optional<Category> findById(Long id) {
        return categoryService.findById(id).map(categoryJpaConverter::toDomain);
    }
}
