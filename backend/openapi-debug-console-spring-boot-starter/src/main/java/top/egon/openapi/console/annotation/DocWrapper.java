/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @FileName: DocWrapper.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: 文档响应包装注解文件
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
 * @ClassName: DocWrapper
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: 文档响应包装注解
 * @Version: 1.0
 */
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocWrapper {

    /**
     * 包装类型
     *
     * @return Class 返回包装类型
     */
    Class<?> type() default Void.class;

    /**
     * wrapper 中承载数据的字段路径
     *
     * @return String 返回字段路径
     */
    String dataPath() default "data";

    /**
     * wrapper 泛型参数索引
     *
     * @return int 返回泛型参数索引
     */
    int genericIndex() default 0;
}
