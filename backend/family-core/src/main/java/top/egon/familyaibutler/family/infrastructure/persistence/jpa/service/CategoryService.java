package top.egon.familyaibutler.family.infrastructure.persistence.jpa.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.entity.CategoryPo;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.persistence.jpa.service
 * @ClassName: CategoryService
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-04Day-11:47
 * @Description: CategoryService
 * @Version: 1.0
 */
public interface CategoryService {

    Page<CategoryPo> findAll(PageRequest pageRequest);

    Optional<CategoryPo> findById(Long id);

    CategoryPo save(CategoryPo categoryPo);

    CategoryPo update(CategoryPo categoryPo);

    Boolean delete(Long id);
}
