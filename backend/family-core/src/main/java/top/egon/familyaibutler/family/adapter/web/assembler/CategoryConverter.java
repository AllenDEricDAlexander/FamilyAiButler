package top.egon.familyaibutler.family.adapter.web.assembler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import top.egon.familyaibutler.common.utils.BaseConverter;
import top.egon.familyaibutler.family.application.result.CategoryDTO;
import top.egon.familyaibutler.family.domain.category.model.aggregate.Category;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.adapter.web.assembler
 * @ClassName: UserConverter
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-22Day-20:45
 * @Description: Converter CategoryDTO 2 Category
 * @Version: 1.0
 */
@Mapper
public interface CategoryConverter extends BaseConverter<Category, CategoryDTO> {

    CategoryConverter INSTANCE = Mappers.getMapper(CategoryConverter.class);

    @Override
    @Mapping(source = "categoryName", target = "name")
    Category toSource(CategoryDTO target);

    @Override
    List<Category> toSourceList(List<CategoryDTO> targetList);

    @Override
    @Mapping(source = "name", target = "categoryName")
    CategoryDTO toTarget(Category source);

    @Override
    List<CategoryDTO> toTargetList(List<Category> sourceList);
}
