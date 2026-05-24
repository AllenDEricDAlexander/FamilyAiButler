/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.openapi
 * @FileName: ApiDocOpenApiSchemaGenerator.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:35
 * @Description: OpenAPI Schema 生成器文件
 * @Version: 1.0
 */
package top.egon.openapi.console.openapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ResolvableType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import top.egon.openapi.console.ApiDocConsoleProperties;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocExampleMode;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocIgnore;
import top.egon.openapi.console.annotation.DocModel;
import top.egon.openapi.console.annotation.DocTypeReference;
import top.egon.openapi.console.annotation.DocWrapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.openapi
 * @ClassName: ApiDocOpenApiSchemaGenerator
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:35
 * @Description: OpenAPI Schema 生成器
 * @Version: 1.0
 */
public class ApiDocOpenApiSchemaGenerator {

    private static final Object NO_EXAMPLE = new Object();

    private final ObjectMapper objectMapper;

    private final Map<String, Object> schemas = new LinkedHashMap<>();

    private final Map<String, Class<?>> schemaSources = new LinkedHashMap<>();

    private final Set<String> generatingTypes = new LinkedHashSet<>();

    private ApiDocConsoleProperties.Policy examplePolicy = ApiDocConsoleProperties.Policy.WARN;

    public ApiDocOpenApiSchemaGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 设置示例校验策略
     *
     * @param examplePolicy 示例校验策略
     */
    public void setExamplePolicy(ApiDocConsoleProperties.Policy examplePolicy) {
        this.examplePolicy = examplePolicy == null ? ApiDocConsoleProperties.Policy.WARN : examplePolicy;
    }

    /**
     * 生成类型 Schema
     *
     * @param type Java 类型
     * @return Map<String, Object> 返回 OpenAPI Schema
     */
    public Map<String, Object> schema(Type type) {
        return schema(ResolvableType.forType(type));
    }

    /**
     * 生成方法参数 Schema
     *
     * @param parameter 方法参数
     * @return Map<String, Object> 返回 OpenAPI Schema
     */
    public Map<String, Object> schema(Parameter parameter) {
        return schema(ResolvableType.forType(parameter.getParameterizedType()));
    }

    /**
     * 根据文档数据类型生成 Schema
     *
     * @param dataType     文档数据类型
     * @param inferredType 推断类型
     * @return Map<String, Object> 返回 OpenAPI Schema
     */
    public Map<String, Object> schema(DocDataType dataType, Type inferredType) {
        if (dataType.kind() == DocDataKind.FILE || dataType.kind() == DocDataKind.BINARY) {
            return fileSchema();
        }
        Map<String, Object> schema = schema(resolveDataType(dataType, inferredType));
        if (StringUtils.hasText(dataType.dataPath())) {
            schema = new LinkedHashMap<>(schema);
            schema.put("x-doc-data-path", dataType.dataPath());
        }
        return schema;
    }

    /**
     * 根据文档数据类型和包装生成 Schema
     *
     * @param dataType     文档数据类型
     * @param wrapper      文档包装
     * @param inferredType 推断类型
     * @return Map<String, Object> 返回 OpenAPI Schema
     */
    public Map<String, Object> schema(DocDataType dataType, DocWrapper wrapper, Type inferredType) {
        ResolvableType resolvedDataType = resolveDataType(dataType, inferredType);
        Map<String, Object> schema = wrapper.type() == Void.class ? schema(resolvedDataType) : schema(wrappedType(wrapper, resolvedDataType));
        if (wrapper.type() != Void.class && StringUtils.hasText(wrapper.dataPath())) {
            schema = new LinkedHashMap<>(schema);
            schema.put("x-doc-data-path", wrapper.dataPath());
        }
        return schema;
    }

    /**
     * 生成文档数据类型示例
     *
     * @param dataType     文档数据类型
     * @param wrapper      文档包装
     * @param inferredType 推断类型
     * @param example      显式示例
     * @param exampleMode  示例解析方式
     * @return Object 返回示例
     */
    public Object example(DocDataType dataType, DocWrapper wrapper, Type inferredType, String example, DocExampleMode exampleMode) {
        if (StringUtils.hasText(example)) {
            return parseExample(example, exampleMode, resolveDataType(dataType, inferredType));
        }
        ResolvableType resolvedDataType = resolveDataType(dataType, inferredType);
        return example(wrapper.type() == Void.class ? resolvedDataType : wrappedType(wrapper, resolvedDataType));
    }

    /**
     * 生成文档数据类型示例
     *
     * @param dataType     文档数据类型
     * @param inferredType 推断类型
     * @param example      显式示例
     * @param exampleMode  示例解析方式
     * @return Object 返回示例
     */
    public Object example(DocDataType dataType, Type inferredType, String example, DocExampleMode exampleMode) {
        if (StringUtils.hasText(example)) {
            return parseExample(example, exampleMode, resolveDataType(dataType, inferredType));
        }
        return example(resolveDataType(dataType, inferredType));
    }

    /**
     * 获取组件 Schema
     *
     * @return Map<String, Object> 返回组件 Schema 集合
     */
    public Map<String, Object> getSchemas() {
        return schemas;
    }

    /**
     * 清理本次生成缓存
     */
    public void reset() {
        schemas.clear();
        schemaSources.clear();
        generatingTypes.clear();
    }

    /**
     * 生成 ResolvableType Schema
     *
     * @param type Spring 可解析类型
     * @return Map<String, Object> 返回 OpenAPI Schema
     */
    public Map<String, Object> schema(ResolvableType type) {
        Class<?> rawClass = resolveClass(type);
        if (rawClass == null || rawClass == Void.class || rawClass == Void.TYPE) {
            return Map.of("type", "object");
        }
        if (ResponseEntity.class.isAssignableFrom(rawClass)) {
            return schema(type.getGeneric(0));
        }
        if (Optional.class == rawClass) {
            return schema(type.getGeneric(0));
        }
        if (org.springframework.web.multipart.MultipartFile.class.isAssignableFrom(rawClass)) {
            return fileSchema();
        }
        if (rawClass.isArray()) {
            return arraySchema(schema(rawClass.getComponentType()));
        }
        if (Collection.class.isAssignableFrom(rawClass)) {
            return arraySchema(schema(type.getGeneric(0)));
        }
        if (Map.class.isAssignableFrom(rawClass)) {
            return mapSchema(schema(type.getGeneric(1)));
        }
        if (String.class == rawClass || Character.class == rawClass || Character.TYPE == rawClass) {
            return Map.of("type", "string");
        }
        if (Boolean.class == rawClass || Boolean.TYPE == rawClass) {
            return Map.of("type", "boolean");
        }
        if (Integer.class == rawClass || Integer.TYPE == rawClass || Short.class == rawClass || Short.TYPE == rawClass || Byte.class == rawClass || Byte.TYPE == rawClass) {
            return Map.of("type", "integer", "format", "int32");
        }
        if (Long.class == rawClass || Long.TYPE == rawClass || BigInteger.class == rawClass) {
            return Map.of("type", "integer", "format", "int64");
        }
        if (Float.class == rawClass || Float.TYPE == rawClass) {
            return Map.of("type", "number", "format", "float");
        }
        if (Double.class == rawClass || Double.TYPE == rawClass || BigDecimal.class == rawClass) {
            return Map.of("type", "number", "format", "double");
        }
        if (LocalDate.class == rawClass) {
            return Map.of("type", "string", "format", "date");
        }
        if (LocalDateTime.class == rawClass || Date.class.isAssignableFrom(rawClass)) {
            return Map.of("type", "string", "format", "date-time");
        }
        if (rawClass.isEnum()) {
            return enumSchema(rawClass);
        }
        if (isJavaType(rawClass)) {
            return Map.of("type", "object");
        }
        return componentRef(type);
    }

    /**
     * 生成类型示例
     *
     * @param type Spring 可解析类型
     * @return Object 返回类型示例
     */
    public Object example(ResolvableType type) {
        Class<?> rawClass = resolveClass(type);
        if (rawClass == null || rawClass == Void.class || rawClass == Void.TYPE) {
            return null;
        }
        if (ResponseEntity.class.isAssignableFrom(rawClass) || Optional.class == rawClass) {
            return example(type.getGeneric(0));
        }
        if (rawClass.isArray()) {
            return List.of(example(ResolvableType.forClass(rawClass.getComponentType())));
        }
        if (Collection.class.isAssignableFrom(rawClass)) {
            return List.of(example(type.getGeneric(0)));
        }
        if (Map.class.isAssignableFrom(rawClass)) {
            return Map.of("key", example(type.getGeneric(1)));
        }
        if (String.class == rawClass || Character.class == rawClass || Character.TYPE == rawClass) {
            return "string";
        }
        if (Boolean.class == rawClass || Boolean.TYPE == rawClass) {
            return true;
        }
        if (Integer.class == rawClass || Integer.TYPE == rawClass || Short.class == rawClass || Short.TYPE == rawClass || Byte.class == rawClass || Byte.TYPE == rawClass) {
            return 1;
        }
        if (Long.class == rawClass || Long.TYPE == rawClass || BigInteger.class == rawClass) {
            return 1L;
        }
        if (Float.class == rawClass || Float.TYPE == rawClass || Double.class == rawClass || Double.TYPE == rawClass || BigDecimal.class == rawClass) {
            return 1.0;
        }
        if (LocalDate.class == rawClass) {
            return "2026-05-22";
        }
        if (LocalDateTime.class == rawClass || Date.class.isAssignableFrom(rawClass)) {
            return "2026-05-22T00:00:00";
        }
        if (rawClass.isEnum() && rawClass.getEnumConstants().length > 0) {
            return rawClass.getEnumConstants()[0].toString();
        }
        if (isJavaType(rawClass)) {
            return Map.of();
        }
        return objectExample(type, rawClass);
    }

    /**
     * 解析文档数据类型
     *
     * @param dataType     文档数据类型
     * @param inferredType 推断类型
     * @return ResolvableType 返回解析后的类型
     */
    private ResolvableType resolveDataType(DocDataType dataType, Type inferredType) {
        return switch (dataType.kind()) {
            case VOID -> ResolvableType.forClass(Void.class);
            case BOOLEAN -> ResolvableType.forClass(Boolean.class);
            case INTEGER -> ResolvableType.forClass(Integer.class);
            case LONG -> ResolvableType.forClass(Long.class);
            case DECIMAL -> ResolvableType.forClass(BigDecimal.class);
            case STRING -> ResolvableType.forClass(String.class);
            case DATE -> ResolvableType.forClass(LocalDate.class);
            case DATETIME -> ResolvableType.forClass(LocalDateTime.class);
            case FILE, BINARY -> ResolvableType.forClass(org.springframework.web.multipart.MultipartFile.class);
            case ENUM, OBJECT ->
                    dataType.type() == Void.class ? ResolvableType.forType(inferredType) : ResolvableType.forClass(dataType.type());
            case ARRAY -> arrayType(dataType, inferredType);
            case MAP -> mapType(dataType, inferredType);
            case GENERIC -> ResolvableType.forType(typeReference(dataType.ref()));
            case AUTO -> autoType(dataType, inferredType);
        };
    }

    /**
     * 解析 AUTO 类型
     *
     * @param dataType     文档数据类型
     * @param inferredType 推断类型
     * @return ResolvableType 返回解析后的类型
     */
    private ResolvableType autoType(DocDataType dataType, Type inferredType) {
        if (dataType.ref() != DocTypeReference.None.class) {
            return ResolvableType.forType(typeReference(dataType.ref()));
        }
        if (dataType.type() != Void.class) {
            return ResolvableType.forClass(dataType.type());
        }
        if (dataType.itemType() != Void.class) {
            return ResolvableType.forClassWithGenerics(List.class, dataType.itemType());
        }
        if (dataType.valueType() != Void.class) {
            return ResolvableType.forClassWithGenerics(Map.class, dataType.keyType(), dataType.valueType());
        }
        return ResolvableType.forType(inferredType);
    }

    /**
     * 解析数组类型
     *
     * @param dataType     文档数据类型
     * @param inferredType 推断类型
     * @return ResolvableType 返回数组类型
     */
    private ResolvableType arrayType(DocDataType dataType, Type inferredType) {
        if (dataType.itemType() != Void.class) {
            return ResolvableType.forClassWithGenerics(List.class, dataType.itemType());
        }
        return ResolvableType.forType(inferredType);
    }

    /**
     * 解析 Map 类型
     *
     * @param dataType     文档数据类型
     * @param inferredType 推断类型
     * @return ResolvableType 返回 Map 类型
     */
    private ResolvableType mapType(DocDataType dataType, Type inferredType) {
        if (dataType.valueType() != Void.class) {
            return ResolvableType.forClassWithGenerics(Map.class, dataType.keyType(), dataType.valueType());
        }
        return ResolvableType.forType(inferredType);
    }

    /**
     * 解析复杂泛型引用
     *
     * @param referenceType 泛型引用类型
     * @return Type 返回实际泛型类型
     */
    private Type typeReference(Class<? extends DocTypeReference<?>> referenceType) {
        if (referenceType == DocTypeReference.None.class) {
            throw new IllegalStateException("DocTypeReference 未声明具体类型");
        }
        Type current = referenceType.getGenericSuperclass();
        while (current != null) {
            if (current instanceof ParameterizedType parameterizedType
                    && parameterizedType.getRawType() instanceof Class<?> rawType
                    && DocTypeReference.class.isAssignableFrom(rawType)) {
                return parameterizedType.getActualTypeArguments()[0];
            }
            if (current instanceof Class<?> currentClass) {
                current = currentClass.getGenericSuperclass();
            } else {
                break;
            }
        }
        throw new IllegalStateException("无法解析 DocTypeReference 泛型: " + referenceType.getName());
    }

    /**
     * 生成包装类型
     *
     * @param wrapper  文档包装
     * @param dataType 数据类型
     * @return ResolvableType 返回包装后的类型
     */
    private ResolvableType wrappedType(DocWrapper wrapper, ResolvableType dataType) {
        TypeVariable<?>[] typeParameters = wrapper.type().getTypeParameters();
        if (typeParameters.length == 0) {
            return ResolvableType.forClass(wrapper.type());
        }
        Type[] actualTypes = new Type[typeParameters.length];
        Arrays.fill(actualTypes, Object.class);
        int genericIndex = Math.max(0, Math.min(wrapper.genericIndex(), typeParameters.length - 1));
        actualTypes[genericIndex] = dataType.getType();
        return ResolvableType.forType(new SimpleParameterizedType(wrapper.type(), actualTypes));
    }

    /**
     * 生成数组 Schema
     *
     * @param itemSchema 元素 Schema
     * @return Map<String, Object> 返回数组 Schema
     */
    private Map<String, Object> arraySchema(Map<String, Object> itemSchema) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "array");
        schema.put("items", itemSchema);
        return schema;
    }

    /**
     * 生成 Map Schema
     *
     * @param valueSchema 值 Schema
     * @return Map<String, Object> 返回 Map Schema
     */
    private Map<String, Object> mapSchema(Map<String, Object> valueSchema) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", valueSchema);
        return schema;
    }

    /**
     * 生成文件 Schema
     *
     * @return Map<String, Object> 返回文件 Schema
     */
    public Map<String, Object> fileSchema() {
        return Map.of("type", "string", "format", "binary");
    }

    /**
     * 生成枚举 Schema
     *
     * @param enumClass 枚举类型
     * @return Map<String, Object> 返回枚举 Schema
     */
    private Map<String, Object> enumSchema(Class<?> enumClass) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "string");
        schema.put("enum", Arrays.stream(enumClass.getEnumConstants()).map(Object::toString).toList());
        return schema;
    }

    /**
     * 生成组件引用
     *
     * @param modelType 模型类型
     * @return Map<String, Object> 返回组件引用
     */
    private Map<String, Object> componentRef(ResolvableType modelType) {
        Class<?> modelClass = resolveClass(modelType);
        String schemaName = schemaName(modelType);
        assertSchemaNameNotConflicting(schemaName, modelClass);
        if (!schemas.containsKey(schemaName)) {
            schemas.put(schemaName, new LinkedHashMap<>());
            if (!generatingTypes.contains(schemaName)) {
                generatingTypes.add(schemaName);
                schemas.put(schemaName, objectSchema(modelType, modelClass));
                generatingTypes.remove(schemaName);
            }
        }
        return Map.of("$ref", "#/components/schemas/" + schemaName);
    }

    /**
     * 校验 Schema 名称没有被不同 Java 类型复用。
     *
     * @param schemaName Schema 名称
     * @param modelClass 模型类型
     */
    private void assertSchemaNameNotConflicting(String schemaName, Class<?> modelClass) {
        Class<?> existingClass = schemaSources.putIfAbsent(schemaName, modelClass);
        if (existingClass != null && existingClass != modelClass) {
            throw new IllegalStateException("OpenAPI Schema 名称冲突: " + schemaName
                    + " 已由 " + existingClass.getName()
                    + " 使用，不能再用于 " + modelClass.getName()
                    + "，请为其中一个类型补唯一 @DocModel(name=...)");
        }
    }

    /**
     * 生成对象 Schema
     *
     * @param modelType  模型类型
     * @param modelClass 模型类型
     * @return Map<String, Object> 返回对象 Schema
     */
    private Map<String, Object> objectSchema(ResolvableType modelType, Class<?> modelClass) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        String description = modelDescription(modelClass);
        if (StringUtils.hasText(description)) {
            schema.put("description", description);
        }
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        for (Field field : allFields(modelClass)) {
            if (ignoreField(field)) {
                continue;
            }
            Method getter = getter(modelClass, field);
            if (getter != null && ignoreAccessor(getter)) {
                continue;
            }
            DocField docField = docField(field, getter);
            ResolvableType fieldType = ResolvableType.forField(field, modelType);
            Map<String, Object> propertySchema = new LinkedHashMap<>(fieldSchema(docField, fieldType));
            fillFieldMetadata(propertySchema, field, getter, fieldType);
            properties.put(propertyName(field, getter), propertySchema);
            if (requiredField(field, getter)) {
                required.add(propertyName(field, getter));
            }
        }
        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        return schema;
    }

    /**
     * 生成字段 Schema
     *
     * @param docField  字段文档注解
     * @param fieldType 字段类型
     * @return Map<String, Object> 返回字段 Schema
     */
    private Map<String, Object> fieldSchema(DocField docField, ResolvableType fieldType) {
        if (docField != null && !defaultDataType(docField.dataType())) {
            return schema(docField.dataType(), fieldType.getType());
        }
        return schema(fieldType);
    }

    /**
     * 填充字段文档元数据
     *
     * @param propertySchema 属性 Schema
     * @param field          字段
     * @param getter         Getter 方法
     * @param fieldType      字段类型
     */
    private void fillFieldMetadata(Map<String, Object> propertySchema, Field field, Method getter, ResolvableType fieldType) {
        DocField docField = docField(field, getter);
        if (docField != null) {
            if (StringUtils.hasText(docField.description())) {
                propertySchema.put("description", docField.description());
            }
            if (docField.nullable()) {
                propertySchema.put("nullable", true);
            }
            if (docField.deprecated()) {
                propertySchema.put("deprecated", true);
            }
            Object example = fieldExample(docField, fieldType, field);
            if (example != NO_EXAMPLE) {
                propertySchema.put("example", example);
            }
        }
        fillValidationConstraints(propertySchema, field.getAnnotations());
        if (getter != null) {
            fillValidationConstraints(propertySchema, getter.getAnnotations());
        }
    }

    /**
     * 生成对象示例
     *
     * @param modelType  模型类型
     * @param modelClass 模型类型
     * @return Map<String, Object> 返回对象示例
     */
    private Map<String, Object> objectExample(ResolvableType modelType, Class<?> modelClass) {
        Map<String, Object> example = new LinkedHashMap<>();
        for (Field field : allFields(modelClass)) {
            if (ignoreField(field)) {
                continue;
            }
            Method getter = getter(modelClass, field);
            if (getter != null && ignoreAccessor(getter)) {
                continue;
            }
            DocField docField = docField(field, getter);
            ResolvableType fieldType = ResolvableType.forField(field, modelType);
            Object fieldExample = docField == null ? NO_EXAMPLE : fieldExample(docField, fieldType, field);
            if (fieldExample == NO_EXAMPLE) {
                fieldExample = example(fieldType);
            }
            example.put(propertyName(field, getter), fieldExample);
        }
        return example;
    }

    /**
     * 解析字段示例
     *
     * @param docField  字段文档注解
     * @param fieldType 字段类型
     * @param field     字段
     * @return Object 返回字段示例
     */
    private Object fieldExample(DocField docField, ResolvableType fieldType, Field field) {
        if (!StringUtils.hasText(docField.example())) {
            validateMissingExample(field, fieldType);
            return NO_EXAMPLE;
        }
        return parseExample(docField.example(), docField.exampleMode(), fieldType);
    }

    /**
     * 校验缺失示例
     *
     * @param field     字段
     * @param fieldType 字段类型
     */
    private void validateMissingExample(Field field, ResolvableType fieldType) {
        if (examplePolicy != ApiDocConsoleProperties.Policy.FAIL || !simpleExampleType(resolveClass(fieldType))) {
            return;
        }
        throw new IllegalStateException("DocField 缺少 example: " + field.getDeclaringClass().getName() + "." + field.getName());
    }

    /**
     * 解析示例字符串
     *
     * @param value       示例字符串
     * @param exampleMode 示例解析方式
     * @param type        字段类型
     * @return Object 返回示例值
     */
    private Object parseExample(String value, DocExampleMode exampleMode, ResolvableType type) {
        if (exampleMode == DocExampleMode.JSON) {
            try {
                return objectMapper.readValue(value, Object.class);
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException("DocField JSON example 非法: " + value, ex);
            }
        }
        if (exampleMode == DocExampleMode.STRING) {
            return value;
        }
        Class<?> rawClass = resolveClass(type);
        if (rawClass == null || rawClass == String.class || rawClass == Character.class || rawClass == Character.TYPE) {
            return value;
        }
        if (rawClass == Boolean.class || rawClass == Boolean.TYPE) {
            return Boolean.valueOf(value);
        }
        if (rawClass == Integer.class || rawClass == Integer.TYPE || rawClass == Short.class || rawClass == Short.TYPE || rawClass == Byte.class || rawClass == Byte.TYPE) {
            return Integer.valueOf(value);
        }
        if (rawClass == Long.class || rawClass == Long.TYPE || rawClass == BigInteger.class) {
            return Long.valueOf(value);
        }
        if (rawClass == Float.class || rawClass == Float.TYPE || rawClass == Double.class || rawClass == Double.TYPE || rawClass == BigDecimal.class) {
            return Double.valueOf(value);
        }
        if (rawClass.isEnum()) {
            return value;
        }
        return value;
    }

    /**
     * 判断是否默认文档数据类型
     *
     * @param dataType 文档数据类型
     * @return boolean 返回 true 表示默认数据类型
     */
    private boolean defaultDataType(DocDataType dataType) {
        return dataType.kind() == DocDataKind.AUTO
                && dataType.type() == Void.class
                && dataType.ref() == DocTypeReference.None.class
                && dataType.itemType() == Void.class
                && dataType.valueType() == Void.class
                && !StringUtils.hasText(dataType.dataPath());
    }

    /**
     * 判断是否简单示例类型
     *
     * @param type 类型
     * @return boolean 返回 true 表示简单示例类型
     */
    private boolean simpleExampleType(Class<?> type) {
        return type.isPrimitive()
                || String.class == type
                || Number.class.isAssignableFrom(type)
                || Boolean.class == type
                || Character.class == type
                || LocalDate.class == type
                || LocalDateTime.class == type
                || Date.class.isAssignableFrom(type)
                || type.isEnum();
    }

    /**
     * 判断字段是否必填
     *
     * @param field  字段
     * @param getter Getter 方法
     * @return boolean 返回 true 表示必填
     */
    private boolean requiredField(Field field, Method getter) {
        DocField docField = docField(field, getter);
        return (docField != null && docField.required()) || validationRequired(field.getAnnotations()) || (getter != null && validationRequired(getter.getAnnotations()));
    }

    /**
     * 获取字段文档注解
     *
     * @param field  字段
     * @param getter Getter 方法
     * @return DocField 返回字段文档注解
     */
    private DocField docField(Field field, Method getter) {
        DocField docField = field.getAnnotation(DocField.class);
        if (docField != null) {
            return docField;
        }
        return getter == null ? null : getter.getAnnotation(DocField.class);
    }

    /**
     * 获取模型名称
     *
     * @param modelClass 模型类型
     * @return String 返回模型名称
     */
    private String schemaName(Class<?> modelClass) {
        DocModel docModel = modelClass.getAnnotation(DocModel.class);
        if (docModel != null && StringUtils.hasText(docModel.name())) {
            return docModel.name();
        }
        return modelClass.getSimpleName();
    }

    /**
     * 获取 Schema 名称
     *
     * @param modelType 模型类型
     * @return String 返回 Schema 名称
     */
    private String schemaName(ResolvableType modelType) {
        Class<?> modelClass = resolveClass(modelType);
        ResolvableType[] generics = modelType.getGenerics();
        if (generics.length == 0) {
            return schemaName(modelClass);
        }
        StringBuilder schemaName = new StringBuilder(schemaName(modelClass));
        for (ResolvableType generic : generics) {
            schemaName.append(schemaNamePart(generic));
        }
        return schemaName.toString();
    }

    /**
     * 获取 Schema 名称片段
     *
     * @param type 类型
     * @return String 返回 Schema 名称片段
     */
    private String schemaNamePart(ResolvableType type) {
        Class<?> rawClass = type.resolve();
        if (rawClass != null && rawClass != Object.class) {
            if (type.getGenerics().length == 0) {
                return schemaName(rawClass);
            }
            return schemaName(type);
        }
        return schemaNamePart(type.getType());
    }

    /**
     * 获取 Schema 名称片段
     *
     * @param type Java 类型
     * @return String 返回 Schema 名称片段
     */
    private String schemaNamePart(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            StringBuilder name = new StringBuilder(schemaName((Class<?>) parameterizedType.getRawType()));
            for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
                name.append(schemaNamePart(actualTypeArgument));
            }
            return name.toString();
        }
        if (type instanceof Class<?> clazz) {
            return schemaName(clazz);
        }
        if (type instanceof GenericArrayType genericArrayType) {
            return "Array" + schemaNamePart(genericArrayType.getGenericComponentType());
        }
        if (type instanceof WildcardType wildcardType && wildcardType.getUpperBounds().length > 0) {
            return schemaNamePart(wildcardType.getUpperBounds()[0]);
        }
        if (type instanceof TypeVariable<?> typeVariable && typeVariable.getBounds().length > 0) {
            return schemaNamePart(typeVariable.getBounds()[0]);
        }
        return "Object";
    }

    /**
     * 获取模型描述
     *
     * @param modelClass 模型类型
     * @return String 返回模型描述
     */
    private String modelDescription(Class<?> modelClass) {
        DocModel docModel = modelClass.getAnnotation(DocModel.class);
        if (docModel != null && StringUtils.hasText(docModel.description())) {
            return docModel.description();
        }
        return "";
    }

    /**
     * 判断是否忽略字段
     *
     * @param field 字段
     * @return boolean 返回 true 表示忽略字段
     */
    private boolean ignoreField(Field field) {
        int modifiers = field.getModifiers();
        DocField docField = field.getAnnotation(DocField.class);
        return Modifier.isStatic(modifiers)
                || Modifier.isTransient(modifiers)
                || field.isSynthetic()
                || field.isAnnotationPresent(DocIgnore.class)
                || field.isAnnotationPresent(JsonIgnore.class)
                || (docField != null && docField.hidden());
    }

    /**
     * 判断是否忽略访问方法
     *
     * @param method 访问方法
     * @return boolean 返回 true 表示忽略访问方法
     */
    private boolean ignoreAccessor(Method method) {
        DocField docField = method.getAnnotation(DocField.class);
        return method.isAnnotationPresent(DocIgnore.class)
                || method.isAnnotationPresent(JsonIgnore.class)
                || (docField != null && docField.hidden());
    }

    /**
     * 获取 JSON 属性名
     *
     * @param field  字段
     * @param getter Getter 方法
     * @return String 返回 JSON 属性名
     */
    private String propertyName(Field field, Method getter) {
        DocField docField = docField(field, getter);
        if (docField != null && StringUtils.hasText(docField.name())) {
            return docField.name();
        }
        JsonProperty fieldProperty = field.getAnnotation(JsonProperty.class);
        if (fieldProperty != null && StringUtils.hasText(fieldProperty.value())) {
            return fieldProperty.value();
        }
        JsonProperty methodProperty = getter == null ? null : getter.getAnnotation(JsonProperty.class);
        if (methodProperty != null && StringUtils.hasText(methodProperty.value())) {
            return methodProperty.value();
        }
        return objectMapper.getPropertyNamingStrategy() == null ? field.getName() : objectMapper.getPropertyNamingStrategy().nameForField(null, null, field.getName());
    }

    /**
     * 获取 Getter 方法
     *
     * @param modelClass 模型类型
     * @param field      字段
     * @return Method 返回 Getter 方法
     */
    private Method getter(Class<?> modelClass, Field field) {
        String suffix = field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
        for (String methodName : List.of("get" + suffix, "is" + suffix)) {
            try {
                Method method = modelClass.getMethod(methodName);
                if (method.getParameterCount() == 0) {
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    /**
     * 获取所有字段
     *
     * @param modelClass 模型类型
     * @return List<Field> 返回字段集合
     */
    private List<Field> allFields(Class<?> modelClass) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = modelClass;
        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fields;
    }

    /**
     * 判断 Bean Validation 是否要求必填
     *
     * @param annotations 注解集合
     * @return boolean 返回 true 表示必填
     */
    private boolean validationRequired(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            String annotationName = annotation.annotationType().getName();
            if ("jakarta.validation.constraints.NotNull".equals(annotationName)
                    || "jakarta.validation.constraints.NotBlank".equals(annotationName)
                    || "jakarta.validation.constraints.NotEmpty".equals(annotationName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 填充 Bean Validation 约束
     *
     * @param schema      Schema
     * @param annotations 注解集合
     */
    private void fillValidationConstraints(Map<String, Object> schema, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            String annotationName = annotation.annotationType().getName();
            if ("jakarta.validation.constraints.Size".equals(annotationName)) {
                fillSizeConstraint(schema, annotation);
            } else if ("jakarta.validation.constraints.Min".equals(annotationName)) {
                schema.put("minimum", annotationLongValue(annotation, "value"));
            } else if ("jakarta.validation.constraints.Max".equals(annotationName)) {
                schema.put("maximum", annotationLongValue(annotation, "value"));
            } else if ("jakarta.validation.constraints.Pattern".equals(annotationName)) {
                schema.put("pattern", annotationStringValue(annotation, "regexp"));
            }
        }
    }

    /**
     * 填充 Size 约束
     *
     * @param schema     Schema
     * @param annotation Size 注解
     */
    private void fillSizeConstraint(Map<String, Object> schema, Annotation annotation) {
        int min = annotationIntValue(annotation, "min");
        int max = annotationIntValue(annotation, "max");
        if ("array".equals(schema.get("type"))) {
            schema.put("minItems", min);
            schema.put("maxItems", max);
            return;
        }
        schema.put("minLength", min);
        schema.put("maxLength", max);
    }

    /**
     * 获取注解 int 属性
     *
     * @param annotation 注解
     * @param name       属性名
     * @return int 返回属性值
     */
    private int annotationIntValue(Annotation annotation, String name) {
        return (Integer) annotationValue(annotation, name);
    }

    /**
     * 获取注解 long 属性
     *
     * @param annotation 注解
     * @param name       属性名
     * @return long 返回属性值
     */
    private long annotationLongValue(Annotation annotation, String name) {
        return (Long) annotationValue(annotation, name);
    }

    /**
     * 获取注解 String 属性
     *
     * @param annotation 注解
     * @param name       属性名
     * @return String 返回属性值
     */
    private String annotationStringValue(Annotation annotation, String name) {
        return (String) annotationValue(annotation, name);
    }

    /**
     * 获取注解属性
     *
     * @param annotation 注解
     * @param name       属性名
     * @return Object 返回属性值
     */
    private Object annotationValue(Annotation annotation, String name) {
        try {
            return annotation.annotationType().getMethod(name).invoke(annotation);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
            throw new IllegalStateException("读取 Bean Validation 约束失败: " + annotation.annotationType().getName() + "." + name, ex);
        }
    }

    /**
     * 解析原始类型
     *
     * @param type Spring 可解析类型
     * @return Class<?> 返回原始类型
     */
    private Class<?> resolveClass(ResolvableType type) {
        Class<?> rawClass = type.resolve();
        return rawClass == null ? Object.class : rawClass;
    }

    /**
     * 判断是否为 JDK 内置类型
     *
     * @param type 类型
     * @return boolean 返回 true 表示 JDK 内置类型
     */
    private boolean isJavaType(Class<?> type) {
        Package typePackage = type.getPackage();
        return typePackage != null && typePackage.getName().startsWith("java.");
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console.openapi
     * @ClassName: SimpleParameterizedType
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-19:20
     * @Description: 简单参数化类型
     * @Version: 1.0
     */
    private record SimpleParameterizedType(Class<?> rawType, Type[] actualTypeArguments) implements ParameterizedType {

        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
