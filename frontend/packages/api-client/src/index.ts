/**
 * @BelongsProject: FamilyAiButler
 * @BelongsPackage: frontend.packages.api-client
 * @ClassName: index
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day
 * @Description: 后端接口客户端
 * @Version: 1.0
 */
export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE";

export interface ServiceEndpoint {
    name: string;
    basePath: string;
    description: string;
}

export interface ApiContract {
    key: string;
    group: "category" | "password";
    method: HttpMethod;
    path: string;
    title: string;
    description: string;
}

export interface ApiResult<T> {
    code?: number;
    msg?: string;
    message?: string;
    data?: T;
    success?: boolean;
}

export interface PageApiResult<T> extends ApiResult<T> {
    total?: number;
    pageNum?: number;
    pageSize?: number;
    current?: number;
    size?: number;
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
    updateTime?: string;
}

export interface StrengthInfo {
    score?: number;
    warning?: string;
    suggestions?: string[];
    crackTimeDisplay?: string;
}

export interface RequestPreview {
    method: HttpMethod;
    url: string;
    body?: unknown;
}

export interface ApiClientOptions {
    gatewayBaseUrl: string;
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
        key: "category-list",
        group: "category",
        method: "GET",
        path: "/base/category/list",
        title: "分类列表",
        description: "获取前 10 条分类数据"
    },
    {
        key: "category-type-list",
        group: "category",
        method: "GET",
        path: "/base/category/type/list",
        title: "分类类型列表",
        description: "获取前 10 条分类类型数据"
    },
    {
        key: "password-list",
        group: "password",
        method: "GET",
        path: "/base/password/password/list/{pageNum}/{pageSize}",
        title: "账号密码列表",
        description: "按页获取账号密码数据"
    },
    {
        key: "password-add",
        group: "password",
        method: "POST",
        path: "/base/password/password/add",
        title: "新增账号密码",
        description: "提交一个账号密码记录"
    },
    {
        key: "password-update",
        group: "password",
        method: "PUT",
        path: "/base/password",
        title: "编辑账号密码",
        description: "更新一个账号密码记录"
    },
    {
        key: "password-delete",
        group: "password",
        method: "DELETE",
        path: "/base/password",
        title: "删除账号密码",
        description: "删除一个或多个账号密码记录"
    },
    {
        key: "password-generate",
        group: "password",
        method: "GET",
        path: "/base/password/generate/{passwordLength}",
        title: "随机密码生成",
        description: "根据长度生成随机密码"
    },
    {
        key: "password-strength",
        group: "password",
        method: "GET",
        path: "/base/password/checkStrength/{password}",
        title: "密码强度检查",
        description: "返回密码强度对象"
    },
    {
        key: "password-valid",
        group: "password",
        method: "GET",
        path: "/base/password/checkValid/{password}",
        title: "密码合法性检查",
        description: "返回密码是否合法"
    },
    {
        key: "category-add",
        group: "category",
        method: "POST",
        path: "/base/category/category",
        title: "新增分类",
        description: "新增一个家庭分类"
    },
    {
        key: "category-update",
        group: "category",
        method: "PUT",
        path: "/base/category/category",
        title: "编辑分类",
        description: "更新一个家庭分类"
    },
    {
        key: "category-delete",
        group: "category",
        method: "DELETE",
        path: "/base/category/category/{id}",
        title: "删除分类",
        description: "删除一个家庭分类"
    }
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
 * 生成请求预览。
 *
 * @param gatewayBaseUrl 网关根地址
 * @param contract 接口契约
 * @param pathParams 路径参数
 * @param body 请求体
 * @returns 页面可展示的请求预览
 */
export function buildRequestPreview(
    gatewayBaseUrl: string,
    contract: ApiContract,
    pathParams: Record<string, string | number> = {},
    body?: unknown
): RequestPreview {
    const path = Object.entries(pathParams).reduce(
        (currentPath, [key, value]) => currentPath.replace(`{${key}}`, encodeURIComponent(String(value))),
        contract.path
    );
    return {
        method: contract.method,
        url: buildGatewayUrl(gatewayBaseUrl, path),
        body
    };
}

/**
 * 请求 JSON 接口。
 *
 * @param preview 请求预览
 * @returns 后端 JSON 响应
 */
export async function requestJson<T>(preview: RequestPreview): Promise<T> {
    const response = await fetch(preview.url, {
        method: preview.method,
        headers: {
            "Content-Type": "application/json"
        },
        body: preview.body ? JSON.stringify(preview.body) : undefined
    });

    if (!response.ok) {
        throw new Error(`Request failed: ${response.status} ${response.statusText}`);
    }

    return response.json() as Promise<T>;
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
    const contract = familyCoreContracts.find((item) => item.key === "password-list") ?? familyCoreContracts[2];
    return buildRequestPreview(gatewayBaseUrl, contract, {pageNum, pageSize});
}

/**
 * 获取随机密码请求预览。
 *
 * @param gatewayBaseUrl 网关根地址
 * @param passwordLength 密码长度
 * @returns 请求预览
 */
export function previewGeneratePassword(gatewayBaseUrl: string, passwordLength = 16) {
    const contract = familyCoreContracts.find((item) => item.key === "password-generate") ?? familyCoreContracts[4];
    return buildRequestPreview(gatewayBaseUrl, contract, {passwordLength});
}
