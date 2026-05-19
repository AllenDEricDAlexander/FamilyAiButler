package top.egon.familyaibutler.family.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import top.egon.familyaibutler.family.po.CategoryPo;
import top.egon.familyaibutler.family.po.CategoryTypePo;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.service
 * @ClassName: CategoryService
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-04Day-11:47
 * @Description: CategoryService
 * @Version: 1.0
 */
public interface CategoryTypeService {

    Page<CategoryTypePo> findAll(PageRequest pageRequest);

    Optional<CategoryTypePo> findById(Long id);

    CategoryTypePo save(CategoryTypePo categoryTypePo);

    CategoryTypePo update(CategoryTypePo categoryTypePo);

    Boolean delete(Long id);
}
