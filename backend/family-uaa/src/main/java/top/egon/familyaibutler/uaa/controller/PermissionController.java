package top.egon.familyaibutler.uaa.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.uaa.po.PermissionPO;
import top.egon.familyaibutler.uaa.service.PermissionService;

import java.util.List;

 /**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa
 * @Author: atluofu
 * @CreateTime: 2025-08-13 00:48:18
 * @Description: (Permission)表控制层
 * @Version: 1.0
 */
@Tag(name = "PermissionController模块")
@RestController
@Slf4j
@Validated
@RequestMapping("permission")
public class PermissionController {
    /**
     * 服务对象
     */
    private final PermissionService permissionService;
    
    PermissionController(PermissionService permissionService){this.permissionService = permissionService;}

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param permission 查询实体
     * @return 所有数据
     */
    @GetMapping
    public Result selectAll(Page<PermissionPO> page, PermissionPO permission) {
        return Result.success(this.permissionService.page(page, new QueryWrapper<>(permission)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public Result selectOne(@PathVariable Long id) {
        return Result.success(this.permissionService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param permission 实体对象
     * @return 新增结果
     */
    @PostMapping
    public Result insert(@RequestBody PermissionPO permission) {
        return Result.success(this.permissionService.save(permission));
    }

    /**
     * 修改数据
     *
     * @param permission 实体对象
     * @return 修改结果
     */
    @PutMapping
    public Result update(@RequestBody PermissionPO permission) {
        return Result.success(this.permissionService.updateById(permission));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public Result delete(@RequestParam("idList") List<Long> idList) {
        return Result.success(this.permissionService.removeByIds(idList));
    }
}

