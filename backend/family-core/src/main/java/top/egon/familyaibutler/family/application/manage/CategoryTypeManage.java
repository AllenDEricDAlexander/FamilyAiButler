package top.egon.familyaibutler.family.application.manage;

import top.egon.familyaibutler.common.pojo.PageResult;
import top.egon.familyaibutler.family.application.result.CategoryTypeDTO;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.manage
 * @ClassName: CategoryTypeManage
 * @Author: atluofu
 * @CreateTime: 2026-05-20 11:05
 * @Description: 分类类型 COLA 客户端接口
 * @Version: 1.0
 */
public interface CategoryTypeManage {

    /**
     * 新增分类类型。
     *
     * @param categoryType 分类类型 DTO
     * @return 保存后的分类类型
     */
    CategoryTypeDTO save(CategoryTypeDTO categoryType);

    /**
     * 修改分类类型。
     *
     * @param categoryType 分类类型 DTO
     * @return 保存后的分类类型
     */
    CategoryTypeDTO update(CategoryTypeDTO categoryType);

    /**
     * 删除分类类型。
     *
     * @param id 主键
     * @return 是否删除成功
     */
    Boolean delete(Long id);

    /**
     * 分页查询分类类型。
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResult<CategoryTypeDTO> findAll(int pageNum, int pageSize);

    /**
     * 按主键查询分类类型。
     *
     * @param id 主键
     * @return 分类类型 DTO
     */
    Optional<CategoryTypeDTO> findById(Long id);
}
