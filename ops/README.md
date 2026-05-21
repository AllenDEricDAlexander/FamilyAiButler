# FamilyAiButler 运维手册

`ops` 目录用于管理本地开发、桌面端调试、镜像验证和 Kubernetes 部署脚本。脚本默认在仓库根目录执行，运行日志统一写入
`ops/.runtime/`。

## 脚本总览

| 脚本                                   | 作用                        | 默认行为                  |
|--------------------------------------|---------------------------|-----------------------|
| `ops/scripts/build-backend.sh`       | 构建后端 Maven 模块             | `services skip-tests` |
| `ops/scripts/backend-local.sh`       | 本机 JDK/Maven 启动后端服务       | `all dev start`       |
| `ops/scripts/frontend-web-dev.sh`    | 构建 Web 静态产物并用本地 HTTP 服务预览 | `start dev`，监听 `8081` |
| `ops/scripts/desktop-tauri-dev.sh`   | 启动 Tauri 桌面端开发模式          | `start dev`，拉起桌面客户端窗口 |
| `ops/scripts/desktop-tauri-build.sh` | 打包 Tauri 客户端              | 生成 macOS `.dmg`       |
| `ops/scripts/frontend-nginx-up.sh`   | 用本机 Nginx 验证前端静态资源和网关代理   | 监听 `80`               |
| `ops/scripts/docker-compose-up.sh`   | 启动 Docker Compose 依赖服务    | `start dev`           |
| `ops/scripts/k8s-apply.sh`           | 应用或删除 Kubernetes 资源       | `start dev`           |

## 通用命令格式

启动类脚本统一支持 `status|start|stop|restart` 和 `dev|prod`。不传参数时默认使用 `start dev`。

```bash
./ops/scripts/frontend-web-dev.sh status dev
./ops/scripts/frontend-web-dev.sh start dev
./ops/scripts/frontend-web-dev.sh restart dev
./ops/scripts/frontend-web-dev.sh stop dev
```

也可以把环境放在前面：

```bash
./ops/scripts/frontend-web-dev.sh dev status
```

## 后端构建

后端构建脚本默认只构建可运行后端服务及其依赖，并跳过测试，避免每次都把 codegen、starter 等非运行服务全部构建一遍。

```bash
./ops/scripts/build-backend.sh
```

需要指定构建范围时，可以传入模块别名；脚本会自动加 `-am` 构建依赖模块。`all` 或 `full` 才会构建整个 backend Maven reactor。

```bash
./ops/scripts/build-backend.sh services
./ops/scripts/build-backend.sh all
./ops/scripts/build-backend.sh core
./ops/scripts/build-backend.sh uaa
./ops/scripts/build-backend.sh core,uaa,gateway
./ops/scripts/build-backend.sh framework,openapi
./ops/scripts/build-backend.sh qwen-ai test
```

`services` 会构建 `family-uaa/uaa-core`、`family-core`、`family-ai/qwen-ai` 和 `family-gateway`。`uaa`、`family-uaa`
和 `be-uaa` 都会映射到当前 UAA 启动模块 `family-uaa/uaa-core`。

## 后端本机启动

后端本机启动脚本会使用 Maven reactor 启动当前后端服务模块，不会启动 Docker。

```bash
./ops/scripts/backend-local.sh all dev start
```

当前后端服务映射如下：

| 命令模块                 | Maven 模块              | Spring 应用名       | 端口      |
|----------------------|-----------------------|------------------|---------|
| `uaa` / `family-uaa` | `family-uaa/uaa-core` | `family-uaa`     | `39092` |
| `core`               | `family-core`         | `family-core`    | `39090` |
| `qwen-ai`            | `family-ai/qwen-ai`   | `family-ai-qwen` | `39091` |
| `gateway`            | `family-gateway`      | `family-gateway` | `9527`  |

`all` 会按 `uaa -> core -> qwen-ai -> gateway` 顺序启动，停止时反向处理。脚本支持使用 `uaa`、`family-uaa` 或 `be-uaa`
启动 `family-uaa/uaa-core`。

## Web 预览

只验证浏览器里的前端页面时，使用 Web 预览脚本。

```bash
./ops/scripts/frontend-web-dev.sh start dev
```

脚本会在缺少依赖时执行 `corepack pnpm install`，然后构建 `frontend/apps/web/dist` 并通过本地 HTTP 服务托管。默认访问地址：

```text
http://localhost:8081
```

该模式不是热更新开发模式。前端代码改动后，需要重新执行：

```bash
./ops/scripts/frontend-web-dev.sh restart dev
```

## 本机 Nginx 静态代理

需要验证前端静态资源和后端 gateway 代理时，使用本机 Nginx 脚本。

```bash
./ops/scripts/frontend-nginx-up.sh start dev
```

脚本会按需构建 `frontend/apps/web/dist`，以 `/api` 作为前后端分离边界前缀构建前端页面。默认访问地址：

```text
http://localhost
```

默认监听 `80` 端口。如果端口被占用，脚本会直接失败并提示换端口；本地临时验证可以显式设置 `FRONTEND_NGINX_PORT`，例如
`18080`。

默认代理目标是：

```text
http://127.0.0.1:9527
```

Nginx 只把 `/api/**` 代理到 gateway，并在 Nginx 层 rewrite 掉 `/api` 前缀。gateway 继续接收 `/base`、`/uaa`、`/ai`，再由
gateway
按服务路由和 `StripPrefix=1` 转发到后端模块。直接访问 Nginx 的 `/base`、`/uaa`、`/ai` 不会代理，避免后端路径和前端路由混在一起。

前端页面实际放在：

```text
frontend/apps/web/dist
```

脚本生成的 Nginx 配置位于：

```text
ops/.runtime/nginx/<env>/conf.d/default.conf
```

脚本读取的 Nginx 模板位于：

```text
ops/nginx/family-ai-butler.local.conf
ops/nginx/nginx.local.conf
```

需要调整端口、边界前缀或网关地址时，通过环境变量传入：

```bash
FRONTEND_NGINX_PORT=18080 NGINX_API_PREFIX=/api NGINX_GATEWAY_PROXY=http://127.0.0.1:9527 ./ops/scripts/frontend-nginx-up.sh restart dev
```

该脚本只使用本机 Nginx，不会启动 Docker。如果本机没有 `nginx`，脚本会尝试使用 Homebrew、`apt-get`、`dnf` 或 `yum`
安装；没有可用包管理器时会输出手动安装命令。默认 `80` 端口需要管理员权限，脚本会在启动或停止 Nginx 时使用 `sudo`。

## 桌面端开发

需要进入 Tauri 桌面客户端窗口调试时，使用桌面端开发脚本。

```bash
./ops/scripts/desktop-tauri-dev.sh start dev
```

这个命令会后台托管 `corepack pnpm dev:desktop`，Tauri 会执行自己的 `beforeDevCommand` 启动 Expo Web dev server，然后打开
macOS 桌面窗口 `FamilyAiButler`。它不是浏览器预览命令，正常结果是出现桌面客户端窗口。

桌面端开发模式固定依赖 `localhost:8081`。如果已经启动了 `frontend-web-dev.sh` 或其他进程占用了 `8081`，Tauri 的 Expo dev
server 会启动失败。先停止 Web 预览：

```bash
./ops/scripts/frontend-web-dev.sh stop dev
```

如果端口仍被占用，查看占用进程：

```bash
lsof -nP -iTCP:8081 -sTCP:LISTEN
```

桌面端脚本是否需要保留，取决于测试目标：

| 测试目标                             | 推荐命令                                           |
|----------------------------------|------------------------------------------------|
| 只看 Web 页面                        | `./ops/scripts/frontend-web-dev.sh start dev`  |
| 调试 Tauri 桌面窗口、菜单、系统能力、WebView 行为 | `./ops/scripts/desktop-tauri-dev.sh start dev` |
| 验证可分发桌面包                         | `./ops/scripts/desktop-tauri-build.sh`         |

因此 `desktop-tauri-dev.sh` 仍然需要保留，它服务的是桌面客户端开发调试，不替代 Web 预览脚本。

## 桌面端打包

日常生成 macOS `.dmg` 安装包：

```bash
./ops/scripts/desktop-tauri-build.sh
```

常用参数：

```bash
./ops/scripts/desktop-tauri-build.sh --skip-web
./ops/scripts/desktop-tauri-build.sh dmg
./ops/scripts/desktop-tauri-build.sh app
./ops/scripts/desktop-tauri-build.sh exe
./ops/scripts/desktop-tauri-build.sh apk
./ops/scripts/desktop-tauri-build.sh all
./ops/scripts/desktop-tauri-build.sh no-bundle
```

`dmg` 是 macOS 默认产物；`app` 只用于本机调试裸应用包。`exe` 需要在 Windows 环境执行，因为 Tauri 的 Windows 安装包依赖
Windows bundler 工具链。`apk` 需要先完成 Tauri Android 工程初始化，存在 `frontend/apps/desktop/src-tauri/gen/android`
后才能构建。

`all` 表示项目级全量客户端制品，会依次尝试生成 `dmg`、`exe`、`apk`。如果当前系统或工程条件不满足某个目标，脚本会明确输出原因并返回失败，避免误以为全部平台都已完成打包。

默认接口地址是 `http://localhost/api`，请求会先进入本机 `80` 端口 Nginx。需要切换接口地址时，通过环境变量传入：

```bash
EXPO_PUBLIC_API_BASE_URL=http://localhost/api ./ops/scripts/desktop-tauri-build.sh
```

## Docker Compose

默认只启动前端 Nginx、PostgreSQL、Redis 和 Nacos，后端服务仍建议本机 JDK/Maven 或 IDEA 启动。

```bash
cp ops/docker-compose/.env.example ops/docker-compose/.env
./ops/scripts/docker-compose-up.sh start dev
```

需要把后端服务也放进 Compose 时，启用 `backend` profile：

```bash
COMPOSE_PROFILES=backend ./ops/scripts/docker-compose-up.sh start dev
```

启用 `backend` profile 时，脚本会先执行 `build-backend.sh services skip-tests`，确认 `family-uaa`、`family-core`、
`family-ai-qwen`
和 `family-gateway` 的 jar 都存在后再构建镜像。容器内资源服务访问 UAA 使用 `http://family-uaa:39092`，前端 Nginx 默认代理
到 Compose 网络里的 `http://family-gateway:9527`。

## Kubernetes

```bash
./ops/scripts/k8s-apply.sh start dev
./ops/scripts/k8s-apply.sh status dev
./ops/scripts/k8s-apply.sh restart prod
./ops/scripts/k8s-apply.sh stop prod
```

`ops/k8s/base/secret.example.yaml` 只作为样例，默认不会被 base kustomization 直接应用。真实环境需要先创建
`family-ai-butler-secret`，或者在 overlay 中接入实际 Secret 管理方式。

## 日志和状态

运行日志位于 `ops/.runtime/`：

```bash
tail -f ops/.runtime/desktop-tauri-dev.log
tail -f ops/.runtime/frontend-web-dev.log
```

查询状态：

```bash
./ops/scripts/desktop-tauri-dev.sh status dev
./ops/scripts/frontend-web-dev.sh status dev
```

停止桌面端开发进程：

```bash
./ops/scripts/desktop-tauri-dev.sh stop dev
```

如果脚本使用 `tmux` 托管进程，也可以进入会话查看实时输出：

```bash
tmux attach -t family-ai-butler-desktop-tauri-dev
```

## 后端依赖

桌面端和 Web 前端默认请求本机 Nginx：

```text
http://localhost/api
```

Nginx 默认把 `/api/**` 代理到后端 gateway `http://127.0.0.1:9527`，并在转发前去掉 `/api` 前缀。

本机启动后端：

```bash
./ops/scripts/backend-local.sh all dev start
```

如果后端通过 IDEA 启动，至少需要保证 `family-uaa`、`family-core`、`family-ai-qwen` 已注册到 Nacos，并且 `family-gateway` 在
`9527` 端口可访问。
