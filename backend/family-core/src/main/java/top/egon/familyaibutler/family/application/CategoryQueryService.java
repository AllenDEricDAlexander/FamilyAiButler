package top.egon.familyaibutler.family.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.entity.CategoryPo;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.service.CategoryService;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application
 * @ClassName: CategoryQueryService
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 分类查询应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class CategoryQueryService {
    private final CategoryService categoryService;

    /**
     * 分页查询分类。
     *
     * @param pageRequest 分页对象
     * @return 分页结果
     */
    public Page<CategoryPo> findAll(PageRequest pageRequest) {
        return categoryService.findAll(pageRequest);
    }

    /**
     * 按主键查询分类。
     *
     * @param id 主键
     * @return 分类数据对象
     */
    public Optional<CategoryPo> findById(Long id) {
        return categoryService.findById(id);
    }
}
