/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.category.gateway
 * @FileName: CategoryTypeGateway.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:10
 * @Description: 分类类型领域网关文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.family.domain.category.gateway;

import top.egon.familyaibutler.family.domain.category.model.entity.CategoryType;
import top.egon.familyaibutler.family.domain.common.model.valueobject.PageSlice;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.category.gateway
 * @ClassName: CategoryTypeGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:10
 * @Description: 分类类型领域网关
 * @Version: 1.0
 */
public interface CategoryTypeGateway {

    /**
     * 保存分类类型。
     *
     * @param categoryType 分类类型领域实体
     * @return 保存后的分类类型
     */
    CategoryType save(CategoryType categoryType);

    /**
     * 修改分类类型。
     *
     * @param categoryType 分类类型领域实体
     * @return 修改后的分类类型
     */
    CategoryType update(CategoryType categoryType);

    /**
     * 删除分类类型。
     *
     * @param id 主键
     * @return true 表示删除成功
     */
    Boolean delete(Long id);

    /**
     * 分页查询分类类型。
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @return 分类类型分页切片
     */
    PageSlice<CategoryType> findAll(int pageNum, int pageSize);

    /**
     * 按主键查询分类类型。
     *
     * @param id 主键
     * @return 分类类型领域实体
     */
    Optional<CategoryType> findById(Long id);
}
