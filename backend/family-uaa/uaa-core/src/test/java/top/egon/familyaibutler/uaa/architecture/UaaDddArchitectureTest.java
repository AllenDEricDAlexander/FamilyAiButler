/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.architecture
 * @FileName: UaaDddArchitectureTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: uaa-core DDD 分层结构测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.architecture;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocParameter;
import top.egon.openapi.console.annotation.DocService;
import top.egon.openapi.console.annotation.DocTypeReference;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.architecture
 * @ClassName: UaaDddArchitectureTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: uaa-core DDD 分层结构测试
 * @Version: 1.0
 */
class UaaDddArchitectureTest {

    /**
     * 校验认证授权核心模块具备 DDD 四层结构。
     *
     * @throws Exception 文件读取异常
     */
    @Test
    void shouldProvideDddLayers() throws Exception {
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/UserAuthenticationAuthorizationApplication.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/adapter/web/AccountController.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/adapter/rpc/dubbo/AuthDubboAdapter.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/manage/AuthManage.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/manage/impl/AuthManageImpl.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/executor/command/AccountCommandExe.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/executor/query/AccountQueryExe.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/command/account/RegisterAccountCommand.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/result/account/AccountResponse.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/account/model/aggregate/Account.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/auth/model/aggregate/AuthSession.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/oauth/model/aggregate/OAuthClient.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/rbac/model/aggregate/Role.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/account/gateway/AccountGateway.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/auth/gateway/TokenGateway.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/infrastructure/gateway/impl/MpAccountGatewayImpl.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/infrastructure")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/adapter/AccountController.java")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/AuthServiceI.java")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/application/AuthServiceImpl.java")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/model")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/gateway")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/domain/service")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/infrastructure/gatewayimpl")).doesNotExist();
    }

    /**
     * 校验认证授权核心模块没有旧式根包目录。
     */
    @Test
    void shouldNotContainForbiddenRootPackages() {
        List<String> forbiddenPackages = List.of(
                "controller",
                "service",
                "mapper",
                "po",
                "do",
                "repository",
                "configuration",
                "enums",
                "filter",
                "utils",
                "vo",
                "domain/dto",
                "domain/repository",
                "infrastructure/persistence/impl",
                "adapter/rpc/grpc"
        );
        for (String forbiddenPackage : forbiddenPackages) {
            assertThat(Path.of("src/main/java/top/egon/familyaibutler/uaa/" + forbiddenPackage)).doesNotExist();
        }
    }

    /**
     * 校验领域层不依赖外层实现。
     *
     * @throws Exception 文件读取异常
     */
    @Test
    void domainShouldNotDependOnOuterLayers() throws Exception {
        Path domainPath = Path.of("src/main/java/top/egon/familyaibutler/uaa/domain");
        try (Stream<Path> files = Files.walk(domainPath)) {
            List<String> sources = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
            assertThat(String.join("\n", sources))
                    .doesNotContain("org.springframework.web")
                    .doesNotContain("top.egon.familyaibutler.uaa.application.dto")
                    .doesNotContain("top.egon.familyaibutler.uaa.infrastructure")
                    .doesNotContain("top.egon.familyaibutler.uaa.adapter.web")
                    .doesNotContain("Mapper")
                    .doesNotContain("Repository")
                    .doesNotContain("PO");
        }
    }

    /**
     * 校验应用层不直接承载 facade 契约 Provider。
     *
     * @throws Exception 文件读取异常
     */
    @Test
    void applicationShouldNotImplementFacadeContracts() throws Exception {
        Path applicationPath = Path.of("src/main/java/top/egon/familyaibutler/uaa/application");
        try (Stream<Path> files = Files.walk(applicationPath)) {
            List<String> sources = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
            assertThat(String.join("\n", sources))
                    .doesNotContain("implements AccountManage, AccountFacade")
                    .doesNotContain("implements AuthManage, AuthFacade")
                    .doesNotContain("implements AuthorizationManage, AuthorizationFacade")
                    .doesNotContain("implements ProfileManage, ProfileFacade")
                    .doesNotContain("implements TokenManage, TokenFacade")
                    .doesNotContain("extends OAuthClientFacade")
                    .doesNotContain("extends RbacFacade");
        }
    }

    /**
     * 校验业务适配层使用项目自有文档注解，且不依赖 Swagger / Springdoc 注解。
     *
     * @throws Exception 文件读取异常
     */
    @Test
    void businessAdapterShouldUseProjectDocAnnotationsAndRejectSwaggerAnnotations() throws Exception {
        for (Path adapterPath : businessAdapterPaths()) {
            try (Stream<Path> files = Files.walk(adapterPath)) {
                List<String> sources = files.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".java"))
                        .map(this::readSource)
                        .toList();
                assertThat(String.join("\n", sources))
                        .as(adapterPath + " should not use swagger or spring" + "doc annotations")
                        .doesNotContain("io." + "swagger")
                        .doesNotContain("spring" + "doc")
                        .doesNotContain("spring" + "fox")
                        .doesNotContain("org." + "spring" + "doc")
                        .doesNotContain("@" + "Tag")
                        .doesNotContain("@" + "Operation")
                        .doesNotContain("@" + "Parameter")
                        .doesNotContain("@" + "Schema")
                        .doesNotContain("@" + "ApiResponse")
                        .doesNotContain("@" + "ApiResponses");
            }
        }

        for (Class<?> adapterClass : businessAdapterTypes()) {
            if (adapterClass.getPackageName().endsWith(".adapter.web")) {
                assertThat(adapterClass.isAnnotationPresent(RestController.class)).isTrue();
            }
            assertThat(adapterClass.isAnnotationPresent(DocService.class))
                    .as(adapterClass.getName() + " should have @DocService")
                    .isTrue();
            for (Method method : adapterClass.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) && !method.isSynthetic()) {
                    assertThat(method.isAnnotationPresent(DocOperation.class))
                            .as(adapterClass.getName() + "." + method.getName() + " should have @DocOperation")
                            .isTrue();
                }
            }
        }
    }

    /**
     * 校验 Dubbo 适配层使用项目自有文档注解，且不依赖 Swagger / Springdoc 注解。
     *
     * @throws Exception 文件读取异常
     */
    @Test
    void dubboAdapterShouldUseProjectDocAnnotationsAndRejectSwaggerAnnotations() throws Exception {
        Path dubboAdapterPath = Path.of("src/main/java/top/egon/familyaibutler/uaa/adapter/rpc/dubbo");
        try (Stream<Path> files = Files.walk(dubboAdapterPath)) {
            List<String> sources = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
            assertThat(String.join("\n", sources))
                    .as(dubboAdapterPath + " should not use swagger or spring" + "doc annotations")
                    .doesNotContain("io." + "swagger")
                    .doesNotContain("spring" + "doc")
                    .doesNotContain("spring" + "fox")
                    .doesNotContain("org." + "spring" + "doc")
                    .doesNotContain("@" + "Tag")
                    .doesNotContain("@" + "Operation")
                    .doesNotContain("@" + "Parameter")
                    .doesNotContain("@" + "Schema")
                    .doesNotContain("@" + "ApiResponse")
                    .doesNotContain("@" + "ApiResponses");
        }

        for (Class<?> adapterClass : dubboAdapterTypes()) {
            assertThat(adapterClass.isAnnotationPresent(DocService.class))
                    .as(adapterClass.getName() + " should have @DocService")
                    .isTrue();
            for (Method method : adapterClass.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) && !method.isSynthetic()) {
                    assertThat(method.isAnnotationPresent(DocOperation.class))
                            .as(adapterClass.getName() + "." + method.getName() + " should have @DocOperation")
                            .isTrue();
                }
            }
        }
    }

    /**
     * 校验 Web 适配层和 facade DTO 记录对象字段具备文档字段示例。
     */
    @Test
    void webDtoRecordFieldsShouldProvideDocFieldExamples() {
        for (Class<?> recordType : adapterDocDataRecordTypes()) {
            if (recordType.getName().startsWith("top.egon.familyaibutler.uaa.facade.dto.")) {
                continue;
            }
            assertThat(recordType.isRecord()).isTrue();
            for (RecordComponent recordComponent : recordType.getRecordComponents()) {
                DocField docField = recordComponent.getAnnotation(DocField.class);
                assertThat(docField)
                        .as(recordType.getName() + "." + recordComponent.getName())
                        .isNotNull();
                assertThat(docField.example())
                        .as(recordType.getName() + "." + recordComponent.getName() + " example")
                        .isNotBlank();
            }
        }
        assertFacadeDtoSourcesHaveDocFieldExamples();
    }

    /**
     * 校验 Web 文档引用和 facade DTO 记录具备唯一且完整的 DocModel 元数据。
     */
    @Test
    void docReferencedRecordsShouldProvideUniqueDocModelMetadata() {
        Map<String, Class<?>> modelNames = new LinkedHashMap<>();
        for (Class<?> modelType : controlledDocModelTypes()) {
            DocModel docModel = modelType.getAnnotation(DocModel.class);
            assertThat(docModel)
                    .as(modelType.getName() + " should have @DocModel")
                    .isNotNull();
            assertThat(docModel.name())
                    .as(modelType.getName() + " @DocModel.name")
                    .isNotBlank()
                    .matches("^[A-Z][A-Za-z0-9]*$");
            assertThat(docModel.description())
                    .as(modelType.getName() + " @DocModel.description")
                    .isNotBlank();
            Class<?> existingType = modelNames.putIfAbsent(docModel.name(), modelType);
            assertThat(existingType)
                    .as(docModel.name() + " should be unique")
                    .isNull();
        }
    }

    /**
     * 读取 Java 源码文件内容。
     *
     * @param path Java 源码路径
     * @return Java 源码文本
     */
    private String readSource(Path path) {
        try {
            return Files.readString(path);
        } catch (Exception exception) {
            throw new IllegalStateException("读取源码文件失败: " + path, exception);
        }
    }

    /**
     * 获取业务适配层源码路径。
     *
     * @return List 返回业务适配层源码路径
     */
    private List<Path> businessAdapterPaths() {
        return List.of(
                Path.of("src/main/java/top/egon/familyaibutler/uaa/adapter/web"),
                Path.of("src/main/java/top/egon/familyaibutler/uaa/adapter/rpc/dubbo")
        );
    }

    /**
     * 获取业务适配层类型。
     *
     * @return List 返回业务适配层类型列表
     */
    private List<Class<?>> businessAdapterTypes() {
        return businessAdapterPaths().stream()
                .flatMap(adapterPath -> loadAdapterTypes(adapterPath).stream())
                .toList();
    }

    /**
     * 获取 Dubbo 适配层类型。
     *
     * @return List 返回 Dubbo 适配层类型列表
     */
    private List<Class<?>> dubboAdapterTypes() {
        return loadAdapterTypes(Path.of("src/main/java/top/egon/familyaibutler/uaa/adapter/rpc/dubbo"));
    }

    /**
     * 加载适配层类型。
     *
     * @param adapterPath 适配层源码路径
     * @return List 返回适配层类型列表
     */
    private List<Class<?>> loadAdapterTypes(Path adapterPath) {
        try (Stream<Path> files = Files.walk(adapterPath, 1)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::toClassName)
                    .map(this::loadClass)
                    .toList();
        } catch (Exception exception) {
            throw new IllegalStateException("加载适配层类型失败: " + adapterPath, exception);
        }
    }

    /**
     * 转换 Java 源码路径为类型全限定名。
     *
     * @param path Java 源码路径
     * @return String 返回类型全限定名
     */
    private String toClassName(Path path) {
        String sourcePath = path.toString();
        int sourceRootIndex = sourcePath.indexOf("src/main/java/");
        return sourcePath.substring(sourceRootIndex + "src/main/java/".length())
                .replace("/", ".")
                .replace(".java", "");
    }

    /**
     * 加载 Java 类型。
     *
     * @param className 类型全限定名
     * @return Class 返回 Java 类型
     */
    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (Exception exception) {
            throw new IllegalStateException("加载 Java 类型失败: " + className, exception);
        }
    }

    /**
     * 获取业务适配层文档数据类型直接指向的记录类型。
     *
     * @return List 返回记录类型列表
     */
    private List<Class<?>> adapterDocDataRecordTypes() {
        Set<Class<?>> recordTypes = new LinkedHashSet<>();
        for (Class<?> adapterType : businessAdapterTypes()) {
            for (Method method : adapterType.getDeclaredMethods()) {
                DocOperation docOperation = method.getAnnotation(DocOperation.class);
                if (docOperation == null) {
                    continue;
                }
                collectDocDataRecordTypes(docOperation.request().body().dataType(), recordTypes);
                for (DocParameter parameter : docOperation.request().params()) {
                    collectDocDataRecordTypes(parameter.dataType(), recordTypes);
                }
                collectDocDataRecordTypes(docOperation.response().dataType(), recordTypes);
            }
        }
        return List.copyOf(recordTypes);
    }

    /**
     * 获取受控 DocModel 类型。
     *
     * @return List 返回受控模型类型
     */
    private List<Class<?>> controlledDocModelTypes() {
        Set<Class<?>> modelTypes = new LinkedHashSet<>(adapterDocDataRecordTypes());
        modelTypes.addAll(facadeDtoRecordTypes());
        return List.copyOf(modelTypes);
    }

    /**
     * 获取 facade DTO 包下所有 public record 类型。
     *
     * @return List 返回 facade DTO 记录类型
     */
    private List<Class<?>> facadeDtoRecordTypes() {
        Path facadeDtoPath = Path.of("../uaa-facade/src/main/java/top/egon/familyaibutler/uaa/facade/dto");
        try (Stream<Path> files = Files.walk(facadeDtoPath)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> readSource(path).contains("public record "))
                    .map(this::toClassName)
                    .map(this::loadClass)
                    .filter(Class::isRecord)
                    .toList();
        } catch (Exception exception) {
            throw new IllegalStateException("加载 facade DTO 记录类型失败: " + facadeDtoPath, exception);
        }
    }

    /**
     * 校验 facade DTO 包下所有记录类型字段具备文档字段示例。
     */
    private void assertFacadeDtoSourcesHaveDocFieldExamples() {
        Path facadeDtoPath = Path.of("../uaa-facade/src/main/java/top/egon/familyaibutler/uaa/facade/dto");
        try (Stream<Path> files = Files.walk(facadeDtoPath)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(this::assertFacadeDtoSourceHasDocFieldExamples);
        } catch (Exception exception) {
            throw new IllegalStateException("读取 facade DTO 记录源码失败: " + facadeDtoPath, exception);
        }
    }

    /**
     * 校验 facade DTO 记录源码字段具备文档字段示例。
     *
     * @param path Java 源码路径
     */
    private void assertFacadeDtoSourceHasDocFieldExamples(Path path) {
        String source = readSource(path);
        if (!source.contains("public record ")) {
            return;
        }
        int recordIndex = source.indexOf("public record ");
        int componentsStartIndex = source.indexOf("(", recordIndex);
        int componentsEndIndex = source.indexOf(") {", componentsStartIndex);
        String componentsSource = source.substring(componentsStartIndex + 1, componentsEndIndex);
        List<String> recordComponents = componentsSource.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> !line.startsWith("@DocField"))
                .filter(line -> line.contains(" "))
                .toList();
        assertThat(source)
                .as(path + " should import DocField")
                .contains("top.egon.openapi.console.annotation.DocField");
        long docFieldCount = source.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("@DocField(description = "))
                .filter(line -> line.contains(", example = "))
                .count();
        assertThat(docFieldCount)
                .as(path + " DocField annotation count")
                .isEqualTo(recordComponents.size());
    }

    /**
     * 收集文档数据类型中的记录类型。
     *
     * @param docDataType 文档数据类型
     * @param recordTypes 记录类型集合
     */
    private void collectDocDataRecordTypes(DocDataType docDataType, Set<Class<?>> recordTypes) {
        if (docDataType.type().isRecord()) {
            recordTypes.add(docDataType.type());
        }
        if (docDataType.ref() != DocTypeReference.None.class) {
            collectDocTypeReferenceRecordTypes(docDataType.ref(), recordTypes);
        }
    }

    /**
     * 收集复杂泛型引用中的记录类型。
     *
     * @param referenceType 复杂泛型引用类型
     * @param recordTypes   记录类型集合
     */
    private void collectDocTypeReferenceRecordTypes(Class<? extends DocTypeReference<?>> referenceType, Set<Class<?>> recordTypes) {
        Type genericSuperclass = referenceType.getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType parameterizedType)) {
            return;
        }
        for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
            collectRecordTypesFromGenericType(actualTypeArgument, recordTypes);
        }
    }

    /**
     * 收集泛型参数树中的记录类型。
     *
     * @param genericType 泛型参数
     * @param recordTypes 记录类型集合
     */
    private void collectRecordTypesFromGenericType(Type genericType, Set<Class<?>> recordTypes) {
        if (genericType instanceof Class<?> clazz && clazz.isRecord()) {
            recordTypes.add(clazz);
            return;
        }
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> clazz && clazz.isRecord()) {
                recordTypes.add(clazz);
            }
            for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
                collectRecordTypesFromGenericType(actualTypeArgument, recordTypes);
            }
        }
    }
}
