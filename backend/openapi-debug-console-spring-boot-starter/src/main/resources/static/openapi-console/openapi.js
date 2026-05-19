/*
 * @BelongsProject: openapi-console
 * @BelongsPackage: static.openapi-console
 * @FileName: openapi.js
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-19:15
 * @Description: OpenAPI 调试文档控制台规范解析脚本
 * @Version: 1.0
 */
(function (root, factory) {
    const api = factory();
    if (typeof module === "object" && module.exports) {
        module.exports = api;
    }
    root.OpenApiConsoleSpec = api;
}(typeof window !== "undefined" ? window : globalThis, function () {
    const HTTP_METHODS = ["get", "post", "put", "patch", "delete", "options", "head"];
    const MAX_SAMPLE_DEPTH = 6;

    /**
     * 汇总 OpenAPI 操作
     *
     * @param {object} spec OpenAPI JSON
     * @returns {Array<object>} 返回接口集合
     */
    function collectOperations(spec) {
        const result = [];
        Object.entries(spec?.paths || {}).forEach(([path, pathItem]) => {
            Object.entries(pathItem || {}).forEach(([method, operation]) => {
                if (!HTTP_METHODS.includes(method)) {
                    return;
                }
                result.push({
                    key: operationKey("", method.toUpperCase(), path),
                    method: method.toUpperCase(),
                    path,
                    summary: operation.summary || operation.operationId || "",
                    tags: operation.tags || [],
                    operation: {
                        ...operation,
                        parameters: [...(pathItem.parameters || []), ...(operation.parameters || [])],
                    },
                });
            });
        });
        return result;
    }

    /**
     * 查询当前服务可见接口
     *
     * @param {Array<object>} operations 当前服务接口集合
     * @param {string} keyword 搜索关键字
     * @returns {Array<object>} 返回可见接口集合
     */
    function visibleOperationsForService(operations, keyword) {
        const normalizedKeyword = String(keyword || "").trim().toLowerCase();
        return (operations || [])
            .filter((item) => {
                if (!normalizedKeyword) {
                    return true;
                }
                return `${item.method} ${item.path} ${item.summary || ""} ${(item.tags || []).join(" ")}`
                    .toLowerCase()
                    .includes(normalizedKeyword);
            });
    }

    /**
     * 创建接口键
     *
     * @param {string} serviceId 服务 ID
     * @param {string} method 请求方法
     * @param {string} path 请求路径
     * @returns {string} 返回接口键
     */
    function operationKey(serviceId, method, path) {
        return `${serviceId}:${String(method || "GET").toLowerCase()}:${path || "/"}`;
    }

    /**
     * 获取 OpenAPI 参数默认值
     *
     * @param {Array<object>} parameters 参数集合
     * @param {string} location 参数位置
     * @param {object} spec OpenAPI JSON
     * @returns {object} 返回参数默认值
     */
    function defaultParameters(parameters, location, spec) {
        const value = {};
        (parameters || [])
            .filter((item) => item.in === location)
            .forEach((item) => {
                const sample = sampleSchema(item.schema || {}, spec);
                if (sample !== undefined) {
                    value[item.name] = sample;
                }
            });
        return value;
    }

    /**
     * 获取第一个请求体 Schema
     *
     * @param {object} requestBody OpenAPI requestBody
     * @returns {object|null} 返回请求体 Schema
     */
    function firstRequestSchema(requestBody) {
        const content = requestBody?.content || {};
        const media = content["application/json"] || Object.values(content)[0];
        return media?.schema || null;
    }

    /**
     * 解析 OpenAPI $ref
     *
     * @param {object} schema Schema
     * @param {object} spec OpenAPI JSON
     * @returns {object} 返回解析后的 Schema
     */
    function resolveRef(schema, spec) {
        if (!schema?.$ref) {
            return schema || {};
        }
        return schema.$ref.replace(/^#\//, "").split("/").reduce((node, key) => node?.[key], spec) || {};
    }

    /**
     * 生成 Schema 样例
     *
     * @param {object} schema Schema
     * @param {object} spec OpenAPI JSON
     * @param {Set<string>} refStack 当前引用栈
     * @param {number} depth 当前深度
     * @returns {any} 返回样例值
     */
    function sampleSchema(schema, spec, refStack = new Set(), depth = 0) {
        if (!schema || depth > MAX_SAMPLE_DEPTH || schema.writeOnly) {
            return undefined;
        }
        if (schema.$ref) {
            if (refStack.has(schema.$ref)) {
                return undefined;
            }
            const nextRefStack = new Set(refStack);
            nextRefStack.add(schema.$ref);
            return sampleSchema(resolveRef(schema, spec), spec, nextRefStack, depth + 1);
        }
        schema = resolveRef(schema, spec);
        if (schema.example !== undefined) return schema.example;
        if (schema.default !== undefined) return schema.default;
        if (schema.enum?.length) return schema.enum[0];
        if (schema.allOf?.length) return Object.assign({}, ...schema.allOf.map((item) => sampleSchema(item, spec, refStack, depth + 1)).filter((item) => item !== undefined));
        if (schema.oneOf?.length) return sampleSchema(schema.oneOf[0], spec, refStack, depth + 1);
        if (schema.anyOf?.length) return sampleSchema(schema.anyOf[0], spec, refStack, depth + 1);
        if (schema.type === "array") {
            const itemSample = sampleSchema(schema.items || {}, spec, refStack, depth + 1);
            return itemSample === undefined ? [] : [itemSample];
        }
        if (schema.type === "object" || schema.properties) {
            const value = {};
            Object.entries(schema.properties || {}).forEach(([key, property]) => {
                const propertySample = sampleSchema(property, spec, refStack, depth + 1);
                if (propertySample !== undefined) {
                    value[key] = propertySample;
                }
            });
            return value;
        }
        if (schema.type === "integer") return 1;
        if (schema.type === "number") return 1.1;
        if (schema.type === "boolean") return true;
        if (schema.format === "date-time") return new Date().toISOString();
        if (schema.format === "date") return new Date().toISOString().slice(0, 10);
        if (schema.format === "email") return "user@example.com";
        if (schema.format === "uuid") return createUuid();
        return "string";
    }

    /**
     * 创建默认 Header
     *
     * @param {Array<object>} parameters 参数集合
     * @param {object} spec OpenAPI JSON
     * @returns {object} 返回 Header 默认值
     */
    function defaultHeaders(parameters, spec) {
        return {
            "Content-Type": "application/json",
            ...defaultParameters(parameters, "header", spec),
        };
    }

    /**
     * 计算文本字节数
     *
     * @param {string} value 文本
     * @returns {number} 返回字节数
     */
    function byteLength(value) {
        return new Blob([value || ""]).size;
    }

    /**
     * 创建 UUID
     *
     * @returns {string} 返回 UUID
     */
    function createUuid() {
        if (typeof crypto !== "undefined" && crypto.randomUUID) {
            return crypto.randomUUID();
        }
        return "00000000-0000-4000-8000-000000000000";
    }

    return {
        collectOperations,
        visibleOperationsForService,
        operationKey,
        defaultParameters,
        firstRequestSchema,
        resolveRef,
        sampleSchema,
        defaultHeaders,
        byteLength,
    };
}));
