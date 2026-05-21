package top.egon.familyaibutler.family.application.manage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.common.pojo.PageResult;
import top.egon.familyaibutler.family.application.executor.command.CategoryTypeCommandExe;
import top.egon.familyaibutler.family.application.executor.query.CategoryTypeQueryExe;
import top.egon.familyaibutler.family.application.manage.CategoryTypeManage;
import top.egon.familyaibutler.family.application.result.CategoryTypeDTO;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.manage.impl
 * @ClassName: CategoryTypeManageImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 11:05
 * @Description: 分类类型 COLA 应用服务
 * @Version: 1.0
 */
@Service("categoryTypeApplicationService")
@RequiredArgsConstructor
public class CategoryTypeManageImpl implements CategoryTypeManage {
    private final CategoryTypeCommandExe categoryTypeCommandService;
    private final CategoryTypeQueryExe categoryTypeQueryService;

    /**
     * 新增分类类型。
     *
     * @param categoryType 分类类型 DTO
     * @return 保存后的分类类型
     */
    @Override
    public CategoryTypeDTO save(CategoryTypeDTO categoryType) {
        return categoryTypeCommandService.save(categoryType);
    }

    /**
     * 修改分类类型。
     *
     * @param categoryType 分类类型 DTO
     * @return 保存后的分类类型
     */
    @Override
    public CategoryTypeDTO update(CategoryTypeDTO categoryType) {
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
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    @Override
    public PageResult<CategoryTypeDTO> findAll(int pageNum, int pageSize) {
        return categoryTypeQueryService.findAll(pageNum, pageSize);
    }

    /**
     * 按主键查询分类类型。
     *
     * @param id 主键
     * @return 分类类型 DTO
     */
    @Override
    public Optional<CategoryTypeDTO> findById(Long id) {
        return categoryTypeQueryService.findById(id);
    }
}
