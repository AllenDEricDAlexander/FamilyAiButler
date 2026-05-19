package top.egon.familyaibutler.family.po.convetor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import top.egon.familyaibutler.common.utils.BaseConverter;
import top.egon.familyaibutler.family.po.CategoryDTO;
import top.egon.familyaibutler.family.po.CategoryPo;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: PACKAGE_NAME
 * @ClassName: UserConverter
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-22Day-20:45
 * @Description: Converter CategoryDTO 2 CategoryPo
 * @Version: 1.0
 */
@Mapper
public interface CategoryConverter extends BaseConverter<CategoryPo, CategoryDTO> {

    CategoryConverter INSTANCE = Mappers.getMapper(CategoryConverter.class);

    @Override
    @Mapping(source = "categoryName", target = "name")
    CategoryPo toSource(CategoryDTO target);

    @Override
    List<CategoryPo> toSourceList(List<CategoryDTO> targetList);

    @Override
    @Mapping(source = "name", target = "categoryName")
    CategoryDTO toTarget(CategoryPo source);

    @Override
    List<CategoryDTO> toTargetList(List<CategoryPo> sourceList);
}
