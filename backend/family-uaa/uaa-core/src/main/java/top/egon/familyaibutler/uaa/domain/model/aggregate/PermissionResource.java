/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.model.aggregate
 * @FileName: PermissionResource.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:05
 * @Description: 权限资源聚合文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.model.aggregate;

import top.egon.familyaibutler.uaa.domain.model.enums.PermissionResourceType;
import top.egon.familyaibutler.uaa.domain.model.enums.RbacStatus;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.model.aggregate
 * @ClassName: PermissionResource
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:05
 * @Description: 权限资源聚合
 * @Version: 1.0
 */
public class PermissionResource {
    private final String resourceCode;
    private final String resourceName;
    private final PermissionResourceType resourceType;
    private final String resourceService;
    private final String pathPattern;
    private final String action;
    private final RbacStatus status;

    private PermissionResource(String resourceCode, String resourceName, PermissionResourceType resourceType,
                               String resourceService, String pathPattern, String action, RbacStatus status) {
        this.resourceCode = resourceCode;
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.resourceService = resourceService;
        this.pathPattern = pathPattern;
        this.action = action;
        this.status = status;
    }

    /**
     * 创建可用权限资源。
     *
     * @param resourceCode    资源编码
     * @param resourceName    资源名称
     * @param resourceType    资源类型
     * @param resourceService 资源服务
     * @param pathPattern     路径模式
     * @param action          操作
     * @return 权限资源聚合
     */
    public static PermissionResource active(String resourceCode, String resourceName, PermissionResourceType resourceType,
                                            String resourceService, String pathPattern, String action) {
        return restore(resourceCode, resourceName, resourceType, resourceService, pathPattern, action, RbacStatus.ACTIVE);
    }

    /**
     * 还原权限资源。
     *
     * @param resourceCode    资源编码
     * @param resourceName    资源名称
     * @param resourceType    资源类型
     * @param resourceService 资源服务
     * @param pathPattern     路径模式
     * @param action          操作
     * @param status          状态
     * @return 权限资源聚合
     */
    public static PermissionResource restore(String resourceCode, String resourceName, PermissionResourceType resourceType,
                                             String resourceService, String pathPattern, String action, RbacStatus status) {
        return new PermissionResource(resourceCode, resourceName, resourceType, resourceService, pathPattern, action, status);
    }

    /**
     * 判断权限资源是否可用。
     *
     * @return true 表示可用
     */
    public boolean isActive() {
        return status == RbacStatus.ACTIVE;
    }

    /**
     * 获取资源编码。
     *
     * @return 资源编码
     */
    public String getResourceCode() {
        return resourceCode;
    }

    /**
     * 获取资源名称。
     *
     * @return 资源名称
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * 获取资源类型。
     *
     * @return 资源类型
     */
    public PermissionResourceType getResourceType() {
        return resourceType;
    }

    /**
     * 获取资源服务。
     *
     * @return 资源服务
     */
    public String getResourceService() {
        return resourceService;
    }

    /**
     * 获取路径模式。
     *
     * @return 路径模式
     */
    public String getPathPattern() {
        return pathPattern;
    }

    /**
     * 获取操作。
     *
     * @return 操作
     */
    public String getAction() {
        return action;
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
