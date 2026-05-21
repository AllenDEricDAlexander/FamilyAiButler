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
const SIGNING_KEY_STORAGE = "openapi-console-request-signing-key";
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
    responseTab: "response",
    artifactTab: "curl",
    localPanel: "",
    lastResponseBody: "",
    lastResponseHeaders: {},
    lastResponseLog: {},
    lastCurl: "",
    lastCode: "",
    lastRequestPayload: null,
    signingKey: "",
    serviceDrawerOpen: false,
    operationDrawerOpen: false,
    sendingRequest: false,
    runningLoadTest: false,
    toastTimer: 0,
};

const $ = (id) => document.getElementById(id);

document.addEventListener("DOMContentLoaded", () => {
    bindEvents();
    syncTabState();
    syncDrawerState();
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
    $("serviceSearch").addEventListener("input", renderServices);
    $("operationSearch").addEventListener("input", renderCurrentOperationList);
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
    $("clearLocalData").addEventListener("click", clearBrowserStorage);
    $("favoriteOperation").addEventListener("click", toggleFavorite);
    $("savePreset").addEventListener("click", saveCurrentPreset);
    $("serviceDrawerToggle").addEventListener("click", () => toggleServiceDrawer(true));
    $("operationDrawerToggle").addEventListener("click", () => toggleOperationDrawer(true));
    $("closeServiceDrawer").addEventListener("click", () => toggleServiceDrawer(false));
    $("closeOperationDrawer").addEventListener("click", () => toggleOperationDrawer(false));
    $("drawerBackdrop").addEventListener("click", closeDrawers);
    $("applyAuth").addEventListener("click", applyAuth);
    $("copyResponse").addEventListener("click", () => copyText(state.lastResponseBody));
    $("copyCurl").addEventListener("click", () => copyText(state.artifactTab === "code" ? state.lastCode : state.lastCurl));
    $("downloadResponse").addEventListener("click", downloadResponse);
    if ($("showHistory")) {
        $("showHistory").addEventListener("click", () => renderLocalPanel("history"));
    }
    $("showFavorites").addEventListener("click", () => renderLocalPanel("favorites"));
    $("showPresets").addEventListener("click", () => renderLocalPanel("presets"));
    $("showSettings").addEventListener("click", () => renderLocalPanel("settings"));
    document.querySelectorAll(".tab").forEach((tab) => {
        tab.addEventListener("click", () => activateTab(tab.dataset.tab));
    });
    document.querySelectorAll(".response-tab").forEach((tab) => {
        tab.addEventListener("click", () => activateResponseTab(tab.dataset.responseTab));
    });
    document.querySelectorAll(".artifact-tab").forEach((tab) => {
        tab.addEventListener("click", () => activateArtifactTab(tab.dataset.artifactTab));
    });
    document.querySelectorAll(".action-menu").forEach((menu) => {
        menu.addEventListener("toggle", () => closeSiblingMenus(menu));
    });
    document.querySelectorAll(".menu-panel button").forEach((button) => {
        button.addEventListener("click", closeMenus);
    });
    document.addEventListener("keydown", handleGlobalKeydown);
    window.addEventListener("resize", syncDrawerState);
}

/**
 * 处理全局键盘快捷键
 *
 * @param {KeyboardEvent} event 键盘事件
 */
function handleGlobalKeydown(event) {
    if (event.key === "Escape" && (state.serviceDrawerOpen || state.operationDrawerOpen || state.localPanel)) {
        event.preventDefault();
        closeDrawers();
        return;
    }
    if (!(event.metaKey || event.ctrlKey) || event.key !== "Enter" || event.repeat) {
        return;
    }
    if ($("consoleView").classList.contains("hidden") || $("sendRequest").disabled) {
        return;
    }
    event.preventDefault();
    sendRequest();
}

/**
 * 关闭同组之外的菜单
 *
 * @param {HTMLDetailsElement} currentMenu 当前菜单
 */
function closeSiblingMenus(currentMenu) {
    if (!currentMenu.open) {
        return;
    }
    document.querySelectorAll(".action-menu").forEach((menu) => {
        if (menu !== currentMenu) {
            menu.open = false;
        }
    });
}

/**
 * 关闭顶部操作菜单
 */
function closeMenus() {
    document.querySelectorAll(".action-menu").forEach((menu) => {
        menu.open = false;
    });
}

/**
 * 切换服务抽屉
 *
 * @param {boolean} open 是否打开
 */
function toggleServiceDrawer(open) {
    state.serviceDrawerOpen = Boolean(open);
    syncDrawerState();
}

/**
 * 切换接口抽屉
 *
 * @param {boolean} open 是否打开
 */
function toggleOperationDrawer(open) {
    state.operationDrawerOpen = Boolean(open);
    if (state.operationDrawerOpen) {
        closeLocalPanel();
    }
    syncDrawerState();
    if (state.operationDrawerOpen && !$("operationSearch").disabled) {
        window.setTimeout(() => $("operationSearch").focus(), 0);
    }
}

/**
 * 关闭响应式抽屉和弹出面板
 */
function closeDrawers() {
    state.serviceDrawerOpen = false;
    state.operationDrawerOpen = false;
    closeLocalPanel();
    syncDrawerState();
}

/**
 * 同步抽屉、接口弹窗和遮罩可见性
 */
function syncDrawerState() {
    const width = window.innerWidth || document.documentElement.clientWidth;
    const serviceOpen = width < 1180 && state.serviceDrawerOpen;
    const operationOpen = state.operationDrawerOpen;
    $("consoleView").classList.toggle("service-drawer-open", serviceOpen);
    $("consoleView").classList.toggle("operation-drawer-open", operationOpen);
    $("serviceDrawerToggle").setAttribute("aria-expanded", String(serviceOpen));
    $("operationDrawerToggle").setAttribute("aria-expanded", String(operationOpen));
    $("operationDialog").classList.toggle("hidden", !operationOpen);
    $("operationDialog").hidden = !operationOpen;
    $("drawerBackdrop").hidden = !(serviceOpen || operationOpen || state.localPanel);
}

/**
 * 检查登录态
 */
async function checkLogin() {
    state.signingKey = readSigningKey();
    const response = await apiFetch("/me", {sign: false});
    if (!response.ok) {
        showLogin();
        return;
    }
    const me = await response.json();
    if (me.authenticated && state.signingKey) {
        $("usernameLabel").textContent = me.username || "admin";
        $("userAvatar").textContent = (me.username || "A").slice(0, 1).toUpperCase();
        $("modeLabel").textContent = me.mode || "FULL";
        showConsole();
        await loadCatalog();
        return;
    }
    if (me.authenticated) {
        $("loginMessage").textContent = "登录签名密钥已失效，请重新输入密码";
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
    const username = $("username").value || "admin";
    const password = $("password").value;
    try {
        const proof = await buildLoginProof(username, password);
        const response = await apiFetch("/login", {
            method: "POST",
            sign: false,
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                username,
                challengeId: proof.challenge.challengeId,
                timestamp: proof.timestamp,
                proof: proof.proof,
            }),
        });
        if (!response.ok) {
            $("loginMessage").textContent = "账号或密码错误";
            return;
        }
        state.signingKey = proof.requestSigningKey;
        writeSigningKey(state.signingKey);
        $("password").value = "";
        $("usernameLabel").textContent = username;
        $("userAvatar").textContent = (username || "A").slice(0, 1).toUpperCase();
        showConsole();
        await loadCatalog();
    } catch (error) {
        $("loginMessage").textContent = error.message || "登录失败";
    }
}

/**
 * 退出登录
 */
async function logout() {
    try {
        await apiFetch("/logout", {method: "POST"});
    } finally {
        state.signingKey = "";
        writeSigningKey("");
    }
    showLogin();
}

/**
 * 调用控制台 API
 *
 * @param {string} path API 路径
 * @param {object} options 请求配置
 * @returns {Promise<Response>} 返回响应
 */
async function apiFetch(path, options = {}) {
    const method = (options.method || "GET").toUpperCase();
    const headers = {...(options.headers || {})};
    const body = options.body || "";
    if (options.sign !== false) {
        await signConsoleRequest(headers, method, path, body);
    }
    const response = await fetch(`${API_BASE}${path}`, {
        method,
        credentials: "include",
        headers,
        body: body || undefined,
    });
    if (response.status === 401 && options.sign !== false) {
        state.signingKey = "";
        writeSigningKey("");
        showLogin();
    }
    return response;
}

/**
 * 构造登录挑战证明
 *
 * @param {string} username 用户名
 * @param {string} password 密码
 * @returns {Promise<object>} 返回登录证明
 */
async function buildLoginProof(username, password) {
    if (!window.crypto?.subtle) {
        throw new Error("当前浏览器环境不支持安全登录，请使用 HTTPS 或 localhost");
    }
    const challengeResponse = await apiFetch(`/login-challenge?username=${encodeURIComponent(username)}`, {sign: false});
    if (!challengeResponse.ok) {
        throw new Error("登录挑战创建失败");
    }
    const challenge = await challengeResponse.json();
    const timestamp = Date.now();
    const passwordDigest = await sha256Bytes(password || "");
    const payload = loginProofPayload(challenge, username, timestamp);
    const proof = bytesToHex(await hmacSha256Bytes(passwordDigest, payload));
    const requestSigningKey = base64UrlEncode(await hmacSha256Bytes(passwordDigest, `openapi-console-request-signing\n${payload}`));
    return {challenge, timestamp, proof, requestSigningKey};
}

/**
 * 签名控制台 API 请求
 *
 * @param {object} headers 请求头
 * @param {string} method HTTP 方法
 * @param {string} path API 路径
 * @param {string} body 请求体
 */
async function signConsoleRequest(headers, method, path, body) {
    state.signingKey = state.signingKey || readSigningKey();
    if (!state.signingKey) {
        throw new Error("登录签名密钥已失效，请重新登录");
    }
    const timestamp = String(Date.now());
    const nonce = randomToken(16);
    const url = new URL(`${API_BASE}${path}`, location.origin);
    const canonical = [
        method.toUpperCase(),
        `${url.pathname}${url.search}`,
        timestamp,
        nonce,
        await sha256Hex(body || ""),
    ].join("\n");
    headers["X-OpenAPI-Console-Timestamp"] = timestamp;
    headers["X-OpenAPI-Console-Nonce"] = nonce;
    headers["X-OpenAPI-Console-Signature"] = bytesToHex(await hmacSha256Bytes(base64UrlDecode(state.signingKey), canonical));
}

/**
 * 构造登录证明载荷
 *
 * @param {object} challenge 登录挑战
 * @param {string} username 用户名
 * @param {number} timestamp 时间戳
 * @returns {string} 返回证明载荷
 */
function loginProofPayload(challenge, username, timestamp) {
    return `${challenge.challengeId}\n${challenge.nonce}\n${username}\n${timestamp}`;
}

/**
 * 读取控制台请求签名密钥
 *
 * @returns {string} 返回签名密钥
 */
function readSigningKey() {
    try {
        return sessionStorage.getItem(SIGNING_KEY_STORAGE) || "";
    } catch (error) {
        return "";
    }
}

/**
 * 写入控制台请求签名密钥
 *
 * @param {string} value 签名密钥
 */
function writeSigningKey(value) {
    try {
        if (value) {
            sessionStorage.setItem(SIGNING_KEY_STORAGE, value);
        } else {
            sessionStorage.removeItem(SIGNING_KEY_STORAGE);
        }
    } catch (error) {
        // 忽略浏览器隐私模式下的 sessionStorage 写入失败。
    }
}

/**
 * 读取 JSON 响应，失败时抛出错误
 *
 * @param {Response} response 响应对象
 * @returns {Promise<object>} 返回 JSON
 */
async function readJsonResult(response) {
    const text = await response.text();
    let result = {};
    if (text) {
        result = JSON.parse(text);
    }
    if (!response.ok) {
        throw new Error(result.message || "控制台请求失败");
    }
    return result;
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
    const response = await apiFetch("/catalog");
    if (!response.ok) {
        $("serviceList").innerHTML = `<div class="empty-state">服务目录加载失败，请检查控制台配置</div>`;
        disableWorkspace(true);
        return;
    }
    state.catalog = await response.json();
    state.specs = {};
    state.serviceOperations = {};
    state.operationCounts = {};
    $("title").textContent = state.catalog.title || "FamilyAiButler";
    $("environment").textContent = environmentName(state.catalog.environment);
    $("modeLabel").textContent = state.catalog.mode || "FULL";
    renderServices();
    disableWorkspace(!state.catalog.services.length);
    if (!state.catalog.services.length) {
        $("serviceList").innerHTML = `<div class="empty-state">暂无服务配置，请检查 egon.openapi.console.services</div>`;
        return;
    }
    state.service = null;
    state.spec = null;
    state.operations = [];
    state.operation = null;
    $("currentServiceName").textContent = "请选择服务";
    $("currentServiceTag").textContent = "服务";
    renderCurrentOperationList();
    renderOperationDetail();
    hydrateServiceCounts();
}

/**
 * 后台刷新服务接口数量
 */
async function hydrateServiceCounts() {
    const catalog = state.catalog;
    await Promise.all((catalog?.services || []).map(async (service) => {
        try {
            await fetchServiceSpec(service);
        } catch (error) {
            state.operationCounts[service.id] = 0;
        } finally {
            if (state.catalog === catalog) {
                renderServices();
                if (state.service?.id === service.id) {
                    renderCurrentOperationList();
                }
            }
        }
    }));
    if (state.catalog === catalog) {
        renderServices();
        renderCurrentOperationList();
    }
}

/**
 * 加载服务 OpenAPI
 *
 * @param {object} service 服务条目
 * @param {string} operationKey 接口键
 * @param {object} snapshot 请求快照
 */
async function loadService(service, operationKey, snapshot) {
    const previousServiceId = state.service?.id;
    state.service = service;
    if (previousServiceId !== service.id) {
        $("operationSearch").value = "";
    }
    renderServices();
    $("currentServiceName").textContent = service.name || service.id;
    $("currentServiceTag").textContent = service.group || "服务";
    try {
        state.spec = await fetchServiceSpec(service);
    } catch (error) {
        state.spec = null;
        state.operations = [];
        state.operation = null;
        $("operationServiceName").textContent = service.name || service.id;
        $("operationSearch").disabled = true;
        $("currentOperationList").innerHTML = `<div class="empty-state">OpenAPI JSON 加载失败</div>`;
        $("operationCount").textContent = "0";
        renderOperationDetail();
        state.serviceDrawerOpen = false;
        toggleOperationDrawer(true);
        return;
    }
    state.operations = state.serviceOperations[service.id] || [];
    state.operation = operationKey
        ? state.operations.find((item) => item.key === operationKey) || state.operations[0] || null
        : null;
    renderServices();
    renderCurrentOperationList();
    try {
        renderOperationDetail(snapshot);
        state.serviceDrawerOpen = false;
        if (operationKey) {
            toggleOperationDrawer(false);
        } else {
            toggleOperationDrawer(true);
        }
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
    const response = await apiFetch(`/openapi/${encodeURIComponent(service.id)}`);
    if (!response.ok) {
        let message = "OpenAPI JSON 加载失败";
        try {
            const payload = await response.json();
            message = payload.message || message;
        } catch (error) {
            message = response.statusText || message;
        }
        throw new Error(message);
    }
    const spec = normalizeOpenApiSpec(await response.json());
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
 * 标准化 OpenAPI JSON
 *
 * @param {object} spec OpenAPI JSON
 * @returns {object} 返回有效 OpenAPI JSON
 */
function normalizeOpenApiSpec(spec) {
    if (!spec || typeof spec !== "object" || Array.isArray(spec) || !spec.paths || typeof spec.paths !== "object" || Array.isArray(spec.paths)) {
        throw new Error("OpenAPI JSON 不是有效规范对象");
    }
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
    const keyword = $("serviceSearch").value.trim().toLowerCase();
    const services = state.catalog.services.filter((service) => {
        if (!keyword) {
            return true;
        }
        return `${service.name || ""} ${service.id || ""} ${service.group || ""}`.toLowerCase().includes(keyword);
    });
    if (!services.length) {
        $("serviceList").innerHTML = `<div class="empty-state">暂无匹配服务</div>`;
        return;
    }
    services.forEach((service) => {
        const button = document.createElement("button");
        const count = state.operationCounts[service.id] ?? "-";
        button.className = `service-item${state.service?.id === service.id ? " active" : ""}`;
        button.type = "button";
        if (state.service?.id === service.id) {
            button.setAttribute("aria-current", "true");
        }
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
    const keyword = $("operationSearch").value.trim();
    const operations = specTools.visibleOperationsForService(state.operations, keyword);
    $("operationServiceName").textContent = state.service ? (state.service.name || state.service.id) : "请选择服务";
    $("operationSearch").disabled = !state.service || !state.operations.length;
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
    button.type = "button";
    if (state.operation?.key === item.key) {
        button.setAttribute("aria-current", "true");
    }
    button.innerHTML = `
    <span class="operation-main">
      <span class="method-pill method-${escapeHtml(item.method.toLowerCase())}">${escapeHtml(item.method)}</span>
      <span class="operation-path">${escapeHtml(item.path)}</span>
    </span>
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
    renderOperationDetail();
    toggleOperationDrawer(false);
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
        renderOperationContext();
        $("methodSelect").value = "GET";
        $("requestPath").value = "";
        $("pathInput").value = "{}";
        $("queryInput").value = "{}";
        $("headersInput").value = "{}";
        $("bodyInput").value = "";
        $("preRequestInput").value = "";
        $("testsInput").value = "";
        $("responseOutput").textContent = "请选择服务和接口";
        $("previewOutput").textContent = "";
        $("responseHeadersOutput").textContent = "";
        $("cookiesOutput").textContent = "";
        $("logsOutput").textContent = "";
        $("curlOutput").textContent = "";
        $("codeOutput").textContent = "";
        clearResponseAlert();
        setStatusPills();
        return;
    }
    disableWorkspace(false);
    renderOperationContext();
    $("methodSelect").value = state.operation.method;
    $("requestPath").value = state.operation.path;
    fillDefaults();
    if (snapshot) {
        applyRequestSnapshot(snapshot);
    }
    setStatusPills();
    $("responseOutput").textContent = "";
    $("previewOutput").textContent = "";
    $("responseHeadersOutput").textContent = "";
    $("cookiesOutput").textContent = "";
    $("logsOutput").textContent = "";
    $("curlOutput").textContent = "";
    $("codeOutput").textContent = "";
    clearResponseAlert();
    $("loadResult").classList.add("hidden");
    renderFavoriteState();
}

/**
 * 渲染当前接口上下文
 */
function renderOperationContext() {
    if (!state.operation) {
        $("currentOperationName").textContent = "请选择接口";
        $("currentOperationMeta").textContent = "GET /";
        return;
    }
    $("currentOperationName").textContent = state.operation.summary || state.operation.operation?.operationId || state.operation.path;
    $("currentOperationMeta").textContent = `${state.operation.method} ${state.operation.path}`;
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
    $("preRequestInput").value = "";
    $("testsInput").value = "";
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
    syncTabState();
}

/**
 * 激活响应 Tab
 *
 * @param {string} tabName Tab 名称
 */
function activateResponseTab(tabName) {
    state.responseTab = tabName;
    syncTabState();
}

/**
 * 激活请求产物 Tab
 *
 * @param {string} tabName Tab 名称
 */
function activateArtifactTab(tabName) {
    state.artifactTab = tabName;
    syncTabState();
}

/**
 * 同步 Tab 可访问状态
 */
function syncTabState() {
    syncTabGroup(".tab", ".tab-panel", "tab", state.activeTab);
    syncTabGroup(".response-tab", ".response-panel", "responseTab", state.responseTab);
    syncTabGroup(".artifact-tab", ".artifact-panel", "artifactTab", state.artifactTab);
}

/**
 * 同步单组 Tab 和面板状态
 *
 * @param {string} tabSelector Tab 选择器
 * @param {string} panelSelector 面板选择器
 * @param {string} dataKey 数据字段
 * @param {string} activeName 当前激活名称
 */
function syncTabGroup(tabSelector, panelSelector, dataKey, activeName) {
    document.querySelectorAll(tabSelector).forEach((tab) => {
        const active = tab.dataset[dataKey] === activeName;
        tab.classList.toggle("active", active);
        tab.setAttribute("aria-selected", String(active));
    });
    document.querySelectorAll(panelSelector).forEach((panel) => {
        const active = panel.id === `${activeName}Panel`;
        panel.classList.toggle("active", active);
        panel.hidden = !active;
    });
}

/**
 * 美化当前编辑器
 */
function formatActiveEditor() {
    const editor = activeJsonEditor();
    if (!editor) {
        return;
    }
    if (["preRequest", "tests"].includes(state.activeTab)) {
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
        editor.value = ["preRequest", "tests"].includes(state.activeTab) ? "" : "{}";
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
    if (!["preRequest", "tests"].includes(state.activeTab)) {
        formatActiveEditor();
    }
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
    if (state.activeTab === "preRequest") {
        return $("preRequestInput");
    }
    if (state.activeTab === "tests") {
        return $("testsInput");
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
    if (!state.service || !state.operation || state.sendingRequest) {
        return;
    }
    let payload;
    try {
        payload = buildExecutePayload();
    } catch (error) {
        setResponseError(error.message);
        return;
    }
    state.lastRequestPayload = payload;
    setActionLoading("sendRequest", true, "发送中");
    try {
        const requestBody = JSON.stringify(payload);
        const response = await apiFetch("/execute", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: requestBody,
        });
        const result = await readJsonResult(response);
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
    } catch (error) {
        setResponseError(error.message);
    } finally {
        setActionLoading("sendRequest", false);
    }
}

/**
 * 执行轻量压测
 */
async function runLoadTest() {
    if (!state.service || !state.operation || state.runningLoadTest) {
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
    state.lastRequestPayload = executeRequest;
    setActionLoading("runLoadTest", true, "压测中");
    try {
        const requestBody = JSON.stringify(payload);
        const response = await apiFetch("/load-test", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: requestBody,
        });
        const result = await readJsonResult(response);
        state.lastCurl = JSON.stringify(result, null, 2);
        state.lastCode = buildFetchExample(executeRequest);
        $("curlOutput").textContent = state.lastCurl;
        $("codeOutput").textContent = state.lastCode;
        renderLoadResult(result);
    } catch (error) {
        setResponseError(error.message);
    } finally {
        setActionLoading("runLoadTest", false);
    }
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
        preRequest: $("preRequestInput").value,
        tests: $("testsInput").value,
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
    $("preRequestInput").value = snapshot.preRequest || "";
    $("testsInput").value = snapshot.tests || "";
    refreshHeaderCount();
}

/**
 * 渲染调试结果
 *
 * @param {object} result 调试结果
 */
function renderExecuteResult(result) {
    const body = result.body || "";
    const responseHeaders = result.headers || {};
    clearResponseAlert();
    state.lastResponseBody = prettyText(body);
    state.lastResponseHeaders = responseHeaders;
    state.lastResponseLog = {
        service: state.service?.name || state.service?.id || "",
        operation: state.operation ? `${state.operation.method} ${state.operation.path}` : "",
        status: result.status || 0,
        durationMillis: result.durationMillis || 0,
        size: specTools.byteLength(body),
        completedAt: new Date().toISOString(),
    };
    state.lastCurl = result.curl || "";
    state.lastCode = buildFetchExample(state.lastRequestPayload);
    $("responseOutput").textContent = state.lastResponseBody;
    $("previewOutput").textContent = previewText(body, responseHeaders);
    $("responseHeadersOutput").textContent = JSON.stringify(responseHeaders, null, 2);
    $("cookiesOutput").textContent = JSON.stringify(extractCookies(responseHeaders), null, 2);
    $("logsOutput").textContent = JSON.stringify(state.lastResponseLog, null, 2);
    $("curlOutput").textContent = state.lastCurl;
    $("codeOutput").textContent = state.lastCode;
    $("loadResult").classList.add("hidden");
    setStatusPills(result.status, result.durationMillis, specTools.byteLength(body));
}

/**
 * 构造响应预览文本
 *
 * @param {string} body 响应体
 * @param {object} headers 响应头
 * @returns {string} 返回预览文本
 */
function previewText(body, headers) {
    const contentType = Object.entries(headers || {}).find(([key]) => key.toLowerCase() === "content-type")?.[1] || "";
    if (contentType.includes("text/html")) {
        return body || "";
    }
    return prettyText(body);
}

/**
 * 提取响应 Cookie
 *
 * @param {object} headers 响应头
 * @returns {Array<string>} 返回 Cookie 集合
 */
function extractCookies(headers) {
    return Object.entries(headers || {})
        .filter(([key]) => key.toLowerCase() === "set-cookie")
        .flatMap(([, value]) => String(value || "").split(/,(?=\s*[^;,\s]+=)/g).map((item) => item.trim()).filter(Boolean));
}

/**
 * 构造 fetch 代码示例
 *
 * @param {object} payload 调试请求
 * @returns {string} 返回代码示例
 */
function buildFetchExample(payload) {
    if (!payload) {
        return "";
    }
    const query = new URLSearchParams(payload.query || {}).toString();
    const path = `${payload.path || "/"}${query ? `?${query}` : ""}`;
    return `const response = await fetch(${JSON.stringify(path)}, {
  method: ${JSON.stringify(payload.method || "GET")},
  headers: ${JSON.stringify(payload.headers || {}, null, 2)},
  body: ${payload.body ? JSON.stringify(payload.body) : "undefined"},
});

const text = await response.text();
console.log(response.status, text);`;
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
    renderLocalPanel("presets", true);
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
        renderLocalPanel("favorites", true);
    }
}

/**
 * 渲染当前收藏状态
 */
function renderFavoriteState() {
    $("favoriteOperation").textContent = localStore.isFavorite(currentOperationKey()) ? "已收藏" : "收藏接口";
}

/**
 * 渲染本地数据徽标
 */
function renderLocalBadges() {
    const local = localStore.loadState();
    if ($("historyCount")) {
        $("historyCount").textContent = local.history.length;
    }
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
    if (!state.localPanel) {
        closeLocalPanel();
        return;
    }
    state.operationDrawerOpen = false;
    const panelButton = $(`show${type.slice(0, 1).toUpperCase()}${type.slice(1)}`);
    if (panelButton) {
        panelButton.classList.add("active");
    }
    $("localPanel").classList.remove("hidden");
    $("localPanel").hidden = false;
    $("localPanel").innerHTML = `
      <div class="local-panel-head">
        <div>
          <strong id="localPanelTitle">${escapeHtml(localPanelTitle(type))}</strong>
          <small>${escapeHtml(localPanelHint(type))}</small>
        </div>
        <button id="closeLocalPanel" class="icon-button" type="button" title="关闭">×</button>
      </div>
      <div id="localPanelBody" class="local-panel-body"></div>
    `;
    $("closeLocalPanel").addEventListener("click", closeLocalPanel);
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
        $("localPanelBody").innerHTML = `
      <div class="local-list">
        <button class="local-item" type="button" id="panelExportData"><strong>导出本地数据</strong><small>history / favorites / presets / settings</small></button>
        <button class="local-item" type="button" id="panelImportData"><strong>导入本地数据</strong><small>openapi-console-local-data.json</small></button>
        <button class="local-item danger-item" type="button" id="panelClearData"><strong>清除浏览器缓存</strong><small>清除历史、收藏、预设、设置和登录签名</small></button>
      </div>
    `;
        $("panelExportData").addEventListener("click", exportLocalData);
        $("panelImportData").addEventListener("click", () => $("localDataFile").click());
        $("panelClearData").addEventListener("click", clearBrowserStorage);
    }
    syncDrawerState();
}

/**
 * 关闭本地数据弹窗
 */
function closeLocalPanel() {
    state.localPanel = "";
    document.querySelectorAll(".side-nav button").forEach((button) => button.classList.remove("active"));
    $("localPanel").classList.add("hidden");
    $("localPanel").hidden = true;
    $("localPanel").innerHTML = "";
    $("drawerBackdrop").hidden = !state.operationDrawerOpen && !state.serviceDrawerOpen;
}

/**
 * 获取本地数据弹窗标题
 *
 * @param {string} type 面板类型
 * @returns {string} 返回标题
 */
function localPanelTitle(type) {
    if (type === "favorites") {
        return "收藏接口";
    }
    if (type === "presets") {
        return "接口预设";
    }
    if (type === "settings") {
        return "本地数据";
    }
    return "接口历史";
}

/**
 * 获取本地数据弹窗说明
 *
 * @param {string} type 面板类型
 * @returns {string} 返回说明
 */
function localPanelHint(type) {
    if (type === "favorites") {
        return "点击收藏项后自动切换到对应服务接口";
    }
    if (type === "presets") {
        return "点击预设后自动选中接口并回填请求数据";
    }
    if (type === "settings") {
        return "导入、导出或清除当前浏览器的控制台数据";
    }
    return "历史数据保留在本地缓存中，不再占用主导航位置";
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
        $("localPanelBody").innerHTML = `<div class="empty-state">${escapeHtml(emptyText)}</div>`;
        return;
    }
    $("localPanelBody").innerHTML = `<div class="local-list"></div>`;
    const list = $("localPanelBody").querySelector(".local-list");
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
async function restoreHistory(item) {
    await loadStoredOperation(item.serviceId, specTools.operationKey(item.serviceId, item.method, item.path), item.request);
    closeLocalPanel();
}

/**
 * 加载收藏接口
 *
 * @param {object} item 收藏条目
 */
async function loadFavorite(item) {
    await loadStoredOperation(item.serviceId, item.key);
    closeLocalPanel();
}

/**
 * 应用接口预设
 *
 * @param {object} item 预设条目
 */
async function applyPreset(item) {
    const [serviceId] = item.key.split(":");
    await loadStoredOperation(serviceId, item.key, item.request);
    closeLocalPanel();
}

/**
 * 加载本地存储中的接口
 *
 * @param {string} serviceId 服务 ID
 * @param {string} operationKey 接口键
 * @param {object} snapshot 请求快照
 */
async function loadStoredOperation(serviceId, operationKey, snapshot) {
    const service = state.catalog?.services?.find((item) => item.id === serviceId);
    if (service) {
        await loadService(service, operationKey, snapshot);
    }
}

/**
 * 导出本地数据
 */
function exportLocalData() {
    downloadText(downloadFilename("openapi-console", "json"), JSON.stringify(localStore.exportState(), null, 2), "application/json");
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
 * 清除浏览器本地缓存
 */
function clearBrowserStorage() {
    const confirmed = confirm("确认清除当前控制台的浏览器缓存？历史、收藏、预设、设置和登录签名缓存会被清除，此操作不可恢复。");
    if (!confirmed) {
        return;
    }
    localStore.clearState();
    state.localPanel = "";
    state.signingKey = "";
    writeSigningKey("");
    $("localPanel").classList.add("hidden");
    $("localPanel").hidden = true;
    $("localPanel").innerHTML = "";
    renderLocalBadges();
    renderFavoriteState();
    setResponseError("浏览器缓存已清除，请重新登录后继续调试");
    showLogin();
}

/**
 * 导出接口文档
 *
 * @param {string} format 导出格式
 */
async function exportDoc(format) {
    if (!state.service) {
        return;
    }
    const response = await apiFetch(`/export/${encodeURIComponent(state.service.id)}?format=${encodeURIComponent(format)}`);
    if (!response.ok) {
        setResponseError("接口文档导出失败");
        return;
    }
    const blob = await response.blob();
    downloadBlob(downloadFilename(serviceFilenamePrefix(state.service), format), blob);
}

/**
 * 设置请求动作的 loading 状态
 *
 * @param {string} buttonId 按钮 ID
 * @param {boolean} loading 是否 loading
 * @param {string} label loading 文案
 */
function setActionLoading(buttonId, loading, label) {
    const button = $(buttonId);
    if (buttonId === "sendRequest") {
        state.sendingRequest = loading;
    }
    if (buttonId === "runLoadTest") {
        state.runningLoadTest = loading;
    }
    if (loading) {
        button.dataset.defaultText = button.dataset.defaultText || button.textContent;
        button.textContent = label || button.textContent;
        button.disabled = true;
        button.classList.add("is-loading");
        return;
    }
    button.textContent = button.dataset.defaultText || button.textContent;
    button.classList.remove("is-loading");
    disableWorkspace(!state.operation);
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
    state.lastResponseHeaders = {};
    state.lastResponseLog = {error: message, completedAt: new Date().toISOString()};
    $("responseOutput").setAttribute("role", "alert");
    $("responseOutput").textContent = message;
    $("previewOutput").textContent = message;
    $("responseHeadersOutput").textContent = "{}";
    $("cookiesOutput").textContent = "[]";
    $("logsOutput").textContent = JSON.stringify(state.lastResponseLog, null, 2);
    setStatusPills(0, 0, 0);
}

/**
 * 清除响应错误状态
 */
function clearResponseAlert() {
    $("responseOutput").removeAttribute("role");
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
    downloadBlob(filename, blob);
}

/**
 * 下载二进制文件
 *
 * @param {string} filename 文件名
 * @param {Blob} blob 文件内容
 */
function downloadBlob(filename, blob) {
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
}

/**
 * 获取服务导出文件名前缀
 *
 * @param {object} service 服务条目
 * @returns {string} 返回文件名前缀
 */
function serviceFilenamePrefix(service) {
    return safeFilenameSlug(service?.id || service?.name || "openapi-doc", "openapi-doc");
}

/**
 * 生成带时间戳的下载文件名
 *
 * @param {string} prefix 文件名前缀
 * @param {string} extension 文件扩展名
 * @returns {string} 返回下载文件名
 */
function downloadFilename(prefix, extension) {
    const now = new Date();
    const parts = [
        String(now.getFullYear()).slice(-2),
        String(now.getMonth() + 1).padStart(2, "0"),
        String(now.getDate()).padStart(2, "0"),
        String(now.getHours()).padStart(2, "0"),
        String(now.getMinutes()).padStart(2, "0"),
    ];
    return `${safeFilenameSlug(prefix, "openapi-console")}-${parts.join("-")}.${safeFilenameSlug(extension, "dat")}`;
}

/**
 * 转换安全文件名片段
 *
 * @param {string} value 原始文本
 * @param {string} fallback 默认文本
 * @returns {string} 返回安全 slug
 */
function safeFilenameSlug(value, fallback) {
    const slug = String(value || "")
        .trim()
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, "-")
        .replace(/^-+|-+$/g, "");
    return slug || fallback;
}

/**
 * 复制文本
 *
 * @param {string} text 文本
 */
async function copyText(text) {
    if (!text) {
        return;
    }
    try {
        if (navigator.clipboard) {
            await navigator.clipboard.writeText(text);
            showToast("已复制");
            return;
        }
        const input = document.createElement("textarea");
        input.value = text;
        input.setAttribute("readonly", "true");
        input.style.position = "fixed";
        input.style.opacity = "0";
        document.body.appendChild(input);
        input.select();
        document.execCommand("copy");
        input.remove();
        showToast("已复制");
    } catch (error) {
        setResponseError("复制失败，请手动复制输出内容");
    }
}

/**
 * 显示轻量提示
 *
 * @param {string} message 提示文案
 */
function showToast(message) {
    const toast = $("toast");
    toast.textContent = message;
    toast.hidden = false;
    clearTimeout(state.toastTimer);
    state.toastTimer = window.setTimeout(() => {
        toast.hidden = true;
    }, 1200);
}

/**
 * 计算 SHA-256 十六进制摘要
 *
 * @param {string} value 原文
 * @returns {Promise<string>} 返回十六进制摘要
 */
async function sha256Hex(value) {
    return bytesToHex(await sha256Bytes(value));
}

/**
 * 计算 SHA-256 摘要字节
 *
 * @param {string} value 原文
 * @returns {Promise<Uint8Array>} 返回摘要字节
 */
async function sha256Bytes(value) {
    return new Uint8Array(await crypto.subtle.digest("SHA-256", textBytes(value)));
}

/**
 * 计算 HmacSHA256 摘要字节
 *
 * @param {Uint8Array} key 密钥
 * @param {string} value 原文
 * @returns {Promise<Uint8Array>} 返回摘要字节
 */
async function hmacSha256Bytes(key, value) {
    const cryptoKey = await crypto.subtle.importKey("raw", key, {name: "HMAC", hash: "SHA-256"}, false, ["sign"]);
    return new Uint8Array(await crypto.subtle.sign("HMAC", cryptoKey, textBytes(value)));
}

/**
 * 文本转字节
 *
 * @param {string} value 原文
 * @returns {Uint8Array} 返回字节数组
 */
function textBytes(value) {
    return new TextEncoder().encode(value);
}

/**
 * 字节数组转十六进制字符串
 *
 * @param {Uint8Array} bytes 字节数组
 * @returns {string} 返回十六进制字符串
 */
function bytesToHex(bytes) {
    return Array.from(bytes).map((item) => item.toString(16).padStart(2, "0")).join("");
}

/**
 * 字节数组转 URL 安全 Base64
 *
 * @param {Uint8Array} bytes 字节数组
 * @returns {string} 返回 URL 安全 Base64
 */
function base64UrlEncode(bytes) {
    let value = "";
    bytes.forEach((item) => {
        value += String.fromCharCode(item);
    });
    return btoa(value).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

/**
 * URL 安全 Base64 转字节数组
 *
 * @param {string} value URL 安全 Base64
 * @returns {Uint8Array} 返回字节数组
 */
function base64UrlDecode(value) {
    const normalized = value.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized.padEnd(normalized.length + ((4 - normalized.length % 4) % 4), "=");
    return Uint8Array.from(atob(padded), (item) => item.charCodeAt(0));
}

/**
 * 生成随机 URL 安全令牌
 *
 * @param {number} byteLength 字节长度
 * @returns {string} 返回随机令牌
 */
function randomToken(byteLength) {
    const bytes = new Uint8Array(byteLength);
    crypto.getRandomValues(bytes);
    return base64UrlEncode(bytes);
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
