/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @FileName: DocField.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:25
 * @Description: 文档字段注解文件
 * @Version: 1.0
 */
package top.egon.openapi.console.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @ClassName: DocField
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:25
 * @Description: 文档字段注解
 * @Version: 1.0
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocField {

    /**
     * 字段名
     *
     * @return String 返回字段名
     */
    String name() default "";

    /**
     * 字段描述
     *
     * @return String 返回字段描述
     */
    String description() default "";

    /**
     * 是否必填
     *
     * @return boolean 返回 true 表示必填
     */
    boolean required() default false;

    /**
     * 是否隐藏
     *
     * @return boolean 返回 true 表示隐藏
     */
    boolean hidden() default false;

    /**
     * 是否可为空
     *
     * @return boolean 返回 true 表示可为空
     */
    boolean nullable() default false;

    /**
     * 是否废弃
     *
     * @return boolean 返回 true 表示废弃
     */
    boolean deprecated() default false;

    /**
     * 字段类型补充
     *
     * @return DocDataType 返回字段类型补充
     */
    DocDataType dataType() default @DocDataType;

    /**
     * 字段示例
     *
     * @return String 返回字段示例
     */
    String example() default "";

    /**
     * 示例解析方式
     *
     * @return DocExampleMode 返回示例解析方式
     */
    DocExampleMode exampleMode() default DocExampleMode.AUTO;
}
