/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.category.gateway
 * @FileName: CategoryGateway.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:10
 * @Description: 分类领域网关文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.family.domain.category.gateway;

import top.egon.familyaibutler.family.domain.category.model.aggregate.Category;
import top.egon.familyaibutler.family.domain.common.model.valueobject.PageSlice;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.category.gateway
 * @ClassName: CategoryGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:10
 * @Description: 分类领域网关
 * @Version: 1.0
 */
public interface CategoryGateway {

    /**
     * 保存分类。
     *
     * @param category 分类领域模型
     * @return 保存后的分类
     */
    Category save(Category category);

    /**
     * 修改分类。
     *
     * @param category 分类领域模型
     * @return 修改后的分类
     */
    Category update(Category category);

    /**
     * 删除分类。
     *
     * @param id 主键
     * @return true 表示删除成功
     */
    Boolean delete(Long id);

    /**
     * 分页查询分类。
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 分类分页切片
     */
    PageSlice<Category> findAll(int pageNum, int pageSize);

    /**
     * 按主键查询分类。
     *
     * @param id 主键
     * @return 分类领域模型
     */
    Optional<Category> findById(Long id);
}
