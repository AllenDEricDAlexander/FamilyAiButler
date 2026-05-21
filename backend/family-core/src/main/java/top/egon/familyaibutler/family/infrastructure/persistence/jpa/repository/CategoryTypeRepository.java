package top.egon.familyaibutler.family.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.dataobject.CategoryTypePo;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.persistence.jpa.repository
 * @ClassName: CategoryRepository
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-04Day-11:45
 * @Description: CategoryTypeRepository
 * @Version: 1.0
 */
public interface CategoryTypeRepository extends JpaRepository<CategoryTypePo, Long>, JpaSpecificationExecutor<CategoryTypePo> {
}