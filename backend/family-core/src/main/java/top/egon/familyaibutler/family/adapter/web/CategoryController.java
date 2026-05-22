package top.egon.familyaibutler.family.adapter.web;

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
import top.egon.openapi.console.annotation.DocBody;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocParamIn;
import top.egon.openapi.console.annotation.DocParameter;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;
import top.egon.openapi.console.annotation.DocTypeReference;
import top.egon.openapi.console.annotation.DocWrapper;

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
@DocService(groupId = "core", groupName = "家庭核心服务", serviceId = "family-core-category",
        serviceName = "分类管理相关接口", serviceDescription = "分类和分类类型管理接口", protocol = DocProtocol.HTTP)
@Slf4j
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryManage categoryService;
    private final CategoryTypeManage categoryTypeService;

    @GetMapping("/list")
    @DocOperation(summary = "获取所有分类", description = "获取所有分类",
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.GENERIC, ref = CategoryPageDataType.class)))
    public PageResult<CategoryDTO> getAllCategory() {
        return categoryService.findAll(0, 10);
    }

    @GetMapping("/category/{id}")
    @DocOperation(summary = "获取指定分类", description = "获取指定分类",
            request = @DocRequest(params = {
                    @DocParameter(name = "id", in = DocParamIn.PATH, description = "分类 ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.LONG), example = "1")
            }),
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = CategoryDTO.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<CategoryDTO> getCategoryById(@PathVariable(value = "id") Long id) {
        return Result.success(categoryService.findById(id).orElse(null));
    }


    @PostMapping(value = "/category")
    @DocOperation(summary = "添加分类", description = "添加分类",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = CategoryDTO.class))),
            response = @DocResponse(description = "添加成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = CategoryDTO.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<CategoryDTO> saveCategory(@RequestBody @Valid CategoryDTO category) {
        return Result.success(categoryService.save(category));
    }

    @PutMapping(value = "/category")
    @DocOperation(summary = "更新分类", description = "更新分类",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = CategoryDTO.class))),
            response = @DocResponse(description = "更新成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = CategoryDTO.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<CategoryDTO> updateCategory(@RequestBody @Valid CategoryDTO category) {
        return Result.success(categoryService.update(category));
    }

    @DeleteMapping(value = "/category/{id}")
    @DocOperation(summary = "删除分类", description = "删除分类",
            request = @DocRequest(params = {
                    @DocParameter(name = "id", in = DocParamIn.PATH, description = "分类 ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.LONG), example = "1")
            }),
            response = @DocResponse(description = "删除成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<Boolean> deleteCategory(@PathVariable(value = "id") Long id) {
        return Result.success(categoryService.delete(id));
    }

    @GetMapping("/type/list")
    @DocOperation(summary = "获取所有分类类型", description = "获取所有分类类型",
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.GENERIC, ref = CategoryTypePageDataType.class)))
    public PageResult<CategoryTypeDTO> getAllType() {
        return categoryTypeService.findAll(0, 10);
    }

    @GetMapping("/category/type/{id}")
    @DocOperation(summary = "获取指定分类类型", description = "获取指定分类类型",
            request = @DocRequest(params = {
                    @DocParameter(name = "id", in = DocParamIn.PATH, description = "分类类型 ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.LONG), example = "1")
            }),
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = CategoryTypeDTO.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<CategoryTypeDTO> getUser(@PathVariable(value = "id") Long id) {
        return Result.success(categoryTypeService.findById(id).orElse(null));
    }

    @PostMapping(value = "/category/type")
    @DocOperation(summary = "添加分类类型", description = "添加分类类型",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = CategoryTypeDTO.class))),
            response = @DocResponse(description = "添加成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = CategoryTypeDTO.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<CategoryTypeDTO> saveUser(@RequestBody @Valid CategoryTypeDTO category) {
        return Result.success(categoryTypeService.save(category));
    }

    @PutMapping(value = "/category/type")
    @DocOperation(summary = "更新分类类型", description = "更新分类类型",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = CategoryTypeDTO.class))),
            response = @DocResponse(description = "更新成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = CategoryTypeDTO.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<CategoryTypeDTO> updateUser(@RequestBody @Valid CategoryTypeDTO category) {
        return Result.success(categoryTypeService.update(category));
    }

    @DeleteMapping(value = "/category/type/{id}")
    @DocOperation(summary = "删除分类类型", description = "删除分类类型",
            request = @DocRequest(params = {
                    @DocParameter(name = "id", in = DocParamIn.PATH, description = "分类类型 ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.LONG), example = "1")
            }),
            response = @DocResponse(description = "删除成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<Boolean> deleteUser(@PathVariable(value = "id") Long id) {
        return Result.success(categoryTypeService.delete(id));
    }

    public static final class CategoryPageDataType extends DocTypeReference<PageResult<CategoryDTO>> {
    }

    public static final class CategoryTypePageDataType extends DocTypeReference<PageResult<CategoryTypeDTO>> {
    }

}
