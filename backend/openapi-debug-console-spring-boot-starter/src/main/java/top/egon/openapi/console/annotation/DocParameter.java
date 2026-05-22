/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @FileName: DocParameter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: 文档参数注解文件
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
 * @ClassName: DocParameter
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: 文档参数注解
 * @Version: 1.0
 */
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocParameter {

    /**
     * 参数名称
     *
     * @return String 返回参数名称
     */
    String name() default "";

    /**
     * 参数位置
     *
     * @return DocParamIn 返回参数位置
     */
    DocParamIn in() default DocParamIn.AUTO;

    /**
     * 参数描述
     *
     * @return String 返回参数描述
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
     * 对应复杂对象展开后的来源字段路径
     *
     * @return String 返回来源字段路径
     */
    String source() default "";

    /**
     * 参数类型
     *
     * @return DocDataType 返回参数类型
     */
    DocDataType dataType() default @DocDataType;

    /**
     * 参数示例
     *
     * @return String 返回参数示例
     */
    String example() default "";

    /**
     * 示例解析方式
     *
     * @return DocExampleMode 返回示例解析方式
     */
    DocExampleMode exampleMode() default DocExampleMode.AUTO;
}
