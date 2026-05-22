/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @FileName: DocService.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:25
 * @Description: 文档服务注解文件
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
 * @ClassName: DocService
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:25
 * @Description: 文档服务注解
 * @Version: 1.0
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocService {

    /**
     * 一级分组 ID
     *
     * @return String 返回一级分组 ID
     */
    String groupId() default "default";

    /**
     * 一级分组名称
     *
     * @return String 返回一级分组名称
     */
    String groupName() default "默认分组";

    /**
     * 一级分组排序
     *
     * @return int 返回一级分组排序
     */
    int groupOrder() default 0;

    /**
     * 二级服务 ID
     *
     * @return String 返回二级服务 ID
     */
    String serviceId() default "";

    /**
     * 二级服务名称
     *
     * @return String 返回二级服务名称
     */
    String serviceName();

    /**
     * 服务描述
     *
     * @return String 返回服务描述
     */
    String serviceDescription() default "";

    /**
     * 二级服务排序
     *
     * @return int 返回二级服务排序
     */
    int serviceOrder() default 0;

    /**
     * 服务版本
     *
     * @return String 返回服务版本
     */
    String version() default "";

    /**
     * 协议类型
     *
     * @return DocProtocol 返回协议类型
     */
    DocProtocol protocol() default DocProtocol.AUTO;

    /**
     * Dubbo / RPC 场景下的接口类型
     *
     * @return Class 返回服务接口类型
     */
    Class<?> serviceInterface() default Void.class;

    /**
     * 是否生成文档
     *
     * @return boolean 返回 true 表示生成文档
     */
    boolean enabled() default true;
}
