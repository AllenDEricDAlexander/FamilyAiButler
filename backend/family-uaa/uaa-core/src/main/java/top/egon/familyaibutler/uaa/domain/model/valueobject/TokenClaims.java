/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.model.valueobject
 * @FileName: TokenClaims.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: Token 声明值对象文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.model.valueobject;

import java.time.Instant;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.model.valueobject
 * @ClassName: TokenClaims
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: Token 声明值对象
 * @Version: 1.0
 */
public record TokenClaims(
        String accountId,
        String profileId,
        String clientId,
        String sessionId,
        String deviceId,
        long authVersion,
        long entitlementVersion,
        String riskLevel,
        Instant expiresAt
) {

    /**
     * 判断令牌是否已过期。
     *
     * @return true 表示已过期
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }
}
