/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.security
 * @FileName: FamilySecurityHeaders.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-15:40
 * @Description: 家庭服务安全身份请求头常量文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.common.security;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.security
 * @ClassName: FamilySecurityHeaders
 * @Author: atluofu
 * @CreateTime: 2026-05-20 15:40
 * @Description: 家庭服务安全身份请求头常量
 * @Version: 1.0
 */
public final class FamilySecurityHeaders {
    public static final String ACCOUNT_ID = "X-Family-Account-Id";
    public static final String PROFILE_ID = "X-Family-Profile-Id";
    public static final String CLIENT_ID = "X-Family-Client-Id";
    public static final String SESSION_ID = "X-Family-Session-Id";
    public static final String DEVICE_ID = "X-Family-Device-Id";
    public static final String AUTH_VERSION = "X-Family-Auth-Version";
    public static final String ENTITLEMENT_VERSION = "X-Family-Entitlement-Version";
    public static final String RISK_LEVEL = "X-Family-Risk-Level";

    private static final List<String> IDENTITY_HEADERS = List.of(
            ACCOUNT_ID,
            PROFILE_ID,
            CLIENT_ID,
            SESSION_ID,
            DEVICE_ID,
            AUTH_VERSION,
            ENTITLEMENT_VERSION,
            RISK_LEVEL
    );

    private FamilySecurityHeaders() {
    }

    /**
     * 获取全部身份请求头名称。
     *
     * @return 身份请求头名称列表
     */
    public static List<String> identityHeaders() {
        return IDENTITY_HEADERS;
    }
}
