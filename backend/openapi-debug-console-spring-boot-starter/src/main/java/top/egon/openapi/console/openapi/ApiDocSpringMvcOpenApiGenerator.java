/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.openapi
 * @FileName: ApiDocSpringMvcOpenApiGenerator.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:40
 * @Description: Spring MVC OpenAPI 文档生成器文件
 * @Version: 1.0
 */
package top.egon.openapi.console.openapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.egon.openapi.console.ApiDocConsoleProperties;
import top.egon.openapi.console.annotation.DocBody;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocIgnore;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocParam;
import top.egon.openapi.console.annotation.DocParamIn;
import top.egon.openapi.console.annotation.DocParameter;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;
import top.egon.openapi.console.annotation.DocTypeReference;
import top.egon.openapi.console.annotation.DocWrapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console.openapi
 * @ClassName: ApiDocSpringMvcOpenApiGenerator
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:40
 * @Description: Spring MVC OpenAPI 文档生成器
 * @Version: 1.0
 */
public class ApiDocSpringMvcOpenApiGenerator {

    private final RequestMappingHandlerMapping handlerMapping;

    private final ApiDocOpenApiSchemaGenerator schemaGenerator;

    private final ApiDocConsoleProperties properties;

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public ApiDocSpringMvcOpenApiGenerator(RequestMappingHandlerMapping handlerMapping,
                                           ApiDocOpenApiSchemaGenerator schemaGenerator,
                                           ApiDocConsoleProperties properties) {
        this.handlerMapping = handlerMapping;
        this.schemaGenerator = schemaGenerator;
        this.properties = properties;
    }

    /**
     * 生成 OpenAPI JSON 模型
     *
     * @return Map<String, Object> 返回 OpenAPI JSON 模型
     */
    public Map<String, Object> generate() {
        schemaGenerator.setExamplePolicy(properties.getProducer().getExamplePolicy());
        schemaGenerator.reset();
        Map<String, Object> paths = new LinkedHashMap<>();
        handlerMapping.getHandlerMethods().entrySet().stream()
                .filter(entry -> documentable(entry.getValue()))
                .sorted(Comparator.comparing(entry -> entry.getValue().getBeanType().getName() + "#" + entry.getValue().getMethod().getName()))
                .forEach(entry -> fillPath(paths, entry.getKey(), entry.getValue()));
        Map<String, Object> components = new LinkedHashMap<>();
        components.put("schemas", schemaGenerator.getSchemas());
        components.put("securitySchemes", securitySchemes());
        Map<String, Object> openApi = new LinkedHashMap<>();
        openApi.put("openapi", "3.0.3");
        openApi.put("info", info());
        openApi.put("paths", paths);
        openApi.put("components", components);
        openApi.put("security", List.of(Map.of(properties.getProducer().getAuthorizationHeader(), List.of())));
        return openApi;
    }

    /**
     * 填充路径文档
     *
     * @param paths         路径集合
     * @param mappingInfo   Spring MVC 映射信息
     * @param handlerMethod 处理方法
     */
    private void fillPath(Map<String, Object> paths, RequestMappingInfo mappingInfo, HandlerMethod handlerMethod) {
        for (String path : paths(mappingInfo)) {
            Map<String, Object> pathItem = pathItem(paths, path);
            for (RequestMethod requestMethod : requestMethods(mappingInfo)) {
                pathItem.put(requestMethod.name().toLowerCase(), operation(handlerMethod, requestMethod));
            }
        }
    }

    /**
     * 生成操作文档
     *
     * @param handlerMethod 处理方法
     * @param requestMethod 请求方法
     * @return Map<String, Object> 返回操作文档
     */
    private Map<String, Object> operation(HandlerMethod handlerMethod, RequestMethod requestMethod) {
        Method method = handlerMethod.getMethod();
        DocOperation docOperation = method.getAnnotation(DocOperation.class);
        Map<String, Object> operation = new LinkedHashMap<>();
        List<String> tags = tags(handlerMethod.getBeanType(), docOperation);
        if (!tags.isEmpty()) {
            operation.put("tags", tags);
        }
        String operationId = docOperation != null && StringUtils.hasText(docOperation.id()) ? docOperation.id() : method.getName();
        operation.put("operationId", operationId);
        String summary = operationSummary(docOperation, method);
        if (StringUtils.hasText(summary)) {
            operation.put("summary", summary);
        }
        String description = operationDescription(docOperation);
        if (StringUtils.hasText(description)) {
            operation.put("description", description);
        }
        List<Map<String, Object>> parameters = new ArrayList<>();
        Map<String, Object> inferredRequestBody = null;
        java.lang.reflect.Parameter[] methodParameters = method.getParameters();
        for (int i = 0; i < methodParameters.length; i++) {
            MethodParameter methodParameter = new MethodParameter(method, i);
            methodParameter.initParameterNameDiscovery(parameterNameDiscoverer);
            java.lang.reflect.Parameter parameter = methodParameters[i];
            if (ignoreParameter(parameter, methodParameter)) {
                continue;
            }
            if (methodParameter.hasParameterAnnotation(RequestBody.class) || docParamIn(parameter) == DocParamIn.BODY) {
                inferredRequestBody = requestBody(parameter, methodParameter);
                continue;
            }
            if (explicitMultipartRequestBody(docOperation) && multipartParameter(methodParameter)) {
                continue;
            }
            if (unannotatedComplexParameter(parameter, methodParameter)) {
                if (requestMethod == RequestMethod.GET) {
                    parameters.addAll(queryParameters(methodParameter));
                    continue;
                }
                if (requestMethod == RequestMethod.POST || requestMethod == RequestMethod.PUT || requestMethod == RequestMethod.PATCH) {
                    inferredRequestBody = requestBody(parameter, methodParameter);
                    continue;
                }
            }
            Map<String, Object> parameterDoc = parameter(parameter, methodParameter);
            if (!parameterDoc.isEmpty()) {
                parameters.add(parameterDoc);
            }
        }
        mergeOperationParameters(parameters, docOperation);
        if (!parameters.isEmpty()) {
            operation.put("parameters", parameters);
        }
        Map<String, Object> requestBody = requestBody(docOperation, inferredRequestBody, method);
        if (requestBody != null && !requestBody.isEmpty()) {
            operation.put("requestBody", requestBody);
        }
        operation.put("responses", responseMap(docOperation, method.getGenericReturnType()));
        operation.put("x-doc-auth", docOperation == null || docOperation.auth());
        operation.put("x-doc-protocol", "http");
        operation.put("x-doc-http-method", HttpMethod.valueOf(requestMethod.name()).name());
        fillServiceExtensions(operation, handlerMethod.getBeanType());
        return operation;
    }

    /**
     * 生成参数文档
     *
     * @param parameter       Java 参数
     * @param methodParameter Spring 方法参数
     * @return Map<String, Object> 返回参数文档
     */
    private Map<String, Object> parameter(java.lang.reflect.Parameter parameter, MethodParameter methodParameter) {
        String in = parameterIn(parameter, methodParameter);
        if (!StringUtils.hasText(in)) {
            return Map.of();
        }
        Map<String, Object> parameterDoc = new LinkedHashMap<>();
        parameterDoc.put("name", parameterName(parameter, methodParameter));
        parameterDoc.put("in", in);
        parameterDoc.put("required", parameterRequired(parameter, methodParameter, in));
        String description = parameterDescription(parameter);
        if (StringUtils.hasText(description)) {
            parameterDoc.put("description", description);
        }
        Map<String, Object> schema = parameterSchema(parameter, methodParameter);
        parameterDoc.put("schema", schema);
        Object example = parameterExample(parameter, methodParameter);
        if (example != null) {
            parameterDoc.put("example", example);
        }
        return parameterDoc;
    }

    /**
     * 生成请求体文档
     *
     * @param parameter       Java 参数
     * @param methodParameter Spring 方法参数
     * @return Map<String, Object> 返回请求体文档
     */
    private Map<String, Object> requestBody(java.lang.reflect.Parameter parameter, MethodParameter methodParameter) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        String description = parameterDescription(parameter);
        if (StringUtils.hasText(description)) {
            requestBody.put("description", description);
        }
        RequestBody springRequestBody = methodParameter.getParameterAnnotation(RequestBody.class);
        requestBody.put("required", springRequestBody == null || springRequestBody.required() || docParamRequired(parameter));
        requestBody.put("content", jsonContent(schemaGenerator.schema(methodParameter.getGenericParameterType()), schemaGenerator.example(ResolvableType.forMethodParameter(methodParameter))));
        return requestBody;
    }

    /**
     * 生成操作级请求体文档
     *
     * @param docOperation        文档操作注解
     * @param inferredRequestBody 推断请求体
     * @param method              处理方法
     * @return Map<String, Object> 返回请求体文档
     */
    private Map<String, Object> requestBody(DocOperation docOperation, Map<String, Object> inferredRequestBody, Method method) {
        if (docOperation == null || !docOperation.request().body().enabled()) {
            return inferredRequestBody;
        }
        DocRequest request = docOperation.request();
        DocBody docBody = request.body();
        Map<String, Object> requestBody = new LinkedHashMap<>();
        if (StringUtils.hasText(docBody.description())) {
            requestBody.put("description", docBody.description());
        }
        requestBody.put("required", docBody.required());
        Map<String, Object> schema = MediaType.MULTIPART_FORM_DATA_VALUE.equals(docBody.contentType())
                ? formSchema(request.params())
                : requestBodySchema(docBody, inferredRequestBody, method);
        Object example = MediaType.MULTIPART_FORM_DATA_VALUE.equals(docBody.contentType()) ? null
                : schemaGenerator.example(docBody.dataType(), docBody.wrapper(), requestBodyType(method), docBody.example(), docBody.exampleMode());
        requestBody.put("content", content(docBody.contentType(), schema, example));
        return requestBody;
    }

    /**
     * 生成请求体 Schema
     *
     * @param schemaRef           Schema 引用
     * @param inferredRequestBody 推断请求体
     * @param method              处理方法
     * @return Map<String, Object> 返回请求体 Schema
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> requestBodySchema(DocBody docBody, Map<String, Object> inferredRequestBody, Method method) {
        if (defaultDataType(docBody.dataType()) && docBody.wrapper().type() == Void.class) {
            if (inferredRequestBody != null) {
                Map<String, Object> content = (Map<String, Object>) inferredRequestBody.get("content");
                Map<String, Object> json = (Map<String, Object>) content.get(MediaType.APPLICATION_JSON_VALUE);
                if (json != null) {
                    return (Map<String, Object>) json.get("schema");
                }
            }
            return schemaGenerator.schema(requestBodyType(method));
        }
        return schemaGenerator.schema(docBody.dataType(), docBody.wrapper(), requestBodyType(method));
    }

    /**
     * 获取请求体推断类型
     *
     * @param method 处理方法
     * @return Type 返回请求体推断类型
     */
    private Type requestBodyType(Method method) {
        java.lang.reflect.Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter methodParameter = new MethodParameter(method, i);
            if (methodParameter.hasParameterAnnotation(RequestBody.class) || docParamIn(parameters[i]) == DocParamIn.BODY) {
                return methodParameter.getGenericParameterType();
            }
        }
        return Object.class;
    }

    /**
     * 生成表单 Schema
     *
     * @param formFields 表单字段注解
     * @return Map<String, Object> 返回表单 Schema
     */
    private Map<String, Object> formSchema(DocParameter[] formFields) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        for (DocParameter formField : formFields) {
            if (formField.hidden() || (formField.in() != DocParamIn.FORM && formField.in() != DocParamIn.FILE)) {
                continue;
            }
            Map<String, Object> fieldSchema = formFieldSchema(formField);
            if (StringUtils.hasText(formField.description())) {
                fieldSchema = new LinkedHashMap<>(fieldSchema);
                fieldSchema.put("description", formField.description());
            }
            properties.put(formField.name(), fieldSchema);
            if (formField.required()) {
                required.add(formField.name());
            }
        }
        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        return schema;
    }

    /**
     * 生成表单字段 Schema
     *
     * @param formField 表单字段注解
     * @return Map<String, Object> 返回表单字段 Schema
     */
    private Map<String, Object> formFieldSchema(DocParameter formField) {
        if (formField.dataType().kind() == DocDataKind.ARRAY) {
            if (formField.in() != DocParamIn.FILE) {
                return schemaGenerator.schema(formField.dataType(), String.class);
            }
            Map<String, Object> arraySchema = new LinkedHashMap<>();
            arraySchema.put("type", "array");
            arraySchema.put("items", schemaGenerator.fileSchema());
            return arraySchema;
        }
        return formField.in() == DocParamIn.FILE ? schemaGenerator.fileSchema() : schemaGenerator.schema(formField.dataType(), String.class);
    }

    /**
     * 生成响应文档
     *
     * @param returnType 方法返回类型
     * @return Map<String, Object> 返回响应文档
     */
    private Map<String, Object> responseMap(DocOperation docOperation, Type returnType) {
        if (docOperation != null) {
            DocResponse docResponse = docOperation.response();
            validateResponseContract(docResponse, returnType);
            return Map.of(String.valueOf(docResponse.status()), response(docResponse, returnType));
        }
        validateMissingResponseContract(returnType);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("description", "OK");
        response.put("content", jsonContent(schemaGenerator.schema(returnType), schemaGenerator.example(ResolvableType.forType(returnType))));
        return Map.of("200", response);
    }

    /**
     * 校验响应契约
     *
     * @param docResponse 文档响应
     * @param returnType  方法返回类型
     */
    private void validateResponseContract(DocResponse docResponse, Type returnType) {
        if (properties.getProducer().getContractPolicy() != ApiDocConsoleProperties.Policy.FAIL) {
            return;
        }
        ResolvableType normalizedReturnType = normalizedReturnType(returnType);
        if (voidType(normalizedReturnType) || docResponse.noBody()) {
            return;
        }
        if (defaultResponse(docResponse)) {
            throw new IllegalStateException("DocOperation.response 未声明有效响应契约: " + normalizedReturnType);
        }
        validateWrapperContract(docResponse.wrapper(), normalizedReturnType);
        validateDataTypeContract(docResponse, normalizedReturnType);
    }

    /**
     * 校验缺失响应契约
     *
     * @param returnType 方法返回类型
     */
    private void validateMissingResponseContract(Type returnType) {
        if (properties.getProducer().getContractPolicy() == ApiDocConsoleProperties.Policy.FAIL
                && !voidType(normalizedReturnType(returnType))) {
            throw new IllegalStateException("缺少 DocOperation.response 响应契约: " + returnType);
        }
    }

    /**
     * 校验响应包装契约
     *
     * @param wrapper    响应包装
     * @param returnType 返回类型
     */
    private void validateWrapperContract(DocWrapper wrapper, ResolvableType returnType) {
        if (wrapper.type() == Void.class) {
            return;
        }
        Class<?> returnClass = returnType.resolve(Object.class);
        if (returnClass != wrapper.type()) {
            throw new IllegalStateException("DocResponse.wrapper 和方法返回外层类型不一致: wrapper="
                    + wrapper.type().getName() + ", return=" + returnClass.getName());
        }
    }

    /**
     * 校验响应数据类型契约
     *
     * @param docResponse 文档响应
     * @param returnType  返回类型
     */
    private void validateDataTypeContract(DocResponse docResponse, ResolvableType returnType) {
        if (defaultDataType(docResponse.dataType())) {
            return;
        }
        ResolvableType actualDataType = docResponse.wrapper().type() == Void.class
                ? returnType
                : returnType.getGeneric(docResponse.wrapper().genericIndex());
        ResolvableType declaredDataType = declaredDataType(docResponse.dataType(), actualDataType.getType());
        if (!sameType(declaredDataType, actualDataType)) {
            throw new IllegalStateException("DocResponse.dataType 和方法返回数据类型不一致: dataType="
                    + declaredDataType + ", return=" + actualDataType);
        }
    }

    /**
     * 判断是否默认响应契约
     *
     * @param docResponse 文档响应
     * @return boolean 返回 true 表示默认响应契约
     */
    private boolean defaultResponse(DocResponse docResponse) {
        return docResponse.status() == 200
                && "OK".equals(docResponse.description())
                && !docResponse.noBody()
                && MediaType.APPLICATION_JSON_VALUE.equals(docResponse.contentType())
                && defaultDataType(docResponse.dataType())
                && docResponse.wrapper().type() == Void.class
                && docResponse.headers().length == 0
                && !StringUtils.hasText(docResponse.example());
    }

    /**
     * 获取规范化返回类型
     *
     * @param returnType 方法返回类型
     * @return ResolvableType 返回规范化类型
     */
    private ResolvableType normalizedReturnType(Type returnType) {
        ResolvableType type = ResolvableType.forType(returnType);
        Class<?> rawClass = type.resolve(Object.class);
        if (ResponseEntity.class.isAssignableFrom(rawClass)) {
            return type.getGeneric(0);
        }
        return type;
    }

    /**
     * 判断是否 void 类型
     *
     * @param type 类型
     * @return boolean 返回 true 表示 void 类型
     */
    private boolean voidType(ResolvableType type) {
        Class<?> rawClass = type.resolve(Object.class);
        return rawClass == Void.class || rawClass == Void.TYPE;
    }

    /**
     * 解析声明的数据类型
     *
     * @param dataType     文档数据类型
     * @param inferredType 推断类型
     * @return ResolvableType 返回声明数据类型
     */
    private ResolvableType declaredDataType(DocDataType dataType, Type inferredType) {
        return switch (dataType.kind()) {
            case VOID -> ResolvableType.forClass(Void.class);
            case BOOLEAN -> ResolvableType.forClass(Boolean.class);
            case INTEGER -> ResolvableType.forClass(Integer.class);
            case LONG -> ResolvableType.forClass(Long.class);
            case DECIMAL -> ResolvableType.forClass(BigDecimal.class);
            case STRING -> ResolvableType.forClass(String.class);
            case DATE -> ResolvableType.forClass(LocalDate.class);
            case DATETIME -> ResolvableType.forClass(LocalDateTime.class);
            case FILE, BINARY -> ResolvableType.forClass(MultipartFile.class);
            case ENUM, OBJECT ->
                    dataType.type() == Void.class ? ResolvableType.forType(inferredType) : ResolvableType.forClass(dataType.type());
            case ARRAY ->
                    dataType.itemType() == Void.class ? ResolvableType.forType(inferredType) : ResolvableType.forClassWithGenerics(List.class, dataType.itemType());
            case MAP ->
                    dataType.valueType() == Void.class ? ResolvableType.forType(inferredType) : ResolvableType.forClassWithGenerics(Map.class, dataType.keyType(), dataType.valueType());
            case GENERIC -> ResolvableType.forType(typeReference(dataType.ref()));
            case AUTO -> autoDataType(dataType, inferredType);
        };
    }

    /**
     * 解析 AUTO 数据类型
     *
     * @param dataType     文档数据类型
     * @param inferredType 推断类型
     * @return ResolvableType 返回数据类型
     */
    private ResolvableType autoDataType(DocDataType dataType, Type inferredType) {
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
     * 解析复杂泛型引用
     *
     * @param referenceType 泛型引用类型
     * @return Type 返回实际泛型类型
     */
    private Type typeReference(Class<? extends DocTypeReference<?>> referenceType) {
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
     * 判断两个类型是否一致
     *
     * @param expected 期望类型
     * @param actual   实际类型
     * @return boolean 返回 true 表示一致
     */
    private boolean sameType(ResolvableType expected, ResolvableType actual) {
        Class<?> expectedClass = expected.resolve(Object.class);
        Class<?> actualClass = actual.resolve(Object.class);
        if (expectedClass != actualClass) {
            return false;
        }
        ResolvableType[] expectedGenerics = expected.getGenerics();
        ResolvableType[] actualGenerics = actual.getGenerics();
        if (expectedGenerics.length == 0 || expectedGenerics.length != actualGenerics.length) {
            return expectedGenerics.length == actualGenerics.length;
        }
        for (int i = 0; i < expectedGenerics.length; i++) {
            if (!sameType(expectedGenerics[i], actualGenerics[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * 生成响应文档
     *
     * @param docResponse 文档响应注解
     * @param returnType  方法返回类型
     * @return Map<String, Object> 返回响应文档
     */
    private Map<String, Object> response(DocResponse docResponse, Type returnType) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("description", docResponse.description());
        Map<String, Object> headers = responseHeaders(docResponse.headers());
        if (!headers.isEmpty()) {
            response.put("headers", headers);
        }
        if (!docResponse.noBody()) {
            Map<String, Object> schema = responseSchema(docResponse, returnType);
            Object example = schemaGenerator.example(docResponse.dataType(), docResponse.wrapper(), returnType, docResponse.example(), docResponse.exampleMode());
            response.put("content", content(docResponse.contentType(), schema, example));
        }
        return response;
    }

    /**
     * 生成响应 Schema
     *
     * @param docResponse 文档响应注解
     * @param returnType  方法返回类型
     * @return Map<String, Object> 返回响应 Schema
     */
    private Map<String, Object> responseSchema(DocResponse docResponse, Type returnType) {
        if (defaultDataType(docResponse.dataType()) && docResponse.wrapper().type() == Void.class && (docResponse.status() < 200 || docResponse.status() >= 300)) {
            return Map.of("type", "object");
        }
        return schemaGenerator.schema(docResponse.dataType(), docResponse.wrapper(), returnType);
    }

    /**
     * 判断是否默认数据类型
     *
     * @param dataType 数据类型
     * @return boolean 返回 true 表示默认数据类型
     */
    private boolean defaultDataType(DocDataType dataType) {
        return dataType.kind() == DocDataKind.AUTO
                && dataType.type() == Void.class
                && dataType.ref().getName().endsWith("DocTypeReference$None")
                && dataType.itemType() == Void.class
                && dataType.valueType() == Void.class
                && !StringUtils.hasText(dataType.dataPath());
    }

    /**
     * 生成 JSON 内容文档
     *
     * @param schema Schema
     * @return Map<String, Object> 返回 JSON 内容文档
     */
    private Map<String, Object> jsonContent(Map<String, Object> schema, Object example) {
        return content(MediaType.APPLICATION_JSON_VALUE, schema, example);
    }

    /**
     * 生成内容文档
     *
     * @param contentTypes 内容类型
     * @param schema       Schema
     * @return Map<String, Object> 返回内容文档
     */
    private Map<String, Object> content(String contentType, Map<String, Object> schema, Object example) {
        Map<String, Object> content = new LinkedHashMap<>();
        if (StringUtils.hasText(contentType)) {
            Map<String, Object> media = new LinkedHashMap<>();
            media.put("schema", schema);
            if (example != null) {
                media.put("example", example);
            }
            content.put(contentType, media);
        }
        return content;
    }

    /**
     * 生成响应头文档
     *
     * @param docHeaders 文档响应头注解
     * @return Map<String, Object> 返回响应头文档
     */
    private Map<String, Object> responseHeaders(DocParameter[] docHeaders) {
        Map<String, Object> headers = new LinkedHashMap<>();
        for (DocParameter docHeader : docHeaders) {
            if (docHeader.hidden()) {
                continue;
            }
            Map<String, Object> header = new LinkedHashMap<>();
            if (StringUtils.hasText(docHeader.description())) {
                header.put("description", docHeader.description());
            }
            header.put("required", docHeader.required());
            header.put("schema", schemaGenerator.schema(docHeader.dataType(), String.class));
            headers.put(docHeader.name(), header);
        }
        return headers;
    }

    /**
     * 获取路径对象
     *
     * @param paths 路径集合
     * @param path  路径
     * @return Map<String, Object> 返回路径对象
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> pathItem(Map<String, Object> paths, String path) {
        return (Map<String, Object>) paths.computeIfAbsent(path, key -> new LinkedHashMap<>());
    }

    /**
     * 获取映射路径
     *
     * @param mappingInfo Spring MVC 映射信息
     * @return Set<String> 返回路径集合
     */
    private Set<String> paths(RequestMappingInfo mappingInfo) {
        if (mappingInfo.getPathPatternsCondition() != null) {
            return mappingInfo.getPathPatternsCondition().getPatternValues();
        }
        if (mappingInfo.getPatternsCondition() != null) {
            return mappingInfo.getPatternsCondition().getPatterns();
        }
        return Set.of("/");
    }

    /**
     * 获取请求方法
     *
     * @param mappingInfo Spring MVC 映射信息
     * @return Set<RequestMethod> 返回请求方法集合
     */
    private Set<RequestMethod> requestMethods(RequestMappingInfo mappingInfo) {
        Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
        return methods.isEmpty() ? new LinkedHashSet<>(Arrays.asList(RequestMethod.values())) : methods;
    }

    /**
     * 判断处理方法是否生成文档
     *
     * @param handlerMethod 处理方法
     * @return boolean 返回 true 表示生成文档
     */
    private boolean documentable(HandlerMethod handlerMethod) {
        Class<?> beanType = handlerMethod.getBeanType();
        Method method = handlerMethod.getMethod();
        if (beanType.isAnnotationPresent(DocIgnore.class) || method.isAnnotationPresent(DocIgnore.class)) {
            return false;
        }
        if (beanType.getName().startsWith("top.egon.openapi.console.web.")
                || beanType.getName().startsWith("top.egon.openapi.console.openapi.")) {
            return false;
        }
        if (beanType.isAnnotationPresent(RestController.class)) {
            return true;
        }
        return beanType.isAnnotationPresent(org.springframework.stereotype.Controller.class)
                && (beanType.isAnnotationPresent(ResponseBody.class) || method.isAnnotationPresent(ResponseBody.class));
    }

    /**
     * 获取服务 tag
     *
     * @param beanType     Controller 类型
     * @param docOperation 操作文档注解
     * @return List<String> 返回 tag 集合
     */
    private List<String> tags(Class<?> beanType, DocOperation docOperation) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        DocService docService = beanType.getAnnotation(DocService.class);
        if (docService != null && StringUtils.hasText(docService.serviceName())) {
            tags.add(docService.serviceName());
        }
        if (docOperation != null) {
            for (String tag : docOperation.tags()) {
                if (StringUtils.hasText(tag)) {
                    tags.add(tag);
                }
            }
        }
        if (tags.isEmpty()) {
            tags.add(beanType.getSimpleName());
        }
        return new ArrayList<>(tags);
    }

    /**
     * 填充服务分组扩展
     *
     * @param operation 操作文档
     * @param beanType  Controller 类型
     */
    private void fillServiceExtensions(Map<String, Object> operation, Class<?> beanType) {
        DocService docService = beanType.getAnnotation(DocService.class);
        if (docService == null) {
            return;
        }
        operation.put("x-doc-group-id", docService.groupId());
        operation.put("x-doc-group-name", docService.groupName());
        operation.put("x-doc-group-order", docService.groupOrder());
        operation.put("x-doc-service-id", StringUtils.hasText(docService.serviceId()) ? docService.serviceId() : beanType.getName());
        operation.put("x-doc-service-name", docService.serviceName());
        operation.put("x-doc-service-description", docService.serviceDescription());
        operation.put("x-doc-service-order", docService.serviceOrder());
        operation.put("x-doc-service-version", docService.version());
        DocProtocol protocol = docService.protocol() == DocProtocol.AUTO ? DocProtocol.HTTP : docService.protocol();
        operation.put("x-doc-protocol", protocol.name().toLowerCase());
    }

    /**
     * 获取操作标题
     *
     * @param docOperation 文档操作注解
     * @param method       处理方法
     * @return String 返回操作标题
     */
    private String operationSummary(DocOperation docOperation, Method method) {
        if (docOperation != null && StringUtils.hasText(docOperation.summary())) {
            return docOperation.summary();
        }
        return method.getName();
    }

    /**
     * 获取操作描述
     *
     * @param docOperation 文档操作注解
     * @return String 返回操作描述
     */
    private String operationDescription(DocOperation docOperation) {
        if (docOperation != null && StringUtils.hasText(docOperation.description())) {
            return docOperation.description();
        }
        return "";
    }

    /**
     * 判断参数是否忽略
     *
     * @param parameter       Java 参数
     * @param methodParameter Spring 方法参数
     * @return boolean 返回 true 表示忽略参数
     */
    private boolean ignoreParameter(java.lang.reflect.Parameter parameter, MethodParameter methodParameter) {
        Class<?> parameterType = methodParameter.getParameterType();
        return parameter.isAnnotationPresent(DocIgnore.class)
                || (parameter.isAnnotationPresent(DocParameter.class) && parameter.getAnnotation(DocParameter.class).hidden())
                || (parameter.isAnnotationPresent(DocParam.class) && parameter.getAnnotation(DocParam.class).hidden())
                || jakarta.servlet.ServletRequest.class.isAssignableFrom(parameterType)
                || jakarta.servlet.ServletResponse.class.isAssignableFrom(parameterType)
                || org.springframework.validation.BindingResult.class.isAssignableFrom(parameterType);
    }

    /**
     * 获取参数位置
     *
     * @param parameter       Java 参数
     * @param methodParameter Spring 方法参数
     * @return String 返回参数位置
     */
    private String parameterIn(java.lang.reflect.Parameter parameter, MethodParameter methodParameter) {
        DocParamIn docIn = docParamIn(parameter);
        if (docIn == DocParamIn.PATH || docIn == DocParamIn.QUERY || docIn == DocParamIn.HEADER || docIn == DocParamIn.COOKIE) {
            return docIn.name().toLowerCase();
        }
        if (methodParameter.hasParameterAnnotation(PathVariable.class)) {
            return "path";
        }
        if (methodParameter.hasParameterAnnotation(RequestHeader.class)) {
            return "header";
        }
        if (methodParameter.hasParameterAnnotation(CookieValue.class)) {
            return "cookie";
        }
        if (methodParameter.hasParameterAnnotation(RequestParam.class) || simpleParameter(methodParameter.getParameterType())) {
            return "query";
        }
        return "";
    }

    /**
     * 判断是否为无注解复杂参数
     *
     * @param parameter       Java 参数
     * @param methodParameter Spring 方法参数
     * @return boolean 返回 true 表示无注解复杂参数
     */
    private boolean unannotatedComplexParameter(java.lang.reflect.Parameter parameter, MethodParameter methodParameter) {
        return !hasExplicitParameterBinding(parameter, methodParameter) && !simpleParameter(methodParameter.getParameterType());
    }

    /**
     * 判断参数是否有明确绑定注解
     *
     * @param parameter       Java 参数
     * @param methodParameter Spring 方法参数
     * @return boolean 返回 true 表示有明确绑定注解
     */
    private boolean hasExplicitParameterBinding(java.lang.reflect.Parameter parameter, MethodParameter methodParameter) {
        DocParamIn docIn = docParamIn(parameter);
        if (docIn != DocParamIn.AUTO) {
            return true;
        }
        return methodParameter.hasParameterAnnotation(PathVariable.class)
                || methodParameter.hasParameterAnnotation(RequestParam.class)
                || methodParameter.hasParameterAnnotation(RequestHeader.class)
                || methodParameter.hasParameterAnnotation(CookieValue.class)
                || methodParameter.hasParameterAnnotation(RequestBody.class);
    }

    /**
     * 生成复杂对象查询参数
     *
     * @param methodParameter Spring 方法参数
     * @return List<Map < String, Object>> 返回查询参数集合
     */
    private List<Map<String, Object>> queryParameters(MethodParameter methodParameter) {
        List<Map<String, Object>> parameters = new ArrayList<>();
        ResolvableType ownerType = ResolvableType.forMethodParameter(methodParameter);
        String sourcePrefix = StringUtils.hasText(methodParameter.getParameterName()) ? methodParameter.getParameterName() : "";
        for (Field field : allFields(methodParameter.getParameterType())) {
            if (ignoreField(field)) {
                continue;
            }
            Map<String, Object> parameterDoc = new LinkedHashMap<>();
            parameterDoc.put("name", propertyName(field));
            parameterDoc.put("in", "query");
            parameterDoc.put("required", requiredField(field));
            if (StringUtils.hasText(sourcePrefix)) {
                parameterDoc.put("x-doc-source", sourcePrefix + "." + field.getName());
            }
            DocField docField = field.getAnnotation(DocField.class);
            if (docField != null && StringUtils.hasText(docField.description())) {
                parameterDoc.put("description", docField.description());
            }
            Map<String, Object> schema = new LinkedHashMap<>(schemaGenerator.schema(ResolvableType.forField(field, ownerType)));
            parameterDoc.put("schema", schema);
            parameters.add(parameterDoc);
        }
        return parameters;
    }

    /**
     * 合并操作级参数
     *
     * @param parameters   参数集合
     * @param docOperation 文档操作注解
     */
    private void mergeOperationParameters(List<Map<String, Object>> parameters, DocOperation docOperation) {
        if (docOperation == null) {
            return;
        }
        for (DocParameter docParameter : docOperation.request().params()) {
            if (docParameter.in() == DocParamIn.FORM || docParameter.in() == DocParamIn.FILE) {
                continue;
            }
            Map<String, Object> parameter = operationParameter(parameters, docParameter);
            String in = operationParameterIn(docParameter.in(), parameter);
            if (docParameter.hidden()) {
                parameters.removeIf(item -> sameParameter(item, docParameter.name(), in) || sameSource(item, docParameter.source()));
                continue;
            }
            if (parameter == null) {
                validateMissingExplicitParameter(docParameter);
                parameter = new LinkedHashMap<>();
                parameter.put("schema", Map.of("type", "string"));
                parameters.add(parameter);
            }
            parameter.put("name", docParameter.name());
            parameter.put("in", in);
            if (StringUtils.hasText(docParameter.description())) {
                parameter.put("description", docParameter.description());
            }
            if (docParameter.required()) {
                parameter.put("required", Boolean.TRUE);
            } else {
                parameter.putIfAbsent("required", Boolean.FALSE);
            }
            if (StringUtils.hasText(docParameter.source())) {
                parameter.put("x-doc-source", docParameter.source());
            }
            if (!defaultDataType(docParameter.dataType())) {
                parameter.put("schema", schemaGenerator.schema(docParameter.dataType(), String.class));
            }
            if (StringUtils.hasText(docParameter.example())) {
                parameter.put("example", schemaGenerator.example(docParameter.dataType(), String.class, docParameter.example(), docParameter.exampleMode()));
            }
        }
    }

    /**
     * 获取可被操作级参数覆盖的已推断参数
     *
     * @param parameters   参数集合
     * @param docParameter 操作级参数注解
     * @return Map<String, Object> 返回已推断参数
     */
    private Map<String, Object> operationParameter(List<Map<String, Object>> parameters, DocParameter docParameter) {
        if (StringUtils.hasText(docParameter.source())) {
            Map<String, Object> parameter = parameters.stream()
                    .filter(item -> sameSource(item, docParameter.source()))
                    .findFirst()
                    .orElse(null);
            if (parameter != null) {
                return parameter;
            }
        }
        if (docParameter.in() == DocParamIn.AUTO) {
            return parameters.stream()
                    .filter(item -> docParameter.name().equals(item.get("name")))
                    .findFirst()
                    .orElse(null);
        }
        String in = operationParameterIn(docParameter.in(), null);
        return parameters.stream()
                .filter(item -> sameParameter(item, docParameter.name(), in))
                .findFirst()
                .orElse(null);
    }

    /**
     * 校验未匹配的显式参数
     *
     * @param docParameter 文档参数
     */
    private void validateMissingExplicitParameter(DocParameter docParameter) {
        if (properties.getProducer().getContractPolicy() != ApiDocConsoleProperties.Policy.FAIL) {
            return;
        }
        throw new IllegalStateException("DocParameter 未匹配 Spring MVC mapping 或方法签名: " + docParameter.name());
    }

    /**
     * 判断是否相同参数
     *
     * @param parameter 参数文档
     * @param name      参数名
     * @param in        参数位置
     * @return boolean 返回 true 表示相同参数
     */
    private boolean sameParameter(Map<String, Object> parameter, String name, String in) {
        return name.equals(parameter.get("name")) && in.equals(parameter.get("in"));
    }

    /**
     * 判断是否相同来源字段
     *
     * @param parameter 参数文档
     * @param source    来源字段
     * @return boolean 返回 true 表示相同来源字段
     */
    private boolean sameSource(Map<String, Object> parameter, String source) {
        return StringUtils.hasText(source) && source.equals(parameter.get("x-doc-source"));
    }

    /**
     * 获取操作级参数位置
     *
     * @param in 参数位置
     * @return String 返回 OpenAPI 参数位置
     */
    private String operationParameterIn(DocParamIn in, Map<String, Object> parameter) {
        if (in == DocParamIn.PATH || in == DocParamIn.QUERY || in == DocParamIn.HEADER || in == DocParamIn.COOKIE) {
            return in.name().toLowerCase();
        }
        if (parameter != null && StringUtils.hasText((String) parameter.get("in"))) {
            return (String) parameter.get("in");
        }
        return "query";
    }

    /**
     * 判断操作是否显式声明 multipart 请求体
     *
     * @param docOperation 文档操作注解
     * @return boolean 返回 true 表示显式声明 multipart 请求体
     */
    private boolean explicitMultipartRequestBody(DocOperation docOperation) {
        if (docOperation == null) {
            return false;
        }
        if (docOperation.request().body().enabled() && MediaType.MULTIPART_FORM_DATA_VALUE.equals(docOperation.request().body().contentType())) {
            return true;
        }
        for (DocParameter docParameter : docOperation.request().params()) {
            if (docParameter.in() == DocParamIn.FORM || docParameter.in() == DocParamIn.FILE) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否文件上传参数
     *
     * @param methodParameter Spring 方法参数
     * @return boolean 返回 true 表示文件上传参数
     */
    private boolean multipartParameter(MethodParameter methodParameter) {
        Class<?> parameterType = methodParameter.getParameterType();
        if (MultipartFile.class.isAssignableFrom(parameterType)) {
            return true;
        }
        if (parameterType.isArray() && MultipartFile.class.isAssignableFrom(parameterType.getComponentType())) {
            return true;
        }
        if (List.class.isAssignableFrom(parameterType)) {
            return MultipartFile.class.isAssignableFrom(ResolvableType.forMethodParameter(methodParameter).getGeneric(0).toClass());
        }
        return false;
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
     * 获取 JSON 属性名
     *
     * @param field 字段
     * @return String 返回 JSON 属性名
     */
    private String propertyName(Field field) {
        JsonProperty fieldProperty = field.getAnnotation(JsonProperty.class);
        if (fieldProperty != null && StringUtils.hasText(fieldProperty.value())) {
            return fieldProperty.value();
        }
        return field.getName();
    }

    /**
     * 判断字段是否必填
     *
     * @param field 字段
     * @return boolean 返回 true 表示必填
     */
    private boolean requiredField(Field field) {
        DocField docField = field.getAnnotation(DocField.class);
        return (docField != null && docField.required()) || validationRequired(field.getAnnotations());
    }

    /**
     * 生成参数 Schema
     *
     * @param parameter       Java 参数
     * @param methodParameter Spring 方法参数
     * @return Map<String, Object> 返回参数 Schema
     */
    private Map<String, Object> parameterSchema(java.lang.reflect.Parameter parameter, MethodParameter methodParameter) {
        DocParameter docParameter = parameter.getAnnotation(DocParameter.class);
        if (docParameter != null && !defaultDataType(docParameter.dataType())) {
            return schemaGenerator.schema(docParameter.dataType(), methodParameter.getGenericParameterType());
        }
        DocParam docParam = parameter.getAnnotation(DocParam.class);
        if (docParam != null && !defaultDataType(docParam.dataType())) {
            return schemaGenerator.schema(docParam.dataType(), methodParameter.getGenericParameterType());
        }
        return schemaGenerator.schema(methodParameter.getGenericParameterType());
    }

    /**
     * 生成参数示例
     *
     * @param parameter       Java 参数
     * @param methodParameter Spring 方法参数
     * @return Object 返回参数示例
     */
    private Object parameterExample(java.lang.reflect.Parameter parameter, MethodParameter methodParameter) {
        DocParameter docParameter = parameter.getAnnotation(DocParameter.class);
        if (docParameter != null && StringUtils.hasText(docParameter.example())) {
            return schemaGenerator.example(docParameter.dataType(), methodParameter.getGenericParameterType(), docParameter.example(), docParameter.exampleMode());
        }
        DocParam docParam = parameter.getAnnotation(DocParam.class);
        if (docParam != null && StringUtils.hasText(docParam.example())) {
            return schemaGenerator.example(docParam.dataType(), methodParameter.getGenericParameterType(), docParam.example(), docParam.exampleMode());
        }
        return null;
    }

    /**
     * 获取参数名称
     *
     * @param parameter       Java 参数
     * @param methodParameter Spring 方法参数
     * @return String 返回参数名称
     */
    private String parameterName(java.lang.reflect.Parameter parameter, MethodParameter methodParameter) {
        DocParameter docParameter = parameter.getAnnotation(DocParameter.class);
        if (docParameter != null && StringUtils.hasText(docParameter.name())) {
            return docParameter.name();
        }
        DocParam docParam = parameter.getAnnotation(DocParam.class);
        if (docParam != null && StringUtils.hasText(docParam.name())) {
            return docParam.name();
        }
        String annotationName = annotationName(methodParameter);
        if (StringUtils.hasText(annotationName)) {
            return annotationName;
        }
        return StringUtils.hasText(methodParameter.getParameterName()) ? methodParameter.getParameterName() : parameter.getName();
    }

    /**
     * 获取 Spring MVC 注解参数名
     *
     * @param methodParameter Spring 方法参数
     * @return String 返回注解参数名
     */
    private String annotationName(MethodParameter methodParameter) {
        PathVariable pathVariable = methodParameter.getParameterAnnotation(PathVariable.class);
        if (pathVariable != null) {
            return namedValue(pathVariable.name(), pathVariable.value());
        }
        RequestParam requestParam = methodParameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            return namedValue(requestParam.name(), requestParam.value());
        }
        RequestHeader requestHeader = methodParameter.getParameterAnnotation(RequestHeader.class);
        if (requestHeader != null) {
            return namedValue(requestHeader.name(), requestHeader.value());
        }
        CookieValue cookieValue = methodParameter.getParameterAnnotation(CookieValue.class);
        if (cookieValue != null) {
            return namedValue(cookieValue.name(), cookieValue.value());
        }
        return "";
    }

    /**
     * 获取具名注解值
     *
     * @param name  name 属性
     * @param value value 属性
     * @return String 返回注解值
     */
    private String namedValue(String name, String value) {
        return StringUtils.hasText(name) ? name : value;
    }

    /**
     * 判断参数是否必填
     *
     * @param parameter       Java 参数
     * @param methodParameter Spring 方法参数
     * @param in              参数位置
     * @return boolean 返回 true 表示必填
     */
    private boolean parameterRequired(java.lang.reflect.Parameter parameter, MethodParameter methodParameter, String in) {
        if ("path".equals(in)) {
            return true;
        }
        if (docParamRequired(parameter)) {
            return true;
        }
        RequestParam requestParam = methodParameter.getParameterAnnotation(RequestParam.class);
        if (requestParam != null) {
            return requestParam.required() && ValueConstants.DEFAULT_NONE.equals(requestParam.defaultValue());
        }
        RequestHeader requestHeader = methodParameter.getParameterAnnotation(RequestHeader.class);
        if (requestHeader != null) {
            return requestHeader.required() && ValueConstants.DEFAULT_NONE.equals(requestHeader.defaultValue());
        }
        CookieValue cookieValue = methodParameter.getParameterAnnotation(CookieValue.class);
        if (cookieValue != null) {
            return cookieValue.required() && ValueConstants.DEFAULT_NONE.equals(cookieValue.defaultValue());
        }
        return validationRequired(parameter);
    }

    /**
     * 判断文档参数注解是否标记必填
     *
     * @param parameter Java 参数
     * @return boolean 返回 true 表示必填
     */
    private boolean docParamRequired(java.lang.reflect.Parameter parameter) {
        DocParameter docParameter = parameter.getAnnotation(DocParameter.class);
        if (docParameter != null && docParameter.required()) {
            return true;
        }
        DocParam docParam = parameter.getAnnotation(DocParam.class);
        return docParam != null && docParam.required();
    }

    /**
     * 获取文档参数位置
     *
     * @param parameter Java 参数
     * @return DocParamIn 返回文档参数位置
     */
    private DocParamIn docParamIn(java.lang.reflect.Parameter parameter) {
        DocParameter docParameter = parameter.getAnnotation(DocParameter.class);
        if (docParameter != null) {
            return docParameter.in();
        }
        DocParam docParam = parameter.getAnnotation(DocParam.class);
        return docParam == null ? DocParamIn.AUTO : docParam.in();
    }

    /**
     * 获取参数描述
     *
     * @param parameter Java 参数
     * @return String 返回参数描述
     */
    private String parameterDescription(java.lang.reflect.Parameter parameter) {
        DocParameter docParameter = parameter.getAnnotation(DocParameter.class);
        if (docParameter != null && StringUtils.hasText(docParameter.description())) {
            return docParameter.description();
        }
        DocParam docParam = parameter.getAnnotation(DocParam.class);
        if (docParam != null && StringUtils.hasText(docParam.description())) {
            return docParam.description();
        }
        return "";
    }

    /**
     * 判断 Bean Validation 是否要求必填
     *
     * @param parameter Java 参数
     * @return boolean 返回 true 表示必填
     */
    private boolean validationRequired(java.lang.reflect.Parameter parameter) {
        return validationRequired(parameter.getAnnotations());
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
     * 判断是否简单参数
     *
     * @param parameterType 参数类型
     * @return boolean 返回 true 表示简单参数
     */
    private boolean simpleParameter(Class<?> parameterType) {
        return parameterType.isPrimitive()
                || String.class == parameterType
                || Number.class.isAssignableFrom(parameterType)
                || BigDecimal.class == parameterType
                || BigInteger.class == parameterType
                || Boolean.class == parameterType
                || Character.class == parameterType
                || LocalDate.class == parameterType
                || LocalDateTime.class == parameterType
                || Date.class.isAssignableFrom(parameterType)
                || parameterType.isEnum();
    }

    /**
     * 生成基础信息
     *
     * @return Map<String, Object> 返回基础信息
     */
    private Map<String, Object> info() {
        ApiDocConsoleProperties.Producer producer = properties.getProducer();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", producer.getTitle());
        info.put("description", producer.getDescription());
        info.put("version", producer.getVersion());
        fillContact(info, producer);
        fillLicense(info, producer);
        return info;
    }

    /**
     * 填充联系人信息
     *
     * @param info     OpenAPI 基础信息
     * @param producer 业务模块 OpenAPI 生产配置
     */
    private void fillContact(Map<String, Object> info, ApiDocConsoleProperties.Producer producer) {
        if (!StringUtils.hasText(producer.getContactName())
                && !StringUtils.hasText(producer.getContactUrl())
                && !StringUtils.hasText(producer.getContactEmail())) {
            return;
        }
        Map<String, Object> contact = new LinkedHashMap<>();
        contact.put("name", producer.getContactName());
        contact.put("url", producer.getContactUrl());
        contact.put("email", producer.getContactEmail());
        info.put("contact", contact);
    }

    /**
     * 填充许可证信息
     *
     * @param info     OpenAPI 基础信息
     * @param producer 业务模块 OpenAPI 生产配置
     */
    private void fillLicense(Map<String, Object> info, ApiDocConsoleProperties.Producer producer) {
        if (!StringUtils.hasText(producer.getLicenseName()) && !StringUtils.hasText(producer.getLicenseUrl())) {
            return;
        }
        Map<String, Object> license = new LinkedHashMap<>();
        license.put("name", producer.getLicenseName());
        license.put("url", producer.getLicenseUrl());
        info.put("license", license);
    }

    /**
     * 生成安全方案
     *
     * @return Map<String, Object> 返回安全方案
     */
    private Map<String, Object> securitySchemes() {
        ApiDocConsoleProperties.Producer producer = properties.getProducer();
        Map<String, Object> scheme = new LinkedHashMap<>();
        scheme.put("type", "apiKey");
        scheme.put("name", producer.getAuthorizationHeader());
        scheme.put("in", "header");
        return Map.of(producer.getAuthorizationHeader(), scheme);
    }
}
