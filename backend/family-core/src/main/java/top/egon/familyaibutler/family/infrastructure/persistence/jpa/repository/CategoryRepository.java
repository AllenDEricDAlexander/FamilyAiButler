package top.egon.familyaibutler.family.infrastructure.persistence.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.entity.CategoryPo;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.persistence.jpa.repository
 * @ClassName: CategoryRepository
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-04Day-11:45
 * @Description: CategoryRepository
 * @Version: 1.0
 */
public interface CategoryRepository extends JpaRepository<CategoryPo, Long>, JpaSpecificationExecutor<CategoryPo> {
}