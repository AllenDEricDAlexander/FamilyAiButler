package top.egon.familyaibutler.family.application.manage;

import top.egon.familyaibutler.common.pojo.PageResult;
import top.egon.familyaibutler.family.application.result.CategoryDTO;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.manage
 * @ClassName: CategoryManage
 * @Author: atluofu
 * @CreateTime: 2026-05-20 11:05
 * @Description: 分类 COLA 客户端接口
 * @Version: 1.0
 */
public interface CategoryManage {

    /**
     * 新增分类。
     *
     * @param category 分类 DTO
     * @return 保存后的分类
     */
    CategoryDTO save(CategoryDTO category);

    /**
     * 修改分类。
     *
     * @param category 分类 DTO
     * @return 保存后的分类
     */
    CategoryDTO update(CategoryDTO category);

    /**
     * 删除分类。
     *
     * @param id 主键
     * @return 是否删除成功
     */
    Boolean delete(Long id);

    /**
     * 分页查询分类。
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResult<CategoryDTO> findAll(int pageNum, int pageSize);

    /**
     * 按主键查询分类。
     *
     * @param id 主键
     * @return 分类 DTO
     */
    Optional<CategoryDTO> findById(Long id);
}
