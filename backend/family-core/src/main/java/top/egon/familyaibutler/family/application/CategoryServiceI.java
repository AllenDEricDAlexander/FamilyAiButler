package top.egon.familyaibutler.family.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.entity.CategoryPo;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application
 * @ClassName: CategoryServiceI
 * @Author: atluofu
 * @CreateTime: 2026-05-20 11:05
 * @Description: 分类 COLA 客户端接口
 * @Version: 1.0
 */
public interface CategoryServiceI {

    /**
     * 新增分类。
     *
     * @param category 分类数据对象
     * @return 保存后的分类
     */
    CategoryPo save(CategoryPo category);

    /**
     * 修改分类。
     *
     * @param category 分类数据对象
     * @return 保存后的分类
     */
    CategoryPo update(CategoryPo category);

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
     * @param pageRequest 分页对象
     * @return 分页结果
     */
    Page<CategoryPo> findAll(PageRequest pageRequest);

    /**
     * 按主键查询分类。
     *
     * @param id 主键
     * @return 分类数据对象
     */
    Optional<CategoryPo> findById(Long id);
}
