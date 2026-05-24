/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage.impl
 * @FileName: RbacManageImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:20
 * @Description: RBAC 应用服务实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application.manage.impl;

import org.springframework.stereotype.Service;
import top.egon.familyaibutler.uaa.application.manage.RbacManage;
import top.egon.familyaibutler.uaa.domain.rbac.gateway.RbacGateway;
import top.egon.familyaibutler.uaa.domain.rbac.model.aggregate.PermissionResource;
import top.egon.familyaibutler.uaa.domain.rbac.model.aggregate.Role;
import top.egon.familyaibutler.uaa.facade.dto.rbac.BindAccountRoleRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.BindRoleResourceRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.PermissionResourceResponse;
import top.egon.familyaibutler.uaa.facade.dto.rbac.RoleResponse;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UpsertPermissionResourceRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UpsertRoleRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UserPermissionQuery;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UserPermissionResponse;
import top.egon.familyaibutler.uaa.facade.enums.PermissionResourceType;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage.impl
 * @ClassName: RbacManageImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:20
 * @Description: RBAC 应用服务实现
 * @Version: 1.0
 */
@Service
public class RbacManageImpl implements RbacManage {
    /**
     * RBAC 网关。
     */
    private final RbacGateway rbacGateway;

    /**
     * 创建 RBAC 应用服务实现。
     *
     * @param rbacGateway RBAC 网关
     */
    public RbacManageImpl(RbacGateway rbacGateway) {
        this.rbacGateway = rbacGateway;
    }

    /**
     * 新增或更新角色。
     *
     * @param request 角色请求
     * @return 角色响应
     */
    @Override
    public RoleResponse upsertRole(UpsertRoleRequest request) {
        return toRoleResponse(rbacGateway.saveRole(Role.active(request.roleCode(), request.roleName())));
    }

    /**
     * 新增或更新权限资源。
     *
     * @param request 权限资源请求
     * @return 权限资源响应
     */
    @Override
    public PermissionResourceResponse upsertResource(UpsertPermissionResourceRequest request) {
        PermissionResource resource = PermissionResource.active(request.resourceCode(), request.resourceName(),
                top.egon.familyaibutler.uaa.domain.rbac.model.enums.PermissionResourceType.valueOf(request.resourceType().name()),
                request.resourceService(), request.pathPattern(), request.action());
        return toResourceResponse(rbacGateway.saveResource(resource));
    }

    /**
     * 绑定角色和权限资源。
     *
     * @param request 绑定请求
     * @return true 表示绑定成功
     */
    @Override
    public boolean bindRoleResource(BindRoleResourceRequest request) {
        rbacGateway.findRoleByCode(request.roleCode()).orElseThrow(() -> new IllegalArgumentException("角色不存在"));
        rbacGateway.findResourceByCode(request.resourceCode()).orElseThrow(() -> new IllegalArgumentException("权限资源不存在"));
        rbacGateway.bindRoleResource(request.roleCode(), request.resourceCode());
        return true;
    }

    /**
     * 绑定账号和角色。
     *
     * @param request 绑定请求
     * @return true 表示绑定成功
     */
    @Override
    public boolean bindAccountRole(BindAccountRoleRequest request) {
        rbacGateway.findRoleByCode(request.roleCode()).orElseThrow(() -> new IllegalArgumentException("角色不存在"));
        rbacGateway.bindAccountRole(request.accountId(), request.roleCode());
        return true;
    }

    /**
     * 查询用户权限资源。
     *
     * @param query 查询条件
     * @return 用户权限响应
     */
    @Override
    public UserPermissionResponse listUserPermissions(UserPermissionQuery query) {
        PermissionResourceType resourceType = query.resourceType();
        Set<String> resourceCodes = rbacGateway.findResourcesByAccountId(query.accountId()).stream()
                .filter(resource -> resourceType == null || resource.getResourceType().name().equals(resourceType.name()))
                .map(PermissionResource::getResourceCode)
                .collect(Collectors.toSet());
        return new UserPermissionResponse(query.accountId(), resourceType, resourceCodes);
    }

    /**
     * 转换角色响应。
     *
     * @param role 角色
     * @return 角色响应
     */
    private RoleResponse toRoleResponse(Role role) {
        return new RoleResponse(role.getRoleCode(), role.getRoleName(), role.getStatus().name());
    }

    /**
     * 转换权限资源响应。
     *
     * @param resource 权限资源
     * @return 权限资源响应
     */
    private PermissionResourceResponse toResourceResponse(PermissionResource resource) {
        return new PermissionResourceResponse(resource.getResourceCode(), resource.getResourceName(),
                PermissionResourceType.valueOf(resource.getResourceType().name()), resource.getResourceService(),
                resource.getPathPattern(), resource.getAction(), resource.getStatus().name());
    }
}
