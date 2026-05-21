/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.persistence.mp.converter
 * @FileName: UaaMpConverter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:30
 * @Description: UAA MyBatis Plus 转换器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.persistence.mp.converter;

import org.springframework.stereotype.Component;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Account;
import top.egon.familyaibutler.uaa.domain.account.model.aggregate.Profile;
import top.egon.familyaibutler.uaa.domain.account.model.entity.Credential;
import top.egon.familyaibutler.uaa.domain.account.model.enums.AccountStatus;
import top.egon.familyaibutler.uaa.domain.account.model.enums.AccountType;
import top.egon.familyaibutler.uaa.domain.account.model.enums.CredentialType;
import top.egon.familyaibutler.uaa.domain.account.model.enums.ProfileType;
import top.egon.familyaibutler.uaa.domain.auth.model.aggregate.AuthSession;
import top.egon.familyaibutler.uaa.domain.auth.model.entity.Device;
import top.egon.familyaibutler.uaa.domain.auth.model.entity.TokenRecord;
import top.egon.familyaibutler.uaa.domain.auth.model.enums.DeviceType;
import top.egon.familyaibutler.uaa.domain.auth.model.enums.SessionStatus;
import top.egon.familyaibutler.uaa.domain.auth.model.enums.TokenStatus;
import top.egon.familyaibutler.uaa.domain.auth.model.valueobject.TokenClaims;
import top.egon.familyaibutler.uaa.domain.oauth.model.aggregate.OAuthClient;
import top.egon.familyaibutler.uaa.domain.oauth.model.enums.OAuthClientStatus;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.AccessTokenPO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.AccountPO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.AuthSessionPO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.CredentialPO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.DevicePO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.OAuthClientPO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.ProfilePO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.RefreshTokenPO;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.persistence.mp.converter
 * @ClassName: UaaMpConverter
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:30
 * @Description: UAA MyBatis Plus 转换器
 * @Version: 1.0
 */
@Component
public class UaaMpConverter {

    /**
     * 账号聚合转换为数据对象。
     *
     * @param account 账号聚合
     * @return 账号数据对象
     */
    public AccountPO toAccountPO(Account account) {
        return AccountPO.builder()
                .accountId(account.getAccountId())
                .username(account.getUsername())
                .email(account.getEmail())
                .phone(account.getPhone())
                .accountType(account.getAccountType().name())
                .status(account.getStatus().name())
                .authVersion(account.getAuthVersion())
                .entitlementVersion(account.getEntitlementVersion())
                .sessionVersion(account.getSessionVersion())
                .riskVersion(account.getRiskVersion())
                .deleted(account.getStatus() == AccountStatus.DELETED)
                .build();
    }

    /**
     * 账号数据对象转换为聚合。
     *
     * @param accountPO 账号数据对象
     * @return 账号聚合
     */
    public Account toAccount(AccountPO accountPO) {
        return Account.restore(accountPO.getAccountId(), accountPO.getUsername(), accountPO.getEmail(), accountPO.getPhone(),
                AccountType.valueOf(accountPO.getAccountType()), AccountStatus.valueOf(accountPO.getStatus()),
                accountPO.getAuthVersion(), accountPO.getEntitlementVersion(), accountPO.getSessionVersion(), accountPO.getRiskVersion());
    }

    /**
     * 凭证实体转换为数据对象。
     *
     * @param credential 凭证实体
     * @return 凭证数据对象
     */
    public CredentialPO toCredentialPO(Credential credential) {
        return CredentialPO.builder()
                .credentialId(credential.getAccountId() + ":" + credential.getCredentialType().name())
                .accountId(credential.getAccountId())
                .credentialType(credential.getCredentialType().name())
                .credentialHash(credential.getCredentialHash())
                .deleted(false)
                .build();
    }

    /**
     * 凭证数据对象转换为实体。
     *
     * @param credentialPO 凭证数据对象
     * @return 凭证实体
     */
    public Credential toCredential(CredentialPO credentialPO) {
        return new Credential(credentialPO.getAccountId(), CredentialType.valueOf(credentialPO.getCredentialType()),
                credentialPO.getCredentialHash());
    }

    /**
     * Profile 聚合转换为数据对象。
     *
     * @param profile Profile 聚合
     * @return Profile 数据对象
     */
    public ProfilePO toProfilePO(Profile profile) {
        return ProfilePO.builder()
                .profileId(profile.getProfileId())
                .accountId(profile.getAccountId())
                .profileType(profile.getProfileType().name())
                .nickname(profile.getNickname())
                .avatar(profile.getAvatar())
                .language(profile.getLanguage())
                .region(profile.getRegion())
                .deleted(profile.isDeleted())
                .build();
    }

    /**
     * Profile 数据对象转换为聚合。
     *
     * @param profilePO Profile 数据对象
     * @return Profile 聚合
     */
    public Profile toProfile(ProfilePO profilePO) {
        return Profile.restore(profilePO.getProfileId(), profilePO.getAccountId(), ProfileType.valueOf(profilePO.getProfileType()),
                profilePO.getNickname(), profilePO.getAvatar(), profilePO.getLanguage(), profilePO.getRegion(),
                Boolean.TRUE.equals(profilePO.getDeleted()));
    }

    /**
     * 设备实体转换为数据对象。
     *
     * @param device 设备实体
     * @return 设备数据对象
     */
    public DevicePO toDevicePO(Device device) {
        return DevicePO.builder()
                .deviceId(device.getDeviceId())
                .accountId(device.getAccountId())
                .deviceName(device.getDeviceName())
                .deviceType(device.getDeviceType().name())
                .fingerprint(device.getFingerprint())
                .removed(device.isRemoved())
                .deleted(false)
                .build();
    }

    /**
     * 设备数据对象转换为实体。
     *
     * @param devicePO 设备数据对象
     * @return 设备实体
     */
    public Device toDevice(DevicePO devicePO) {
        return Device.restore(devicePO.getDeviceId(), devicePO.getAccountId(), devicePO.getDeviceName(),
                DeviceType.valueOf(devicePO.getDeviceType()), devicePO.getFingerprint(), Boolean.TRUE.equals(devicePO.getRemoved()));
    }

    /**
     * 会话聚合转换为数据对象。
     *
     * @param session 会话聚合
     * @return 会话数据对象
     */
    public AuthSessionPO toAuthSessionPO(AuthSession session) {
        return AuthSessionPO.builder()
                .sessionId(session.getSessionId())
                .accountId(session.getAccountId())
                .profileId(session.getProfileId())
                .deviceId(session.getDeviceId())
                .clientId(session.getClientId())
                .status(session.getStatus().name())
                .deleted(false)
                .build();
    }

    /**
     * 会话数据对象转换为聚合。
     *
     * @param sessionPO 会话数据对象
     * @return 会话聚合
     */
    public AuthSession toAuthSession(AuthSessionPO sessionPO) {
        return AuthSession.restore(sessionPO.getSessionId(), sessionPO.getAccountId(), sessionPO.getProfileId(),
                sessionPO.getDeviceId(), sessionPO.getClientId(), SessionStatus.valueOf(sessionPO.getStatus()));
    }

    /**
     * OAuth Client 聚合转换为数据对象。
     *
     * @param client OAuth Client 聚合
     * @return OAuth Client 数据对象
     */
    public OAuthClientPO toOAuthClientPO(OAuthClient client) {
        return OAuthClientPO.builder()
                .clientId(client.getClientId())
                .clientName(client.getClientName())
                .clientSecretHash(client.getClientSecretHash())
                .status(client.getStatus().name())
                .grantTypes(join(client.getGrantTypes()))
                .scopes(join(client.getScopes()))
                .resourcePatterns(join(client.getResourcePatterns()))
                .accessTokenTtlSeconds(client.getAccessTokenTtlSeconds())
                .refreshTokenTtlSeconds(client.getRefreshTokenTtlSeconds())
                .deleted(false)
                .build();
    }

    /**
     * OAuth Client 数据对象转换为聚合。
     *
     * @param clientPO OAuth Client 数据对象
     * @return OAuth Client 聚合
     */
    public OAuthClient toOAuthClient(OAuthClientPO clientPO) {
        return OAuthClient.restore(clientPO.getClientId(), clientPO.getClientName(), clientPO.getClientSecretHash(),
                OAuthClientStatus.valueOf(clientPO.getStatus()), split(clientPO.getGrantTypes()), split(clientPO.getScopes()),
                split(clientPO.getResourcePatterns()), clientPO.getAccessTokenTtlSeconds(), clientPO.getRefreshTokenTtlSeconds());
    }

    /**
     * 刷新令牌记录转换为数据对象。
     *
     * @param tokenRecord 刷新令牌记录
     * @return 刷新令牌数据对象
     */
    public RefreshTokenPO toRefreshTokenPO(TokenRecord tokenRecord) {
        return RefreshTokenPO.builder()
                .tokenId(tokenRecord.getTokenId())
                .accountId(tokenRecord.getAccountId())
                .sessionId(tokenRecord.getSessionId())
                .deviceId(tokenRecord.getDeviceId())
                .clientId(tokenRecord.getClientId())
                .tokenHash(tokenRecord.getTokenHash())
                .status(tokenRecord.getStatus().name())
                .deleted(false)
                .build();
    }

    /**
     * 刷新令牌数据对象转换为记录。
     *
     * @param tokenPO 刷新令牌数据对象
     * @return 刷新令牌记录
     */
    public TokenRecord toTokenRecord(RefreshTokenPO tokenPO) {
        return TokenRecord.restore(tokenPO.getTokenId(), tokenPO.getAccountId(), tokenPO.getSessionId(),
                tokenPO.getDeviceId(), tokenPO.getClientId(), tokenPO.getTokenHash(), TokenStatus.valueOf(tokenPO.getStatus()));
    }

    /**
     * 访问令牌声明转换为数据对象。
     *
     * @param accessTokenHash 访问令牌哈希
     * @param claims          访问令牌声明
     * @return 访问令牌数据对象
     */
    public AccessTokenPO toAccessTokenPO(String accessTokenHash, TokenClaims claims) {
        return AccessTokenPO.builder()
                .accessTokenHash(accessTokenHash)
                .accountId(claims.accountId())
                .profileId(claims.profileId())
                .clientId(claims.clientId())
                .sessionId(claims.sessionId())
                .deviceId(claims.deviceId())
                .authVersion(claims.authVersion())
                .entitlementVersion(claims.entitlementVersion())
                .riskLevel(claims.riskLevel())
                .expiresAt(Date.from(claims.expiresAt()))
                .deleted(false)
                .build();
    }

    /**
     * 访问令牌数据对象转换为声明。
     *
     * @param tokenPO 访问令牌数据对象
     * @return 访问令牌声明
     */
    public TokenClaims toTokenClaims(AccessTokenPO tokenPO) {
        Instant expiresAt = tokenPO.getExpiresAt().toInstant();
        return new TokenClaims(tokenPO.getAccountId(), tokenPO.getProfileId(), tokenPO.getClientId(), tokenPO.getSessionId(),
                tokenPO.getDeviceId(), tokenPO.getAuthVersion(), tokenPO.getEntitlementVersion(), tokenPO.getRiskLevel(), expiresAt);
    }

    /**
     * Set 字符串集合转换为逗号分隔存储值。
     *
     * @param values 字符串集合
     * @return 逗号分隔存储值
     */
    private String join(Set<String> values) {
        return String.join(",", values);
    }

    /**
     * 逗号分隔存储值转换为 Set 字符串集合。
     *
     * @param value 逗号分隔存储值
     * @return 字符串集合
     */
    private Set<String> split(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .collect(Collectors.toSet());
    }
}
