/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter
 * @FileName: JwkControllerTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-16:20
 * @Description: JWK 控制器测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter;

import org.junit.jupiter.api.Test;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtProperties;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtService;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter
 * @ClassName: JwkControllerTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 16:20
 * @Description: JWK 控制器测试
 * @Version: 1.0
 */
class JwkControllerTest {

    /**
     * 校验控制器可以发布 RSA 公钥 JWK Set。
     *
     * @throws Exception 密钥生成异常
     */
    @Test
    void jwksShouldExposeRsaPublicKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        FamilyJwtProperties properties = new FamilyJwtProperties();
        properties.setAlgorithm("RS256");
        properties.setKeyId("uaa-rsa-key");
        properties.setAccessPrivateKey(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        properties.setAccessPublicKey(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        properties.setRefreshKey(Base64.getEncoder().encodeToString(("refresh-key-for-jwk-controller-test-2026-12345678901234567890"
                + "-extra-refresh-signature-material").getBytes()));
        JwkController controller = new JwkController(new FamilyJwtService(properties));

        var response = controller.jwks();

        assertThat(response.getData().toString()).contains("uaa-rsa-key");
    }
}
