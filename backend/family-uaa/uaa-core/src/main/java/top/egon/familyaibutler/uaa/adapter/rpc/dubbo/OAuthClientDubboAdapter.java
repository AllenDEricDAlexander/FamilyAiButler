/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @FileName: OAuthClientDubboAdapter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:20
 * @Description: OAuth Client facade 适配器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.rpc.dubbo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.uaa.application.manage.OAuthClientManage;
import top.egon.familyaibutler.uaa.facade.OAuthClientFacade;
import top.egon.familyaibutler.uaa.facade.dto.oauthclient.CreateOAuthClientRequest;
import top.egon.familyaibutler.uaa.facade.dto.oauthclient.OAuthClientResponse;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.rpc.dubbo
 * @ClassName: OAuthClientDubboAdapter
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:20
 * @Description: OAuth Client facade 适配器
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
public class OAuthClientDubboAdapter implements OAuthClientFacade {
    private final OAuthClientManage oAuthClientService;

    /**
     * 创建 OAuth Client。
     *
     * @param request 创建请求
     * @return OAuth Client 响应
     */
    @Override
    public OAuthClientResponse create(CreateOAuthClientRequest request) {
        return oAuthClientService.create(request);
    }

    /**
     * 按客户端 ID 查询 OAuth Client。
     *
     * @param clientId 客户端 ID
     * @return OAuth Client 响应
     */
    @Override
    public OAuthClientResponse get(String clientId) {
        return oAuthClientService.get(clientId);
    }

    /**
     * 查询 OAuth Client 列表。
     *
     * @return OAuth Client 列表
     */
    @Override
    public List<OAuthClientResponse> list() {
        return oAuthClientService.list();
    }
}
