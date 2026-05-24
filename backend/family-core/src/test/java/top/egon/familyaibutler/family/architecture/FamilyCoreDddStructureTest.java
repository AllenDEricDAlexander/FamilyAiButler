/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.architecture
 * @FileName: FamilyCoreDddStructureTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: family-core DDD 分层结构测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.family.architecture;

import org.junit.jupiter.api.Test;
import top.egon.familyaibutler.family.domain.passwordview.model.valueobject.StrengthDTO;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocParameter;
import top.egon.openapi.console.annotation.DocTypeReference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
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
 * @BelongsPackage: top.egon.familyaibutler.family.architecture
 * @ClassName: FamilyCoreDddStructureTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: family-core DDD 分层结构测试
 * @Version: 1.0
 */
class FamilyCoreDddStructureTest {

    /**
     * 校验核心业务模块具备代码生成器风格的 DDD 分层入口。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldProvideGeneratedStyleDddLayers() throws Exception {
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/adapter/web/PasswordViewController.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/adapter/web/CategoryController.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/adapter/web/assembler/PasswordViewWebAssembler.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/application/manage/PasswordViewManage.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/application/manage/CategoryManage.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/application/manage/impl/PasswordViewManageImpl.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/application/manage/impl/CategoryManageImpl.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/application/executor/command/PasswordViewCommandExe.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/application/executor/query/PasswordViewQueryExe.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/application/command/CreatePasswordViewCommand.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/application/query/PasswordViewPageQuery.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/application/result/PasswordViewDTO.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/domain/passwordview/model/aggregate/PasswordView.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/domain/category/model/aggregate/Category.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/domain/passwordview/gateway/PasswordViewGateway.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/domain/passwordview/service/PasswordDomainService.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/infrastructure/gateway/impl/PasswordViewGatewayImpl.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/infrastructure/persistence/mp/converter/PasswordViewMpConverter.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/infrastructure/persistence/jpa/converter/CategoryJpaConverter.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/infrastructure/persistence/mp/mapper/PasswordViewMapper.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/infrastructure/persistence/mp/dataobject/PasswordViewPO.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/infrastructure/persistence/mp/service/PasswordViewService.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/infrastructure/persistence/jpa/dataobject/CategoryPo.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/infrastructure/persistence/jpa/repository/CategoryRepository.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/infrastructure/persistence/jpa/service/CategoryService.java")).exists();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/controller/PasswordViewController.java")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/adapter/PasswordViewController.java")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/application/PasswordViewManage.java")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/application/dto")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/domain/model")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/domain/gateway")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/infrastructure/gatewayimpl")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/client")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/app")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/service")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/infrastructure/persistence/service")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/mapper")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/po")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/repository")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/domain/repository")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/infrastructure/persistence/impl")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/configuration")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/enums")).doesNotExist();
        assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/domain/dto")).doesNotExist();
    }

    /**
     * Controller 只做 Web 适配，不直接拼 MyBatis 查询条件或调用旧实现静态方法。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void controllerShouldDelegateUseCasesToApplicationLayer() throws Exception {
        String controller = Files.readString(Path.of("src/main/java/top/egon/familyaibutler/family/adapter/web/PasswordViewController.java"));
        assertThat(controller)
                .doesNotContain("QueryWrapper")
                .doesNotContain("PasswordViewManageImpl.")
                .doesNotContain("family.application.manage.impl.PasswordViewManageImpl")
                .contains("PasswordViewManage");
        String categoryController = Files.readString(Path.of("src/main/java/top/egon/familyaibutler/family/adapter/web/CategoryController.java"));
        assertThat(categoryController)
                .doesNotContain("family.application.manage.impl.CategoryManageImpl")
                .contains("CategoryManage")
                .contains("CategoryTypeManage");
    }

    /**
     * 校验 Web 适配层直接返回的值对象字段具备文档示例。
     */
    @Test
    void adapterValueObjectFieldsShouldProvideDocFieldExamples() {
        for (Field field : StrengthDTO.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                DocField docField = field.getAnnotation(DocField.class);
                assertThat(docField)
                        .as(StrengthDTO.class.getName() + "." + field.getName())
                        .isNotNull();
                assertThat(docField.example())
                        .as(StrengthDTO.class.getName() + "." + field.getName() + " example")
                        .isNotBlank();
            }
        }
    }

    /**
     * 校验文档引用的 DTO/VO 模型具备唯一且完整的 DocModel 元数据。
     */
    @Test
    void docReferencedModelsShouldProvideUniqueDocModelMetadata() {
        Map<String, Class<?>> modelNames = new LinkedHashMap<>();
        for (Class<?> modelType : adapterDocDataModelTypes()) {
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
     * 校验核心业务模块没有回退到旧的根包目录。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void shouldNotContainForbiddenRootPackages() throws Exception {
        List<String> forbiddenPackages = List.of(
                "controller",
                "service",
                "mapper",
                "po",
                "do",
                "repository",
                "configuration",
                "enums",
                "utils",
                "domain/dto",
                "domain/repository",
                "infrastructure/persistence/impl",
                "adapter/rpc/grpc"
        );
        for (String forbiddenPackage : forbiddenPackages) {
            assertThat(Path.of("src/main/java/top/egon/familyaibutler/family/" + forbiddenPackage)).doesNotExist();
        }
    }

    /**
     * 校验领域层不依赖 Web、应用 DTO、基础设施和持久化技术对象。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void domainShouldNotDependOnOuterLayers() throws Exception {
        Path domainPath = Path.of("src/main/java/top/egon/familyaibutler/family/domain");
        try (Stream<Path> files = Files.walk(domainPath)) {
            List<String> domainSources = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
            assertThat(String.join("\n", domainSources))
                    .doesNotContain("org.springframework.web")
                    .doesNotContain("top.egon.familyaibutler.family.application.dto")
                    .doesNotContain("top.egon.familyaibutler.family.infrastructure")
                    .doesNotContain("top.egon.familyaibutler.family.adapter")
                    .doesNotContain("Mapper")
                    .doesNotContain("Repository")
                    .doesNotContain("PO");
        }
    }

    /**
     * 校验 Web 适配层不直接依赖持久化 Mapper 或 Repository。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void adapterShouldNotAccessPersistenceDirectly() throws Exception {
        Path adapterPath = Path.of("src/main/java/top/egon/familyaibutler/family/adapter");
        try (Stream<Path> files = Files.walk(adapterPath)) {
            List<String> adapterSources = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
            assertThat(String.join("\n", adapterSources))
                    .doesNotContain("top.egon.familyaibutler.family.infrastructure")
                    .doesNotContain(".infrastructure.persistence.mp.mapper")
                    .doesNotContain(".infrastructure.persistence.jpa.repository")
                    .doesNotContain("QueryWrapper");
        }
    }

    /**
     * 校验应用层不反向依赖 Web 适配对象。
     *
     * @throws Exception 文件读写异常
     */
    @Test
    void applicationShouldNotDependOnWebAdapter() throws Exception {
        Path applicationPath = Path.of("src/main/java/top/egon/familyaibutler/family/application");
        try (Stream<Path> files = Files.walk(applicationPath)) {
            List<String> applicationSources = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::readSource)
                    .toList();
            assertThat(String.join("\n", applicationSources))
                    .doesNotContain("org.springframework.web")
                    .doesNotContain("top.egon.familyaibutler.family.adapter")
                    .doesNotContain("top.egon.familyaibutler.family.infrastructure")
                    .doesNotContain("com.baomidou.mybatisplus")
                    .doesNotContain("jakarta.persistence");
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
     * 获取适配层文档数据类型引用的受控模型类型。
     *
     * @return List 返回受控模型类型
     */
    private List<Class<?>> adapterDocDataModelTypes() {
        Set<Class<?>> modelTypes = new LinkedHashSet<>();
        for (Class<?> adapterType : adapterTypes()) {
            for (Method method : adapterType.getDeclaredMethods()) {
                DocOperation docOperation = method.getAnnotation(DocOperation.class);
                if (docOperation == null) {
                    continue;
                }
                collectDocDataModelTypes(docOperation.request().body().dataType(), modelTypes);
                for (DocParameter parameter : docOperation.request().params()) {
                    collectDocDataModelTypes(parameter.dataType(), modelTypes);
                }
                collectDocDataModelTypes(docOperation.response().dataType(), modelTypes);
            }
        }
        return List.copyOf(modelTypes);
    }

    /**
     * 获取适配层类型。
     *
     * @return List 返回适配层类型列表
     */
    private List<Class<?>> adapterTypes() {
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/top/egon/familyaibutler/family/adapter/web"), 1)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .map(this::toClassName)
                    .map(this::loadClass)
                    .toList();
        } catch (Exception exception) {
            throw new IllegalStateException("加载适配层类型失败", exception);
        }
    }

    /**
     * 转换 Java 源码路径为类型全限定名。
     *
     * @param path Java 源码路径
     * @return String 返回类型全限定名
     */
    private String toClassName(Path path) {
        return path.toString()
                .replace("src/main/java/", "")
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
     * 收集文档数据类型中的受控模型类型。
     *
     * @param docDataType 文档数据类型
     * @param modelTypes  受控模型类型集合
     */
    private void collectDocDataModelTypes(DocDataType docDataType, Set<Class<?>> modelTypes) {
        if (isControlledModelType(docDataType.type())) {
            modelTypes.add(docDataType.type());
        }
        if (docDataType.ref() != DocTypeReference.None.class) {
            collectDocTypeReferenceModelTypes(docDataType.ref(), modelTypes);
        }
    }

    /**
     * 收集复杂泛型引用中的受控模型类型。
     *
     * @param referenceType 复杂泛型引用类型
     * @param modelTypes    受控模型类型集合
     */
    private void collectDocTypeReferenceModelTypes(Class<? extends DocTypeReference<?>> referenceType, Set<Class<?>> modelTypes) {
        Type genericSuperclass = referenceType.getGenericSuperclass();
        if (!(genericSuperclass instanceof ParameterizedType parameterizedType)) {
            return;
        }
        for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
            collectModelTypesFromGenericType(actualTypeArgument, modelTypes);
        }
    }

    /**
     * 收集泛型参数树中的受控模型类型。
     *
     * @param genericType 泛型参数
     * @param modelTypes  受控模型类型集合
     */
    private void collectModelTypesFromGenericType(Type genericType, Set<Class<?>> modelTypes) {
        if (genericType instanceof Class<?> clazz) {
            if (isControlledModelType(clazz)) {
                modelTypes.add(clazz);
            }
            return;
        }
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?> clazz && isControlledModelType(clazz)) {
                modelTypes.add(clazz);
            }
            for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
                collectModelTypesFromGenericType(actualTypeArgument, modelTypes);
            }
        }
    }

    /**
     * 判断是否受控文档模型类型。
     *
     * @param type Java 类型
     * @return boolean 返回 true 表示受控模型类型
     */
    private boolean isControlledModelType(Class<?> type) {
        return type != Void.class
                && type.getName().startsWith("top.egon.familyaibutler.family.")
                && (type.getSimpleName().endsWith("DTO")
                || type.getSimpleName().endsWith("VO")
                || type.getSimpleName().endsWith("Request")
                || type.getSimpleName().endsWith("Response"));
    }
}
