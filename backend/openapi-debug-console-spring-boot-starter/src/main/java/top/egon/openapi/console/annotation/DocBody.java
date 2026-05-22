/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @FileName: DocBody.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: 文档 Body 注解文件
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
 * @ClassName: DocBody
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: 文档 Body 注解
 * @Version: 1.0
 */
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocBody {

    /**
     * 是否启用 body
     *
     * @return boolean 返回 true 表示启用
     */
    boolean enabled() default true;

    /**
     * body 描述
     *
     * @return String 返回 body 描述
     */
    String description() default "";

    /**
     * 内容类型
     *
     * @return String 返回内容类型
     */
    String contentType() default "application/json";

    /**
     * 是否必填
     *
     * @return boolean 返回 true 表示必填
     */
    boolean required() default true;

    /**
     * body 数据类型
     *
     * @return DocDataType 返回 body 数据类型
     */
    DocDataType dataType() default @DocDataType;

    /**
     * body 最外层包装
     *
     * @return DocWrapper 返回 body 最外层包装
     */
    DocWrapper wrapper() default @DocWrapper;

    /**
     * body 级别示例
     *
     * @return String 返回 body 级别示例
     */
    String example() default "";

    /**
     * body 示例解析方式
     *
     * @return DocExampleMode 返回 body 示例解析方式
     */
    DocExampleMode exampleMode() default DocExampleMode.JSON;
}
