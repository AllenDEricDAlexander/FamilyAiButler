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
import top.egon.openapi.console.annotation.DocBody;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;

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
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-rbac-dubbo",
        serviceName = "RBAC Dubbo 服务", serviceDescription = "角色、权限资源和用户权限查询 RPC 能力", protocol = DocProtocol.DUBBO_TRIPLE)
public class RbacDubboAdapter implements RbacFacade {
    /**
     * RBAC 应用服务。
     */
    private final RbacManage rbacService;

    /**
     * 新增或更新角色。
     *
     * @param request 角色请求
     * @return 角色响应
     */
    @Override
    @DocOperation(summary = "新增或更新角色", description = "按角色编码新增或更新角色",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = UpsertRoleRequest.class))),
            response = @DocResponse(description = "保存成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = RoleResponse.class)))
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
    @DocOperation(summary = "新增或更新权限资源", description = "按资源编码新增或更新权限资源",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = UpsertPermissionResourceRequest.class))),
            response = @DocResponse(description = "保存成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = PermissionResourceResponse.class)))
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
    @DocOperation(summary = "绑定角色和权限资源", description = "为角色绑定权限资源",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = BindRoleResourceRequest.class))),
            response = @DocResponse(description = "绑定成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN)))
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
    @DocOperation(summary = "绑定账号和角色", description = "为账号绑定角色",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = BindAccountRoleRequest.class))),
            response = @DocResponse(description = "绑定成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN)))
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
    @DocOperation(summary = "查询用户权限资源", description = "按账号和资源类型查询用户权限资源",
            request = @DocRequest(body = @DocBody(dataType = @DocDataType(kind = DocDataKind.OBJECT, type = UserPermissionQuery.class))),
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = UserPermissionResponse.class)))
    public UserPermissionResponse listUserPermissions(UserPermissionQuery query) {
        return rbacService.listUserPermissions(query);
    }
}
