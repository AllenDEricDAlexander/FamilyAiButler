/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @FileName: RbacController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:20
 * @Description: RBAC Web 控制器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.uaa.application.manage.RbacManage;
import top.egon.familyaibutler.uaa.facade.dto.rbac.BindAccountRoleRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.BindRoleResourceRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.PermissionResourceResponse;
import top.egon.familyaibutler.uaa.facade.dto.rbac.RoleResponse;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UpsertPermissionResourceRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UpsertRoleRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UserPermissionQuery;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UserPermissionResponse;
import top.egon.familyaibutler.uaa.facade.enums.PermissionResourceType;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @ClassName: RbacController
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:20
 * @Description: RBAC Web 控制器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/rbac")
public class RbacController {
    private final RbacManage rbacService;

    /**
     * 创建 RBAC Web 控制器。
     *
     * @param rbacService RBAC 应用服务
     */
    public RbacController(RbacManage rbacService) {
        this.rbacService = rbacService;
    }

    /**
     * 新增或更新角色。
     *
     * @param request 角色请求
     * @return 角色响应
     */
    @PostMapping("/roles")
    public Result<RoleResponse> upsertRole(@RequestBody @Valid UpsertRoleRequest request) {
        return Result.success(rbacService.upsertRole(request));
    }

    /**
     * 新增或更新权限资源。
     *
     * @param request 权限资源请求
     * @return 权限资源响应
     */
    @PostMapping("/resources")
    public Result<PermissionResourceResponse> upsertResource(@RequestBody @Valid UpsertPermissionResourceRequest request) {
        return Result.success(rbacService.upsertResource(request));
    }

    /**
     * 绑定角色和权限资源。
     *
     * @param request 绑定请求
     * @return true 表示绑定成功
     */
    @PostMapping("/role-resources")
    public Result<Boolean> bindRoleResource(@RequestBody @Valid BindRoleResourceRequest request) {
        return Result.success(rbacService.bindRoleResource(request));
    }

    /**
     * 绑定账号和角色。
     *
     * @param request 绑定请求
     * @return true 表示绑定成功
     */
    @PostMapping("/account-roles")
    public Result<Boolean> bindAccountRole(@RequestBody @Valid BindAccountRoleRequest request) {
        return Result.success(rbacService.bindAccountRole(request));
    }

    /**
     * 查询用户权限资源。
     *
     * @param accountId    账号 ID
     * @param resourceType 资源类型
     * @return 用户权限响应
     */
    @GetMapping("/accounts/{accountId}/permissions")
    public Result<UserPermissionResponse> listUserPermissions(@PathVariable String accountId,
                                                              @RequestParam(required = false) PermissionResourceType resourceType) {
        return Result.success(rbacService.listUserPermissions(new UserPermissionQuery(accountId, resourceType)));
    }
}
