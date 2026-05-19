/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.security.jwt
 * @FileName: FamilyJwtServiceTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-23:45
 * @Description: 统一 JWT 服务测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.common.security.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.security.jwt
 * @ClassName: FamilyJwtServiceTest
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-23:45
 * @Description: 统一 JWT 服务测试
 * @Version: 1.0
 */
class FamilyJwtServiceTest {

    /**
     * 测试未显式启用 JWT 时不注册 JWT 服务
     */
    @Test
    void testDisableJwtServiceByDefault() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
                .withUserConfiguration(FamilyJwtProperties.class, FamilyJwtService.class);

        runner.run(context -> Assertions.assertFalse(context.containsBean("familyJwtService")));
    }

    /**
     * 测试显式启用 JWT 并配置密钥时注册 JWT 服务
     */
    @Test
    void testEnableJwtServiceWhenConfigured() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
                .withUserConfiguration(FamilyJwtProperties.class, FamilyJwtService.class)
                .withPropertyValues(
                        "family.security.jwt.enabled=true",
                        "family.security.jwt.access-key=" + base64Key("access-key-for-context-runner-test-2026-aaaaaaaaaaaaaaaaaaaa"),
                        "family.security.jwt.refresh-key=" + base64Key("refresh-key-for-context-runner-test-2026-bbbbbbbbbbbbbbbbb"));

        runner.run(context -> Assertions.assertTrue(context.containsBean("familyJwtService")));
    }

    /**
     * 测试 access token 使用统一 HS512 密钥签发并解析权限
     */
    @Test
    void testCreateAndParseAccessToken() {
        FamilyJwtService jwtService = new FamilyJwtService(jwtProperties());

        String token = jwtService.createAccessToken("admin", List.of("ROLE_ADMIN", "ROLE_USER"));
        Claims claims = jwtService.parseAccessClaims(token).orElseThrow();

        Assertions.assertEquals("admin", claims.getSubject());
        Assertions.assertEquals(List.of("ROLE_ADMIN", "ROLE_USER"), claims.get("authorities", List.class));
        Assertions.assertTrue(jwtService.validateAccessToken(token));
    }

    /**
     * 测试 access token 不能被 refresh key 校验通过
     */
    @Test
    void testAccessTokenCanNotUseRefreshKey() {
        FamilyJwtService jwtService = new FamilyJwtService(jwtProperties());

        String token = jwtService.createAccessToken("admin", List.of("ROLE_ADMIN"));

        Assertions.assertFalse(jwtService.validateRefreshToken(token));
    }

    /**
     * 测试 Authorization 请求头兼容 Bearer 前缀和纯 token
     */
    @Test
    void testResolveAuthorizationToken() {
        FamilyJwtService jwtService = new FamilyJwtService(jwtProperties());

        Assertions.assertEquals("abc.def.ghi", jwtService.resolveAuthorizationToken("Bearer abc.def.ghi"));
        Assertions.assertEquals("abc.def.ghi", jwtService.resolveAuthorizationToken("abc.def.ghi"));
        Assertions.assertNull(jwtService.resolveAuthorizationToken(""));
    }

    /**
     * 创建 JWT 测试配置
     *
     * @return FamilyJwtProperties 返回 JWT 测试配置
     */
    private FamilyJwtProperties jwtProperties() {
        FamilyJwtProperties properties = new FamilyJwtProperties();
        properties.setAccessKey(base64Key("access-key-for-family-ai-butler-jwt-test-2026-aaaaaaaaaaaaaaaa"));
        properties.setRefreshKey(base64Key("refresh-key-for-family-ai-butler-jwt-test-2026-bbbbbbbbbbbbbb"));
        properties.setAccessTokenExpireTime(300000);
        properties.setRefreshTokenExpireTime(2592000000L);
        return properties;
    }

    /**
     * 创建 Base64 测试密钥
     *
     * @param raw 原始密钥
     * @return String 返回 Base64 密钥
     */
    private String base64Key(String raw) {
        String normalizedRaw = (raw + "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ").substring(0, 64);
        return Base64.getEncoder().encodeToString(normalizedRaw.getBytes(StandardCharsets.UTF_8));
    }
}
