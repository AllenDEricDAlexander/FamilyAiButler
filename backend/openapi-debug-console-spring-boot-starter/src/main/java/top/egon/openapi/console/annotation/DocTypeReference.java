/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @FileName: DocTypeReference.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: 文档复杂泛型引用文件
 * @Version: 1.0
 */
package top.egon.openapi.console.annotation;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.annotation
 * @ClassName: DocTypeReference
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: 文档复杂泛型引用
 * @Version: 1.0
 */
public abstract class DocTypeReference<T> {

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console.annotation
     * @ClassName: None
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-14:20
     * @Description: 空文档复杂泛型引用
     * @Version: 1.0
     */
    public static final class None extends DocTypeReference<Void> {
    }
}
