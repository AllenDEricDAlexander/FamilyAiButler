/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @FileName: DocIgnore.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:25
 * @Description: 文档忽略注解文件
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
 * @ClassName: DocIgnore
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:25
 * @Description: 文档忽略注解
 * @Version: 1.0
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocIgnore {
}
