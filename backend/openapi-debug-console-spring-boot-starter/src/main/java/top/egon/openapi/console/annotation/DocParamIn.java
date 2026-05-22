/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @FileName: DocParamIn.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-19:20
 * @Description: 文档参数位置枚举文件
 * @Version: 1.0
 */
package top.egon.openapi.console.annotation;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @ClassName: DocParamIn
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-19:20
 * @Description: 文档参数位置枚举
 * @Version: 1.0
 */
public enum DocParamIn {
    AUTO,
    PATH,
    QUERY,
    HEADER,
    COOKIE,
    BODY,
    FORM,
    FILE
}
