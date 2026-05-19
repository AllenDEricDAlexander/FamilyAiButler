#!/usr/bin/env node
/**
 * @BelongsProject: FamilyAiButler
 * @BelongsPackage: frontend.scripts
 * @ClassName: build-web-if-needed
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-19Day
 * @Description: 按需构建 Expo Web 产物供 Tauri 打包复用
 * @Version: 1.0
 */
import {existsSync, readdirSync, readFileSync, statSync, writeFileSync} from "node:fs";
import {resolve} from "node:path";
import {spawnSync} from "node:child_process";

const frontendRoot = resolve(import.meta.dirname, "..");
const distIndex = resolve(frontendRoot, "apps/web/dist/index.html");
const envStampFile = resolve(frontendRoot, "apps/web/dist/.family-ai-butler-build-env");
const buildApiBaseUrl = process.env.EXPO_PUBLIC_API_BASE_URL ?? "";
const sourceRoots = [
    "apps/web",
    "packages/app-core/src",
    "packages/api-client/src",
    "packages/ui/src",
    "package.json",
    "pnpm-lock.yaml",
    "pnpm-workspace.yaml",
    "tsconfig.base.json"
].map((path) => resolve(frontendRoot, path));

/**
 * 转换 Expo Web 产物为 Tauri 静态资源可加载的相对路径。
 */
function prepareTauriWebDist() {
    if (!existsSync(distIndex)) {
        return;
    }
    const rewrites = [
        {
            path: distIndex,
            replacements: [
                [/href="\/(_expo|assets|favicon\.ico)/g, 'href="./$1'],
                [/src="\/(_expo|assets)/g, 'src="./$1']
            ]
        }
    ];
    const javascriptFiles = latestFiles(resolve(frontendRoot, "apps/web/dist/_expo/static/js"), ".js");
    javascriptFiles.forEach((path) => rewrites.push({
        path,
        replacements: [
            [/"\/assets\//g, '"./assets/'],
            [/'\/assets\//g, "'./assets/"]
        ]
    }));
    rewrites.forEach(({path, replacements}) => {
        let content = readFileSync(path, "utf8");
        const originalContent = content;
        replacements.forEach(([pattern, replacement]) => {
            content = content.replace(pattern, replacement);
        });
        if (content !== originalContent) {
            writeFileSync(path, content);
        }
    });
}

/**
 * 获取目录下指定后缀文件。
 *
 * @param path 目录或文件路径
 * @param extension 文件后缀
 * @returns 文件路径列表
 */
function latestFiles(path, extension) {
    if (!existsSync(path)) {
        return [];
    }
    const stat = statSync(path);
    if (!stat.isDirectory()) {
        return path.endsWith(extension) ? [path] : [];
    }
    return readdirSync(path).flatMap((child) => latestFiles(resolve(path, child), extension));
}

if (process.env.SKIP_WEB_BUILD === "1") {
    prepareTauriWebDist();
    console.log("skip Expo Web build: SKIP_WEB_BUILD=1");
    process.exit(0);
}

/**
 * 获取路径下最新修改时间。
 *
 * @param path 文件或目录路径
 * @returns 最新修改时间
 */
function latestMtime(path) {
    if (!existsSync(path)) {
        return 0;
    }
    const stat = statSync(path);
    if (!stat.isDirectory()) {
        return stat.mtimeMs;
    }
    return readdirSync(path).reduce((latest, child) => {
        if (child === "dist" || child === ".expo" || child === "node_modules") {
            return latest;
        }
        return Math.max(latest, latestMtime(resolve(path, child)));
    }, stat.mtimeMs);
}

const distMtime = existsSync(distIndex) ? statSync(distIndex).mtimeMs : 0;
const sourceMtime = Math.max(...sourceRoots.map(latestMtime));
const lastBuildApiBaseUrl = existsSync(envStampFile) ? readFileSync(envStampFile, "utf8") : "";

if (distMtime >= sourceMtime && lastBuildApiBaseUrl === buildApiBaseUrl) {
    prepareTauriWebDist();
    console.log("skip Expo Web build: dist is up to date");
    process.exit(0);
}

const result = spawnSync("corepack", ["pnpm", "--filter", "@family-ai-butler/web", "build"], {
    cwd: frontendRoot,
    env: process.env,
    stdio: "inherit"
});

if (result.status === 0) {
    prepareTauriWebDist();
    writeFileSync(envStampFile, buildApiBaseUrl);
}

process.exit(result.status ?? 1);
