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

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @ClassName: RevokeTokenRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 撤销令牌请求
 * @Version: 1.0
 */
public record RevokeTokenRequest(
        @DocField(description = "待撤销令牌", example = "access-token-001")
        String token,
        @DocField(description = "令牌 ID", example = "token-001")
        String tokenId,
        @DocField(description = "账号 ID", example = "account-001")
        String accountId,
        @DocField(description = "设备 ID", example = "device-001")
        String deviceId,
        @DocField(description = "OAuth 客户端 ID", example = "family-web")
        String clientId
) {
}
