/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.rbac.gateway
 * @FileName: RbacGateway.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:05
 * @Description: RBAC 网关文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.rbac.gateway;

import top.egon.familyaibutler.uaa.domain.rbac.model.aggregate.PermissionResource;
import top.egon.familyaibutler.uaa.domain.rbac.model.aggregate.Role;

import java.util.List;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.rbac.gateway
 * @ClassName: RbacGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:05
 * @Description: RBAC 网关
 * @Version: 1.0
 */
public interface RbacGateway {

    /**
     * 保存角色。
     *
     * @param role 角色
     * @return 角色
     */
    Role saveRole(Role role);

    /**
     * 保存权限资源。
     *
     * @param resource 权限资源
     * @return 权限资源
     */
    PermissionResource saveResource(PermissionResource resource);

    /**
     * 按角色编码查询。
     *
     * @param roleCode 角色编码
     * @return 角色
     */
    Optional<Role> findRoleByCode(String roleCode);

    /**
     * 按资源编码查询。
     *
     * @param resourceCode 资源编码
     * @return 权限资源
     */
    Optional<PermissionResource> findResourceByCode(String resourceCode);

    /**
     * 绑定角色和权限资源。
     *
     * @param roleCode     角色编码
     * @param resourceCode 资源编码
     */
    void bindRoleResource(String roleCode, String resourceCode);

    /**
     * 绑定账号和角色。
     *
     * @param accountId 账号 ID
     * @param roleCode  角色编码
     */
    void bindAccountRole(String accountId, String roleCode);

    /**
     * 查询账号拥有的权限资源。
     *
     * @param accountId 账号 ID
     * @return 权限资源列表
     */
    List<PermissionResource> findResourcesByAccountId(String accountId);
}
