/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter
 * @FileName: JwkController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-16:30
 * @Description: JWK 公钥发布 Web 控制器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtService;

import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter
 * @ClassName: JwkController
 * @Author: atluofu
 * @CreateTime: 2026-05-20 16:30
 * @Description: JWK 公钥发布 Web 控制器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/.well-known")
public class JwkController {
    private final FamilyJwtService familyJwtService;

    /**
     * 创建 JWK 公钥发布 Web 控制器。
     *
     * @param familyJwtService JWT 服务
     */
    public JwkController(FamilyJwtService familyJwtService) {
        this.familyJwtService = familyJwtService;
    }

    /**
     * 发布 JWT RSA 公钥集。
     *
     * @return JWK Set
     */
    @GetMapping("/jwks.json")
    public Result<Map<String, Object>> jwks() {
        return Result.success(familyJwtService.publicJwkSet().orElseGet(() -> Map.of("keys", List.of())));
    }
}
