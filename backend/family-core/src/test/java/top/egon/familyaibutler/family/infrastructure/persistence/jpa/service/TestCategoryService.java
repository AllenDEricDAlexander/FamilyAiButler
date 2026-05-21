package top.egon.familyaibutler.family.infrastructure.persistence.jpa.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.entity.CategoryPo;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.repository.CategoryRepository;
import top.egon.familyaibutler.family.infrastructure.persistence.jpa.service.impl.CategoryServiceImpl;

import java.util.Date;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.persistence.jpa.service
 * @ClassName: TestCategoryService
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-04Day-18:21
 * @Description: TestCategoryService
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class TestCategoryService {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    void testFindById() {
        Long id = 1L;
        CategoryPo build = CategoryPo.builder()
                .id(id)
                .name("house")
                .description("test")
                .parentId(0L)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
        Mockito.when(categoryRepository.findById(id)).thenReturn(Optional.of(build));
        Optional<CategoryPo> byId = categoryService.findById(id);
        Assertions.assertTrue(byId.isPresent());
        Assertions.assertEquals(id, byId.get().getId());
    }

}
