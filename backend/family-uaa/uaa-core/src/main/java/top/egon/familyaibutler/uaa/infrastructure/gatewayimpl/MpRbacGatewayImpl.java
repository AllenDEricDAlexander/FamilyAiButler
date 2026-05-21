/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @FileName: MpRbacGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:15
 * @Description: MyBatis Plus RBAC 网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gatewayimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import top.egon.familyaibutler.uaa.domain.gateway.RbacGateway;
import top.egon.familyaibutler.uaa.domain.model.aggregate.PermissionResource;
import top.egon.familyaibutler.uaa.domain.model.aggregate.Role;
import top.egon.familyaibutler.uaa.domain.model.enums.PermissionResourceType;
import top.egon.familyaibutler.uaa.domain.model.enums.RbacStatus;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.AccountRolePO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.PermissionResourcePO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.RolePO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.RoleResourcePO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper.AccountRoleMapper;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper.PermissionResourceMapper;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper.RoleMapper;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper.RoleResourceMapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @ClassName: MpRbacGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:15
 * @Description: MyBatis Plus RBAC 网关实现
 * @Version: 1.0
 */
@Repository
public class MpRbacGatewayImpl implements RbacGateway {
    private final RoleMapper roleMapper;
    private final PermissionResourceMapper permissionResourceMapper;
    private final RoleResourceMapper roleResourceMapper;
    private final AccountRoleMapper accountRoleMapper;

    /**
     * 创建 MyBatis Plus RBAC 网关实现。
     *
     * @param roleMapper               角色 Mapper
     * @param permissionResourceMapper 权限资源 Mapper
     * @param roleResourceMapper       角色权限资源关系 Mapper
     * @param accountRoleMapper        账号角色关系 Mapper
     */
    public MpRbacGatewayImpl(RoleMapper roleMapper, PermissionResourceMapper permissionResourceMapper,
                             RoleResourceMapper roleResourceMapper, AccountRoleMapper accountRoleMapper) {
        this.roleMapper = roleMapper;
        this.permissionResourceMapper = permissionResourceMapper;
        this.roleResourceMapper = roleResourceMapper;
        this.accountRoleMapper = accountRoleMapper;
    }

    /**
     * 保存角色。
     *
     * @param role 角色
     * @return 角色
     */
    @Override
    public Role saveRole(Role role) {
        RolePO rolePO = toRolePO(role);
        if (roleMapper.selectById(role.getRoleCode()) == null) {
            roleMapper.insert(rolePO);
        } else {
            roleMapper.updateById(rolePO);
        }
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
        PermissionResourcePO resourcePO = toPermissionResourcePO(resource);
        if (permissionResourceMapper.selectById(resource.getResourceCode()) == null) {
            permissionResourceMapper.insert(resourcePO);
        } else {
            permissionResourceMapper.updateById(resourcePO);
        }
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
        return Optional.ofNullable(roleMapper.selectById(roleCode)).map(this::toRole);
    }

    /**
     * 按资源编码查询。
     *
     * @param resourceCode 资源编码
     * @return 权限资源
     */
    @Override
    public Optional<PermissionResource> findResourceByCode(String resourceCode) {
        return Optional.ofNullable(permissionResourceMapper.selectById(resourceCode)).map(this::toPermissionResource);
    }

    /**
     * 绑定角色和权限资源。
     *
     * @param roleCode     角色编码
     * @param resourceCode 资源编码
     */
    @Override
    public void bindRoleResource(String roleCode, String resourceCode) {
        String relationId = roleResourceId(roleCode, resourceCode);
        if (roleResourceMapper.selectById(relationId) == null) {
            roleResourceMapper.insert(RoleResourcePO.builder()
                    .roleResourceId(relationId)
                    .roleCode(roleCode)
                    .resourceCode(resourceCode)
                    .deleted(false)
                    .build());
        }
    }

    /**
     * 绑定账号和角色。
     *
     * @param accountId 账号 ID
     * @param roleCode  角色编码
     */
    @Override
    public void bindAccountRole(String accountId, String roleCode) {
        String relationId = accountRoleId(accountId, roleCode);
        if (accountRoleMapper.selectById(relationId) == null) {
            accountRoleMapper.insert(AccountRolePO.builder()
                    .accountRoleId(relationId)
                    .accountId(accountId)
                    .roleCode(roleCode)
                    .deleted(false)
                    .build());
        }
    }

    /**
     * 查询账号拥有的权限资源。
     *
     * @param accountId 账号 ID
     * @return 权限资源列表
     */
    @Override
    public List<PermissionResource> findResourcesByAccountId(String accountId) {
        List<AccountRolePO> accountRolePOS = accountRoleMapper.selectList(new LambdaQueryWrapper<AccountRolePO>()
                .eq(AccountRolePO::getAccountId, accountId));
        Set<String> resourceCodes = new LinkedHashSet<>();
        for (AccountRolePO accountRolePO : accountRolePOS) {
            Role role = findRoleByCode(accountRolePO.getRoleCode()).orElse(null);
            if (role != null && role.isActive()) {
                roleResourceMapper.selectList(new LambdaQueryWrapper<RoleResourcePO>()
                                .eq(RoleResourcePO::getRoleCode, accountRolePO.getRoleCode()))
                        .forEach(relation -> resourceCodes.add(relation.getResourceCode()));
            }
        }
        List<PermissionResource> resources = new ArrayList<>();
        for (String resourceCode : resourceCodes) {
            findResourceByCode(resourceCode).filter(PermissionResource::isActive).ifPresent(resources::add);
        }
        return resources;
    }

    /**
     * 转换角色数据对象。
     *
     * @param role 角色
     * @return 角色数据对象
     */
    private RolePO toRolePO(Role role) {
        return RolePO.builder()
                .roleCode(role.getRoleCode())
                .roleName(role.getRoleName())
                .status(role.getStatus().name())
                .deleted(false)
                .build();
    }

    /**
     * 转换角色聚合。
     *
     * @param rolePO 角色数据对象
     * @return 角色聚合
     */
    private Role toRole(RolePO rolePO) {
        return Role.restore(rolePO.getRoleCode(), rolePO.getRoleName(), RbacStatus.valueOf(rolePO.getStatus()));
    }

    /**
     * 转换权限资源数据对象。
     *
     * @param resource 权限资源
     * @return 权限资源数据对象
     */
    private PermissionResourcePO toPermissionResourcePO(PermissionResource resource) {
        return PermissionResourcePO.builder()
                .resourceCode(resource.getResourceCode())
                .resourceName(resource.getResourceName())
                .resourceType(resource.getResourceType().name())
                .resourceService(resource.getResourceService())
                .pathPattern(resource.getPathPattern())
                .action(resource.getAction())
                .status(resource.getStatus().name())
                .deleted(false)
                .build();
    }

    /**
     * 转换权限资源聚合。
     *
     * @param resourcePO 权限资源数据对象
     * @return 权限资源聚合
     */
    private PermissionResource toPermissionResource(PermissionResourcePO resourcePO) {
        return PermissionResource.restore(resourcePO.getResourceCode(), resourcePO.getResourceName(),
                PermissionResourceType.valueOf(resourcePO.getResourceType()), resourcePO.getResourceService(),
                resourcePO.getPathPattern(), resourcePO.getAction(), RbacStatus.valueOf(resourcePO.getStatus()));
    }

    /**
     * 生成角色权限资源关系 ID。
     *
     * @param roleCode     角色编码
     * @param resourceCode 资源编码
     * @return 关系 ID
     */
    private String roleResourceId(String roleCode, String resourceCode) {
        return roleCode + "::" + resourceCode;
    }

    /**
     * 生成账号角色关系 ID。
     *
     * @param accountId 账号 ID
     * @param roleCode  角色编码
     * @return 关系 ID
     */
    private String accountRoleId(String accountId, String roleCode) {
        return accountId + "::" + roleCode;
    }
}
