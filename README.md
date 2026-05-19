# FamilyAiButler

FamilyAiButler 是一个家庭 AI 助手项目，仓库按后端、大前端、运维三大目录治理。

## 目录结构

```text
backend/
  family-common/                     # 后端公共能力
  family-core/                       # 家庭核心业务服务
  family-uaa/                        # 用户认证授权服务
  family-gateway/                    # Spring Cloud Gateway
  family-ai/
    ai-common/                       # AI 公共能力
    qwen-ai/                         # 通义千问 AI 服务
  family-cache-spring-boot-starter/  # 自定义缓存 starter

frontend/
  apps/
    web/                             # React Native Web 应用
    desktop/                         # Tauri 桌面端壳
  packages/
    app-core/                        # 跨端业务入口
    ui/                              # 跨端 UI 组件
    api-client/                      # 前端接口客户端

ops/
  docker-compose/                    # 本地 Docker Compose 和 Nginx 前端镜像
  k8s/                               # Kubernetes 基础资源和环境覆盖
  nginx/                             # 本机 Nginx 代理配置样例
  scripts/                           # 构建、启动和部署脚本
```

## 技术栈

- 后端：Java 21、Spring Boot 3.5、Spring Cloud、Spring AI、MyBatis Plus、PostgreSQL、Redis、Nacos
- 大前端：Expo、React、React Native、React Native Web、Ant Design React Native、Ant Design React
- 桌面端：Tauri v2
- 运维：Docker Compose、Kubernetes、Kustomize

## 后端模块

后端 Maven 聚合工程位于 `backend`。

```bash
cd backend
mvn clean package -DskipTests
```

主要服务名：

| 模块                  | Spring 应用名       | 端口      |
|---------------------|------------------|---------|
| `family-core`       | `family-core`    | `39090` |
| `family-uaa`        | `family-uaa`     | `39092` |
| `family-ai/qwen-ai` | `family-ai-qwen` | `39091` |
| `family-gateway`    | `family-gateway` | `9527`  |

日志目录默认使用 `${FAMILY_AI_BUTLER_LOG_PATH:./logs}/${spring.application.name}`，部署时可以通过环境变量
`FAMILY_AI_BUTLER_LOG_PATH` 统一调整。

本地 JDK 启动后端服务：

```bash
./ops/scripts/backend-local.sh
```

后端脚本支持 `all|core|uaa|qwen-ai|gateway`、`dev|prod`、`status|start|stop|restart`。不传参数时默认等价于 `all dev start`。

```bash
./ops/scripts/backend-local.sh status dev
./ops/scripts/backend-local.sh all dev start
./ops/scripts/backend-local.sh core dev restart
./ops/scripts/backend-local.sh gateway prod stop
```

脚本会按 `core -> uaa -> qwen-ai -> gateway` 顺序启动，停止时反向处理，运行日志位于 `ops/.runtime/`。该脚本只使用本机
JDK/Maven 启动应用，不会启动 Docker。

## 大前端

大前端使用 pnpm workspace 管理。Web 端基于 Expo + React Native Web，桌面端通过 Tauri 包装同一套 Web 构建产物。

如果后端通过 IDEA 启动，先启动 `family-gateway`，默认网关地址为 `http://localhost:9527`。

```bash
./ops/scripts/frontend-web-dev.sh
```

启动类脚本统一支持 `status|start|stop|restart` 和 `dev|prod`。不传参数时默认等价于 `start dev`。

```bash
./ops/scripts/frontend-web-dev.sh status dev
./ops/scripts/frontend-web-dev.sh start dev
./ops/scripts/frontend-web-dev.sh restart dev
./ops/scripts/frontend-web-dev.sh stop dev
```

脚本会在缺少依赖时执行 `corepack pnpm install`，然后按需构建 Expo Web 并后台托管 `frontend/apps/web/dist`。`dev` 默认把接口指向
`http://localhost:9527`，`prod` 默认把接口指向本机 Nginx `http://localhost:8090`，运行日志位于 `ops/.runtime/`。需要热更新开发时仍可在
`frontend` 下直接执行 `corepack pnpm dev:web`。

本地开发可以不走 Nginx，直接打开 Expo 地址测试。需要本机 Nginx 做前后端分离代理时，可以参考：

```bash
ops/nginx/family-ai-butler.local.conf
```

该配置默认监听 `http://localhost:8090`，把页面代理到 Expo `http://127.0.0.1:8081`，把 `/base`、`/uaa`、`/ai` 代理到后端网关
`http://127.0.0.1:9527`。

使用容器 Nginx 验证前后端分离镜像：

```bash
./ops/scripts/frontend-nginx-up.sh
```

默认访问地址为 `http://localhost:8080`。Nginx 托管前端静态资源，并把 `/base`、`/uaa`、`/ai` 代理到 `.env` 中的
`NGINX_API_PROXY`，默认是 `http://host.docker.internal:9527`。

桌面端开发：

```bash
./ops/scripts/desktop-tauri-dev.sh start dev
```

该命令用于调试 Tauri 桌面客户端窗口，会启动 `tauri dev`，并由 Tauri 的 `beforeDevCommand` 拉起 Expo Web dev
server。它不是浏览器预览命令；正常结果是出现 macOS 桌面窗口 `FamilyAiButler`。桌面端开发模式需要独占 `localhost:8081`
，如果已经启动了 `frontend-web-dev.sh` 或其他进程占用该端口，需要先停止 Web 预览：

```bash
./ops/scripts/frontend-web-dev.sh stop dev
lsof -nP -iTCP:8081 -sTCP:LISTEN
```

只验证 Web 页面时使用 `frontend-web-dev.sh`；需要调试 Tauri/WebView/桌面端能力时才使用 `desktop-tauri-dev.sh`。更多运维脚本说明见
`ops/README.md`。

桌面端构建：

```bash
./ops/scripts/desktop-tauri-build.sh
```

默认打 macOS `.dmg` 安装包。常用参数：

```bash
./ops/scripts/desktop-tauri-build.sh --skip-web     # 复用已有 frontend/apps/web/dist
./ops/scripts/desktop-tauri-build.sh dmg            # 生成 macOS dmg
./ops/scripts/desktop-tauri-build.sh app            # 生成 macOS app
./ops/scripts/desktop-tauri-build.sh exe            # 在 Windows 环境生成 exe 安装包
./ops/scripts/desktop-tauri-build.sh apk            # 在已初始化 Android 工程后生成 apk
./ops/scripts/desktop-tauri-build.sh all            # 尝试生成 dmg、exe、apk
./ops/scripts/desktop-tauri-build.sh --no-bundle    # 只验证 Rust/Tauri 编译
```

`exe` 需要在 Windows 上执行，`apk` 需要先完成 Tauri Android 工程初始化。macOS 本机默认只适合直接生成 `.dmg`。

## 运维

本地 Docker Compose 默认启动前端 Nginx、PostgreSQL、Redis 和 Nacos，后端服务建议按当前开发习惯通过 IDEA 启动。

```bash
cp ops/docker-compose/.env.example ops/docker-compose/.env
./ops/scripts/docker-compose-up.sh start dev
```

如果需要用 Docker 同时启动后端服务，可以手动启用 `backend` profile：

```bash
cd ops/docker-compose
docker compose --env-file .env --profile backend up -d --build
```

Kubernetes：

```bash
./ops/scripts/k8s-apply.sh start dev
./ops/scripts/k8s-apply.sh status dev
./ops/scripts/k8s-apply.sh restart prod
./ops/scripts/k8s-apply.sh stop prod
```

`ops/k8s/base/secret.example.yaml` 只作为样例，真实环境需要替换为实际 Secret 管理方式。

## 配置约定

- 目录和 Maven artifact 使用 kebab-case。
- 后端服务统一以 `family-*` 命名。
- 前端包统一以 `@family-ai-butler/*` 命名。
- 本地敏感配置使用 `.env`，仓库只保留 `.env.example`。
- 不提交构建产物、依赖目录、IDE 配置和本地日志。
