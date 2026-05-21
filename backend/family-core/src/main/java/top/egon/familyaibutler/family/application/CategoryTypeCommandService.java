package top.egon.familyaibutler.family.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.entity.CategoryTypePo;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.service.CategoryTypeService;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application
 * @ClassName: CategoryTypeCommandService
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 分类类型命令应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class CategoryTypeCommandService {
    private final CategoryTypeService categoryTypeService;

    /**
     * 新增分类类型。
     *
     * @param categoryType 分类类型数据对象
     * @return 保存后的分类类型
     */
    public CategoryTypePo save(CategoryTypePo categoryType) {
        return categoryTypeService.save(categoryType);
    }

    /**
     * 修改分类类型。
     *
     * @param categoryType 分类类型数据对象
     * @return 保存后的分类类型
     */
    public CategoryTypePo update(CategoryTypePo categoryType) {
        return categoryTypeService.update(categoryType);
    }

    /**
     * 删除分类类型。
     *
     * @param id 主键
     * @return 是否删除成功
     */
    public Boolean delete(Long id) {
        return categoryTypeService.delete(id);
    }
}
