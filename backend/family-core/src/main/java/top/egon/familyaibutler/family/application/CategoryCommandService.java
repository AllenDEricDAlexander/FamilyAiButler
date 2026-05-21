package top.egon.familyaibutler.family.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.entity.CategoryPo;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.service.CategoryService;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application
 * @ClassName: CategoryCommandService
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 分类命令应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class CategoryCommandService {
    private final CategoryService categoryService;

    /**
     * 新增分类。
     *
     * @param category 分类数据对象
     * @return 保存后的分类
     */
    public CategoryPo save(CategoryPo category) {
        return categoryService.save(category);
    }

    /**
     * 修改分类。
     *
     * @param category 分类数据对象
     * @return 保存后的分类
     */
    public CategoryPo update(CategoryPo category) {
        return categoryService.update(category);
    }

    /**
     * 删除分类。
     *
     * @param id 主键
     * @return 是否删除成功
     */
    public Boolean delete(Long id) {
        return categoryService.delete(id);
    }
}
