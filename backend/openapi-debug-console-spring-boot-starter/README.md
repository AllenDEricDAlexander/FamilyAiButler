# OpenAPI Debug Console Spring Boot Starter

一个通用的 Spring Boot Starter，用于在业务服务生成 OpenAPI JSON，并在 Spring Cloud Gateway WebFlux 中聚合为内部接口调试控制台。

## 功能边界

- 业务模块：通过 Springdoc 生成 `/v3/api-docs`，默认补充 OpenAPI `Info` 和 `Authorization` 安全描述。
- 网关模块：聚合多个业务模块的 OpenAPI JSON，提供登录、接口浏览、自动测试数据、代理调试、轻量压力测试、Markdown/PDF 导出。
- 服务发现：网关侧可使用 `http://service-name` 形式配置文档和调试地址，Starter 会在存在 `ReactiveDiscoveryClient`
  时解析到实际服务实例。
- 状态管理：账号、密码、服务列表、环境开关、签名、导出、压测均来自配置文件，不落库。
- 环境保护：`mode=auto` 时，`prod` 默认只读，其他环境默认可调试。
- 默认保护：控制台默认关闭，业务模块只生成 OpenAPI JSON，不会暴露调试页面；只有显式配置 `egon.openapi.console.enabled=true`
  的网关模块才开放页面。

## 业务模块配置

```yaml
egon:
  openapi:
    console:
      producer:
        enabled: true
        title: Demo Service API
        description: Demo service interfaces
        version: v1
```

## 网关模块配置

```yaml
egon:
  openapi:
    console:
      enabled: true
      mode: auto
      base-path: /openapi-console
      auth:
        username: admin
        password: OpenApi@123456
      services:
        - id: demo-service
          name: Demo Service
          group: demo
          open-api-url: http://demo-service/v3/api-docs
          base-url: http://demo-service
```

默认页面地址：`/openapi-console/index.html`。
