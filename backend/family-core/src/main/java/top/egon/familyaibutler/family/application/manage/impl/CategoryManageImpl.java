package top.egon.familyaibutler.family.application.manage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.common.pojo.PageResult;
import top.egon.familyaibutler.family.application.executor.command.CategoryCommandExe;
import top.egon.familyaibutler.family.application.executor.query.CategoryQueryExe;
import top.egon.familyaibutler.family.application.manage.CategoryManage;
import top.egon.familyaibutler.family.application.result.CategoryDTO;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.manage.impl
 * @ClassName: CategoryManageImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 11:05
 * @Description: 分类 COLA 应用服务
 * @Version: 1.0
 */
@Service("categoryApplicationService")
@RequiredArgsConstructor
public class CategoryManageImpl implements CategoryManage {
    /**
     * Category 命令执行器。
     */
    private final CategoryCommandExe categoryCommandService;
    /**
     * Category 查询执行器。
     */
    private final CategoryQueryExe categoryQueryService;

    /**
     * 新增分类。
     *
     * @param category 分类 DTO
     * @return 保存后的分类
     */
    @Override
    public CategoryDTO save(CategoryDTO category) {
        return categoryCommandService.save(category);
    }

    /**
     * 修改分类。
     *
     * @param category 分类 DTO
     * @return 保存后的分类
     */
    @Override
    public CategoryDTO update(CategoryDTO category) {
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
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    @Override
    public PageResult<CategoryDTO> findAll(int pageNum, int pageSize) {
        return categoryQueryService.findAll(pageNum, pageSize);
    }

    /**
     * 按主键查询分类。
     *
     * @param id 主键
     * @return 分类 DTO
     */
    @Override
    public Optional<CategoryDTO> findById(Long id) {
        return categoryQueryService.findById(id);
    }
}
