package top.egon.familyaibutler.family.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.entity.CategoryTypePo;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application
 * @ClassName: CategoryTypeServiceImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 11:05
 * @Description: 分类类型 COLA 应用服务
 * @Version: 1.0
 */
@Service("categoryTypeApplicationService")
@RequiredArgsConstructor
public class CategoryTypeServiceImpl implements CategoryTypeServiceI {
    private final CategoryTypeCommandService categoryTypeCommandService;
    private final CategoryTypeQueryService categoryTypeQueryService;

    /**
     * 新增分类类型。
     *
     * @param categoryType 分类类型数据对象
     * @return 保存后的分类类型
     */
    @Override
    public CategoryTypePo save(CategoryTypePo categoryType) {
        return categoryTypeCommandService.save(categoryType);
    }

    /**
     * 修改分类类型。
     *
     * @param categoryType 分类类型数据对象
     * @return 保存后的分类类型
     */
    @Override
    public CategoryTypePo update(CategoryTypePo categoryType) {
        return categoryTypeCommandService.update(categoryType);
    }

    /**
     * 删除分类类型。
     *
     * @param id 主键
     * @return 是否删除成功
     */
    @Override
    public Boolean delete(Long id) {
        return categoryTypeCommandService.delete(id);
    }

    /**
     * 分页查询分类类型。
     *
     * @param pageRequest 分页对象
     * @return 分页结果
     */
    @Override
    public Page<CategoryTypePo> findAll(PageRequest pageRequest) {
        return categoryTypeQueryService.findAll(pageRequest);
    }

    /**
     * 按主键查询分类类型。
     *
     * @param id 主键
     * @return 分类类型数据对象
     */
    @Override
    public Optional<CategoryTypePo> findById(Long id) {
        return categoryTypeQueryService.findById(id);
    }
}
