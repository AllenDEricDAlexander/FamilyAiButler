package top.egon.familyaibutler.family.application.executor.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.application.result.CategoryTypeDTO;
import top.egon.familyaibutler.family.domain.category.gateway.CategoryTypeGateway;
import top.egon.familyaibutler.family.domain.category.model.entity.CategoryType;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.executor.command
 * @ClassName: CategoryTypeCommandExe
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 分类类型命令应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class CategoryTypeCommandExe {
    private final CategoryTypeGateway categoryTypeGateway;

    /**
     * 新增分类类型。
     *
     * @param categoryType 分类类型 DTO
     * @return 保存后的分类类型
     */
    public CategoryTypeDTO save(CategoryTypeDTO categoryType) {
        return toDto(categoryTypeGateway.save(toDomain(categoryType)));
    }

    /**
     * 修改分类类型。
     *
     * @param categoryType 分类类型 DTO
     * @return 保存后的分类类型
     */
    public CategoryTypeDTO update(CategoryTypeDTO categoryType) {
        return toDto(categoryTypeGateway.update(toDomain(categoryType)));
    }

    /**
     * 删除分类类型。
     *
     * @param id 主键
     * @return 是否删除成功
     */
    public Boolean delete(Long id) {
        return categoryTypeGateway.delete(id);
    }

    /**
     * DTO 转领域实体。
     *
     * @param categoryTypeDTO 分类类型 DTO
     * @return 分类类型领域实体
     */
    private CategoryType toDomain(CategoryTypeDTO categoryTypeDTO) {
        CategoryType categoryType = new CategoryType();
        categoryType.setId(categoryTypeDTO.getId());
        categoryType.setTypeName(categoryTypeDTO.getTypeName());
        categoryType.setDescription(categoryTypeDTO.getDescription());
        categoryType.setCreateTime(categoryTypeDTO.getCreateTime());
        categoryType.setUpdateTime(categoryTypeDTO.getUpdateTime());
        return categoryType;
    }

    /**
     * 领域实体转 DTO。
     *
     * @param categoryType 分类类型领域实体
     * @return 分类类型 DTO
     */
    private CategoryTypeDTO toDto(CategoryType categoryType) {
        return CategoryTypeDTO.builder()
                .id(categoryType.getId())
                .typeName(categoryType.getTypeName())
                .description(categoryType.getDescription())
                .createTime(categoryType.getCreateTime())
                .updateTime(categoryType.getUpdateTime())
                .build();
    }
}
