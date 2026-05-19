/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @FileName: ApiDocConsoleProperties.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:20
 * @Description: OpenAPI 调试文档控制台配置文件
 * @Version: 1.0
 */
package top.egon.openapi.console;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @ClassName: ApiDocConsoleProperties
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:20
 * @Description: OpenAPI 调试文档控制台配置属性
 * @Version: 1.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "egon.openapi.console")
public class ApiDocConsoleProperties {

    private boolean enabled = false;

    private String basePath = "/openapi-console";

    private String title = "OpenAPI Debug Console";

    private String environment = "dev";

    private Mode mode = Mode.AUTO;

    private Duration requestTimeout = Duration.ofSeconds(30);

    private DataSize maxResponseSize = DataSize.ofMegabytes(3);

    private Auth auth = new Auth();

    private Signing signing = new Signing();

    private LoadTest loadTest = new LoadTest();

    private Export export = new Export();

    private Producer producer = new Producer();

    private List<ServiceRoute> services = new ArrayList<>();

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: Mode
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: OpenAPI 调试文档控制台开放模式
     * @Version: 1.0
     */
    public enum Mode {
        AUTO,
        OFF,
        READ_ONLY,
        FULL
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: Auth
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 本地配置账号与签名 Cookie 配置
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class Auth {

        private String username = "admin";

        private String password = "OpenApi@123456";

        private String cookieName = "OPENAPI_DEBUG_CONSOLE_SESSION";

        private Duration ttl = Duration.ofHours(8);

        private boolean secureCookie = false;

        private String sameSite = "Lax";

        private String sessionSecret = "OpenApiDebugConsoleSessionSecretChangeMe";
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: Signing
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 代理调试请求签名配置
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class Signing {

        private boolean enabled = false;

        private String accessKeyId = "openapi-debug-console";

        private String secret = "OpenApiDebugConsoleSigningSecretChangeMe";

        private String algorithm = "HmacSHA256";

        private String accessKeyHeader = "X-Access-Key";

        private String timestampHeader = "X-Timestamp";

        private String nonceHeader = "X-Nonce";

        private String signatureHeader = "X-Signature";
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: LoadTest
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 网关内置轻量压测配置
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class LoadTest {

        private boolean enabled = true;

        private int maxRequests = 200;

        private int maxConcurrency = 20;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: Export
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 接口文档导出配置
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class Export {

        private boolean enabled = true;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: ServiceRoute
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 被聚合服务配置
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class ServiceRoute {

        private String id;

        private String name;

        private String group = "default";

        private String openApiUrl;

        private String baseUrl;

        private boolean enabled = true;

        private int order = 0;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: Producer
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:35
     * @Description: 业务模块 OpenAPI 生产配置
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class Producer {

        private boolean enabled = true;

        private String title = "OpenAPI Service";

        private String description = "OpenAPI service generated by openapi-debug-console starter";

        private String version = "v1";

        private String contactName = "";

        private String contactUrl = "";

        private String contactEmail = "";

        private String licenseName = "";

        private String licenseUrl = "";

        private String authorizationHeader = "Authorization";
    }
}
