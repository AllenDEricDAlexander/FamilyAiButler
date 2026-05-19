/*
 * @BelongsProject: openapi-console
 * @BelongsPackage: static.openapi-console
 * @FileName: app.js
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-17:55
 * @Description: OpenAPI 调试文档控制台脚本
 * @Version: 1.0
 */
const API_BASE = `${location.pathname.replace(/\/(?:index\.html)?$/, "")}/api`;
const specTools = window.OpenApiConsoleSpec;
const localStore = window.OpenApiConsoleStorage.createConsoleStore();
const state = {
    catalog: null,
    service: null,
    spec: null,
    specs: {},
    serviceOperations: {},
    operationCounts: {},
    operations: [],
    operation: null,
    activeTab: "params",
    localPanel: "",
    lastResponseBody: "",
    lastCurl: "",
};

const $ = (id) => document.getElementById(id);

document.addEventListener("DOMContentLoaded", () => {
    bindEvents();
    renderLocalBadges();
    checkLogin();
});

/**
 * 绑定页面事件
 */
function bindEvents() {
    $("loginForm").addEventListener("submit", login);
    $("logout").addEventListener("click", logout);
    $("refreshCatalog").addEventListener("click", loadCatalog);
    $("search").addEventListener("input", renderOperationResults);
    $("fillExample").addEventListener("click", fillExample);
    $("sendRequest").addEventListener("click", sendRequest);
    $("runLoadTest").addEventListener("click", runLoadTest);
    $("exportMd").addEventListener("click", () => exportDoc("md"));
    $("exportPdf").addEventListener("click", () => exportDoc("pdf"));
    $("formatEditor").addEventListener("click", formatActiveEditor);
    $("clearEditor").addEventListener("click", clearActiveEditor);
    $("importEditorJson").addEventListener("click", () => $("editorJsonFile").click());
    $("editorJsonFile").addEventListener("change", importEditorJson);
    $("exportLocalData").addEventListener("click", exportLocalData);
    $("importLocalData").addEventListener("click", () => $("localDataFile").click());
    $("localDataFile").addEventListener("change", importLocalData);
    $("favoriteOperation").addEventListener("click", toggleFavorite);
    $("savePreset").addEventListener("click", saveCurrentPreset);
    $("applyAuth").addEventListener("click", applyAuth);
    $("copyResponse").addEventListener("click", () => copyText(state.lastResponseBody));
    $("copyCurl").addEventListener("click", () => copyText(state.lastCurl));
    $("downloadResponse").addEventListener("click", downloadResponse);
    $("showHistory").addEventListener("click", () => renderLocalPanel("history"));
    $("showFavorites").addEventListener("click", () => renderLocalPanel("favorites"));
    $("showPresets").addEventListener("click", () => renderLocalPanel("presets"));
    $("showSettings").addEventListener("click", () => renderLocalPanel("settings"));
    document.querySelectorAll(".tab").forEach((tab) => {
        tab.addEventListener("click", () => activateTab(tab.dataset.tab));
    });
}

/**
 * 检查登录态
 */
async function checkLogin() {
    const response = await fetch(`${API_BASE}/me`, {credentials: "include"});
    if (!response.ok) {
        showLogin();
        return;
    }
    const me = await response.json();
    if (me.authenticated) {
        $("usernameLabel").textContent = me.username || "admin";
        $("userAvatar").textContent = (me.username || "A").slice(0, 1).toUpperCase();
        $("modeLabel").textContent = me.mode || "FULL";
        showConsole();
        await loadCatalog();
        return;
    }
    showLogin();
}

/**
 * 登录控制台
 *
 * @param {SubmitEvent} event 表单事件
 */
async function login(event) {
    event.preventDefault();
    $("loginMessage").textContent = "";
    const response = await fetch(`${API_BASE}/login`, {
        method: "POST",
        credentials: "include",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({username: $("username").value, password: $("password").value}),
    });
    if (!response.ok) {
        $("loginMessage").textContent = "账号或密码错误";
        return;
    }
    $("usernameLabel").textContent = $("username").value || "admin";
    $("userAvatar").textContent = ($("username").value || "A").slice(0, 1).toUpperCase();
    showConsole();
    await loadCatalog();
}

/**
 * 退出登录
 */
async function logout() {
    await fetch(`${API_BASE}/logout`, {method: "POST", credentials: "include"});
    showLogin();
}

/**
 * 显示登录视图
 */
function showLogin() {
    $("loginView").classList.remove("hidden");
    $("consoleView").classList.add("hidden");
}

/**
 * 显示控制台视图
 */
function showConsole() {
    $("loginView").classList.add("hidden");
    $("consoleView").classList.remove("hidden");
}

/**
 * 加载服务目录
 */
async function loadCatalog() {
    const response = await fetch(`${API_BASE}/catalog`, {credentials: "include"});
    if (!response.ok) {
        $("serviceList").innerHTML = `<div class="empty-state">服务目录加载失败，请检查控制台配置</div>`;
        disableWorkspace(true);
        return;
    }
    state.catalog = await response.json();
    $("title").textContent = state.catalog.title || "FamilyAiButler";
    $("environment").textContent = environmentName(state.catalog.environment);
    $("modeLabel").textContent = state.catalog.mode || "FULL";
    renderServices();
    disableWorkspace(!state.catalog.services.length);
    if (!state.catalog.services.length) {
        $("serviceList").innerHTML = `<div class="empty-state">暂无服务配置，请检查 egon.openapi.console.services</div>`;
        return;
    }
    await loadService(state.catalog.services[0]);
    hydrateServiceCounts();
}

/**
 * 后台刷新服务接口数量
 */
async function hydrateServiceCounts() {
    await Promise.all((state.catalog?.services || []).map(async (service) => {
        try {
            await fetchServiceSpec(service);
        } catch (error) {
            state.operationCounts[service.id] = 0;
        }
    }));
    renderServices();
    renderOperationResults();
}

/**
 * 加载服务 OpenAPI
 *
 * @param {object} service 服务条目
 * @param {string} operationKey 接口键
 * @param {object} snapshot 请求快照
 */
async function loadService(service, operationKey, snapshot) {
    state.service = service;
    renderServices();
    $("currentServiceName").textContent = service.name || service.id;
    $("currentServiceTag").textContent = service.group || "服务";
    try {
        state.spec = await fetchServiceSpec(service);
    } catch (error) {
        state.spec = null;
        state.operations = [];
        state.operation = null;
        $("currentOperationList").innerHTML = `<div class="empty-state">OpenAPI JSON 加载失败</div>`;
        $("operationCount").textContent = "0";
        $("operationResults").innerHTML = `<div class="empty-state">OpenAPI JSON 加载失败</div>`;
        renderOperationDetail();
        return;
    }
    state.operations = state.serviceOperations[service.id] || [];
    state.operation = operationKey
        ? state.operations.find((item) => item.key === operationKey) || state.operations[0] || null
        : state.operations[0] || null;
    renderCurrentOperationList();
    renderOperationResults();
    try {
        renderOperationDetail(snapshot);
    } catch (error) {
        setResponseError(`接口详情渲染失败: ${error.message}`);
    }
}

/**
 * 拉取服务 OpenAPI
 *
 * @param {object} service 服务条目
 * @returns {Promise<object>} 返回 OpenAPI JSON
 */
async function fetchServiceSpec(service) {
    if (state.specs[service.id]) {
        return state.specs[service.id];
    }
    const response = await fetch(`${API_BASE}/openapi/${encodeURIComponent(service.id)}`, {credentials: "include"});
    if (!response.ok) {
        throw new Error("OpenAPI JSON 加载失败");
    }
    const spec = await response.json();
    const operations = specTools.collectOperations(spec).map((item) => ({
        ...item,
        serviceId: service.id,
        serviceName: service.name || service.id,
        key: specTools.operationKey(service.id, item.method, item.path),
    }));
    state.specs[service.id] = spec;
    state.serviceOperations[service.id] = operations;
    state.operationCounts[service.id] = operations.length;
    return spec;
}

/**
 * 渲染服务列表
 */
function renderServices() {
    $("serviceList").innerHTML = "";
    if (!state.catalog?.services?.length) {
        $("serviceList").innerHTML = `<div class="empty-state">暂无服务配置</div>`;
        return;
    }
    state.catalog.services.forEach((service) => {
        const button = document.createElement("button");
        const count = state.operationCounts[service.id] ?? "-";
        button.className = `service-item${state.service?.id === service.id ? " active" : ""}`;
        button.innerHTML = `
      <strong>${escapeHtml(service.name || service.id)}</strong>
      <span class="count-badge">${escapeHtml(count)}</span>
      <small>${escapeHtml(service.group || "default")}</small>
    `;
        button.addEventListener("click", () => loadService(service));
        $("serviceList").appendChild(button);
    });
}

/**
 * 渲染当前服务接口列表
 */
function renderCurrentOperationList() {
    const keyword = $("search").value.trim();
    const operations = specTools.visibleOperationsForService(state.operations, keyword);
    $("operationCount").textContent = operations.length;
    $("currentOperationList").innerHTML = "";
    if (!state.service) {
        $("currentOperationList").innerHTML = `<div class="empty-state">请选择服务</div>`;
        return;
    }
    if (!operations.length) {
        $("currentOperationList").innerHTML = `<div class="empty-state">暂无接口</div>`;
        return;
    }
    operations.forEach((item) => {
        $("currentOperationList").appendChild(operationButton(item, () => selectCurrentOperation(item)));
    });
}

/**
 * 渲染搜索结果
 */
function renderOperationResults() {
    const keyword = $("search").value.trim().toLowerCase();
    renderCurrentOperationList();
    $("searchResults").classList.toggle("hidden", !keyword);
    $("operationResults").innerHTML = "";
    if (!keyword) {
        return;
    }
    const operations = Object.values(state.serviceOperations)
        .flat()
        .filter((item) => `${item.serviceName} ${item.method} ${item.path} ${item.summary} ${item.tags.join(" ")}`.toLowerCase().includes(keyword))
        .slice(0, 80);
    if (!operations.length) {
        $("operationResults").innerHTML = `<div class="empty-state">暂无接口</div>`;
        return;
    }
    operations.forEach((item) => $("operationResults").appendChild(operationButton(item, () => selectOperationFromAnyService(item), true)));
}

/**
 * 创建接口按钮
 *
 * @param {object} item 接口条目
 * @param {Function} clickHandler 点击回调
 * @param {boolean} showService 是否展示服务名
 * @returns {HTMLButtonElement} 返回接口按钮
 */
function operationButton(item, clickHandler, showService) {
    const button = document.createElement("button");
    button.className = `operation-item${state.operation?.key === item.key ? " active" : ""}`;
    button.innerHTML = `
    <strong>${escapeHtml(item.method)} ${escapeHtml(item.path)}</strong>
    <small>${showService ? `${escapeHtml(item.serviceName)} · ` : ""}${escapeHtml(item.summary || item.tags.join(","))}</small>
  `;
    button.addEventListener("click", clickHandler);
    return button;
}

/**
 * 选择当前服务接口
 *
 * @param {object} operation 接口条目
 */
function selectCurrentOperation(operation) {
    state.operation = operation;
    $("requestPath").value = operation.path;
    renderCurrentOperationList();
    renderOperationResults();
    renderOperationDetail();
}

/**
 * 跨服务选择接口
 *
 * @param {object} operation 接口条目
 */
function selectOperationFromAnyService(operation) {
    const service = state.catalog.services.find((item) => item.id === operation.serviceId);
    if (!service) {
        return;
    }
    loadService(service, operation.key);
}

/**
 * 渲染接口详情
 *
 * @param {object} snapshot 请求快照
 */
function renderOperationDetail(snapshot) {
    if (!state.operation) {
        disableWorkspace(true);
        $("methodSelect").value = "GET";
        $("requestPath").value = "";
        $("pathInput").value = "{}";
        $("queryInput").value = "{}";
        $("headersInput").value = "{}";
        $("bodyInput").value = "";
        $("responseOutput").textContent = "请选择服务和接口";
        $("curlOutput").textContent = "";
        setStatusPills();
        return;
    }
    disableWorkspace(false);
    $("methodSelect").value = state.operation.method;
    $("requestPath").value = state.operation.path;
    fillDefaults();
    if (snapshot) {
        applyRequestSnapshot(snapshot);
    }
    setStatusPills();
    $("responseOutput").textContent = "";
    $("curlOutput").textContent = "";
    $("loadResult").classList.add("hidden");
    renderFavoriteState();
}

/**
 * 填充默认请求数据
 */
function fillDefaults() {
    const parameters = currentParameters();
    $("pathInput").value = JSON.stringify(specTools.defaultParameters(parameters, "path", state.spec), null, 2);
    $("queryInput").value = JSON.stringify(specTools.defaultParameters(parameters, "query", state.spec), null, 2);
    $("headersInput").value = JSON.stringify(specTools.defaultHeaders(parameters, state.spec), null, 2);
    $("bodyInput").value = "";
    $("scriptInput").value = "{}";
    refreshHeaderCount();
}

/**
 * 填充测试数据
 */
function fillExample() {
    if (!state.operation) {
        return;
    }
    const parameters = currentParameters();
    const bodySchema = specTools.firstRequestSchema(state.operation.operation.requestBody);
    $("pathInput").value = JSON.stringify(specTools.defaultParameters(parameters, "path", state.spec), null, 2);
    $("queryInput").value = JSON.stringify(specTools.defaultParameters(parameters, "query", state.spec), null, 2);
    $("headersInput").value = JSON.stringify(specTools.defaultHeaders(parameters, state.spec), null, 2);
    $("bodyInput").value = bodySchema ? JSON.stringify(specTools.sampleSchema(bodySchema, state.spec), null, 2) : "";
    refreshHeaderCount();
}

/**
 * 获取当前接口参数
 *
 * @returns {Array<object>} 返回参数集合
 */
function currentParameters() {
    return state.operation?.operation?.parameters || [];
}

/**
 * 激活编辑 Tab
 *
 * @param {string} tabName Tab 名称
 */
function activateTab(tabName) {
    state.activeTab = tabName;
    document.querySelectorAll(".tab").forEach((tab) => tab.classList.toggle("active", tab.dataset.tab === tabName));
    document.querySelectorAll(".tab-panel").forEach((panel) => panel.classList.toggle("active", panel.id === `${tabName}Panel`));
}

/**
 * 美化当前编辑器
 */
function formatActiveEditor() {
    const editor = activeJsonEditor();
    if (!editor) {
        return;
    }
    try {
        editor.value = JSON.stringify(JSON.parse(editor.value || "{}"), null, 2);
    } catch (error) {
        setResponseError("当前编辑器不是合法 JSON");
    }
}

/**
 * 清空当前编辑器
 */
function clearActiveEditor() {
    const editor = activeJsonEditor();
    if (editor) {
        editor.value = "{}";
        refreshHeaderCount();
    }
}

/**
 * 导入当前编辑器 JSON
 */
async function importEditorJson() {
    const file = $("editorJsonFile").files[0];
    const editor = activeJsonEditor();
    if (!file || !editor) {
        return;
    }
    editor.value = await file.text();
    $("editorJsonFile").value = "";
    formatActiveEditor();
}

/**
 * 获取当前 JSON 编辑器
 *
 * @returns {HTMLTextAreaElement|null} 返回编辑器
 */
function activeJsonEditor() {
    if (state.activeTab === "params") {
        return $("queryInput");
    }
    if (state.activeTab === "headers") {
        return $("headersInput");
    }
    if (state.activeTab === "body") {
        return $("bodyInput");
    }
    if (state.activeTab === "script") {
        return $("scriptInput");
    }
    return null;
}

/**
 * 应用认证配置
 */
function applyAuth() {
    const headers = parseJsonStrict($("headersInput").value, "Headers");
    delete headers.Authorization;
    const authType = $("authType").value;
    if (authType === "bearer" && $("bearerToken").value.trim()) {
        headers.Authorization = `Bearer ${$("bearerToken").value.trim()}`;
    }
    if (authType === "basic" && ($("basicUsername").value || $("basicPassword").value)) {
        headers.Authorization = `Basic ${btoa(`${$("basicUsername").value}:${$("basicPassword").value}`)}`;
    }
    if (authType === "apiKey" && $("apiKeyName").value.trim()) {
        headers[$("apiKeyName").value.trim()] = $("apiKeyValue").value;
    }
    $("headersInput").value = JSON.stringify(headers, null, 2);
    refreshHeaderCount();
    activateTab("headers");
}

/**
 * 发送调试请求
 */
async function sendRequest() {
    if (!state.service || !state.operation) {
        return;
    }
    let payload;
    try {
        payload = buildExecutePayload();
    } catch (error) {
        setResponseError(error.message);
        return;
    }
    const response = await fetch(`${API_BASE}/execute`, {
        method: "POST",
        credentials: "include",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(payload),
    });
    const result = await response.json();
    renderExecuteResult(result);
    localStore.addHistory({
        serviceId: state.service.id,
        serviceName: state.service.name || state.service.id,
        method: payload.method,
        path: payload.path,
        summary: state.operation.summary,
        status: result.status,
        durationMillis: result.durationMillis,
        request: snapshotRequest(),
    });
    renderLocalBadges();
}

/**
 * 执行轻量压测
 */
async function runLoadTest() {
    if (!state.service || !state.operation) {
        return;
    }
    let executeRequest;
    try {
        executeRequest = buildExecutePayload();
    } catch (error) {
        setResponseError(error.message);
        return;
    }
    const payload = {
        request: executeRequest,
        totalRequests: Number($("totalRequests").value || 10),
        concurrency: Number($("concurrency").value || 2),
    };
    const response = await fetch(`${API_BASE}/load-test`, {
        method: "POST",
        credentials: "include",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(payload),
    });
    const result = await response.json();
    $("curlOutput").textContent = JSON.stringify(result, null, 2);
    renderLoadResult(result);
}

/**
 * 创建调试请求载荷
 *
 * @returns {object} 返回调试请求载荷
 */
function buildExecutePayload() {
    const pathParams = parseJsonStrict($("pathInput").value, "Path 参数");
    const variables = templateVariables();
    const headers = normalizeMap(applyTemplatesToObject(parseJsonStrict($("headersInput").value, "Headers"), variables));
    const query = normalizeMap(applyTemplatesToObject(parseJsonStrict($("queryInput").value, "Query 参数"), variables));
    const body = applyTemplates($("bodyInput").value || "", variables);
    return {
        serviceId: state.service.id,
        method: $("methodSelect").value,
        path: buildPath($("requestPath").value, pathParams),
        headers,
        query,
        contentType: headers["Content-Type"] || headers["content-type"] || "application/json",
        body,
    };
}

/**
 * 构造请求路径
 *
 * @param {string} path 原始路径
 * @param {object} params Path 参数
 * @returns {string} 返回请求路径
 */
function buildPath(path, params) {
    return Object.entries(params || {}).reduce((result, [key, value]) => {
        return result.replace(new RegExp(`\\{${escapeRegExp(key)}\\}`, "g"), encodeURIComponent(String(value)));
    }, path || "/");
}

/**
 * 创建请求快照
 *
 * @returns {object} 返回请求快照
 */
function snapshotRequest() {
    return {
        method: $("methodSelect").value,
        path: $("requestPath").value,
        pathParams: $("pathInput").value,
        headers: $("headersInput").value,
        query: $("queryInput").value,
        body: $("bodyInput").value,
        script: $("scriptInput").value,
    };
}

/**
 * 应用请求快照
 *
 * @param {object} snapshot 请求快照
 */
function applyRequestSnapshot(snapshot) {
    $("methodSelect").value = snapshot.method || state.operation.method;
    $("requestPath").value = snapshot.path || state.operation.path;
    $("pathInput").value = snapshot.pathParams || "{}";
    $("headersInput").value = snapshot.headers || "{}";
    $("queryInput").value = snapshot.query || "{}";
    $("bodyInput").value = snapshot.body || "";
    $("scriptInput").value = snapshot.script || "{}";
    refreshHeaderCount();
}

/**
 * 渲染调试结果
 *
 * @param {object} result 调试结果
 */
function renderExecuteResult(result) {
    const body = result.body || "";
    state.lastResponseBody = prettyText(body);
    state.lastCurl = result.curl || "";
    $("responseOutput").textContent = state.lastResponseBody;
    $("curlOutput").textContent = state.lastCurl;
    $("loadResult").classList.add("hidden");
    setStatusPills(result.status, result.durationMillis, specTools.byteLength(body));
}

/**
 * 渲染压测结果
 *
 * @param {object} result 压测结果
 */
function renderLoadResult(result) {
    $("loadResult").classList.remove("hidden");
    const rate = result.total ? Math.round((result.success / result.total) * 100) : 0;
    $("loadSummary").textContent = `${rate}% 成功`;
    $("loadMetrics").innerHTML = [
        metric("请求次数", result.total),
        metric("并发数", $("concurrency").value || 0),
        metric("成功率", `${rate}%`),
        metric("平均耗时", `${Math.round(result.avgMillis || 0)}ms`),
        metric("最大耗时", `${result.maxMillis || 0}ms`),
        metric("最小耗时", `${result.minMillis || 0}ms`),
    ].join("");
    $("loadChart").innerHTML = renderLoadChart(result.samples || []);
}

/**
 * 创建指标卡片
 *
 * @param {string} label 指标名称
 * @param {string|number} value 指标值
 * @returns {string} 返回指标 HTML
 */
function metric(label, value) {
    return `<div class="metric"><span>${escapeHtml(label)}</span><strong>${escapeHtml(value)}</strong></div>`;
}

/**
 * 渲染压测折线图
 *
 * @param {Array<object>} samples 压测样本
 * @returns {string} 返回 SVG
 */
function renderLoadChart(samples) {
    const values = samples.map((item) => Number(item.durationMillis || 0));
    if (!values.length) {
        return `<div class="empty-state">暂无压测样本</div>`;
    }
    const max = Math.max(...values, 1);
    const width = 640;
    const height = 120;
    const points = values.map((value, index) => {
        const x = values.length === 1 ? 0 : (index / (values.length - 1)) * width;
        const y = height - (value / max) * (height - 18) - 8;
        return `${x.toFixed(2)},${y.toFixed(2)}`;
    }).join(" ");
    return `
    <svg viewBox="0 0 ${width} ${height}" role="img">
      <polyline points="${points}" fill="none" stroke="#0f8b80" stroke-width="3" />
      ${points.split(" ").map((point) => `<circle cx="${point.split(",")[0]}" cy="${point.split(",")[1]}" r="4" fill="#0f8b80" />`).join("")}
    </svg>
  `;
}

/**
 * 保存当前接口预设
 */
function saveCurrentPreset() {
    if (!state.operation) {
        return;
    }
    const name = prompt("预设名称", state.operation.summary || state.operation.path);
    if (!name) {
        return;
    }
    localStore.savePreset(currentOperationKey(), {
        name,
        request: snapshotRequest(),
    });
    renderLocalBadges();
    renderLocalPanel("presets");
}

/**
 * 切换当前接口收藏
 */
function toggleFavorite() {
    if (!state.operation) {
        return;
    }
    localStore.toggleFavorite(currentOperationKey(), {
        serviceId: state.service.id,
        serviceName: state.service.name || state.service.id,
        method: state.operation.method,
        path: state.operation.path,
        summary: state.operation.summary,
    });
    renderFavoriteState();
    renderLocalBadges();
    if (state.localPanel === "favorites") {
        renderLocalPanel("favorites");
    }
}

/**
 * 渲染当前收藏状态
 */
function renderFavoriteState() {
    $("favoriteOperation").textContent = localStore.isFavorite(currentOperationKey()) ? "已收藏" : "收藏";
}

/**
 * 渲染本地数据徽标
 */
function renderLocalBadges() {
    const local = localStore.loadState();
    $("historyCount").textContent = local.history.length;
    $("favoriteCount").textContent = local.favorites.length;
    $("presetCount").textContent = local.presets.length;
}

/**
 * 渲染本地数据面板
 *
 * @param {string} type 面板类型
 * @param {boolean} forceOpen 是否强制打开
 */
function renderLocalPanel(type, forceOpen) {
    state.localPanel = !forceOpen && state.localPanel === type ? "" : type;
    document.querySelectorAll(".side-nav button").forEach((button) => button.classList.remove("active"));
    $("localPanel").classList.toggle("hidden", !state.localPanel);
    if (!state.localPanel) {
        $("localPanel").innerHTML = "";
        return;
    }
    $(`show${type.slice(0, 1).toUpperCase()}${type.slice(1)}`).classList.add("active");
    const local = localStore.loadState();
    if (type === "history") {
        renderLocalItems(local.history, "暂无接口历史", restoreHistory);
    }
    if (type === "favorites") {
        renderLocalItems(local.favorites, "暂无收藏接口", loadFavorite);
    }
    if (type === "presets") {
        renderLocalItems(local.presets, "暂无接口预设", applyPreset);
    }
    if (type === "settings") {
        $("localPanel").innerHTML = `
      <div class="local-list">
        <button class="local-item" type="button" id="panelExportData"><strong>导出本地数据</strong><small>history / favorites / presets / settings</small></button>
        <button class="local-item" type="button" id="panelImportData"><strong>导入本地数据</strong><small>openapi-console-local-data.json</small></button>
      </div>
    `;
        $("panelExportData").addEventListener("click", exportLocalData);
        $("panelImportData").addEventListener("click", () => $("localDataFile").click());
    }
}

/**
 * 渲染本地条目列表
 *
 * @param {Array<object>} items 条目集合
 * @param {string} emptyText 空状态文案
 * @param {Function} handler 点击回调
 */
function renderLocalItems(items, emptyText, handler) {
    if (!items.length) {
        $("localPanel").innerHTML = `<div class="empty-state">${escapeHtml(emptyText)}</div>`;
        return;
    }
    $("localPanel").innerHTML = `<div class="local-list"></div>`;
    const list = $("localPanel").querySelector(".local-list");
    items.forEach((item) => {
        const button = document.createElement("button");
        button.className = "local-item";
        button.type = "button";
        button.innerHTML = `
      <strong>${escapeHtml(item.name || `${item.method || ""} ${item.path || item.key || ""}`)}</strong>
      <small>${escapeHtml(item.serviceName || item.serviceId || item.updatedAt || item.createdAt || "")}</small>
    `;
        button.addEventListener("click", () => handler(item));
        list.appendChild(button);
    });
}

/**
 * 恢复请求历史
 *
 * @param {object} item 历史条目
 */
function restoreHistory(item) {
    loadStoredOperation(item.serviceId, specTools.operationKey(item.serviceId, item.method, item.path), item.request);
}

/**
 * 加载收藏接口
 *
 * @param {object} item 收藏条目
 */
function loadFavorite(item) {
    loadStoredOperation(item.serviceId, item.key);
}

/**
 * 应用接口预设
 *
 * @param {object} item 预设条目
 */
function applyPreset(item) {
    const [serviceId] = item.key.split(":");
    loadStoredOperation(serviceId, item.key, item.request);
}

/**
 * 加载本地存储中的接口
 *
 * @param {string} serviceId 服务 ID
 * @param {string} operationKey 接口键
 * @param {object} snapshot 请求快照
 */
function loadStoredOperation(serviceId, operationKey, snapshot) {
    const service = state.catalog?.services?.find((item) => item.id === serviceId);
    if (service) {
        loadService(service, operationKey, snapshot);
    }
}

/**
 * 导出本地数据
 */
function exportLocalData() {
    downloadText("openapi-console-local-data.json", JSON.stringify(localStore.exportState(), null, 2), "application/json");
}

/**
 * 导入本地数据
 */
async function importLocalData() {
    const file = $("localDataFile").files[0];
    if (!file) {
        return;
    }
    try {
        localStore.importState(JSON.parse(await file.text()));
        renderLocalBadges();
        renderLocalPanel("settings", true);
    } catch (error) {
        setResponseError(error.message);
    } finally {
        $("localDataFile").value = "";
    }
}

/**
 * 导出接口文档
 *
 * @param {string} format 导出格式
 */
function exportDoc(format) {
    if (!state.service) {
        return;
    }
    location.href = `${API_BASE}/export/${encodeURIComponent(state.service.id)}?format=${format}`;
}

/**
 * 禁用或启用工作区
 *
 * @param {boolean} disabled 是否禁用
 */
function disableWorkspace(disabled) {
    const capabilities = state.catalog?.capabilities || {};
    $("fillExample").disabled = disabled;
    $("sendRequest").disabled = disabled || !capabilities.tryout;
    $("runLoadTest").disabled = disabled || !capabilities.loadTest;
    $("savePreset").disabled = disabled;
    $("favoriteOperation").disabled = disabled;
    $("exportMd").disabled = !capabilities.export || !state.service;
    $("exportPdf").disabled = !capabilities.export || !state.service;
}

/**
 * 设置状态摘要
 *
 * @param {number} status HTTP 状态码
 * @param {number} durationMillis 耗时
 * @param {number} size 响应大小
 */
function setStatusPills(status, durationMillis, size) {
    $("statusPill").textContent = status ? `${status} ${status >= 200 && status < 400 ? "OK" : "ERR"}` : "-";
    $("statusPill").className = status ? (status >= 200 && status < 400 ? "ok" : "error") : "";
    $("durationPill").textContent = durationMillis ? `${durationMillis}ms` : "-";
    $("sizePill").textContent = size ? formatBytes(size) : "-";
}

/**
 * 设置响应错误
 *
 * @param {string} message 错误信息
 */
function setResponseError(message) {
    state.lastResponseBody = message;
    $("responseOutput").textContent = message;
    setStatusPills(0, 0, 0);
}

/**
 * 刷新 Header 数量
 */
function refreshHeaderCount() {
    try {
        $("headerCount").textContent = `(${Object.keys(JSON.parse($("headersInput").value || "{}")).length})`;
    } catch (error) {
        $("headerCount").textContent = "(0)";
    }
}

/**
 * 获取当前接口键
 *
 * @returns {string} 返回接口键
 */
function currentOperationKey() {
    return state.operation ? specTools.operationKey(state.service.id, state.operation.method, state.operation.path) : "";
}

/**
 * 解析 JSON
 *
 * @param {string} value JSON 文本
 * @param {string} label 字段名
 * @returns {object} 返回 JSON 对象
 */
function parseJsonStrict(value, label) {
    try {
        return value && value.trim() ? JSON.parse(value) : {};
    } catch (error) {
        throw new Error(`${label} 不是合法 JSON`);
    }
}

/**
 * 标准化 Map 值
 *
 * @param {object} value 原始 Map
 * @returns {object} 返回字符串 Map
 */
function normalizeMap(value) {
    return Object.fromEntries(Object.entries(value || {}).map(([key, item]) => {
        return [key, typeof item === "string" ? item : JSON.stringify(item)];
    }));
}

/**
 * 创建模板变量
 *
 * @returns {object} 返回变量集合
 */
function templateVariables() {
    const now = new Date();
    return {
        timestamp: Date.now(),
        datetime: now.toISOString(),
        date: now.toISOString().slice(0, 10),
        uuid: typeof crypto !== "undefined" && crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}`,
        ...parseJsonStrict($("scriptInput").value, "预设变量"),
    };
}

/**
 * 应用模板变量
 *
 * @param {string} text 原文
 * @param {object} variables 变量集合
 * @returns {string} 返回替换后的文本
 */
function applyTemplates(text, variables) {
    return String(text || "").replace(/\{\{\s*([\w.-]+)\s*}}/g, (match, key) => {
        return variables[key] === undefined ? match : String(variables[key]);
    });
}

/**
 * 对对象应用模板变量
 *
 * @param {object} value 原始对象
 * @param {object} variables 变量集合
 * @returns {object} 返回替换后的对象
 */
function applyTemplatesToObject(value, variables) {
    if (Array.isArray(value)) {
        return value.map((item) => applyTemplatesToObject(item, variables));
    }
    if (value && typeof value === "object") {
        return Object.fromEntries(Object.entries(value).map(([key, item]) => [key, applyTemplatesToObject(item, variables)]));
    }
    if (typeof value === "string") {
        return applyTemplates(value, variables);
    }
    return value;
}

/**
 * 格式化响应文本
 *
 * @param {string} text 原文
 * @returns {string} 返回格式化文本
 */
function prettyText(text) {
    try {
        return JSON.stringify(JSON.parse(text), null, 2);
    } catch (error) {
        return text || "";
    }
}

/**
 * 格式化字节数
 *
 * @param {number} bytes 字节数
 * @returns {string} 返回展示文本
 */
function formatBytes(bytes) {
    if (bytes < 1024) {
        return `${bytes} B`;
    }
    return `${(bytes / 1024).toFixed(2)} KB`;
}

/**
 * 格式化环境名称
 *
 * @param {string} value 环境值
 * @returns {string} 返回环境名称
 */
function environmentName(value) {
    const env = String(value || "dev").toLowerCase();
    if (env === "prod" || env === "production") {
        return "生产环境";
    }
    if (env === "test") {
        return "测试环境";
    }
    return "开发环境";
}

/**
 * 下载响应内容
 */
function downloadResponse() {
    downloadText("openapi-response.txt", state.lastResponseBody || "", "text/plain");
}

/**
 * 下载文本文件
 *
 * @param {string} filename 文件名
 * @param {string} text 文件内容
 * @param {string} type 文件类型
 */
function downloadText(filename, text, type) {
    const blob = new Blob([text], {type});
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
}

/**
 * 复制文本
 *
 * @param {string} text 文本
 */
function copyText(text) {
    if (navigator.clipboard && text) {
        navigator.clipboard.writeText(text);
    }
}

/**
 * 转义正则文本
 *
 * @param {string} value 原始文本
 * @returns {string} 返回正则安全文本
 */
function escapeRegExp(value) {
    return String(value).replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

/**
 * HTML 转义
 *
 * @param {string|number} value 原文
 * @returns {string} 返回转义文本
 */
function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, (item) => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        "\"": "&quot;",
        "'": "&#039;",
    }[item]));
}
