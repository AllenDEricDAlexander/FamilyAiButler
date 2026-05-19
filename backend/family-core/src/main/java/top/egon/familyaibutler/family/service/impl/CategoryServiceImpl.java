package top.egon.familyaibutler.family.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.family.po.CategoryPo;
import top.egon.familyaibutler.family.repository.CategoryRepository;
import top.egon.familyaibutler.family.service.CategoryService;

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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Page<CategoryPo> findAll(PageRequest pageRequest) {
        return categoryRepository.findAll(pageRequest);
    }

    @Override
    public Optional<CategoryPo> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public CategoryPo save(CategoryPo categoryPo) {
        return categoryRepository.saveAndFlush(categoryPo);
    }

    @Override
    public CategoryPo update(CategoryPo categoryPo) {
        return categoryRepository.saveAndFlush(categoryPo);
    }

    @Override
    public Boolean delete(Long id) {
        categoryRepository.deleteById(id);
        return true;
    }
}