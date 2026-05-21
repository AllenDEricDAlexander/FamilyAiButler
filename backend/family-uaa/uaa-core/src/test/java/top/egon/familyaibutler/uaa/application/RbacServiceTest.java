/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @FileName: RbacServiceTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:25
 * @Description: RBAC 应用服务测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application;

import org.junit.jupiter.api.Test;
import top.egon.familyaibutler.uaa.application.manage.impl.RbacManageImpl;
import top.egon.familyaibutler.uaa.facade.dto.rbac.BindAccountRoleRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.BindRoleResourceRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UpsertPermissionResourceRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UpsertRoleRequest;
import top.egon.familyaibutler.uaa.facade.dto.rbac.UserPermissionQuery;
import top.egon.familyaibutler.uaa.facade.enums.PermissionResourceType;
import top.egon.familyaibutler.uaa.infrastructure.gateway.impl.InMemoryRbacGatewayImpl;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @ClassName: RbacServiceTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:25
 * @Description: RBAC 应用服务测试
 * @Version: 1.0
 */
class RbacServiceTest {

    /**
     * 校验 RBAC 可以维护页面、按钮和接口资源并按账号查询。
     */
    @Test
    void shouldManagePageButtonAndApiPermissions() {
        RbacManageImpl service = new RbacManageImpl(new InMemoryRbacGatewayImpl());

        service.upsertRole(new UpsertRoleRequest("family-admin", "Family Admin"));
        service.upsertResource(new UpsertPermissionResourceRequest("password-page", "Password Page",
                PermissionResourceType.PAGE, "family-core", "/password", "view"));
        service.upsertResource(new UpsertPermissionResourceRequest("password-create-button", "Password Create Button",
                PermissionResourceType.BUTTON, "family-core", "/password", "create"));
        service.upsertResource(new UpsertPermissionResourceRequest("password-api", "Password API",
                PermissionResourceType.API, "family-core", "/password/**", "read"));
        service.bindRoleResource(new BindRoleResourceRequest("family-admin", "password-page"));
        service.bindRoleResource(new BindRoleResourceRequest("family-admin", "password-create-button"));
        service.bindRoleResource(new BindRoleResourceRequest("family-admin", "password-api"));
        service.bindAccountRole(new BindAccountRoleRequest("account-1", "family-admin"));

        var pages = service.listUserPermissions(new UserPermissionQuery("account-1", PermissionResourceType.PAGE));
        var buttons = service.listUserPermissions(new UserPermissionQuery("account-1", PermissionResourceType.BUTTON));
        var apis = service.listUserPermissions(new UserPermissionQuery("account-1", PermissionResourceType.API));

        assertThat(pages.resourceCodes()).containsExactly("password-page");
        assertThat(buttons.resourceCodes()).containsExactly("password-create-button");
        assertThat(apis.resourceCodes()).containsExactly("password-api");
    }
}
