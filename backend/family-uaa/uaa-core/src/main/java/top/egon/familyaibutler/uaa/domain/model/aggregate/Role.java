/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.model.aggregate
 * @FileName: Role.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:05
 * @Description: 角色聚合文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.model.aggregate;

import top.egon.familyaibutler.uaa.domain.model.enums.RbacStatus;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.model.aggregate
 * @ClassName: Role
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:05
 * @Description: 角色聚合
 * @Version: 1.0
 */
public class Role {
    private final String roleCode;
    private final String roleName;
    private final RbacStatus status;

    private Role(String roleCode, String roleName, RbacStatus status) {
        this.roleCode = roleCode;
        this.roleName = roleName;
        this.status = status;
    }

    /**
     * 创建可用角色。
     *
     * @param roleCode 角色编码
     * @param roleName 角色名称
     * @return 角色聚合
     */
    public static Role active(String roleCode, String roleName) {
        return restore(roleCode, roleName, RbacStatus.ACTIVE);
    }

    /**
     * 还原角色。
     *
     * @param roleCode 角色编码
     * @param roleName 角色名称
     * @param status   状态
     * @return 角色聚合
     */
    public static Role restore(String roleCode, String roleName, RbacStatus status) {
        return new Role(roleCode, roleName, status);
    }

    /**
     * 判断角色是否可用。
     *
     * @return true 表示可用
     */
    public boolean isActive() {
        return status == RbacStatus.ACTIVE;
    }

    /**
     * 获取角色编码。
     *
     * @return 角色编码
     */
    public String getRoleCode() {
        return roleCode;
    }

    /**
     * 获取角色名称。
     *
     * @return 角色名称
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * 获取状态。
     *
     * @return 状态
     */
    public RbacStatus getStatus() {
        return status;
    }
}
