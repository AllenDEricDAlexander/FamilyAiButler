/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade
 * @FileName: RbacFacade.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:00
 * @Description: RBAC facade 契约文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade;

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
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade
 * @ClassName: RbacFacade
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:00
 * @Description: RBAC facade 契约
 * @Version: 1.0
 */
public interface RbacFacade {

    /**
     * 新增或更新角色。
     *
     * @param request 角色请求
     * @return 角色响应
     */
    RoleResponse upsertRole(UpsertRoleRequest request);

    /**
     * 新增或更新权限资源。
     *
     * @param request 权限资源请求
     * @return 权限资源响应
     */
    PermissionResourceResponse upsertResource(UpsertPermissionResourceRequest request);

    /**
     * 绑定角色和权限资源。
     *
     * @param request 绑定请求
     * @return true 表示绑定成功
     */
    boolean bindRoleResource(BindRoleResourceRequest request);

    /**
     * 绑定账号和角色。
     *
     * @param request 绑定请求
     * @return true 表示绑定成功
     */
    boolean bindAccountRole(BindAccountRoleRequest request);

    /**
     * 查询用户权限资源。
     *
     * @param query 查询条件
     * @return 用户权限响应
     */
    UserPermissionResponse listUserPermissions(UserPermissionQuery query);
}
