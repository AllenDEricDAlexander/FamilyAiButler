/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.security.jwt
 * @FileName: FamilyJwtClaims.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:00
 * @Description: 统一 JWT 身份声明文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.common.security.jwt;

import java.time.Instant;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.security.jwt
 * @ClassName: FamilyJwtClaims
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:00
 * @Description: 统一 JWT 身份声明
 * @Version: 1.0
 */
public record FamilyJwtClaims(
        String tokenId,
        String accountId,
        String profileId,
        String clientId,
        String sessionId,
        String deviceId,
        long authVersion,
        long entitlementVersion,
        String riskLevel,
        String issuer,
        String audience,
        Instant expiresAt
) {
}
