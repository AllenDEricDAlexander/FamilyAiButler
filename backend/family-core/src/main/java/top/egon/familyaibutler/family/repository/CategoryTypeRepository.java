package top.egon.familyaibutler.family.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import top.egon.familyaibutler.family.po.CategoryTypePo;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.repository
 * @ClassName: CategoryRepository
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-04Day-11:45
 * @Description: CategoryTypeRepository
 * @Version: 1.0
 */
public interface CategoryTypeRepository extends JpaRepository<CategoryTypePo, Long>, JpaSpecificationExecutor<CategoryTypePo> {
}