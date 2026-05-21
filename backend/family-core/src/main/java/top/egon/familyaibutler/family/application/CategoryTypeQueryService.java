package top.egon.familyaibutler.family.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.entity.CategoryTypePo;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.service.CategoryTypeService;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application
 * @ClassName: CategoryTypeQueryService
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 分类类型查询应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class CategoryTypeQueryService {
    private final CategoryTypeService categoryTypeService;

    /**
     * 分页查询分类类型。
     *
     * @param pageRequest 分页对象
     * @return 分页结果
     */
    public Page<CategoryTypePo> findAll(PageRequest pageRequest) {
        return categoryTypeService.findAll(pageRequest);
    }

    /**
     * 按主键查询分类类型。
     *
     * @param id 主键
     * @return 分类类型数据对象
     */
    public Optional<CategoryTypePo> findById(Long id) {
        return categoryTypeService.findById(id);
    }
}
