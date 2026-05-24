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
import top.egon.openapi.console.annotation.DocBody;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocParam;
import top.egon.openapi.console.annotation.DocParamIn;
import top.egon.openapi.console.annotation.DocParameter;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;
import top.egon.openapi.console.annotation.DocWrapper;

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
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-rbac",
        serviceName = "RBAC 服务", serviceDescription = "角色、权限资源和用户权限查询能力", protocol = DocProtocol.HTTP)
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
    @DocOperation(summary = "新增或更新角色", description = "按角色编码新增或更新角色",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = UpsertRoleRequest.class))),
            response = @DocResponse(description = "保存成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = RoleResponse.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
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
    @DocOperation(summary = "新增或更新权限资源", description = "按资源编码新增或更新权限资源",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = UpsertPermissionResourceRequest.class))),
            response = @DocResponse(description = "保存成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = PermissionResourceResponse.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
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
    @DocOperation(summary = "绑定角色和权限资源", description = "为角色绑定权限资源",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = BindRoleResourceRequest.class))),
            response = @DocResponse(description = "绑定成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
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
    @DocOperation(summary = "绑定账号和角色", description = "为账号绑定角色",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = BindAccountRoleRequest.class))),
            response = @DocResponse(description = "绑定成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
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
    @DocOperation(summary = "查询用户权限资源", description = "按账号和可选资源类型查询用户权限资源",
            request = @DocRequest(params = {
                    @DocParameter(name = "accountId", in = DocParamIn.PATH, description = "账号 ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.STRING), example = "account-001"),
                    @DocParameter(name = "resourceType", in = DocParamIn.QUERY, description = "资源类型",
                            dataType = @DocDataType(kind = DocDataKind.ENUM, type = PermissionResourceType.class), example = "API")
            }),
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = UserPermissionResponse.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<UserPermissionResponse> listUserPermissions(@PathVariable @DocParam(description = "账号 ID", required = true) String accountId,
                                                              @RequestParam(required = false) @DocParam(description = "资源类型") PermissionResourceType resourceType) {
        return Result.success(rbacService.listUserPermissions(new UserPermissionQuery(accountId, resourceType)));
    }
}
