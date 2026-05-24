/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @FileName: JwkController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-16:30
 * @Description: JWK 公钥发布 Web 控制器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtService;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;
import top.egon.openapi.console.annotation.DocWrapper;

import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @ClassName: JwkController
 * @Author: atluofu
 * @CreateTime: 2026-05-20 16:30
 * @Description: JWK 公钥发布 Web 控制器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/.well-known")
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-jwk",
        serviceName = "JWK 公钥服务", serviceDescription = "JWT RSA 公钥集发布能力", protocol = DocProtocol.HTTP)
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
    @DocOperation(summary = "发布 JWT RSA 公钥集", description = "返回用于 JWT 验签的 JWK Set",
            response = @DocResponse(description = "查询成功",
                    dataType = @DocDataType(kind = DocDataKind.MAP, keyType = String.class, valueType = Object.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<Map<String, Object>> jwks() {
        return Result.success(familyJwtService.publicJwkSet().orElseGet(() -> Map.of("keys", List.of())));
    }
}
