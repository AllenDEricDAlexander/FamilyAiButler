/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @FileName: ApiDocConsolePayloads.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:20
 * @Description: OpenAPI 调试文档控制台请求响应对象
 * @Version: 1.0
 */
package top.egon.openapi.console;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: openapi-console
 * @BelongsPackage: top.egon.openapi.console
 * @ClassName: ApiDocConsolePayloads
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:20
 * @Description: OpenAPI 调试文档控制台传输对象集合
 * @Version: 1.0
 */
public final class ApiDocConsolePayloads {

    /**
     * 构造方法
     */
    private ApiDocConsolePayloads() {
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: LoginRequest
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 登录请求
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class LoginRequest {

        private String username;

        private String password;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: LoginResponse
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 登录响应
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class LoginResponse {

        private String username;

        private String mode;

        private boolean readOnly;

        private long expiresAt;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: MeResponse
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 当前登录用户响应
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class MeResponse {

        private boolean authenticated;

        private String username;

        private String mode;

        private boolean readOnly;

        private Map<String, Boolean> capabilities = new LinkedHashMap<>();
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: CatalogResponse
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 服务目录响应
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class CatalogResponse {

        private String title;

        private String environment;

        private String mode;

        private boolean readOnly;

        private Map<String, Boolean> capabilities = new LinkedHashMap<>();

        private List<ServiceItem> services = new ArrayList<>();
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: ServiceItem
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 服务目录条目
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class ServiceItem {

        private String id;

        private String name;

        private String group;

        private String baseUrl;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: ExecuteRequest
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 接口调试请求
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class ExecuteRequest {

        private String serviceId;

        private String method;

        private String path;

        private Map<String, String> headers = new LinkedHashMap<>();

        private Map<String, String> query = new LinkedHashMap<>();

        private String contentType = "application/json";

        private String body;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: ExecuteResponse
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 接口调试响应
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class ExecuteResponse {

        private int status;

        private long durationMillis;

        private Map<String, String> headers = new LinkedHashMap<>();

        private String body;

        private String curl;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: LoadTestRequest
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 轻量压测请求
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class LoadTestRequest {

        private ExecuteRequest request = new ExecuteRequest();

        private int totalRequests = 10;

        private int concurrency = 2;
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: LoadTestResult
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 轻量压测结果
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class LoadTestResult {

        private int total;

        private int success;

        private int failed;

        private long minMillis;

        private long maxMillis;

        private double avgMillis;

        private long p95Millis;

        private Map<Integer, Integer> statusCounts = new LinkedHashMap<>();

        private List<String> errors = new ArrayList<>();

        private List<LoadTestSample> samples = new ArrayList<>();
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: LoadTestSample
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-19:20
     * @Description: 轻量压测单次请求样本
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class LoadTestSample {

        private int index;

        private int status;

        private long durationMillis;

        private String error;

        /**
         * 判断样本是否成功
         *
         * @return boolean 返回 true 表示成功
         */
        public boolean success() {
            return status >= 200 && status < 400 && (error == null || error.isBlank());
        }
    }

    /**
     * @BelongsProject: openapi-console
     * @BelongsPackage: top.egon.openapi.console
     * @ClassName: UserSession
     * @Author: atluofu
     * @CreateTime: 2026Year-05Month-19Day-17:20
     * @Description: 本地登录会话
     * @Version: 1.0
     */
    @Getter
    @Setter
    public static class UserSession {

        private String username;

        private long expiresAt;
    }
}
