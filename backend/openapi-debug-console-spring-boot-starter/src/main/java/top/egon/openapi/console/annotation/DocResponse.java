/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @FileName: DocResponse.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-19:20
 * @Description: 响应注解文件
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
 * @ClassName: DocResponse
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-19:20
 * @Description: 响应注解
 * @Version: 1.0
 */
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocResponse {

    /**
     * HTTP 状态码
     *
     * @return int 返回 HTTP 状态码
     */
    int status() default 200;

    /**
     * 响应描述
     *
     * @return String 返回响应描述
     */
    String description() default "OK";

    /**
     * 是否无响应体
     *
     * @return boolean 返回 true 表示无响应体
     */
    boolean noBody() default false;

    /**
     * 响应内容类型
     *
     * @return String 返回响应内容类型
     */
    String contentType() default "application/json";

    /**
     * 业务数据类型
     *
     * @return DocDataType 返回业务数据类型
     */
    DocDataType dataType() default @DocDataType;

    /**
     * 最外层包装
     *
     * @return DocWrapper 返回最外层包装
     */
    DocWrapper wrapper() default @DocWrapper;

    /**
     * 响应头
     *
     * @return DocParameter[] 返回响应头
     */
    DocParameter[] headers() default {};

    /**
     * 响应级示例
     *
     * @return String 返回响应级示例
     */
    String example() default "";

    /**
     * 响应级示例解析方式
     *
     * @return DocExampleMode 返回响应级示例解析方式
     */
    DocExampleMode exampleMode() default DocExampleMode.JSON;
}
