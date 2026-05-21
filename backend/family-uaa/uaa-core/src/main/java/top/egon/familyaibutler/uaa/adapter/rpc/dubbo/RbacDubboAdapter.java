/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @FileName: RbacDubboAdapter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:20
 * @Description: RBAC facade 适配器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.rpc.dubbo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.uaa.application.manage.RbacManage;
import top.egon.familyaibutler.uaa.facade.RbacFacade;
import top.egon.familyaibutler.uaa.facade.dto.rbac.BindAccountRoleRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.BindRoleResourceRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.PermissionResourceResponse;
import top.egon.familyaibutler.uaa.facade.dto.rbac.RoleResponse;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UpsertPermissionResourceRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UpsertRoleRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UserPermissionQuery;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UserPermissionResponse;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @ClassName: RbacDubboAdapter
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:20
 * @Description: RBAC facade 适配器
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
public class RbacDubboAdapter implements RbacFacade {
    private final RbacManage rbacService;

    /**
     * 新增或更新角色。
     *
     * @param request 角色请求
     * @return 角色响应
     */
    @Override
    public RoleResponse upsertRole(UpsertRoleRequest request) {
        return rbacService.upsertRole(request);
    }

    /**
     * 新增或更新权限资源。
     *
     * @param request 权限资源请求
     * @return 权限资源响应
     */
    @Override
    public PermissionResourceResponse upsertResource(UpsertPermissionResourceRequest request) {
        return rbacService.upsertResource(request);
    }

    /**
     * 绑定角色和权限资源。
     *
     * @param request 绑定请求
     * @return true 表示绑定成功
     */
    @Override
    public boolean bindRoleResource(BindRoleResourceRequest request) {
        return rbacService.bindRoleResource(request);
    }

    /**
     * 绑定账号和角色。
     *
     * @param request 绑定请求
     * @return true 表示绑定成功
     */
    @Override
    public boolean bindAccountRole(BindAccountRoleRequest request) {
        return rbacService.bindAccountRole(request);
    }

    /**
     * 查询用户权限资源。
     *
     * @param query 查询条件
     * @return 用户权限响应
     */
    @Override
    public UserPermissionResponse listUserPermissions(UserPermissionQuery query) {
        return rbacService.listUserPermissions(query);
    }
}
