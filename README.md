# FamilyAiButler

FamilyAiButler 是一个家庭 AI 助手项目，仓库按后端、大前端、运维三大目录治理。

## 目录结构

```text
backend/
  family-framework/                  # 后端框架公共能力
    family-common/                   # 纯公共模型、返回值和异常
    family-common-web/               # WebMVC 公共配置和异常处理
    family-common-mybatis/           # MyBatis Plus 公共扩展
    family-common-security/          # JWT 和安全公共能力
    family-log/                      # Servlet 接口日志切面
  family-core/                       # 家庭核心业务服务
  family-uaa/                            # 用户认证授权聚合模块
    uaa-facade/                      # UAA 契约模块
    uaa-core/                        # UAA 服务实现
    uaa-resource-server-spring-boot-starter/ # 资源服务授权 starter
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
  docker-compose/                    # 本地 Docker Compose 依赖服务和容器样例
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

也可以使用 ops 构建脚本。不带参数时只构建运行服务 `family-uaa/uaa-core`、`family-core`、`family-ai/qwen-ai` 和
`family-gateway`；需要全量构建整个 backend Maven reactor 时使用 `all` 或 `full`。

```bash
./ops/scripts/build-backend.sh
./ops/scripts/build-backend.sh all
./ops/scripts/build-backend.sh core
./ops/scripts/build-backend.sh uaa
./ops/scripts/build-backend.sh core,uaa,gateway
./ops/scripts/build-backend.sh qwen-ai test
```

主要服务名：

| 模块                    | Spring 应用名       | 端口      |
|-----------------------|------------------|---------|
| `family-core`         | `family-core`    | `39090` |
| `family-uaa/uaa-core` | `family-uaa`     | `39092` |
| `family-ai/qwen-ai`   | `family-ai-qwen` | `39091` |
| `family-gateway`      | `family-gateway` | `9527`  |

日志目录默认使用 `${FAMILY_AI_BUTLER_LOG_PATH:./logs}/${spring.application.name}`，部署时可以通过环境变量
`FAMILY_AI_BUTLER_LOG_PATH` 统一调整。

本地 JDK 启动后端服务：

```bash
./ops/scripts/backend-local.sh
```

后端脚本支持 `all|core|uaa|family-uaa|be-uaa|qwen-ai|gateway`、`dev|prod`、`status|start|stop|restart`。`uaa`、`family-uaa`
和 `be-uaa` 都会启动新的 `family-uaa/uaa-core` 模块；不传参数时默认等价于 `all dev start`。

```bash
./ops/scripts/backend-local.sh status dev
./ops/scripts/backend-local.sh all dev start
./ops/scripts/backend-local.sh core dev restart
./ops/scripts/backend-local.sh gateway prod stop
```

脚本会按 `uaa -> core -> qwen-ai -> gateway` 顺序启动，停止时反向处理，运行日志位于 `ops/.runtime/`。该脚本只使用本机
JDK/Maven 启动应用，不会启动 Docker。

后端运行服务验证：

```bash
mvn -f backend/pom.xml -pl family-core,family-ai/qwen-ai,family-gateway,family-uaa/uaa-core -am -DskipTests compile
mvn -f backend/pom.xml -pl family-core -am test
mvn -f backend/pom.xml -pl family-ai/qwen-ai -am test
mvn -f backend/pom.xml -pl family-gateway -am test
mvn -f backend/pom.xml -pl family-uaa/uaa-core -am test
./ops/scripts/build-backend.sh services test
./ops/scripts/backend-local.sh all dev restart
./ops/scripts/backend-local.sh all dev status
```

本地启动健康检查路径：

| 服务               | 健康检查                                           |
|------------------|------------------------------------------------|
| `family-uaa`     | `http://127.0.0.1:39092/.well-known/jwks.json` |
| `family-core`    | `http://127.0.0.1:39090/actuator/health`       |
| `family-ai-qwen` | `http://127.0.0.1:39091/actuator/health`       |
| `family-gateway` | `http://127.0.0.1:9527/actuator/health`        |

## 接口文档与调试控制台

后端接口文档由 `openapi-debug-console-spring-boot-starter` 提供。业务模块只负责生产 OpenAPI JSON，网关负责聚合服务列表、
登录认证、接口调试、测试数据生成、压测和文档导出。

adapter 层 Controller / RPC adapter 使用 doc 模块自有注解描述接口、请求、响应和 DTO/VO 字段示例。被 schema 引用的
Java 类型已按 `@DocModel`、`@DocField(description, required, example)`、字段 JavaDoc 和稳定模型名输出。adapter POJO class
和 application command/query/result 使用链式 Lombok 组合；非继承对象使用 `@Builder` 和 `@EqualsAndHashCode`
，存在继承关系且需要父类字段参与构建时才使用
`@SuperBuilder`；
facade record 保持 record 契约并补齐 record component 文档。注解和示例见
`backend/openapi-debug-console-spring-boot-starter/README.md`，adapter 层编码约束见 `backend/code_style.md`。

## 大前端

大前端使用 pnpm workspace 管理。Web 端基于 Expo + React Native Web，桌面端通过 Tauri 包装同一套 Web 构建产物。

如果后端通过 IDEA 启动，先启动 `family-gateway`，前端统一请求本机 Nginx `http://localhost/api`，由 Nginx 代理到后端网关
`http://127.0.0.1:9527`。

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

脚本会在缺少依赖时执行 `corepack pnpm install`，然后按需构建 Expo Web 并后台托管 `frontend/apps/web/dist`。`dev` 和 `prod`
默认都把接口指向本机 Nginx `http://localhost/api`，运行日志位于 `ops/.runtime/`。需要热更新开发时仍可在 `frontend` 下直接执行
`corepack pnpm dev:web`，该命令默认也请求 `http://localhost/api`。

本地开发的后端请求也走 Nginx 的 `/api` 统一前缀。需要用本机 Nginx 托管静态资源时，脚本会读取下面的 server 模板：

```bash
ops/nginx/family-ai-butler.local.conf
```

该模板会被渲染到 `ops/.runtime/nginx/<env>/conf.d/default.conf`，页面目录是 `frontend/apps/web/dist`，把 `/api/**` 代理到后端网关
`http://127.0.0.1:9527`，并在 Nginx 层去掉 `/api` 前缀后再交给 gateway。顶层 Nginx 主配置模板是
`ops/nginx/nginx.local.conf`。

使用本机 Nginx 验证前端静态资源和网关代理：

```bash
./ops/scripts/frontend-nginx-up.sh
```

默认访问地址为 `http://localhost`。脚本会按需构建 `frontend/apps/web/dist`，用本机 Nginx 托管静态资源，并把前端接口地址构建为
`/api`。Nginx 只把 `/api/**` 代理到后端网关 `NGINX_GATEWAY_PROXY`，默认是 `http://127.0.0.1:9527`，再 rewrite 掉 `/api` 后交给
gateway 处理 `/base`、`/uaa`、`/ai` 路由。该脚本不启动 Docker；如果本机没有 `nginx`，会优先使用 Homebrew、`apt-get`、`dnf`
或 `yum` 安装。`80` 端口需要管理员权限，脚本会在启动或停止 Nginx 时使用 `sudo`。

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
COMPOSE_PROFILES=backend ./ops/scripts/docker-compose-up.sh start dev
```

启用 `backend` profile 时，脚本会先构建后端运行服务 jar，再构建镜像。Compose 容器内的资源服务会通过
`http://family-uaa:39092` 访问 UAA，前端 Nginx 会代理到 `http://family-gateway:9527`。

Kubernetes：

```bash
./ops/scripts/k8s-apply.sh start dev
./ops/scripts/k8s-apply.sh status dev
./ops/scripts/k8s-apply.sh restart prod
./ops/scripts/k8s-apply.sh stop prod
```

`ops/k8s/base/secret.example.yaml` 只作为样例，默认不会被 base kustomization 直接应用。真实环境需要先创建
`family-ai-butler-secret`，或者在 overlay 中接入实际 Secret 管理方式。

## 配置约定

- 目录和 Maven artifact 使用 kebab-case。
- 后端服务名以各模块 `spring.application.name` 为准，例如 `family-uaa`、`family-core`、`family-ai-qwen`、`family-gateway`。
- 前端包统一以 `@family-ai-butler/*` 命名。
- 本地敏感配置使用 `.env`，仓库只保留 `.env.example`。
- 不提交构建产物、依赖目录、IDE 配置和本地日志。
