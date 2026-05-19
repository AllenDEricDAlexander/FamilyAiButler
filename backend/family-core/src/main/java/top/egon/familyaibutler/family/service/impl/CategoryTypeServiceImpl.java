package top.egon.familyaibutler.family.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.po.CategoryTypePo;
import top.egon.familyaibutler.family.repository.CategoryTypeRepository;
import top.egon.familyaibutler.family.service.CategoryTypeService;

import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.service.impl
 * @ClassName: CategoryServiceImpl
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-04Day-11:48
 * @Description: CategoryServiceImpl
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class CategoryTypeServiceImpl implements CategoryTypeService {

    private final CategoryTypeRepository categoryTypeRepository;

    @Override
    public Page<CategoryTypePo> findAll(PageRequest pageRequest) {
        return categoryTypeRepository.findAll(pageRequest);
    }

    @Override
    public Optional<CategoryTypePo> findById(Long id) {
        return categoryTypeRepository.findById(id);
    }

    @Override
    public CategoryTypePo save(CategoryTypePo categoryPo) {
        return categoryTypeRepository.saveAndFlush(categoryPo);
    }

    @Override
    public CategoryTypePo update(CategoryTypePo categoryPo) {
        return categoryTypeRepository.saveAndFlush(categoryPo);
    }

    @Override
    public Boolean delete(Long id) {
        categoryTypeRepository.deleteById(id);
        return true;
    }
}