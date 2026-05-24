/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @FileName: RevokeTokenRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 撤销令牌请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.token;

import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @ClassName: RevokeTokenRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 撤销令牌请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeRevokeTokenRequest", description = "认证授权撤销令牌请求")
public record RevokeTokenRequest(
        /**待撤销令牌。*/
        @DocField(description = "待撤销令牌", required = false, example = "access-token-001")
        String token,
        /**令牌ID。*/
        @DocField(description = "令牌 ID", required = false, example = "token-001")
        String tokenId,
        /**账号ID。*/
        @DocField(description = "账号 ID", required = false, example = "account-001")
        String accountId,
        /**设备ID。*/
        @DocField(description = "设备 ID", required = false, example = "device-001")
        String deviceId,
        /**OAuth客户端ID。*/
        @DocField(description = "OAuth 客户端 ID", required = false, example = "family-web")
        String clientId
) {
}
