package top.egon.familyaibutler.family.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.PageResult;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.family.application.manage.CategoryManage;
import top.egon.familyaibutler.family.application.manage.CategoryTypeManage;
import top.egon.familyaibutler.family.application.result.CategoryDTO;
import top.egon.familyaibutler.family.application.result.CategoryTypeDTO;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.adapter.web
 * @ClassName: CategoryController
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-04Day-11:55
 * @Description: CategoryController
 * @Version: 1.0
 */
@RestController
@RequestMapping("/category")
@Validated
@Tag(name = "分类管理相关接口")
@Slf4j
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryManage categoryService;
    private final CategoryTypeManage categoryTypeService;

    @GetMapping("/list")
    @Operation(summary = "获取所有分类", description = "获取所有分类")
    public PageResult<CategoryDTO> getAllCategory() {
        return categoryService.findAll(0, 10);
    }

    @GetMapping("/category/{id}")
    @Operation(summary = "获取指定分类", description = "获取指定分类")
    public Result<CategoryDTO> getCategoryById(@PathVariable(value = "id") Long id) {
        return Result.success(categoryService.findById(id).orElse(null));
    }


    @PostMapping(value = "/category")
    @Operation(summary = "添加分类", description = "添加分类")
    public Result<CategoryDTO> saveCategory(@RequestBody @Valid CategoryDTO category) {
        return Result.success(categoryService.save(category));
    }

    @PutMapping(value = "/category")
    @Operation(summary = "更新分类", description = "更新分类")
    public Result<CategoryDTO> updateCategory(@RequestBody @Valid CategoryDTO category) {
        return Result.success(categoryService.update(category));
    }

    @DeleteMapping(value = "/category/{id}")
    @Operation(summary = "删除分类", description = "删除分类")
    public Result<Boolean> deleteCategory(@PathVariable(value = "id") Long id) {
        return Result.success(categoryService.delete(id));
    }

    @GetMapping("/type/list")
    @Operation(summary = "获取所有分类类型", description = "获取所有分类类型")
    public PageResult<CategoryTypeDTO> getAllType() {
        return categoryTypeService.findAll(0, 10);
    }

    @GetMapping("/category/type/{id}")
    @Operation(summary = "获取指定分类类型", description = "获取指定分类类型")
    public Result<CategoryTypeDTO> getUser(@PathVariable(value = "id") Long id) {
        return Result.success(categoryTypeService.findById(id).orElse(null));
    }

    @PostMapping(value = "/category/type")
    @Operation(summary = "添加分类类型", description = "添加分类类型")
    public Result<CategoryTypeDTO> saveUser(@RequestBody @Valid CategoryTypeDTO category) {
        return Result.success(categoryTypeService.save(category));
    }

    @PutMapping(value = "/category/type")
    @Operation(summary = "更新分类类型", description = "更新分类类型")
    public Result<CategoryTypeDTO> updateUser(@RequestBody @Valid CategoryTypeDTO category) {
        return Result.success(categoryTypeService.update(category));
    }

    @DeleteMapping(value = "/category/type/{id}")
    @Operation(summary = "删除分类类型", description = "删除分类类型")
    public Result<Boolean> deleteUser(@PathVariable(value = "id") Long id) {
        return Result.success(categoryTypeService.delete(id));
    }

}
