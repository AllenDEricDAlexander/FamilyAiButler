#!/usr/bin/env node
/**
 * @BelongsProject: FamilyAiButler
 * @BelongsPackage: ops.scripts
 * @ClassName: frontend-preview-server
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day
 * @Description: Web 前端静态预览和本地 gateway 代理服务
 * @Version: 1.0
 */
import {createReadStream, existsSync, statSync} from "node:fs";
import {createServer, request as httpRequest} from "node:http";
import {request as httpsRequest} from "node:https";
import {extname, isAbsolute, join, normalize, relative, resolve} from "node:path";
import {URL} from "node:url";

const options = parseArgs(process.argv.slice(2));
const webRoot = resolve(options.root ?? "frontend/apps/web/dist");
const listenPort = Number(options.port ?? "8081");
const apiPrefix = normalizePathPrefix(options.apiPrefix ?? "/api");
const gatewayUrl = new URL(options.gateway ?? "http://127.0.0.1:9527");
const mimeTypes = new Map([
    [".css", "text/css; charset=utf-8"],
    [".html", "text/html; charset=utf-8"],
    [".ico", "image/x-icon"],
    [".js", "application/javascript; charset=utf-8"],
    [".json", "application/json; charset=utf-8"],
    [".png", "image/png"],
    [".svg", "image/svg+xml; charset=utf-8"],
    [".webp", "image/webp"]
]);

if (!Number.isInteger(listenPort) || listenPort <= 0) {
    throw new Error(`Invalid listen port: ${options.port}`);
}

if (!existsSync(join(webRoot, "index.html"))) {
    throw new Error(`Web dist is missing index.html: ${webRoot}`);
}

const server = createServer((req, res) => {
    if (req.url === undefined) {
        res.writeHead(400);
        res.end("Bad Request");
        return;
    }
    const requestUrl = new URL(req.url, `http://${req.headers.host ?? "localhost"}`);
    if (isApiRequest(requestUrl.pathname)) {
        proxyGatewayRequest(req, res, requestUrl);
        return;
    }
    serveStaticFile(res, requestUrl.pathname);
});

server.on("error", (error) => {
    console.error(`frontend preview server failed: ${error.message}`);
    process.exit(1);
});

server.listen(listenPort, "127.0.0.1", () => {
    console.log(`frontend preview server started: http://localhost:${listenPort}`);
    console.log(`api proxy: ${apiPrefix} -> ${gatewayUrl.href}`);
    console.log(`web root: ${webRoot}`);
});

/**
 * 解析命令行参数。
 *
 * @param args 原始参数列表
 * @returns 参数映射
 */
function parseArgs(args) {
    const parsed = {};
    for (let index = 0; index < args.length; index += 2) {
        const key = args[index];
        const value = args[index + 1];
        if (!key?.startsWith("--") || value === undefined) {
            throw new Error(`Invalid argument near: ${key ?? ""}`);
        }
        parsed[key.slice(2).replace(/-([a-z])/g, (_, char) => char.toUpperCase())] = value;
    }
    return parsed;
}

/**
 * 规范化 API 路由前缀。
 *
 * @param prefix 原始前缀
 * @returns 带前导斜杠且不带尾斜杠的前缀
 */
function normalizePathPrefix(prefix) {
    const normalized = `/${prefix.replace(/^\/+/, "")}`.replace(/\/+$/, "");
    return normalized === "" ? "/" : normalized;
}

/**
 * 判断请求是否应该代理到后端 gateway。
 *
 * @param pathname 请求路径
 * @returns 是否为 API 请求
 */
function isApiRequest(pathname) {
    return pathname === apiPrefix || pathname.startsWith(`${apiPrefix}/`);
}

/**
 * 代理 API 请求到本地 gateway，并去掉前端边界前缀。
 *
 * @param incomingRequest 前端预览请求
 * @param incomingResponse 前端预览响应
 * @param requestUrl 请求地址
 */
function proxyGatewayRequest(incomingRequest, incomingResponse, requestUrl) {
    const gatewayPath = requestUrl.pathname.slice(apiPrefix.length) || "/";
    const targetUrl = new URL(gatewayPath + requestUrl.search, gatewayUrl);
    const requestImpl = targetUrl.protocol === "https:" ? httpsRequest : httpRequest;
    const proxyRequest = requestImpl(targetUrl, {
        method: incomingRequest.method,
        headers: {
            ...incomingRequest.headers,
            host: targetUrl.host
        }
    }, (proxyResponse) => {
        incomingResponse.writeHead(proxyResponse.statusCode ?? 502, proxyResponse.headers);
        proxyResponse.pipe(incomingResponse);
    });
    proxyRequest.on("error", (error) => {
        incomingResponse.writeHead(502, {"content-type": "text/plain; charset=utf-8"});
        incomingResponse.end(`Gateway proxy failed: ${error.message}`);
    });
    incomingRequest.pipe(proxyRequest);
}

/**
 * 托管前端静态文件，并对 SPA 路由回退到 index.html。
 *
 * @param res HTTP 响应
 * @param pathname 请求路径
 */
function serveStaticFile(res, pathname) {
    const decodedPath = decodeURIComponent(pathname);
    const normalizedPath = normalize(decodedPath).replace(/^(\.\.[/\\])+/, "");
    const candidatePath = resolve(webRoot, `.${normalizedPath}`);
    const filePath = resolveStaticFile(candidatePath);
    if (!isInsideWebRoot(filePath)) {
        res.writeHead(403);
        res.end("Forbidden");
        return;
    }
    res.writeHead(200, {"content-type": mimeTypes.get(extname(filePath)) ?? "application/octet-stream"});
    createReadStream(filePath).pipe(res);
}

/**
 * 获取实际静态文件路径。
 *
 * @param candidatePath 请求候选路径
 * @returns 静态文件路径
 */
function resolveStaticFile(candidatePath) {
    if (existsSync(candidatePath) && statSync(candidatePath).isFile()) {
        return candidatePath;
    }
    return join(webRoot, "index.html");
}

/**
 * 判断静态文件是否位于 Web 根目录下。
 *
 * @param filePath 静态文件路径
 * @returns 是否允许访问
 */
function isInsideWebRoot(filePath) {
    const relativePath = relative(webRoot, filePath);
    return relativePath === "" || (!relativePath.startsWith("..") && !isAbsolute(relativePath));
}
