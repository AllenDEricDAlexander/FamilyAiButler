/**
 * @BelongsProject: FamilyAiButler
 * @BelongsPackage: frontend.packages.api-client
 * @ClassName: index
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day
 * @Description: 后端接口客户端
 * @Version: 1.0
 */
export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH";

export type ServiceName = "family-core" | "family-uaa" | "family-ai-qwen";

export type ApiGroup =
    | "password"
    | "category"
    | "categoryType"
    | "auth"
    | "account"
    | "profile"
    | "session"
    | "device"
    | "jwk"
    | "authorization"
    | "rbac"
    | "oauthClient"
    | "aiImage";

export type QueryValue = string | number | boolean | null | undefined;

export interface ServiceEndpoint {
    name: ServiceName;
    basePath: string;
    description: string;
}

export interface ApiContract {
    key: string;
    service: ServiceName;
    group: ApiGroup;
    method: HttpMethod;
    path: string;
    title: string;
    description: string;
    authRequired: boolean;
    requestSchema?: string;
    responseSchema?: string;
}

export interface ApiResult<T> {
    code?: number;
    msg?: string;
    message?: string;
    data?: T;
    success?: boolean;
    traceId?: string;
    timestamp?: string;
}

export interface PageApiResult<T> extends ApiResult<T> {
    total?: number;
    pageNum?: number;
    pageSize?: number;
    current?: number;
    size?: number;
    records?: T[];
    list?: T[];
}

export interface CategoryType {
    id?: number;
    typeName?: string;
    description?: string;
    createTime?: string;
    updateTime?: string;
}

export interface Category {
    id?: number;
    name?: string;
    categoryName?: string;
    description?: string;
    parentId?: number;
    categoryType?: CategoryType | null;
    createTime?: string;
    updateTime?: string;
}

export interface PasswordView {
    id?: number;
    businessId?: string;
    name?: string;
    password?: string;
    description?: string;
    accountNumber?: string;
    websit?: string;
    likeStatus?: boolean;
    category?: string;
    lastViewTime?: string;
    createTime?: string;
    updateTime?: string;
}

export interface StrengthInfo {
    crackTimeSeconds?: Record<string, number>;
    crackTimesDisplay?: Record<string, string>;
    score?: number;
    feedback?: {
        warning?: string;
        suggestions?: string[];
    };
    warning?: string;
    suggestions?: string[];
    crackTimeDisplay?: string;
}

export interface PasswordLoginRequest {
    principal: string;
    password: string;
    clientId?: string;
    clientSecret?: string;
    deviceName?: string;
    deviceFingerprint?: string;
}

export interface VerifyCodeLoginRequest {
    principal: string;
    verifyCode: string;
    clientId?: string;
    deviceName?: string;
    deviceFingerprint?: string;
}

export interface RegisterAccountRequest {
    username?: string;
    email?: string;
    phone?: string;
    password: string;
}

export interface PasswordRecoveryRequest {
    principal: string;
    channel?: string;
}

export interface ResetPasswordRequest {
    principal: string;
    verificationCode: string;
    newPassword: string;
}

export interface LogoutRequest {
    accessToken?: string;
    refreshToken?: string;
    sessionId?: string;
    deviceId?: string;
}

export interface TokenPairResponse {
    accessToken?: string;
    refreshToken?: string;
    tokenType?: string;
    expiresIn?: number;
    refreshExpiresIn?: number;
    scope?: string;
}

export interface AccountResponse {
    accountId?: string;
    username?: string;
    email?: string;
    phone?: string;
    status?: string;
    accountType?: string;
    authVersion?: number;
    entitlementVersion?: number;
    sessionVersion?: number;
    riskVersion?: number;
}

export interface ProfileRequest {
    profileId?: string;
    accountId?: string;
    nickname?: string;
    avatar?: string;
    language?: string;
    region?: string;
    profileType?: string;
}

export interface ProfileResponse extends ProfileRequest {
    createTime?: string;
    updateTime?: string;
}

export interface AuthorizationDecisionRequest {
    accessToken?: string;
    resourceService?: string;
    resourcePath?: string;
    action?: string;
}

export interface AuthorizationDecisionResponse {
    allowed?: boolean;
    reason?: string;
    accountId?: string;
    resourceService?: string;
    resourcePath?: string;
    action?: string;
}

export interface UpsertRoleRequest {
    roleId?: string;
    roleCode?: string;
    roleName?: string;
    description?: string;
    status?: string;
}

export interface RoleResponse extends UpsertRoleRequest {
    createTime?: string;
    updateTime?: string;
}

export interface UpsertPermissionResourceRequest {
    resourceId?: string;
    resourceCode?: string;
    resourceName?: string;
    resourceService?: string;
    resourcePath?: string;
    resourceType?: string;
    action?: string;
    description?: string;
    status?: string;
}

export interface PermissionResourceResponse extends UpsertPermissionResourceRequest {
    createTime?: string;
    updateTime?: string;
}

export interface BindRoleResourceRequest {
    roleId?: string;
    roleCode?: string;
    resourceId?: string;
    resourceCode?: string;
}

export interface BindAccountRoleRequest {
    accountId?: string;
    roleId?: string;
    roleCode?: string;
}

export interface UserPermissionResponse {
    accountId?: string;
    roles?: RoleResponse[];
    resources?: PermissionResourceResponse[];
    permissions?: PermissionResourceResponse[];
}

export interface CreateOAuthClientRequest {
    clientId?: string;
    clientSecret?: string;
    clientName?: string;
    redirectUris?: string[];
    scopes?: string[];
    grantTypes?: string[];
    status?: string;
}

export interface OAuthClientResponse extends CreateOAuthClientRequest {
    createTime?: string;
    updateTime?: string;
}

export interface ImageToMessageQuery {
    [key: string]: QueryValue | QueryValue[];

    file: string | string[];
}

export interface RequestPreview {
    method: HttpMethod;
    url: string;
    path: string;
    headers?: Record<string, string>;
    body?: unknown;
    contract?: ApiContract;
}

export interface ApiClientOptions {
    gatewayBaseUrl: string;
    accessToken?: string;
    headers?: Record<string, string>;
}

export const serviceEndpoints: ServiceEndpoint[] = [
    {
        name: "family-core",
        basePath: "/base",
        description: "家庭核心业务服务"
    },
    {
        name: "family-uaa",
        basePath: "/uaa",
        description: "认证授权服务"
    },
    {
        name: "family-ai-qwen",
        basePath: "/ai",
        description: "通义千问 AI 服务"
    }
];

export const familyCoreContracts: ApiContract[] = [
    {
        key: "password-update",
        service: "family-core",
        group: "password",
        method: "PUT",
        path: "/base/password",
        title: "编辑账号密码",
        description: "更新一个账号密码记录",
        authRequired: true,
        requestSchema: "PasswordViewDTO",
        responseSchema: "ResultBoolean"
    },
    {
        key: "password-delete",
        service: "family-core",
        group: "password",
        method: "DELETE",
        path: "/base/password",
        title: "删除账号密码",
        description: "删除一个或多个账号密码记录",
        authRequired: true,
        requestSchema: "array<integer>",
        responseSchema: "ResultBoolean"
    },
    {
        key: "password-add",
        service: "family-core",
        group: "password",
        method: "POST",
        path: "/base/password/password/add",
        title: "新增账号密码",
        description: "提交一个账号密码记录",
        authRequired: true,
        requestSchema: "PasswordViewDTO",
        responseSchema: "Result"
    },
    {
        key: "password-detail",
        service: "family-core",
        group: "password",
        method: "GET",
        path: "/base/password/{id}",
        title: "账号密码详情",
        description: "通过主键查询单条账号密码数据",
        authRequired: true,
        responseSchema: "ResultPasswordViewDTO"
    },
    {
        key: "password-list-all",
        service: "family-core",
        group: "password",
        method: "GET",
        path: "/base/password/password/list",
        title: "账号密码列表",
        description: "获取账号密码列表",
        authRequired: true,
        requestSchema: "PasswordViewDTO",
        responseSchema: "Result"
    },
    {
        key: "password-list",
        service: "family-core",
        group: "password",
        method: "GET",
        path: "/base/password/password/list/{pageNum}/{pageSize}",
        title: "账号密码分页列表",
        description: "按页获取账号密码数据",
        authRequired: true,
        requestSchema: "PasswordViewDTO",
        responseSchema: "Result"
    },
    {
        key: "password-generate",
        service: "family-core",
        group: "password",
        method: "GET",
        path: "/base/password/generate/{passwordLength}",
        title: "随机密码生成",
        description: "根据长度生成随机密码",
        authRequired: true,
        responseSchema: "Result"
    },
    {
        key: "password-generate-special",
        service: "family-core",
        group: "password",
        method: "GET",
        path: "/base/password/generate/{passwordLength}/{needSpecialCharacters}",
        title: "随机密码生成",
        description: "根据长度和特殊字符开关生成随机密码",
        authRequired: true,
        responseSchema: "Result"
    },
    {
        key: "password-generate-custom-special",
        service: "family-core",
        group: "password",
        method: "GET",
        path: "/base/password/generate/{passwordLength}/{needSpecialCharacters}/{specialCharacters}",
        title: "随机密码生成",
        description: "根据长度、特殊字符开关和自定义特殊字符生成随机密码",
        authRequired: true,
        responseSchema: "Result"
    },
    {
        key: "password-valid",
        service: "family-core",
        group: "password",
        method: "GET",
        path: "/base/password/checkValid/{password}",
        title: "密码合法性检查",
        description: "返回密码是否合法",
        authRequired: true,
        responseSchema: "boolean"
    },
    {
        key: "password-strength",
        service: "family-core",
        group: "password",
        method: "GET",
        path: "/base/password/checkStrength/{password}",
        title: "密码强度检查",
        description: "返回密码强度对象",
        authRequired: true,
        responseSchema: "ResultStrengthDTO"
    },
    {
        key: "password-business-detail",
        service: "family-core",
        group: "password",
        method: "GET",
        path: "/base/password/business/{businessId}",
        title: "账号密码业务详情",
        description: "通过业务主键查询单条账号密码数据",
        authRequired: true,
        responseSchema: "ResultPasswordViewDTO"
    },
    {
        key: "category-type-list",
        service: "family-core",
        group: "categoryType",
        method: "GET",
        path: "/base/category/type/list",
        title: "分类类型列表",
        description: "获取所有分类类型",
        authRequired: true,
        responseSchema: "PageResultCategoryTypeDTO"
    },
    {
        key: "category-list",
        service: "family-core",
        group: "category",
        method: "GET",
        path: "/base/category/list",
        title: "分类列表",
        description: "获取所有分类",
        authRequired: true,
        responseSchema: "PageResultCategoryDTO"
    },
    {
        key: "category-add",
        service: "family-core",
        group: "category",
        method: "POST",
        path: "/base/category/category",
        title: "新增分类",
        description: "新增一个家庭分类",
        authRequired: true,
        requestSchema: "CategoryDTO",
        responseSchema: "ResultCategoryDTO"
    },
    {
        key: "category-update",
        service: "family-core",
        group: "category",
        method: "PUT",
        path: "/base/category/category",
        title: "编辑分类",
        description: "更新一个家庭分类",
        authRequired: true,
        requestSchema: "CategoryDTO",
        responseSchema: "ResultCategoryDTO"
    },
    {
        key: "category-detail",
        service: "family-core",
        group: "category",
        method: "GET",
        path: "/base/category/category/{id}",
        title: "分类详情",
        description: "获取指定分类",
        authRequired: true,
        responseSchema: "ResultCategoryDTO"
    },
    {
        key: "category-delete",
        service: "family-core",
        group: "category",
        method: "DELETE",
        path: "/base/category/category/{id}",
        title: "删除分类",
        description: "删除一个家庭分类",
        authRequired: true,
        responseSchema: "ResultBoolean"
    },
    {
        key: "category-type-add",
        service: "family-core",
        group: "categoryType",
        method: "POST",
        path: "/base/category/category/type",
        title: "新增分类类型",
        description: "新增一个家庭分类类型",
        authRequired: true,
        requestSchema: "CategoryTypeDTO",
        responseSchema: "ResultCategoryTypeDTO"
    },
    {
        key: "category-type-update",
        service: "family-core",
        group: "categoryType",
        method: "PUT",
        path: "/base/category/category/type",
        title: "编辑分类类型",
        description: "更新一个家庭分类类型",
        authRequired: true,
        requestSchema: "CategoryTypeDTO",
        responseSchema: "ResultCategoryTypeDTO"
    },
    {
        key: "category-type-detail",
        service: "family-core",
        group: "categoryType",
        method: "GET",
        path: "/base/category/category/type/{id}",
        title: "分类类型详情",
        description: "获取指定分类类型",
        authRequired: true,
        responseSchema: "ResultCategoryTypeDTO"
    },
    {
        key: "category-type-delete",
        service: "family-core",
        group: "categoryType",
        method: "DELETE",
        path: "/base/category/category/type/{id}",
        title: "删除分类类型",
        description: "删除一个家庭分类类型",
        authRequired: true,
        responseSchema: "ResultBoolean"
    }
];

export const familyUaaContracts: ApiContract[] = [
    {
        key: "auth-login-password",
        service: "family-uaa",
        group: "auth",
        method: "POST",
        path: "/uaa/auth/login/password",
        title: "密码登录",
        description: "使用账号密码登录并获取令牌",
        authRequired: false,
        requestSchema: "PasswordLoginRequest",
        responseSchema: "ResultTokenPairResponse"
    },
    {
        key: "auth-login-email-code",
        service: "family-uaa",
        group: "auth",
        method: "POST",
        path: "/uaa/auth/login/email-code",
        title: "邮箱验证码登录",
        description: "使用邮箱验证码登录并获取令牌",
        authRequired: false,
        requestSchema: "VerifyCodeLoginRequest",
        responseSchema: "ResultTokenPairResponse"
    },
    {
        key: "auth-login-sms-code",
        service: "family-uaa",
        group: "auth",
        method: "POST",
        path: "/uaa/auth/login/sms-code",
        title: "短信验证码登录",
        description: "使用短信验证码登录并获取令牌",
        authRequired: false,
        requestSchema: "VerifyCodeLoginRequest",
        responseSchema: "ResultTokenPairResponse"
    },
    {
        key: "account-register",
        service: "family-uaa",
        group: "account",
        method: "POST",
        path: "/uaa/account/register",
        title: "账号注册",
        description: "注册一个家庭账号",
        authRequired: false,
        requestSchema: "RegisterAccountRequest",
        responseSchema: "ResultAccountResponse"
    },
    {
        key: "auth-password-recovery",
        service: "family-uaa",
        group: "auth",
        method: "POST",
        path: "/uaa/auth/password/recovery",
        title: "密码恢复",
        description: "发起密码恢复流程",
        authRequired: false,
        requestSchema: "PasswordRecoveryRequest",
        responseSchema: "ResultString"
    },
    {
        key: "auth-password-reset",
        service: "family-uaa",
        group: "auth",
        method: "POST",
        path: "/uaa/auth/password/reset",
        title: "密码重置",
        description: "使用验证码重置密码",
        authRequired: false,
        requestSchema: "ResetPasswordRequest",
        responseSchema: "ResultBoolean"
    },
    {
        key: "auth-logout-current",
        service: "family-uaa",
        group: "auth",
        method: "POST",
        path: "/uaa/auth/logout/current",
        title: "当前会话退出",
        description: "退出当前登录会话",
        authRequired: true,
        requestSchema: "LogoutRequest",
        responseSchema: "ResultBoolean"
    },
    {
        key: "auth-logout-all",
        service: "family-uaa",
        group: "auth",
        method: "POST",
        path: "/uaa/auth/logout/all",
        title: "全部会话退出",
        description: "退出当前账号的全部会话",
        authRequired: true,
        requestSchema: "LogoutRequest",
        responseSchema: "ResultBoolean"
    },
    {
        key: "profile-add",
        service: "family-uaa",
        group: "profile",
        method: "POST",
        path: "/uaa/profile",
        title: "新增 Profile",
        description: "创建账号 Profile",
        authRequired: true,
        requestSchema: "ProfileRequest",
        responseSchema: "ResultProfileResponse"
    },
    {
        key: "profile-update",
        service: "family-uaa",
        group: "profile",
        method: "PUT",
        path: "/uaa/profile",
        title: "编辑 Profile",
        description: "更新账号 Profile",
        authRequired: true,
        requestSchema: "ProfileRequest",
        responseSchema: "ResultProfileResponse"
    },
    {
        key: "profile-list-by-account",
        service: "family-uaa",
        group: "profile",
        method: "GET",
        path: "/uaa/profile/account/{accountId}",
        title: "账号 Profile 列表",
        description: "按账号 ID 查询 Profile 列表",
        authRequired: true,
        responseSchema: "ResultListProfileResponse"
    },
    {
        key: "profile-delete",
        service: "family-uaa",
        group: "profile",
        method: "DELETE",
        path: "/uaa/profile/{profileId}",
        title: "删除 Profile",
        description: "删除指定 Profile",
        authRequired: true,
        responseSchema: "ResultBoolean"
    },
    {
        key: "session-delete",
        service: "family-uaa",
        group: "session",
        method: "DELETE",
        path: "/uaa/session/{sessionId}",
        title: "删除会话",
        description: "删除指定会话",
        authRequired: true,
        responseSchema: "ResultBoolean"
    },
    {
        key: "device-delete",
        service: "family-uaa",
        group: "device",
        method: "DELETE",
        path: "/uaa/device/{deviceId}",
        title: "删除设备",
        description: "删除指定设备",
        authRequired: true,
        responseSchema: "ResultBoolean"
    },
    {
        key: "jwk-list",
        service: "family-uaa",
        group: "jwk",
        method: "GET",
        path: "/uaa/.well-known/jwks.json",
        title: "JWK",
        description: "获取 JWK 公钥集合",
        authRequired: true,
        responseSchema: "ResultMapStringObject"
    },
    {
        key: "authorization-decide",
        service: "family-uaa",
        group: "authorization",
        method: "POST",
        path: "/uaa/authorization/decide",
        title: "授权决策",
        description: "判断令牌是否允许访问指定资源",
        authRequired: true,
        requestSchema: "AuthorizationDecisionRequest",
        responseSchema: "ResultAuthorizationDecisionResponse"
    },
    {
        key: "rbac-role-upsert",
        service: "family-uaa",
        group: "rbac",
        method: "POST",
        path: "/uaa/rbac/roles",
        title: "新增或更新角色",
        description: "新增或更新 RBAC 角色",
        authRequired: true,
        requestSchema: "UpsertRoleRequest",
        responseSchema: "ResultRoleResponse"
    },
    {
        key: "rbac-resource-upsert",
        service: "family-uaa",
        group: "rbac",
        method: "POST",
        path: "/uaa/rbac/resources",
        title: "新增或更新权限资源",
        description: "新增或更新 RBAC 权限资源",
        authRequired: true,
        requestSchema: "UpsertPermissionResourceRequest",
        responseSchema: "ResultPermissionResourceResponse"
    },
    {
        key: "rbac-bind-role-resource",
        service: "family-uaa",
        group: "rbac",
        method: "POST",
        path: "/uaa/rbac/role-resources",
        title: "绑定角色资源",
        description: "绑定角色和权限资源",
        authRequired: true,
        requestSchema: "BindRoleResourceRequest",
        responseSchema: "ResultBoolean"
    },
    {
        key: "rbac-bind-account-role",
        service: "family-uaa",
        group: "rbac",
        method: "POST",
        path: "/uaa/rbac/account-roles",
        title: "绑定账号角色",
        description: "绑定账号和角色",
        authRequired: true,
        requestSchema: "BindAccountRoleRequest",
        responseSchema: "ResultBoolean"
    },
    {
        key: "rbac-account-permissions",
        service: "family-uaa",
        group: "rbac",
        method: "GET",
        path: "/uaa/rbac/accounts/{accountId}/permissions",
        title: "账号权限查询",
        description: "按账号 ID 查询权限，可选 resourceType 查询参数",
        authRequired: true,
        responseSchema: "ResultUserPermissionResponse"
    },
    {
        key: "oauth-client-list",
        service: "family-uaa",
        group: "oauthClient",
        method: "GET",
        path: "/uaa/oauth-clients",
        title: "OAuth Client 列表",
        description: "查询 OAuth Client 列表",
        authRequired: true,
        responseSchema: "ResultListOAuthClientResponse"
    },
    {
        key: "oauth-client-add",
        service: "family-uaa",
        group: "oauthClient",
        method: "POST",
        path: "/uaa/oauth-clients",
        title: "新增 OAuth Client",
        description: "新增 OAuth Client",
        authRequired: true,
        requestSchema: "CreateOAuthClientRequest",
        responseSchema: "ResultOAuthClientResponse"
    },
    {
        key: "oauth-client-detail",
        service: "family-uaa",
        group: "oauthClient",
        method: "GET",
        path: "/uaa/oauth-clients/{clientId}",
        title: "OAuth Client 详情",
        description: "查询 OAuth Client 详情",
        authRequired: true,
        responseSchema: "ResultOAuthClientResponse"
    }
];

export const familyAiQwenContracts: ApiContract[] = [
    {
        key: "qwen-image-message-get",
        service: "family-ai-qwen",
        group: "aiImage",
        method: "GET",
        path: "/ai/ai/v1/image2Message",
        title: "图片生成文本描述",
        description: "图片生成文本描述，query file 为图片输入",
        authRequired: true,
        responseSchema: "string"
    },
    {
        key: "qwen-image-message-post",
        service: "family-ai-qwen",
        group: "aiImage",
        method: "POST",
        path: "/ai/ai/v1/image2Message",
        title: "图片生成文本描述",
        description: "图片生成文本描述，query file 为图片输入",
        authRequired: true,
        responseSchema: "string"
    },
    {
        key: "qwen-image-message-put",
        service: "family-ai-qwen",
        group: "aiImage",
        method: "PUT",
        path: "/ai/ai/v1/image2Message",
        title: "图片生成文本描述",
        description: "图片生成文本描述，query file 为图片输入",
        authRequired: true,
        responseSchema: "string"
    },
    {
        key: "qwen-image-message-delete",
        service: "family-ai-qwen",
        group: "aiImage",
        method: "DELETE",
        path: "/ai/ai/v1/image2Message",
        title: "图片生成文本描述",
        description: "图片生成文本描述，query file 为图片输入",
        authRequired: true,
        responseSchema: "string"
    },
    {
        key: "qwen-image-message-patch",
        service: "family-ai-qwen",
        group: "aiImage",
        method: "PATCH",
        path: "/ai/ai/v1/image2Message",
        title: "图片生成文本描述",
        description: "图片生成文本描述，query file 为图片输入",
        authRequired: true,
        responseSchema: "string"
    }
];

export const allApiContracts: ApiContract[] = [
    ...familyCoreContracts,
    ...familyUaaContracts,
    ...familyAiQwenContracts
];

/**
 * 拼接网关接口地址。
 *
 * @param gatewayBaseUrl 网关根地址
 * @param path 后端接口路径
 * @returns 可请求的完整 URL
 */
export function buildGatewayUrl(gatewayBaseUrl: string, path: string) {
    const baseUrl = gatewayBaseUrl.replace(/\/+$/, "");
    const normalizedPath = path.startsWith("/") ? path : `/${path}`;
    return `${baseUrl}${normalizedPath}`;
}

/**
 * 根据 key 查找接口契约。
 *
 * @param key 接口契约 key
 * @param contracts 接口契约集合
 * @returns 接口契约
 */
export function findApiContract(key: string, contracts: ApiContract[] = allApiContracts): ApiContract {
    const contract = contracts.find((item) => item.key === key);
    if (contract === undefined) {
        throw new Error(`Unknown api contract: ${key}`);
    }
    return contract;
}

/**
 * 拼接 Authorization 请求头。
 *
 * @param accessToken 访问令牌
 * @returns 请求头
 */
export function buildAuthorizationHeaders(accessToken?: string): Record<string, string> {
    return accessToken ? {Authorization: `Bearer ${accessToken}`} : {};
}

/**
 * 生成请求预览。
 *
 * @param gatewayBaseUrl 网关根地址
 * @param contract 接口契约
 * @param pathParams 路径参数
 * @param body 请求体
 * @param queryParams 查询参数
 * @param headers 请求头
 * @returns 页面可展示的请求预览
 */
export function buildRequestPreview(
    gatewayBaseUrl: string,
    contract: ApiContract,
    pathParams: Record<string, string | number | boolean> = {},
    body?: unknown,
    queryParams: Record<string, QueryValue | QueryValue[]> = {},
    headers: Record<string, string> = {}
): RequestPreview {
    const path = Object.entries(pathParams).reduce(
        (currentPath, [key, value]) => currentPath.replace(`{${key}}`, encodeURIComponent(String(value))),
        contract.path
    );
    const url = appendQueryParams(buildGatewayUrl(gatewayBaseUrl, path), queryParams);
    return {
        method: contract.method,
        url,
        path,
        headers,
        body,
        contract
    };
}

/**
 * 请求 JSON 接口。
 *
 * @param preview 请求预览
 * @returns 后端 JSON 响应
 */
export async function requestJson<T>(preview: RequestPreview): Promise<T> {
    const headers = new Headers(preview.headers);
    const hasBody = preview.body !== undefined && preview.body !== null;
    const isFormData = typeof FormData !== "undefined" && preview.body instanceof FormData;

    if (hasBody && !isFormData && !headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
    }

    const response = await fetch(preview.url, {
        method: preview.method,
        headers,
        body: hasBody ? (isFormData ? preview.body as BodyInit : JSON.stringify(preview.body)) : undefined
    });

    if (!response.ok) {
        throw new Error(`Request failed: ${response.status} ${response.statusText}`);
    }

    return response.json() as Promise<T>;
}

/**
 * 创建带网关配置的接口客户端。
 *
 * @param options 客户端配置
 * @returns 接口客户端
 */
export function createApiClient(options: ApiClientOptions) {
    const defaultHeaders = {
        ...options.headers,
        ...buildAuthorizationHeaders(options.accessToken)
    };
    return {
        /**
         * 生成接口请求预览。
         *
         * @param contract 接口契约
         * @param pathParams 路径参数
         * @param body 请求体
         * @param queryParams 查询参数
         * @returns 请求预览
         */
        preview(
            contract: ApiContract,
            pathParams: Record<string, string | number | boolean> = {},
            body?: unknown,
            queryParams: Record<string, QueryValue | QueryValue[]> = {}
        ) {
            return buildRequestPreview(options.gatewayBaseUrl, contract, pathParams, body, queryParams, defaultHeaders);
        },
        /**
         * 请求指定接口契约。
         *
         * @param contract 接口契约
         * @param pathParams 路径参数
         * @param body 请求体
         * @param queryParams 查询参数
         * @returns 后端 JSON 响应
         */
        request<T>(
            contract: ApiContract,
            pathParams: Record<string, string | number | boolean> = {},
            body?: unknown,
            queryParams: Record<string, QueryValue | QueryValue[]> = {}
        ) {
            return requestJson<T>(buildRequestPreview(options.gatewayBaseUrl, contract, pathParams, body, queryParams, defaultHeaders));
        }
    };
}

/**
 * 获取账号密码列表请求预览。
 *
 * @param gatewayBaseUrl 网关根地址
 * @param pageNum 页码
 * @param pageSize 页大小
 * @returns 请求预览
 */
export function previewPasswordList(gatewayBaseUrl: string, pageNum = 1, pageSize = 10) {
    return buildRequestPreview(gatewayBaseUrl, findApiContract("password-list", familyCoreContracts), {
        pageNum,
        pageSize
    });
}

/**
 * 获取随机密码请求预览。
 *
 * @param gatewayBaseUrl 网关根地址
 * @param passwordLength 密码长度
 * @returns 请求预览
 */
export function previewGeneratePassword(gatewayBaseUrl: string, passwordLength = 16) {
    return buildRequestPreview(gatewayBaseUrl, findApiContract("password-generate", familyCoreContracts), {passwordLength});
}

/**
 * 获取密码登录请求预览。
 *
 * @param gatewayBaseUrl 网关根地址
 * @param body 密码登录请求
 * @returns 请求预览
 */
export function previewPasswordLogin(gatewayBaseUrl: string, body: PasswordLoginRequest) {
    return buildRequestPreview(gatewayBaseUrl, findApiContract("auth-login-password", familyUaaContracts), {}, body);
}

/**
 * 获取图片理解请求预览。
 *
 * @param gatewayBaseUrl 网关根地址
 * @param query 图片理解查询参数
 * @param method 请求方法
 * @returns 请求预览
 */
export function previewQwenImageToMessage(
    gatewayBaseUrl: string,
    query: ImageToMessageQuery,
    method: HttpMethod = "POST"
) {
    const contractKey = `qwen-image-message-${method.toLowerCase()}`;
    return buildRequestPreview(gatewayBaseUrl, findApiContract(contractKey, familyAiQwenContracts), {}, undefined, query);
}

/**
 * 追加查询参数。
 *
 * @param url 原始 URL
 * @param queryParams 查询参数
 * @returns 带查询参数的 URL
 */
function appendQueryParams(url: string, queryParams: Record<string, QueryValue | QueryValue[]>) {
    const searchParams = new URLSearchParams();
    for (const [key, value] of Object.entries(queryParams)) {
        if (Array.isArray(value)) {
            for (const item of value) {
                if (item !== undefined && item !== null) {
                    searchParams.append(key, String(item));
                }
            }
        } else if (value !== undefined && value !== null) {
            searchParams.append(key, String(value));
        }
    }
    const queryString = searchParams.toString();
    if (queryString.length === 0) {
        return url;
    }
    return `${url}${url.includes("?") ? "&" : "?"}${queryString}`;
}
