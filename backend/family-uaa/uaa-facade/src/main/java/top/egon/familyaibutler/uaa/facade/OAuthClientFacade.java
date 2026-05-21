/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade
 * @FileName: OAuthClientFacade.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-16:45
 * @Description: OAuth Client facade 契约文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade;

import top.egon.familyaibutler.uaa.facade.dto.oauthclient.CreateOAuthClientRequest;
import top.egon.familyaibutler.uaa.facade.dto.oauthclient.OAuthClientResponse;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade
 * @ClassName: OAuthClientFacade
 * @Author: atluofu
 * @CreateTime: 2026-05-20 16:45
 * @Description: OAuth Client facade 契约
 * @Version: 1.0
 */
public interface OAuthClientFacade {

    /**
     * 创建 OAuth Client。
     *
     * @param request 创建请求
     * @return OAuth Client 响应
     */
    OAuthClientResponse create(CreateOAuthClientRequest request);

    /**
     * 按客户端 ID 查询 OAuth Client。
     *
     * @param clientId 客户端 ID
     * @return OAuth Client 响应
     */
    OAuthClientResponse get(String clientId);

    /**
     * 查询 OAuth Client 列表。
     *
     * @return OAuth Client 列表
     */
    List<OAuthClientResponse> list();
}
