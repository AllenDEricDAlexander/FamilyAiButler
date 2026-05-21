/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade
 * @FileName: TokenFacade.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: Token facade 契约文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade;

import top.egon.familyaibutler.uaa.facade.dto.token.RefreshTokenRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.RevokeTokenRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenPairResponse;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenValidationRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenValidationResponse;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade
 * @ClassName: TokenFacade
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Token facade 契约
 * @Version: 1.0
 */
public interface TokenFacade {

    /**
     * 刷新访问令牌。
     *
     * @param request 刷新请求
     * @return 新令牌对
     */
    TokenPairResponse refreshAccessToken(RefreshTokenRequest request);

    /**
     * 校验访问令牌。
     *
     * @param request 校验请求
     * @return 校验结果
     */
    TokenValidationResponse validateAccessToken(TokenValidationRequest request);

    /**
     * 撤销单个令牌。
     *
     * @param request 撤销请求
     * @return true 表示撤销成功
     */
    boolean revokeToken(RevokeTokenRequest request);

    /**
     * 按账号撤销令牌。
     *
     * @param accountId 账号 ID
     * @return true 表示撤销成功
     */
    boolean revokeAccountTokens(String accountId);

    /**
     * 按设备撤销令牌。
     *
     * @param deviceId 设备 ID
     * @return true 表示撤销成功
     */
    boolean revokeDeviceTokens(String deviceId);
}
