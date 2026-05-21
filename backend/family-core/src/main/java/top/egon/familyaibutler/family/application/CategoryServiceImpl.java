package top.egon.familyaibutler.family.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.entity.CategoryPo;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application
 * @ClassName: CategoryServiceImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 11:05
 * @Description: 分类 COLA 应用服务
 * @Version: 1.0
 */
@Service("categoryApplicationService")
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryServiceI {
    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;

    /**
     * 新增分类。
     *
     * @param category 分类数据对象
     * @return 保存后的分类
     */
    @Override
    public CategoryPo save(CategoryPo category) {
        return categoryCommandService.save(category);
    }

    /**
     * 修改分类。
     *
     * @param category 分类数据对象
     * @return 保存后的分类
     */
    @Override
    public CategoryPo update(CategoryPo category) {
        return categoryCommandService.update(category);
    }

    /**
     * 删除分类。
     *
     * @param id 主键
     * @return 是否删除成功
     */
    @Override
    public Boolean delete(Long id) {
        return categoryCommandService.delete(id);
    }

    /**
     * 分页查询分类。
     *
     * @param pageRequest 分页对象
     * @return 分页结果
     */
    @Override
    public Page<CategoryPo> findAll(PageRequest pageRequest) {
        return categoryQueryService.findAll(pageRequest);
    }

    /**
     * 按主键查询分类。
     *
     * @param id 主键
     * @return 分类数据对象
     */
    @Override
    public Optional<CategoryPo> findById(Long id) {
        return categoryQueryService.findById(id);
    }
}
