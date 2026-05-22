/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @FileName: DocOperation.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:25
 * @Description: 文档操作注解文件
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
 * @ClassName: DocOperation
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:25
 * @Description: 文档操作注解
 * @Version: 1.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocOperation {

    /**
     * 稳定操作 ID
     *
     * @return String 返回稳定操作 ID
     */
    String id() default "";

    /**
     * 一句话标题
     *
     * @return String 返回一句话标题
     */
    String summary();

    /**
     * 详细说明
     *
     * @return String 返回详细说明
     */
    String description() default "";

    /**
     * 是否需要登录
     *
     * @return boolean 返回 true 表示需要登录
     */
    boolean auth() default true;

    /**
     * 排序值
     *
     * @return int 返回排序值
     */
    int order() default 0;

    /**
     * 额外 tag
     *
     * @return String[] 返回额外 tag
     */
    String[] tags() default {};

    /**
     * 请求契约
     *
     * @return DocRequest 返回请求契约
     */
    DocRequest request() default @DocRequest;

    /**
     * 唯一主响应契约
     *
     * @return DocResponse 返回唯一主响应契约
     */
    DocResponse response() default @DocResponse;
}
