/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @FileName: RefreshTokenRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 刷新令牌请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.token;

import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @ClassName: RefreshTokenRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 刷新令牌请求
 * @Version: 1.0
 */
@DocModel(name = "UaaFacadeRefreshTokenRequest", description = "认证授权刷新令牌请求")
public record RefreshTokenRequest(
        @DocField(description = "刷新令牌", example = "refresh-token-001")
        String refreshToken,
        @DocField(description = "OAuth 客户端 ID", example = "family-web")
        String clientId,
        @DocField(description = "设备 ID", example = "device-001")
        String deviceId
) {
}
