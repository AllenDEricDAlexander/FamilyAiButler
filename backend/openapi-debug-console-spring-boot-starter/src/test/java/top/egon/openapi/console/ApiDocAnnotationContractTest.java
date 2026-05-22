/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @FileName: ApiDocAnnotationContractTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: OpenAPI 自有注解契约测试文件
 * @Version: 1.0
 */
package top.egon.openapi.console;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import top.egon.openapi.console.annotation.DocExampleMode;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocTypeReference;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @ClassName: ApiDocAnnotationContractTest
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: OpenAPI 自有注解契约测试
 * @Version: 1.0
 */
class ApiDocAnnotationContractTest {

    /**
     * 测试 DocOperation 使用 request 和单 response 契约
     */
    @Test
    void testDocOperationUsesRequestAndSingleResponseContract() throws NoSuchMethodException {
        Assertions.assertNotNull(DocOperation.class.getMethod("request"));
        Assertions.assertNotNull(DocOperation.class.getMethod("response"));
        Assertions.assertThrows(NoSuchMethodException.class, () -> DocOperation.class.getMethod("responses"));
    }

    /**
     * 测试 DocResponse 使用 dataType 和单 wrapper 契约
     */
    @Test
    void testDocResponseUsesDataTypeAndSingleWrapperContract() throws NoSuchMethodException {
        Assertions.assertNotNull(DocResponse.class.getMethod("dataType"));
        Assertions.assertNotNull(DocResponse.class.getMethod("wrapper"));
        Assertions.assertThrows(NoSuchMethodException.class, () -> DocResponse.class.getMethod("wrappers"));
    }

    /**
     * 测试旧契约类型不再暴露
     */
    @Test
    void testOldContractTypesRemainAbsent() {
        Assertions.assertThrows(ClassNotFoundException.class, () -> Class.forName("top.egon.openapi.console.annotation.DocSchemaRef"));
        Assertions.assertThrows(ClassNotFoundException.class, () -> Class.forName("top.egon.openapi.console.annotation.DocOperationParameter"));
        Assertions.assertThrows(ClassNotFoundException.class, () -> Class.forName("top.egon.openapi.console.annotation.DocRequestBody"));
        Assertions.assertThrows(ClassNotFoundException.class, () -> Class.forName("top.egon.openapi.console.annotation.DocFormField"));
        Assertions.assertThrows(ClassNotFoundException.class, () -> Class.forName("top.egon.openapi.console.annotation.DocResponseHeader"));
    }

    /**
     * 测试 DocField 支持字段示例
     */
    @Test
    void testDocFieldSupportsExampleContract() throws NoSuchMethodException {
        Assertions.assertEquals(String.class, DocField.class.getMethod("example").getReturnType());
        Assertions.assertEquals(DocExampleMode.class, DocField.class.getMethod("exampleMode").getReturnType());
    }

    /**
     * 测试 DocTypeReference 可以捕获复杂泛型
     */
    @Test
    void testDocTypeReferenceCapturesGenericDataType() {
        Type genericSuperclass = UserPageDataType.class.getGenericSuperclass();
        Assertions.assertInstanceOf(ParameterizedType.class, genericSuperclass);
        Type actualType = ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
        Assertions.assertInstanceOf(ParameterizedType.class, actualType);
        Assertions.assertEquals(PageResultDoc.class, ((ParameterizedType) actualType).getRawType());
    }

    static final class UserPageDataType extends DocTypeReference<PageResultDoc<UserDocResponse>> {
    }

    static class PageResultDoc<T> {

        private List<T> records;
    }

    static class UserDocResponse {
    }
}
