package top.egon.familyaibutler.family.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.entity.CategoryTypePo;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application
 * @ClassName: CategoryTypeServiceI
 * @Author: atluofu
 * @CreateTime: 2026-05-20 11:05
 * @Description: 分类类型 COLA 客户端接口
 * @Version: 1.0
 */
public interface CategoryTypeServiceI {

    /**
     * 新增分类类型。
     *
     * @param categoryType 分类类型数据对象
     * @return 保存后的分类类型
     */
    CategoryTypePo save(CategoryTypePo categoryType);

    /**
     * 修改分类类型。
     *
     * @param categoryType 分类类型数据对象
     * @return 保存后的分类类型
     */
    CategoryTypePo update(CategoryTypePo categoryType);

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
     * @param pageRequest 分页对象
     * @return 分页结果
     */
    Page<CategoryTypePo> findAll(PageRequest pageRequest);

    /**
     * 按主键查询分类类型。
     *
     * @param id 主键
     * @return 分类类型数据对象
     */
    Optional<CategoryTypePo> findById(Long id);
}
