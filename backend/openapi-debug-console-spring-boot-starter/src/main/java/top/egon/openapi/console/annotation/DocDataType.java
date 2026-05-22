/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @FileName: DocDataType.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: 文档数据类型注解文件
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
 * @ClassName: DocDataType
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: 文档数据类型注解
 * @Version: 1.0
 */
@Documented
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocDataType {

    /**
     * 类型种类
     *
     * @return DocDataKind 返回类型种类
     */
    DocDataKind kind() default DocDataKind.AUTO;

    /**
     * 普通对象类型
     *
     * @return Class 返回普通对象类型
     */
    Class<?> type() default Void.class;

    /**
     * 复杂泛型引用
     *
     * @return Class 返回复杂泛型引用
     */
    Class<? extends DocTypeReference<?>> ref() default DocTypeReference.None.class;

    /**
     * 简单数组 / List 的元素类型
     *
     * @return Class 返回元素类型
     */
    Class<?> itemType() default Void.class;

    /**
     * 简单 Map 的 key 类型
     *
     * @return Class 返回 key 类型
     */
    Class<?> keyType() default String.class;

    /**
     * 简单 Map 的 value 类型
     *
     * @return Class 返回 value 类型
     */
    Class<?> valueType() default Void.class;

    /**
     * 数据路径
     *
     * @return String 返回数据路径
     */
    String dataPath() default "";
}
