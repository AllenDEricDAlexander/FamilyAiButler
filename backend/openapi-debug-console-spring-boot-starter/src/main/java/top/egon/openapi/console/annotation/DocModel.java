/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @FileName: DocModel.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:25
 * @Description: 文档模型注解文件
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
 * @ClassName: DocModel
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:25
 * @Description: 文档模型注解
 * @Version: 1.0
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocModel {

    /**
     * 模型名称
     *
     * @return String 返回模型名称
     */
    String name() default "";

    /**
     * 模型描述
     *
     * @return String 返回模型描述
     */
    String description() default "";
}
