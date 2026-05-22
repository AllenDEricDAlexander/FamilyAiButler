/*
 * @BelongsProject: openapi-console
 * @BelongsPackage: static.openapi-console
 * @FileName: openapi-console-openapi.test.cjs
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-20:40
 * @Description: OpenAPI 调试文档控制台接口解析测试脚本
 * @Version: 1.0
 */
const assert = require("node:assert/strict");
const test = require("node:test");
const {
    collectOperations,
    defaultParameters,
    firstRequestSchema,
    sampleSchema,
    visibleOperationsForService
} = require("../../main/resources/static/openapi-console/openapi.js");

test("shows current service operations without requiring search keyword", () => {
    const spec = {
        paths: {
            "/api/users": {
                get: {
                    summary: "查询用户",
                    tags: ["user"],
                },
            },
            "/api/users/{id}": {
                get: {
                    summary: "查询用户详情",
                    tags: ["user"],
                },
            },
        },
    };
    const operations = collectOperations(spec).map((operation) => ({
        ...operation,
        serviceId: "family-uaa",
        key: `family-uaa:${operation.method.toLowerCase()}:${operation.path}`,
    }));

    const visible = visibleOperationsForService(operations, "");

    assert.equal(visible.length, 2);
    assert.equal(visible[0].path, "/api/users");
    assert.equal(visible[1].path, "/api/users/{id}");
});

test("filters current service operations by keyword when searching", () => {
    const operations = [
        {method: "GET", path: "/api/users", summary: "查询用户", tags: ["user"]},
        {method: "POST", path: "/api/login", summary: "登录", tags: ["auth"]},
    ];

    const visible = visibleOperationsForService(operations, "login");

    assert.equal(visible.length, 1);
    assert.equal(visible[0].path, "/api/login");
});

test("generates sample data for recursive schema without stack overflow", () => {
    const spec = {
        components: {
            schemas: {
                PageUserPO: {
                    type: "object",
                    properties: {
                        records: {
                            type: "array",
                            items: {$ref: "#/components/schemas/UserPO"},
                        },
                        optimizeCountSql: {
                            $ref: "#/components/schemas/PageUserPO",
                            writeOnly: true,
                        },
                        searchCount: {
                            $ref: "#/components/schemas/PageUserPO",
                            writeOnly: true,
                        },
                    },
                },
                UserPO: {
                    type: "object",
                    properties: {
                        id: {type: "integer", format: "int64"},
                        username: {type: "string"},
                        userName: {
                            $ref: "#/components/schemas/UserPO",
                            writeOnly: true,
                        },
                    },
                },
            },
        },
    };
    const parameters = [
        {
            name: "page",
            in: "query",
            schema: {$ref: "#/components/schemas/PageUserPO"},
        },
    ];

    const value = defaultParameters(parameters, "query", spec);

    assert.equal(typeof value.page, "object");
    assert.equal(Array.isArray(value.page.records), true);
    assert.equal(value.page.records[0].id, 1);
    assert.equal(value.page.optimizeCountSql, undefined);
    assert.equal(value.page.records[0].userName, undefined);
});

test("parses single response, multipart request body and generated examples", () => {
    const spec = {
        paths: {
            "/api/doc-users/upload": {
                post: {
                    summary: "上传文件",
                    tags: ["文档用户"],
                    requestBody: {
                        content: {
                            "multipart/form-data": {
                                schema: {
                                    type: "object",
                                    required: ["file"],
                                    properties: {
                                        file: {
                                            type: "array",
                                            items: {type: "string", format: "binary"},
                                        },
                                    },
                                },
                            },
                        },
                    },
                    responses: {
                        "200": {
                            description: "上传成功",
                            content: {
                                "application/json": {
                                    schema: {$ref: "#/components/schemas/ResultDocFileUploadDocResponse"},
                                    example: {
                                        code: 0,
                                        data: {count: 2},
                                    },
                                },
                            },
                        },
                    },
                },
            },
        },
        components: {
            schemas: {
                ResultDocFileUploadDocResponse: {
                    type: "object",
                    properties: {
                        code: {type: "integer", format: "int32", example: 0},
                        data: {$ref: "#/components/schemas/FileUploadDocResponse"},
                    },
                },
                FileUploadDocResponse: {
                    type: "object",
                    properties: {
                        count: {type: "integer", format: "int32", example: 2},
                    },
                },
            },
        },
    };

    const operations = collectOperations(spec);
    const requestSchema = firstRequestSchema(operations[0].operation.requestBody);
    const responseContent = spec.paths["/api/doc-users/upload"].post.responses["200"].content["application/json"];
    const responseSample = sampleSchema(responseContent.schema, spec);

    assert.equal(operations.length, 1);
    assert.equal(requestSchema.properties.file.items.format, "binary");
    assert.equal(responseContent.example.data.count, 2);
    assert.equal(responseSample.data.count, 2);
    assert.equal(spec.components.schemas.FileUploadDocResponse.properties.count.example, 2);
});
