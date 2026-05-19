/*
 * @BelongsProject: openapi-console
 * @BelongsPackage: static.openapi-console
 * @FileName: storage.js
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day-19:15
 * @Description: OpenAPI 调试文档控制台本地数据存储脚本
 * @Version: 1.0
 */
(function (root, factory) {
    const api = factory();
    if (typeof module === "object" && module.exports) {
        module.exports = api;
    }
    root.OpenApiConsoleStorage = api;
}(typeof window !== "undefined" ? window : globalThis, function () {
    const STORAGE_KEY = "egon.openapi.console.local.data";
    const VERSION = 1;
    const MAX_HISTORY = 100;

    /**
     * 创建控制台本地存储
     *
     * @param {Storage} storage 浏览器存储对象
     * @returns {object} 返回本地存储操作对象
     */
    function createConsoleStore(storage) {
        const targetStorage = storage || safeLocalStorage();

        /**
         * 读取本地状态
         *
         * @returns {object} 返回本地状态
         */
        function loadState() {
            if (!targetStorage) {
                return emptyState();
            }
            try {
                return normalizeState(JSON.parse(targetStorage.getItem(STORAGE_KEY) || "{}"));
            } catch (error) {
                return emptyState();
            }
        }

        /**
         * 保存本地状态
         *
         * @param {object} nextState 下一个本地状态
         * @returns {object} 返回标准化后的状态
         */
        function saveState(nextState) {
            const normalized = normalizeState(nextState);
            if (targetStorage) {
                targetStorage.setItem(STORAGE_KEY, JSON.stringify(normalized));
            }
            return normalized;
        }

        /**
         * 增加请求历史
         *
         * @param {object} entry 请求历史
         * @returns {object} 返回保存后的状态
         */
        function addHistory(entry) {
            const state = loadState();
            const record = {
                id: entry.id || createId(),
                createdAt: entry.createdAt || new Date().toISOString(),
                serviceId: entry.serviceId || "",
                serviceName: entry.serviceName || "",
                method: entry.method || "GET",
                path: entry.path || "",
                summary: entry.summary || "",
                status: Number(entry.status || 0),
                durationMillis: Number(entry.durationMillis || 0),
                request: entry.request || {},
            };
            state.history = [record, ...state.history].slice(0, MAX_HISTORY);
            return saveState(state);
        }

        /**
         * 切换收藏接口
         *
         * @param {string} key 接口键
         * @param {object} item 收藏条目
         * @returns {object} 返回保存后的状态
         */
        function toggleFavorite(key, item) {
            const state = loadState();
            const exists = state.favorites.some((favorite) => favorite.key === key);
            state.favorites = exists
                ? state.favorites.filter((favorite) => favorite.key !== key)
                : [{key, createdAt: new Date().toISOString(), ...item}, ...state.favorites];
            return saveState(state);
        }

        /**
         * 判断接口是否已收藏
         *
         * @param {string} key 接口键
         * @returns {boolean} 返回 true 表示已收藏
         */
        function isFavorite(key) {
            return loadState().favorites.some((favorite) => favorite.key === key);
        }

        /**
         * 保存接口预设
         *
         * @param {string} key 接口键
         * @param {object} preset 预设内容
         * @returns {object} 返回保存后的状态
         */
        function savePreset(key, preset) {
            const state = loadState();
            const record = {
                key,
                id: preset.id || createId(),
                name: preset.name || "未命名预设",
                updatedAt: new Date().toISOString(),
                request: preset.request || {},
            };
            state.presets = [record, ...state.presets.filter((item) => item.id !== record.id)];
            return saveState(state);
        }

        /**
         * 删除接口预设
         *
         * @param {string} presetId 预设 ID
         * @returns {object} 返回保存后的状态
         */
        function removePreset(presetId) {
            const state = loadState();
            state.presets = state.presets.filter((item) => item.id !== presetId);
            return saveState(state);
        }

        /**
         * 导出本地状态
         *
         * @returns {object} 返回导出快照
         */
        function exportState() {
            return {
                version: VERSION,
                exportedAt: new Date().toISOString(),
                data: loadState(),
            };
        }

        /**
         * 导入本地状态
         *
         * @param {object} snapshot 导入快照
         * @returns {object} 返回导入后的状态
         */
        function importState(snapshot) {
            if (!snapshot || snapshot.version !== VERSION || !snapshot.data || typeof snapshot.data !== "object") {
                throw new Error("本地数据格式不正确");
            }
            return saveState(snapshot.data);
        }

        return {
            loadState,
            saveState,
            addHistory,
            toggleFavorite,
            isFavorite,
            savePreset,
            removePreset,
            exportState,
            importState,
        };
    }

    /**
     * 创建空本地状态
     *
     * @returns {object} 返回空状态
     */
    function emptyState() {
        return {
            history: [],
            favorites: [],
            presets: [],
            settings: {},
        };
    }

    /**
     * 标准化本地状态
     *
     * @param {object} value 原始状态
     * @returns {object} 返回标准化状态
     */
    function normalizeState(value) {
        const state = value && typeof value === "object" ? value : {};
        return {
            history: Array.isArray(state.history) ? state.history : [],
            favorites: Array.isArray(state.favorites) ? state.favorites : [],
            presets: Array.isArray(state.presets) ? state.presets : [],
            settings: state.settings && typeof state.settings === "object" ? state.settings : {},
        };
    }

    /**
     * 创建本地 ID
     *
     * @returns {string} 返回本地 ID
     */
    function createId() {
        if (typeof crypto !== "undefined" && crypto.randomUUID) {
            return crypto.randomUUID();
        }
        return `local-${Date.now()}-${Math.random().toString(16).slice(2)}`;
    }

    /**
     * 安全读取 localStorage
     *
     * @returns {Storage|null} 返回 localStorage
     */
    function safeLocalStorage() {
        try {
            return typeof localStorage === "undefined" ? null : localStorage;
        } catch (error) {
            return null;
        }
    }

    return {
        createConsoleStore,
    };
}));
