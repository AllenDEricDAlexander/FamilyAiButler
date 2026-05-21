/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @FileName: TokenServiceI.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: Token 应用服务接口文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application;

import top.egon.familyaibutler.uaa.facade.dto.token.RefreshTokenRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.RevokeTokenRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenPairResponse;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenValidationRequest;
import top.egon.familyaibutler.uaa.facade.dto.token.TokenValidationResponse;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @ClassName: TokenServiceI
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Token 应用服务接口
 * @Version: 1.0
 */
public interface TokenServiceI {

    /**
     * 刷新访问令牌。
     *
     * @param request 刷新请求
     * @return 令牌对
     */
    TokenPairResponse refreshAccessToken(RefreshTokenRequest request);

    /**
     * 校验访问令牌。
     *
     * @param request 校验请求
     * @return 校验响应
     */
    TokenValidationResponse validateAccessToken(TokenValidationRequest request);

    /**
     * 撤销 Token。
     *
     * @param request 撤销请求
     * @return true 表示撤销成功
     */
    boolean revokeToken(RevokeTokenRequest request);

    /**
     * 按账号撤销 Token。
     *
     * @param accountId 账号 ID
     * @return true 表示撤销成功
     */
    boolean revokeAccountTokens(String accountId);
}
