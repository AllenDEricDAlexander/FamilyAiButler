/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @FileName: ApiDocSpringMvcOpenApiGeneratorTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: Spring MVC OpenAPI 文档生成器测试文件
 * @Version: 1.0
 */
package top.egon.openapi.console;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockServletContext;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import top.egon.openapi.console.annotation.DocBody;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocIgnore;
import top.egon.openapi.console.annotation.DocModel;
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
import top.egon.openapi.console.autoconfigure.ApiDocOpenApiAutoConfiguration;
import top.egon.openapi.console.openapi.ApiDocOpenApiSchemaGenerator;
import top.egon.openapi.console.openapi.ApiDocSpringMvcOpenApiGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @ClassName: ApiDocSpringMvcOpenApiGeneratorTest
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-22Day-14:20
 * @Description: Spring MVC OpenAPI 文档生成器测试
 * @Version: 1.0
 */
class ApiDocSpringMvcOpenApiGeneratorTest {

    private AnnotationConfigWebApplicationContext context;

    /**
     * 测试新文档注解可以生成 REST OpenAPI 基础结构
     */
    @Test
    @SuppressWarnings("unchecked")
    void testGenerateOpenApiFromDocAnnotations() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, UserDocController.class, PlainDocController.class);
        context.refresh();
        ApiDocConsoleProperties properties = new ApiDocConsoleProperties();
        properties.getProducer().setTitle("Doc Test API");
        properties.getProducer().setVersion("2026.05");
        ApiDocSpringMvcOpenApiGenerator generator = new ApiDocSpringMvcOpenApiGenerator(
                context.getBean(RequestMappingHandlerMapping.class),
                new ApiDocOpenApiSchemaGenerator(new ObjectMapper()),
                properties);

        Map<String, Object> openApi = generator.generate();

        Assertions.assertEquals("3.0.3", openApi.get("openapi"));
        Assertions.assertEquals("Doc Test API", ((Map<String, Object>) openApi.get("info")).get("title"));
        Map<String, Object> paths = (Map<String, Object>) openApi.get("paths");
        Assertions.assertTrue(paths.containsKey("/api/doc-users/{tenantId}"));
        Assertions.assertFalse(paths.containsKey("/api/doc-users/ignored"));
        Map<String, Object> post = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/doc-users/{tenantId}")).get("post");
        Assertions.assertEquals("doc.user.create", post.get("operationId"));
        Assertions.assertEquals("创建文档用户", post.get("summary"));
        Assertions.assertEquals(List.of("文档用户"), post.get("tags"));
        Assertions.assertEquals("doc-test", post.get("x-doc-group-id"));
        Assertions.assertEquals("doc-user", post.get("x-doc-service-id"));
        List<Map<String, Object>> parameters = (List<Map<String, Object>>) post.get("parameters");
        Assertions.assertTrue(parameters.stream().anyMatch(parameter ->
                "tenantId".equals(parameter.get("name")) && "path".equals(parameter.get("in")) && Boolean.TRUE.equals(parameter.get("required"))));
        Assertions.assertTrue(parameters.stream().anyMatch(parameter ->
                "source".equals(parameter.get("name")) && "query".equals(parameter.get("in")) && "来源".equals(parameter.get("description"))));
        Map<String, Object> requestBody = (Map<String, Object>) post.get("requestBody");
        Assertions.assertEquals(Boolean.TRUE, requestBody.get("required"));
        Map<String, Object> requestSchema = jsonSchema(requestBody);
        Assertions.assertEquals("#/components/schemas/UserCreateDocRequest", requestSchema.get("$ref"));
        Map<String, Object> response = (Map<String, Object>) ((Map<String, Object>) post.get("responses")).get("200");
        Assertions.assertEquals("#/components/schemas/UserDocResponse", jsonSchema(response).get("$ref"));
        Map<String, Object> schemas = (Map<String, Object>) ((Map<String, Object>) openApi.get("components")).get("schemas");
        Map<String, Object> requestModel = (Map<String, Object>) schemas.get("UserCreateDocRequest");
        Map<String, Object> propertiesNode = (Map<String, Object>) requestModel.get("properties");
        Assertions.assertTrue(propertiesNode.containsKey("full_name"));
        Assertions.assertFalse(propertiesNode.containsKey("internalCode"));
        Assertions.assertFalse(propertiesNode.containsKey("jsonIgnored"));
        Assertions.assertEquals(List.of("full_name"), requestModel.get("required"));
    }

    /**
     * 测试不同 Java 类型不能复用同一个 OpenAPI Schema 名称。
     */
    @Test
    void testSchemaNameConflictShouldFailFast() {
        ApiDocOpenApiSchemaGenerator generator = new ApiDocOpenApiSchemaGenerator(new ObjectMapper());
        generator.schema(FirstConflictDoc.class);

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> generator.schema(SecondConflictDoc.class));

        Assertions.assertTrue(exception.getMessage().contains("OpenAPI Schema 名称冲突"));
        Assertions.assertTrue(exception.getMessage().contains(FirstConflictDoc.class.getName()));
        Assertions.assertTrue(exception.getMessage().contains(SecondConflictDoc.class.getName()));
        Assertions.assertTrue(exception.getMessage().contains("@DocModel(name=...)"));
    }

    /**
     * 测试 OpenAPI info 可以保留 producer 元数据
     */
    @Test
    @SuppressWarnings("unchecked")
    void testInfoIncludesProducerContactAndLicense() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, UserDocController.class);
        context.refresh();
        ApiDocConsoleProperties properties = new ApiDocConsoleProperties();
        properties.getProducer().setTitle("Doc Test API");
        properties.getProducer().setDescription("Doc Test Description");
        properties.getProducer().setVersion("2026.05");
        properties.getProducer().setContactName("Egon");
        properties.getProducer().setContactUrl("https://example.com/contact");
        properties.getProducer().setContactEmail("egon@example.com");
        properties.getProducer().setLicenseName("MIT");
        properties.getProducer().setLicenseUrl("https://example.com/license");
        ApiDocSpringMvcOpenApiGenerator generator = new ApiDocSpringMvcOpenApiGenerator(
                context.getBean(RequestMappingHandlerMapping.class),
                new ApiDocOpenApiSchemaGenerator(new ObjectMapper()),
                properties);

        Map<String, Object> openApi = generator.generate();

        Map<String, Object> info = (Map<String, Object>) openApi.get("info");
        Assertions.assertEquals("Doc Test API", info.get("title"));
        Assertions.assertEquals("Doc Test Description", info.get("description"));
        Assertions.assertEquals("2026.05", info.get("version"));
        Assertions.assertEquals(Map.of(
                "name", "Egon",
                "url", "https://example.com/contact",
                "email", "egon@example.com"), info.get("contact"));
        Assertions.assertEquals(Map.of(
                "name", "MIT",
                "url", "https://example.com/license"), info.get("license"));
    }

    /**
     * 测试 Servlet MVC 自动配置可以暴露 /v3/api-docs
     */
    @Test
    void testAutoConfigurationExposesApiDocsEndpoint() throws Exception {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, AutoConfigurationSupport.class, UserDocController.class, ApiDocOpenApiAutoConfiguration.class);
        context.refresh();
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value("3.0.3"))
                .andExpect(jsonPath("$.info.title").value("OpenAPI Service"))
                .andExpect(jsonPath("$.paths['/api/doc-users/{tenantId}'].post.operationId").value("doc.user.create"))
                .andExpect(jsonPath("$.components.schemas.UserCreateDocRequest").exists());
    }

    /**
     * 测试存在 Actuator controllerEndpointHandlerMapping 时自动配置仍选择 MVC 映射处理器
     */
    @Test
    @SuppressWarnings("unchecked")
    void testAutoConfigurationUsesMvcHandlerMappingWhenActuatorMappingExists() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, AutoConfigurationSupport.class,
                ActuatorMappingCollisionSupport.class, UserDocController.class, ApiDocOpenApiAutoConfiguration.class);
        context.refresh();
        ApiDocSpringMvcOpenApiGenerator generator = context.getBean(ApiDocSpringMvcOpenApiGenerator.class);

        Map<String, Object> openApi = generator.generate();

        Map<String, Object> paths = (Map<String, Object>) openApi.get("paths");
        Assertions.assertTrue(paths.containsKey("/api/doc-users/{tenantId}"));
    }

    /**
     * 测试扫描器覆盖 Spring MVC 常见映射和参数位置
     */
    @Test
    @SuppressWarnings("unchecked")
    void testScannerCoversMvcControllerAndMappingVariants() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, UserDocController.class, PlainDocController.class);
        context.refresh();
        ApiDocSpringMvcOpenApiGenerator generator = generator();

        Map<String, Object> openApi = generator.generate();

        Map<String, Object> paths = (Map<String, Object>) openApi.get("paths");
        Assertions.assertTrue(paths.containsKey("/api/plain/ping"));
        Assertions.assertTrue(((Map<String, Object>) paths.get("/api/plain/ping")).containsKey("get"));
        Map<String, Object> headers = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/doc-users/header-cookie")).get("get");
        List<Map<String, Object>> parameters = (List<Map<String, Object>>) headers.get("parameters");
        Assertions.assertTrue(parameters.stream().anyMatch(parameter -> "X-Trace-Id".equals(parameter.get("name")) && "header".equals(parameter.get("in"))));
        Assertions.assertTrue(parameters.stream().anyMatch(parameter -> "SESSION".equals(parameter.get("name")) && "cookie".equals(parameter.get("in"))));
        Assertions.assertTrue(((Map<String, Object>) paths.get("/api/doc-users/remove/{userId}")).containsKey("delete"));
        Assertions.assertTrue(((Map<String, Object>) paths.get("/api/doc-users/partial/{userId}")).containsKey("patch"));
        Assertions.assertTrue(((Map<String, Object>) paths.get("/api/doc-users/explicit")).containsKey("delete"));
        Map<String, Object> allMethods = (Map<String, Object>) paths.get("/api/doc-users/all-methods");
        Assertions.assertTrue(allMethods.containsKey("get"));
        Assertions.assertTrue(allMethods.containsKey("post"));
        Assertions.assertTrue(allMethods.containsKey("put"));
        Assertions.assertTrue(allMethods.containsKey("patch"));
        Assertions.assertTrue(allMethods.containsKey("delete"));
        Assertions.assertTrue(allMethods.containsKey("options"));
        Assertions.assertTrue(allMethods.containsKey("head"));
        Assertions.assertTrue(allMethods.containsKey("trace"));
    }

    /**
     * 测试泛型响应包装可以解析实际 DTO Schema
     */
    @Test
    @SuppressWarnings("unchecked")
    void testGenericResponseWrapperResolvesTypeVariableSchema() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, UserDocController.class, PlainDocController.class);
        context.refresh();
        ApiDocSpringMvcOpenApiGenerator generator = generator();

        Map<String, Object> openApi = generator.generate();

        Map<String, Object> paths = (Map<String, Object>) openApi.get("paths");
        Map<String, Object> get = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/doc-users/wrapped")).get("get");
        Assertions.assertEquals("#/components/schemas/ResultDocPageResultDocUserDocResponse", jsonSchema((Map<String, Object>) ((Map<String, Object>) get.get("responses")).get("200")).get("$ref"));
        Map<String, Object> schemas = (Map<String, Object>) ((Map<String, Object>) openApi.get("components")).get("schemas");
        Map<String, Object> resultSchema = (Map<String, Object>) schemas.get("ResultDocPageResultDocUserDocResponse");
        Map<String, Object> resultProperties = (Map<String, Object>) resultSchema.get("properties");
        Assertions.assertEquals("#/components/schemas/PageResultDocUserDocResponse", ((Map<String, Object>) resultProperties.get("data")).get("$ref"));
        Map<String, Object> pageSchema = (Map<String, Object>) schemas.get("PageResultDocUserDocResponse");
        Map<String, Object> pageProperties = (Map<String, Object>) pageSchema.get("properties");
        Map<String, Object> records = (Map<String, Object>) pageProperties.get("records");
        Assertions.assertEquals("#/components/schemas/UserDocResponse", ((Map<String, Object>) records.get("items")).get("$ref"));
    }

    /**
     * 测试 DocTypeReference 显式复杂泛型可以生成包装响应 Schema 和示例
     */
    @Test
    @SuppressWarnings("unchecked")
    void testDocTypeReferenceResponseGeneratesWrapperSchemaAndExample() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, UserDocController.class);
        context.refresh();
        ApiDocSpringMvcOpenApiGenerator generator = generator();

        Map<String, Object> openApi = generator.generate();

        Map<String, Object> paths = (Map<String, Object>) openApi.get("paths");
        Map<String, Object> get = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/doc-users/generic-ref")).get("get");
        Map<String, Object> response = (Map<String, Object>) ((Map<String, Object>) get.get("responses")).get("200");
        Assertions.assertEquals("#/components/schemas/ResultDocPageResultDocUserDocResponse", jsonSchema(response).get("$ref"));
        Map<String, Object> content = (Map<String, Object>) ((Map<String, Object>) response.get("content")).get("application/json");
        Map<String, Object> example = (Map<String, Object>) content.get("example");
        Map<String, Object> data = (Map<String, Object>) example.get("data");
        List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records");
        Assertions.assertEquals(10001L, records.get(0).get("userId"));
    }

    /**
     * 测试 FAIL 契约策略会拒绝无法匹配的显式参数
     */
    @Test
    void testContractPolicyFailRejectsMissingExplicitParameter() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, PolicyDocController.class);
        context.refresh();
        ApiDocConsoleProperties properties = new ApiDocConsoleProperties();
        properties.getProducer().setContractPolicy(ApiDocConsoleProperties.Policy.FAIL);
        ApiDocSpringMvcOpenApiGenerator generator = new ApiDocSpringMvcOpenApiGenerator(
                context.getBean(RequestMappingHandlerMapping.class),
                new ApiDocOpenApiSchemaGenerator(new ObjectMapper()),
                properties);

        Assertions.assertThrows(IllegalStateException.class, generator::generate);
    }

    /**
     * 测试 FAIL 契约策略会拒绝非 void 方法的默认响应契约
     */
    @Test
    void testContractPolicyFailRejectsDefaultResponseOnNonVoidMethod() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, DefaultResponsePolicyController.class);
        context.refresh();

        Assertions.assertThrows(IllegalStateException.class, () -> policyGenerator(ApiDocConsoleProperties.Policy.FAIL).generate());
    }

    /**
     * 测试 WARN 和 OFF 契约策略不会阻断默认响应契约
     */
    @Test
    void testContractPolicyWarnAndOffAllowDefaultResponseOnNonVoidMethod() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, DefaultResponsePolicyController.class);
        context.refresh();

        Assertions.assertDoesNotThrow(() -> policyGenerator(ApiDocConsoleProperties.Policy.WARN).generate());
        Assertions.assertDoesNotThrow(() -> policyGenerator(ApiDocConsoleProperties.Policy.OFF).generate());
    }

    /**
     * 测试 FAIL 契约策略会拒绝响应 wrapper 外层类型不一致
     */
    @Test
    void testContractPolicyFailRejectsWrapperOuterTypeMismatch() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, WrapperMismatchPolicyController.class);
        context.refresh();

        Assertions.assertThrows(IllegalStateException.class, () -> policyGenerator(ApiDocConsoleProperties.Policy.FAIL).generate());
    }

    /**
     * 测试 FAIL 契约策略会拒绝响应 dataType 和返回泛型不一致
     */
    @Test
    void testContractPolicyFailRejectsResponseDataTypeMismatch() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, DataMismatchPolicyController.class);
        context.refresh();

        Assertions.assertThrows(IllegalStateException.class, () -> policyGenerator(ApiDocConsoleProperties.Policy.FAIL).generate());
    }

    /**
     * 测试 FAIL 示例策略会拒绝缺少 example 的简单字段
     */
    @Test
    void testExamplePolicyFailRejectsMissingFieldExample() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, MissingExampleController.class);
        context.refresh();
        ApiDocConsoleProperties properties = new ApiDocConsoleProperties();
        properties.getProducer().setExamplePolicy(ApiDocConsoleProperties.Policy.FAIL);
        ApiDocSpringMvcOpenApiGenerator generator = new ApiDocSpringMvcOpenApiGenerator(
                context.getBean(RequestMappingHandlerMapping.class),
                new ApiDocOpenApiSchemaGenerator(new ObjectMapper()),
                properties);

        Assertions.assertThrows(IllegalStateException.class, generator::generate);
    }

    /**
     * 测试操作级参数、请求体、响应和 Schema 提示可以合并到 OpenAPI
     */
    @Test
    @SuppressWarnings("unchecked")
    void testDocOperationSupportsExplicitInputsResponsesAndSchemaHints() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, UserDocController.class, PlainDocController.class);
        context.refresh();
        ApiDocSpringMvcOpenApiGenerator generator = generator();

        Map<String, Object> openApi = generator.generate();

        Map<String, Object> paths = (Map<String, Object>) openApi.get("paths");
        Map<String, Object> complex = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/doc-users/complex")).get("post");
        List<Map<String, Object>> parameters = (List<Map<String, Object>>) complex.get("parameters");
        Assertions.assertTrue(parameters.stream().anyMatch(parameter ->
                "X-Tenant-Id".equals(parameter.get("name"))
                        && "header".equals(parameter.get("in"))
                        && Boolean.TRUE.equals(parameter.get("required"))));
        Assertions.assertFalse(parameters.stream().anyMatch(parameter -> "debug".equals(parameter.get("name"))));
        Map<String, Object> requestBody = (Map<String, Object>) complex.get("requestBody");
        Assertions.assertEquals("创建用户请求", requestBody.get("description"));
        Assertions.assertTrue(((Map<String, Object>) requestBody.get("content")).containsKey("application/json"));
        Map<String, Object> responses = (Map<String, Object>) complex.get("responses");
        Assertions.assertTrue(responses.containsKey("201"));
        Assertions.assertFalse(responses.containsKey("400"));
        Assertions.assertFalse(responses.containsKey("204"));
        Map<String, Object> createdSchema = jsonSchema((Map<String, Object>) responses.get("201"));
        Assertions.assertEquals("#/components/schemas/ResultDocUserDocResponse", createdSchema.get("$ref"));
        Assertions.assertEquals("data", createdSchema.get("x-doc-data-path"));
        Assertions.assertTrue(((Map<String, Object>) ((Map<String, Object>) responses.get("201")).get("headers")).containsKey("X-Request-Id"));
        Map<String, Object> createdContent = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) responses.get("201")).get("content")).get("application/json");
        Map<String, Object> createdExample = (Map<String, Object>) createdContent.get("example");
        Assertions.assertEquals(0, createdExample.get("code"));
        Assertions.assertEquals(10001L, ((Map<String, Object>) createdExample.get("data")).get("userId"));

        Map<String, Object> upload = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/doc-users/upload")).get("post");
        Map<String, Object> uploadRequestBody = (Map<String, Object>) upload.get("requestBody");
        Map<String, Object> multipartContent = (Map<String, Object>) ((Map<String, Object>) uploadRequestBody.get("content")).get("multipart/form-data");
        Map<String, Object> multipartSchema = (Map<String, Object>) multipartContent.get("schema");
        Map<String, Object> multipartProperties = (Map<String, Object>) multipartSchema.get("properties");
        Map<String, Object> fileSchema = (Map<String, Object>) multipartProperties.get("file");
        Assertions.assertEquals("array", fileSchema.get("type"));
        Assertions.assertEquals("binary", ((Map<String, Object>) fileSchema.get("items")).get("format"));

        Map<String, Object> schemas = (Map<String, Object>) ((Map<String, Object>) openApi.get("components")).get("schemas");
        Map<String, Object> requestModel = (Map<String, Object>) schemas.get("UserCreateDocRequest");
        Map<String, Object> propertiesNode = (Map<String, Object>) requestModel.get("properties");
        Map<String, Object> fullName = (Map<String, Object>) propertiesNode.get("full_name");
        Assertions.assertEquals(2, fullName.get("minLength"));
        Assertions.assertEquals(32, fullName.get("maxLength"));
        Assertions.assertEquals("mario", fullName.get("example"));
        Map<String, Object> nickname = (Map<String, Object>) propertiesNode.get("nickname");
        Assertions.assertEquals("string", nickname.get("type"));
        Assertions.assertFalse(((List<String>) requestModel.get("required")).contains("nickname"));
        Map<String, Object> requestContent = (Map<String, Object>) ((Map<String, Object>) requestBody.get("content")).get("application/json");
        Assertions.assertTrue(((Map<String, Object>) requestContent.get("example")).containsKey("full_name"));
    }

    /**
     * 测试操作级参数可以通过 source 匹配并覆盖展开查询 DTO 字段
     */
    @Test
    @SuppressWarnings("unchecked")
    void testOperationParameterSourceOverridesFlattenedQueryField() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, UserDocController.class, PlainDocController.class);
        context.refresh();
        ApiDocSpringMvcOpenApiGenerator generator = generator();

        Map<String, Object> openApi = generator.generate();

        Map<String, Object> paths = (Map<String, Object>) openApi.get("paths");
        Map<String, Object> get = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/doc-users/source-search")).get("get");
        List<Map<String, Object>> parameters = (List<Map<String, Object>>) get.get("parameters");
        Assertions.assertTrue(parameters.stream().anyMatch(parameter ->
                "keywordAlias".equals(parameter.get("name"))
                        && "query".equals(parameter.get("in"))
                        && "filter.keyword".equals(parameter.get("x-doc-source"))
                        && "关键词 source 覆盖".equals(parameter.get("description"))
                        && Boolean.TRUE.equals(parameter.get("required"))));
        Assertions.assertFalse(parameters.stream().anyMatch(parameter -> "keyword".equals(parameter.get("name"))));
        Assertions.assertTrue(parameters.stream().anyMatch(parameter ->
                "missingBySource".equals(parameter.get("name"))
                        && "query".equals(parameter.get("in"))
                        && "filter.missing".equals(parameter.get("x-doc-source"))));
        Assertions.assertTrue(parameters.stream().noneMatch(parameter -> "auto".equals(parameter.get("in"))));
    }

    /**
     * 测试操作级 AUTO 参数优先匹配已推断参数，未匹配时使用安全默认 query
     */
    @Test
    @SuppressWarnings("unchecked")
    void testOperationParameterAutoResolvesMatchedParameterOrDefaultsToQuery() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, UserDocController.class, PlainDocController.class);
        context.refresh();
        ApiDocSpringMvcOpenApiGenerator generator = generator();

        Map<String, Object> openApi = generator.generate();

        Map<String, Object> paths = (Map<String, Object>) openApi.get("paths");
        Map<String, Object> get = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/doc-users/auto-header")).get("get");
        List<Map<String, Object>> parameters = (List<Map<String, Object>>) get.get("parameters");
        Assertions.assertTrue(parameters.stream().anyMatch(parameter ->
                "X-Auto-Trace".equals(parameter.get("name"))
                        && "header".equals(parameter.get("in"))
                        && "AUTO 匹配 Header".equals(parameter.get("description"))
                        && Boolean.TRUE.equals(parameter.get("required"))));
        Assertions.assertTrue(parameters.stream().anyMatch(parameter ->
                "fallbackAuto".equals(parameter.get("name"))
                        && "query".equals(parameter.get("in"))
                        && Boolean.FALSE.equals(parameter.get("required"))));
        Assertions.assertTrue(parameters.stream().noneMatch(parameter -> "auto".equals(parameter.get("in"))));
    }

    /**
     * 测试非 2xx Void Schema 响应不会错误推断成功返回类型
     */
    @Test
    @SuppressWarnings("unchecked")
    void testNon2xxVoidResponseDoesNotInferSuccessReturnType() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, UserDocController.class, PlainDocController.class);
        context.refresh();
        ApiDocSpringMvcOpenApiGenerator generator = generator();

        Map<String, Object> openApi = generator.generate();

        Map<String, Object> paths = (Map<String, Object>) openApi.get("paths");
        Map<String, Object> post = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/doc-users/non2xx-void")).get("post");
        Map<String, Object> responses = (Map<String, Object>) post.get("responses");
        Assertions.assertFalse(responses.containsKey("200"));
        Assertions.assertEquals("object", jsonSchema((Map<String, Object>) responses.get("400")).get("type"));
    }

    /**
     * 测试无注解复杂参数按请求方法推断 query 或 requestBody
     */
    @Test
    @SuppressWarnings("unchecked")
    void testUnannotatedComplexParameterInferredByHttpMethod() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, UserDocController.class, PlainDocController.class);
        context.refresh();
        ApiDocSpringMvcOpenApiGenerator generator = generator();

        Map<String, Object> openApi = generator.generate();

        Map<String, Object> paths = (Map<String, Object>) openApi.get("paths");
        Map<String, Object> get = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/doc-users/search")).get("get");
        List<Map<String, Object>> getParameters = (List<Map<String, Object>>) get.get("parameters");
        Assertions.assertTrue(getParameters.stream().anyMatch(parameter -> "keyword".equals(parameter.get("name")) && "query".equals(parameter.get("in"))));
        Assertions.assertTrue(getParameters.stream().anyMatch(parameter -> "pageNum".equals(parameter.get("name")) && "query".equals(parameter.get("in"))));
        Assertions.assertFalse(get.containsKey("requestBody"));
        Map<String, Object> put = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/doc-users/replace")).get("put");
        Assertions.assertFalse(put.containsKey("parameters"));
        Assertions.assertEquals("#/components/schemas/UserCreateDocRequest", jsonSchema((Map<String, Object>) put.get("requestBody")).get("$ref"));
    }

    /**
     * 测试普通 Java 参数按 Spring 和 Java 规则推断为 query
     */
    @Test
    @SuppressWarnings("unchecked")
    void testPlainJavaParameterFallsBackToQuery() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestMvcConfiguration.class, UserDocController.class, PlainDocController.class);
        context.refresh();
        ApiDocSpringMvcOpenApiGenerator generator = generator();

        Map<String, Object> openApi = generator.generate();

        Map<String, Object> paths = (Map<String, Object>) openApi.get("paths");
        Map<String, Object> get = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/doc-users/plain-query")).get("get");
        List<Map<String, Object>> parameters = (List<Map<String, Object>>) get.get("parameters");
        Assertions.assertTrue(parameters.stream().anyMatch(parameter ->
                "keyword".equals(parameter.get("name")) && "query".equals(parameter.get("in"))));
        Assertions.assertTrue(parameters.stream().noneMatch(parameter -> "default".equals(parameter.get("in"))));
    }

    /**
     * 清理 MVC 测试上下文
     */
    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    /**
     * 获取 JSON 内容中的 schema
     *
     * @param node OpenAPI 内容节点
     * @return Map<String, Object> 返回 schema 节点
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> jsonSchema(Map<String, Object> node) {
        return (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) node.get("content")).get("application/json")).get("schema");
    }

    /**
     * 创建 Spring MVC OpenAPI 文档生成器
     *
     * @return ApiDocSpringMvcOpenApiGenerator 返回 Spring MVC OpenAPI 文档生成器
     */
    private ApiDocSpringMvcOpenApiGenerator generator() {
        ApiDocConsoleProperties properties = new ApiDocConsoleProperties();
        properties.getProducer().setTitle("Doc Test API");
        properties.getProducer().setVersion("2026.05");
        return new ApiDocSpringMvcOpenApiGenerator(
                context.getBean(RequestMappingHandlerMapping.class),
                new ApiDocOpenApiSchemaGenerator(new ObjectMapper()),
                properties);
    }

    /**
     * 创建指定契约策略的 Spring MVC OpenAPI 文档生成器
     *
     * @param policy 契约策略
     * @return ApiDocSpringMvcOpenApiGenerator 返回 Spring MVC OpenAPI 文档生成器
     */
    private ApiDocSpringMvcOpenApiGenerator policyGenerator(ApiDocConsoleProperties.Policy policy) {
        ApiDocConsoleProperties properties = new ApiDocConsoleProperties();
        properties.getProducer().setContractPolicy(policy);
        return new ApiDocSpringMvcOpenApiGenerator(
                context.getBean(RequestMappingHandlerMapping.class),
                new ApiDocOpenApiSchemaGenerator(new ObjectMapper()),
                properties);
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: TestMvcConfiguration
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-14:20
     * @Description: Spring MVC 测试配置
     * @Version: 1.0
     */
    @Configuration
    @EnableWebMvc
    static class TestMvcConfiguration {
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: AutoConfigurationSupport
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-18:10
     * @Description: OpenAPI 自动配置测试支撑配置
     * @Version: 1.0
     */
    @Configuration
    static class AutoConfigurationSupport {

        /**
         * 创建 Jackson 映射器
         *
         * @return ObjectMapper 返回 Jackson 映射器
         */
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: ActuatorMappingCollisionSupport
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-18:20
     * @Description: Actuator 映射处理器冲突测试配置
     * @Version: 1.0
     */
    @Configuration
    static class ActuatorMappingCollisionSupport {

        /**
         * 创建 Actuator ControllerEndpoint 映射处理器
         *
         * @return RequestMappingHandlerMapping 返回 Actuator ControllerEndpoint 映射处理器
         */
        @Bean("controllerEndpointHandlerMapping")
        RequestMappingHandlerMapping controllerEndpointHandlerMapping() {
            return new RequestMappingHandlerMapping();
        }
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: UserDocController
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-14:20
     * @Description: 文档用户测试控制器
     * @Version: 1.0
     */
    @RestController
    @RequestMapping("/api/doc-users")
    @DocService(groupId = "doc-test", groupName = "文档测试", serviceId = "doc-user", serviceName = "文档用户",
            serviceDescription = "文档用户 REST API", version = "1.0.0", protocol = DocProtocol.HTTP)
    static class UserDocController {

        /**
         * 创建文档用户
         *
         * @param tenantId 租户 ID
         * @param source   来源
         * @param request  创建请求
         * @return ResponseEntity<UserDocResponse> 返回用户信息
         */
        @PostMapping("/{tenantId}")
        @DocOperation(id = "doc.user.create", summary = "创建文档用户", description = "创建文档用户并返回详情")
        ResponseEntity<UserDocResponse> create(@PathVariable @DocParam(description = "租户 ID", required = true) Long tenantId,
                                               @RequestParam @DocParam(description = "来源") String source,
                                               @Valid @RequestBody UserCreateDocRequest request) {
            return ResponseEntity.ok(new UserDocResponse());
        }

        /**
         * 被忽略的接口
         *
         * @return String 返回忽略结果
         */
        @DocIgnore
        @GetMapping("/ignored")
        String ignored() {
            return "ignored";
        }

        /**
         * 查询泛型包装响应
         *
         * @return ResultDoc<PageResultDoc < UserDocResponse>> 返回包装响应
         */
        @GetMapping("/wrapped")
        @DocOperation(id = "doc.user.wrapped", summary = "查询包装用户")
        ResultDoc<PageResultDoc<UserDocResponse>> wrapped() {
            return new ResultDoc<>();
        }

        /**
         * 查询显式泛型引用响应
         *
         * @return ResultDoc<PageResultDoc < UserDocResponse>> 返回包装响应
         */
        @GetMapping("/generic-ref")
        @DocOperation(id = "doc.user.genericRef", summary = "查询显式泛型用户",
                response = @DocResponse(description = "查询成功",
                        dataType = @DocDataType(kind = DocDataKind.GENERIC, ref = UserPageDataType.class),
                        wrapper = @DocWrapper(type = ResultDoc.class, dataPath = "data")))
        ResultDoc<PageResultDoc<UserDocResponse>> genericRef() {
            return new ResultDoc<>();
        }

        /**
         * 复杂创建用户
         *
         * @param request 创建请求
         * @return ResultDoc<UserDocResponse> 返回创建结果
         */
        @PostMapping("/complex")
        @DocOperation(
                id = "doc.user.complex",
                summary = "复杂创建用户",
                request = @DocRequest(
                        params = {
                                @DocParameter(name = "X-Tenant-Id", in = DocParamIn.HEADER, description = "租户 ID", required = true,
                                        dataType = @DocDataType(kind = DocDataKind.LONG), example = "10001"),
                                @DocParameter(name = "debug", in = DocParamIn.QUERY, hidden = true)
                        },
                        body = @DocBody(description = "创建用户请求", contentType = "application/json",
                                dataType = @DocDataType(kind = DocDataKind.OBJECT, type = UserCreateDocRequest.class))),
                response = @DocResponse(status = 201, description = "创建成功",
                        headers = {@DocParameter(name = "X-Request-Id", description = "请求 ID")},
                        dataType = @DocDataType(kind = DocDataKind.OBJECT, type = UserDocResponse.class),
                        wrapper = @DocWrapper(type = ResultDoc.class, dataPath = "data"))
        )
        ResultDoc<UserDocResponse> complex(@RequestBody UserCreateDocRequest request) {
            return new ResultDoc<>();
        }

        /**
         * 上传文件
         *
         * @param file 上传文件
         * @return FileUploadDocResponse 返回上传结果
         */
        @PostMapping("/upload")
        @DocOperation(
                id = "doc.user.upload",
                summary = "上传文件",
                request = @DocRequest(
                        body = @DocBody(enabled = true, contentType = "multipart/form-data"),
                        params = {
                                @DocParameter(name = "file", in = DocParamIn.FILE, required = true, description = "上传文件列表",
                                        dataType = @DocDataType(kind = DocDataKind.ARRAY, itemType = MultipartFile.class))
                        }),
                response = @DocResponse(description = "上传成功",
                        dataType = @DocDataType(kind = DocDataKind.OBJECT, type = FileUploadDocResponse.class))
        )
        FileUploadDocResponse upload(@RequestParam("file") List<MultipartFile> file) {
            return new FileUploadDocResponse();
        }

        /**
         * 查询用户
         *
         * @param query 查询条件
         * @return List<UserDocResponse> 返回用户列表
         */
        @GetMapping("/search")
        @DocOperation(id = "doc.user.search", summary = "查询用户")
        List<UserDocResponse> search(UserQueryDocRequest query) {
            return List.of();
        }

        /**
         * 通过 source 覆盖展开查询参数
         *
         * @param filter 查询条件
         * @return List<UserDocResponse> 返回用户列表
         */
        @GetMapping("/source-search")
        @DocOperation(id = "doc.user.sourceSearch", summary = "通过 source 查询用户",
                request = @DocRequest(params = {
                        @DocParameter(name = "keywordAlias", source = "filter.keyword", description = "关键词 source 覆盖", required = true),
                        @DocParameter(name = "missingBySource", source = "filter.missing", description = "不存在 source")
                }))
        List<UserDocResponse> sourceSearch(UserQueryDocRequest filter) {
            return List.of();
        }

        /**
         * 查询 AUTO 参数
         *
         * @param traceId 请求追踪 ID
         * @return UserDocResponse 返回用户信息
         */
        @GetMapping("/auto-header")
        @DocOperation(id = "doc.user.autoHeader", summary = "查询 AUTO 参数",
                request = @DocRequest(params = {
                        @DocParameter(name = "X-Auto-Trace", description = "AUTO 匹配 Header", required = true),
                        @DocParameter(name = "fallbackAuto", description = "AUTO 未匹配默认 query")
                }))
        UserDocResponse autoHeader(@RequestHeader("X-Auto-Trace") String traceId) {
            return new UserDocResponse();
        }

        /**
         * 测试非 2xx Void Schema
         *
         * @param request 创建请求
         * @return UserDocResponse 返回用户信息
         */
        @PostMapping("/non2xx-void")
        @DocOperation(id = "doc.user.non2xxVoid", summary = "测试非 2xx Void Schema",
                response = @DocResponse(status = 400, description = "参数错误",
                        dataType = @DocDataType(kind = DocDataKind.VOID)))
        UserDocResponse non2xxVoid(@RequestBody UserCreateDocRequest request) {
            return new UserDocResponse();
        }

        /**
         * 替换用户
         *
         * @param request 替换请求
         * @return UserDocResponse 返回用户信息
         */
        @PutMapping("/replace")
        @DocOperation(id = "doc.user.replace", summary = "替换用户")
        UserDocResponse replace(UserCreateDocRequest request) {
            return new UserDocResponse();
        }

        /**
         * 查询普通参数默认位置
         *
         * @param keyword 关键词
         * @return List<UserDocResponse> 返回用户列表
         */
        @GetMapping("/plain-query")
        @DocOperation(id = "doc.user.plainQuery", summary = "查询普通参数默认位置")
        List<UserDocResponse> plainQuery(String keyword) {
            return List.of();
        }

        /**
         * 查询 Header 和 Cookie 参数
         *
         * @param traceId 请求追踪 ID
         * @param session 会话标识
         * @return UserDocResponse 返回用户信息
         */
        @GetMapping("/header-cookie")
        @DocOperation(id = "doc.user.headerCookie", summary = "查询 Header 和 Cookie 参数")
        UserDocResponse headerCookie(@RequestHeader("X-Trace-Id") String traceId,
                                     @CookieValue("SESSION") String session) {
            return new UserDocResponse();
        }

        /**
         * 删除用户
         *
         * @param userId 用户 ID
         */
        @DeleteMapping("/remove/{userId}")
        @DocOperation(id = "doc.user.remove", summary = "删除用户")
        void remove(@PathVariable Long userId) {
        }

        /**
         * 局部更新用户
         *
         * @param userId  用户 ID
         * @param request 更新请求
         * @return UserDocResponse 返回用户信息
         */
        @PatchMapping("/partial/{userId}")
        @DocOperation(id = "doc.user.partial", summary = "局部更新用户")
        UserDocResponse partial(@PathVariable Long userId, UserCreateDocRequest request) {
            return new UserDocResponse();
        }

        /**
         * 显式 RequestMapping 删除用户
         */
        @RequestMapping(value = "/explicit", method = RequestMethod.DELETE)
        @DocOperation(id = "doc.user.explicit", summary = "显式 RequestMapping 删除用户")
        void explicitDelete() {
        }

        /**
         * 未显式声明 HTTP Method 的映射
         *
         * @return String 返回结果
         */
        @RequestMapping("/all-methods")
        @DocOperation(id = "doc.user.allMethods", summary = "未显式声明 HTTP Method 的映射")
        String allMethods() {
            return "ok";
        }
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: PlainDocController
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-18:10
     * @Description: 普通 MVC 文档测试控制器
     * @Version: 1.0
     */
    @Controller
    @RequestMapping("/api/plain")
    @DocService(groupId = "doc-test", groupName = "文档测试", serviceId = "plain-mvc", serviceName = "普通 MVC",
            serviceDescription = "普通 MVC REST API", protocol = DocProtocol.HTTP)
    static class PlainDocController {

        /**
         * 测试 ResponseBody 方法
         *
         * @return String 返回结果
         */
        @ResponseBody
        @GetMapping("/ping")
        @DocOperation(id = "plain.ping", summary = "测试 ResponseBody 方法")
        String ping() {
            return "pong";
        }
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: PolicyDocController
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-14:20
     * @Description: 策略测试控制器
     * @Version: 1.0
     */
    @RestController
    @RequestMapping("/api/policy")
    @DocService(groupId = "doc-test", groupName = "文档测试", serviceId = "policy", serviceName = "策略测试",
            protocol = DocProtocol.HTTP)
    static class PolicyDocController {

        @GetMapping("/missing-param")
        @DocOperation(id = "policy.missingParam", summary = "缺失参数",
                request = @DocRequest(params = {
                        @DocParameter(name = "missing", in = DocParamIn.HEADER, description = "缺失参数")
                }))
        String missingParam() {
            return "ok";
        }
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: DefaultResponsePolicyController
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-14:20
     * @Description: 默认响应策略测试控制器
     * @Version: 1.0
     */
    @RestController
    @RequestMapping("/api/default-response-policy")
    @DocService(groupId = "doc-test", groupName = "文档测试", serviceId = "default-response-policy",
            serviceName = "默认响应策略", protocol = DocProtocol.HTTP)
    static class DefaultResponsePolicyController {

        @GetMapping
        @DocOperation(id = "policy.defaultResponse", summary = "默认响应")
        UserDocResponse defaultResponse() {
            return new UserDocResponse();
        }
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: WrapperMismatchPolicyController
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-14:20
     * @Description: wrapper 不一致策略测试控制器
     * @Version: 1.0
     */
    @RestController
    @RequestMapping("/api/wrapper-mismatch-policy")
    @DocService(groupId = "doc-test", groupName = "文档测试", serviceId = "wrapper-mismatch-policy",
            serviceName = "wrapper 不一致策略", protocol = DocProtocol.HTTP)
    static class WrapperMismatchPolicyController {

        @GetMapping
        @DocOperation(id = "policy.wrapperMismatch", summary = "wrapper 不一致",
                response = @DocResponse(description = "查询成功",
                        dataType = @DocDataType(kind = DocDataKind.OBJECT, type = UserDocResponse.class),
                        wrapper = @DocWrapper(type = ResultDoc.class, dataPath = "data")))
        UserDocResponse wrapperMismatch() {
            return new UserDocResponse();
        }
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: DataMismatchPolicyController
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-14:20
     * @Description: dataType 不一致策略测试控制器
     * @Version: 1.0
     */
    @RestController
    @RequestMapping("/api/data-mismatch-policy")
    @DocService(groupId = "doc-test", groupName = "文档测试", serviceId = "data-mismatch-policy",
            serviceName = "dataType 不一致策略", protocol = DocProtocol.HTTP)
    static class DataMismatchPolicyController {

        @GetMapping
        @DocOperation(id = "policy.dataMismatch", summary = "dataType 不一致",
                response = @DocResponse(description = "查询成功",
                        dataType = @DocDataType(kind = DocDataKind.OBJECT, type = ErrorDocResponse.class),
                        wrapper = @DocWrapper(type = ResultDoc.class, dataPath = "data")))
        ResultDoc<UserDocResponse> dataMismatch() {
            return new ResultDoc<>();
        }
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: MissingExampleController
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-14:20
     * @Description: 缺失示例测试控制器
     * @Version: 1.0
     */
    @RestController
    @RequestMapping("/api/missing-example")
    @DocService(groupId = "doc-test", groupName = "文档测试", serviceId = "missing-example", serviceName = "缺失示例",
            protocol = DocProtocol.HTTP)
    static class MissingExampleController {

        @GetMapping
        @DocOperation(id = "example.missing", summary = "缺失示例")
        MissingExampleDoc missing() {
            return new MissingExampleDoc();
        }
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: UserCreateDocRequest
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-14:20
     * @Description: 文档用户创建请求
     * @Version: 1.0
     */
    @DocModel(description = "文档用户创建请求")
    static class UserCreateDocRequest {

        @NotBlank
        @Size(min = 2, max = 32)
        @JsonProperty("full_name")
        @DocField(description = "完整姓名", required = true, example = "mario")
        private String fullName;

        @DocField(description = "昵称", example = "Egon")
        private Optional<String> nickname;

        @DocIgnore
        private String internalCode;

        @JsonIgnore
        private String jsonIgnored;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: UserDocResponse
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-14:20
     * @Description: 文档用户响应
     * @Version: 1.0
     */
    @DocModel(description = "文档用户响应")
    static class UserDocResponse {

        @DocField(description = "用户 ID", example = "10001")
        private Long userId;
    }

    static final class UserPageDataType extends DocTypeReference<PageResultDoc<UserDocResponse>> {
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: ErrorDocResponse
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-19:15
     * @Description: 文档错误响应
     * @Version: 1.0
     */
    @DocModel(description = "文档错误响应")
    static class ErrorDocResponse {

        @DocField(description = "错误编码", example = "PARAM_ERROR")
        private String errorCode;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: FileUploadDocResponse
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-19:15
     * @Description: 文件上传响应
     * @Version: 1.0
     */
    @DocModel(description = "文件上传响应")
    static class FileUploadDocResponse {

        @DocField(description = "文件数量", example = "2")
        private Integer count;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: UserQueryDocRequest
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-15:45
     * @Description: 文档用户查询请求
     * @Version: 1.0
     */
    @DocModel(description = "文档用户查询请求")
    static class UserQueryDocRequest {

        @DocField(description = "关键词", example = "mario")
        private String keyword;

        @DocField(description = "页码", example = "1")
        private Integer pageNum;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: ResultDoc
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-15:45
     * @Description: 测试通用响应包装
     * @Version: 1.0
     */
    @DocModel(description = "测试通用响应包装")
    static class ResultDoc<T> {

        @DocField(description = "业务编码", example = "0")
        private Integer code;

        @DocField(description = "响应数据")
        private T data;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: PageResultDoc
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-22Day-15:45
     * @Description: 测试分页响应包装
     * @Version: 1.0
     */
    @DocModel(description = "测试分页响应包装")
    static class PageResultDoc<T> {

        @DocField(description = "数据列表")
        private List<T> records = new ArrayList<>();

        @DocField(description = "总数", example = "100")
        private Long total;
    }

    @DocModel(description = "缺失示例对象")
    static class MissingExampleDoc {

        @DocField(description = "名称")
        private String name;
    }

    @DocModel(name = "ConflictDoc", description = "第一个冲突测试对象")
    static class FirstConflictDoc {

        @DocField(description = "名称", example = "first")
        private String name;
    }

    @DocModel(name = "ConflictDoc", description = "第二个冲突测试对象")
    static class SecondConflictDoc {

        @DocField(description = "编码", example = "second")
        private String code;
    }
}
