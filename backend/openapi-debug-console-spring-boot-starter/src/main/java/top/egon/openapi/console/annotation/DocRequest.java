/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @FileName: DocRequest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: 文档请求注解文件
 * @Version: 1.0
 */
package top.egon.openapi.console.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @ClassName: DocRequest
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: 文档请求注解
 * @Version: 1.0
 */
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocRequest {

    /**
     * path/query/header/cookie/form 参数
     *
     * @return DocParameter[] 返回请求参数
     */
    DocParameter[] params() default {};

    /**
     * 请求体
     *
     * @return DocBody 返回请求体
     */
    DocBody body() default @DocBody(enabled = false);
}
