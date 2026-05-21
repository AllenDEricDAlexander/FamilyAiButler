/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @FileName: InMemoryRbacGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:10
 * @Description: 内存 RBAC 网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gatewayimpl;

import top.egon.familyaibutler.uaa.domain.gateway.RbacGateway;
import top.egon.familyaibutler.uaa.domain.model.aggregate.PermissionResource;
import top.egon.familyaibutler.uaa.domain.model.aggregate.Role;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @ClassName: InMemoryRbacGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:10
 * @Description: 内存 RBAC 网关实现
 * @Version: 1.0
 */
public class InMemoryRbacGatewayImpl implements RbacGateway {
    private final Map<String, Role> roles = new ConcurrentHashMap<>();
    private final Map<String, PermissionResource> resources = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> roleResources = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> accountRoles = new ConcurrentHashMap<>();

    /**
     * 保存角色。
     *
     * @param role 角色
     * @return 角色
     */
    @Override
    public Role saveRole(Role role) {
        roles.put(role.getRoleCode(), role);
        return role;
    }

    /**
     * 保存权限资源。
     *
     * @param resource 权限资源
     * @return 权限资源
     */
    @Override
    public PermissionResource saveResource(PermissionResource resource) {
        resources.put(resource.getResourceCode(), resource);
        return resource;
    }

    /**
     * 按角色编码查询。
     *
     * @param roleCode 角色编码
     * @return 角色
     */
    @Override
    public Optional<Role> findRoleByCode(String roleCode) {
        return Optional.ofNullable(roles.get(roleCode));
    }

    /**
     * 按资源编码查询。
     *
     * @param resourceCode 资源编码
     * @return 权限资源
     */
    @Override
    public Optional<PermissionResource> findResourceByCode(String resourceCode) {
        return Optional.ofNullable(resources.get(resourceCode));
    }

    /**
     * 绑定角色和权限资源。
     *
     * @param roleCode     角色编码
     * @param resourceCode 资源编码
     */
    @Override
    public void bindRoleResource(String roleCode, String resourceCode) {
        roleResources.computeIfAbsent(roleCode, key -> ConcurrentHashMap.newKeySet()).add(resourceCode);
    }

    /**
     * 绑定账号和角色。
     *
     * @param accountId 账号 ID
     * @param roleCode  角色编码
     */
    @Override
    public void bindAccountRole(String accountId, String roleCode) {
        accountRoles.computeIfAbsent(accountId, key -> ConcurrentHashMap.newKeySet()).add(roleCode);
    }

    /**
     * 查询账号拥有的权限资源。
     *
     * @param accountId 账号 ID
     * @return 权限资源列表
     */
    @Override
    public List<PermissionResource> findResourcesByAccountId(String accountId) {
        Set<String> resourceCodes = new HashSet<>();
        for (String roleCode : accountRoles.getOrDefault(accountId, Set.of())) {
            Role role = roles.get(roleCode);
            if (role != null && role.isActive()) {
                resourceCodes.addAll(roleResources.getOrDefault(roleCode, Set.of()));
            }
        }
        return resourceCodes.stream()
                .map(resources::get)
                .filter(resource -> resource != null && resource.isActive())
                .toList();
    }
}
