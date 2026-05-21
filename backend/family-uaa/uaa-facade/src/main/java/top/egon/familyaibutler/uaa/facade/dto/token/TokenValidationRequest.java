/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @FileName: TokenValidationRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: Token 校验请求文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.facade.dto.token;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.facade.dto.token
 * @ClassName: TokenValidationRequest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: Token 校验请求
 * @Version: 1.0
 */
public record TokenValidationRequest(
        String accessToken,
        String resource,
        String action
) {
}
