/*
 * @BelongsProject: openapi-console
 * @BelongsPackage: static.openapi-console
 * @FileName: openapi-console-storage.test.cjs
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-19:10
 * @Description: OpenAPI 调试文档控制台本地存储测试脚本
 * @Version: 1.0
 */
const assert = require("node:assert/strict");
const test = require("node:test");
const {createConsoleStore} = require("../../main/resources/static/openapi-console/storage.js");

/**
 * 创建内存 localStorage
 *
 * @returns {Storage} 返回内存存储
 */
function memoryStorage() {
    const values = new Map();
    return {
        getItem(key) {
            return values.has(key) ? values.get(key) : null;
        },
        setItem(key, value) {
            values.set(key, String(value));
        },
        removeItem(key) {
            values.delete(key);
        },
    };
}

test("exports and imports console local data for migration", () => {
    const source = createConsoleStore(memoryStorage());
    source.addHistory({
        serviceId: "family-core",
        method: "GET",
        path: "/password/category",
        status: 200,
        durationMillis: 18,
    });
    source.toggleFavorite("family-core:get:/password/category", {
        serviceId: "family-core",
        serviceName: "家庭核心服务",
        method: "GET",
        path: "/password/category",
        summary: "查询分类",
    });
    source.savePreset("family-core:get:/password/category", {
        name: "分类查询",
        request: {
            headers: {Authorization: "Bearer test"},
            query: {pageNo: "1"},
            body: "",
        },
    });

    const snapshot = source.exportState();
    const target = createConsoleStore(memoryStorage());
    const imported = target.importState(snapshot);

    assert.equal(imported.history.length, 1);
    assert.equal(imported.favorites.length, 1);
    assert.equal(imported.presets.length, 1);
    assert.equal(imported.presets[0].name, "分类查询");
});

test("rejects invalid console local data snapshot", () => {
    const store = createConsoleStore(memoryStorage());
    assert.throws(() => store.importState({version: 0, data: null}), /本地数据格式不正确/);
});

test("clears console local data", () => {
    const store = createConsoleStore(memoryStorage());
    store.addHistory({
        serviceId: "family-core",
        method: "GET",
        path: "/password/category",
        status: 200,
        durationMillis: 18,
    });

    const cleared = store.clearState();
    const current = store.loadState();

    assert.equal(cleared.history.length, 0);
    assert.equal(current.history.length, 0);
    assert.equal(current.favorites.length, 0);
    assert.equal(current.presets.length, 0);
});
