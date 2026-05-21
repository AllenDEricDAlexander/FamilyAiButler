# 单模块 DDD/COLA 代码规范

本文档只描述“单个业务模块”的代码风格。它适用于一个 Spring Boot 业务模块内部，重点约束模块内的分层目录、依赖方向、命名、注释和测试。

单模块推荐结构：

```text
module
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java
    │   │   └── <basePackage>
    │   │       ├── <Module>Application.java
    │   │       ├── adapter
    │   │       ├── application
    │   │       ├── domain
    │   │       └── infrastructure
    │   └── resources
    │       ├── application.yml
    │       └── mapper
    └── test
        ├── java
        │   └── <basePackage>
        │       ├── application
        │       ├── domain
        │       ├── infrastructure
        │       └── architecture
        └── resources
```

核心依赖方向：

```text
adapter -> application -> domain <- infrastructure
```

简单理解：

```text
adapter         负责协议适配
application     负责用例编排
domain          负责业务规则
infrastructure  负责技术实现
```

---

## 1. 根包结构

单模块根包下只保留四层和启动类：

```text
<basePackage>
├── <Module>Application.java
├── adapter
├── application
├── domain
└── infrastructure
```

不要在根包下新增任何其他目录

---

## 2. adapter 层

这一层需要调用app层而不是domain的service

目录：

```text
adapter
├── controller 
│   └── XxxController.java
├── assembler
│   └── XxxAssembler.java
├── consumer
│   └── XxxConsumer.java
└── scheduler
    └── XxxScheduler.java
```

职责：

```text
接收 HTTP 请求、任务调度配置、消息中间件消费
做基础参数校验
调用 application 接口
将入参转换为 Command / Query
返回 Result / PageResult
维护 Swagger / OpenAPI 注解
```

允许依赖：

```text
application.XxxServiceI
application.dto
adapter.assembler
common.pojo.Result
common.pojo.PageResult
```

禁止：

```text
写业务规则
直接访问 Mapper / Repository
直接调用外部 SDK
直接依赖 application 实现类
直接依赖 infrastructure.gatewayimpl
新增接口直接返回持久化对象
```

Controller 只做协议适配。复杂转换放到 `adapter.assembler`。

示例：

```java

@RestController
@RequestMapping("/password")
@Validated
@Tag(name = "密码管理相关接口")
@RequiredArgsConstructor
public class PasswordViewController {
    private final PasswordViewServiceI passwordViewService;
    private final PasswordViewWebAssembler passwordViewWebAssembler;

    /**
     * 新增账号密码。
     *
     * @param dto Web 入参
     * @return 新增结果
     */
    @PostMapping
    @Operation(summary = "新增账号密码")
    public Result<Boolean> create(@RequestBody @Valid PasswordViewDTO dto) {
        return Result.success(passwordViewService.create(passwordViewWebAssembler.toCreateCommand(dto)));
    }
}
```

---

## 3. application 层

目录：

```text
application
├── manage
│   ├── XxxManage.java
│   └── impl
│       └── XxxManageImpl.java
└── executor
    ├── 
    └── 
```

这一层 做业务编排，直接调用domain.service

---

## 4. domain 层

domain层需要根据业务，做分包，不要把所有业务都堆在一起。

目录：

```text
domain
├── model
│   ├── aggregate
│   ├── entity
│   ├── valueobject
│   ├── enums
│   ├── dto
│   ├── pojo
│   └── do
├── service
│   ├── XxxService.java
│   └── impl
│       └── XxxServiceImpl.java
└── gateway
    └── XxxGateway.java
```

---

## 5. infrastructure 层

目录：

```text
infrastructure
├── configuration
├── security
├── advisor
├── utils
├── gateway
    └── impl
└── persistence
    ├── do
    ├── jpa
    │   ├── repository
    │   ├── service
    │   └── converter
    └── mp
        ├── mapper
        ├── service
        └── converter
```

职责：

```text
实现 domain gateway
访问数据库
访问缓存
调用外部 HTTP / SDK
提供 Spring 配置
完成持久化对象转换
封装技术异常
```

允许：

```text
@Configuration
@Repository
@Mapper
Spring Data JPA
MyBatis Plus
Redis / Cache
外部 SDK
HTTP Client
第三方工具
```

禁止：

```text
绕过 domain 写核心业务规则
让 domain 依赖 infrastructure 实现
把外部 SDK 对象返回给 domain
把数据库对象当成领域对象到处传
```

JPA 和 MyBatis Plus 分工：

```text
简单写模型、聚合持久化：JPA
复杂查询、分页、统计、动态 SQL：MyBatis Plus
```

JPA 配置必须避免自动刷新数据库结构：

```yaml
spring:
  jpa:
    generate-ddl: false
    open-in-view: false
    hibernate:
      ddl-auto: validate
```

不要在业务模块中使用：

```text
ddl-auto: update
ddl-auto: create
ddl-auto: create-drop
```

---

## 6. DTO、领域对象、持久化对象

### 6.1 application.dto

### 6.2 domain.model

### 6.3 persistence object

---

## 7. 注释规范

新增或修改代码必须补齐文件级、类级、方法级注释，格式沿用当前项目的 `@BelongsProject` 风格。java文件不需要，别的类型文件需要。

### 7.1 文件级注释

新增 Java 文件建议在 `package` 之前添加文件级注释：

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application
 * @FileName: PasswordViewCommandService.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 账号密码命令应用服务文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.family.application;
```

如果修改历史文件，不要为了补文件头大面积重排 imports；但新增文件要补齐。

### 7.2 类级注释

类级注释放在 import 之后、类注解之前：

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application
 * @ClassName: PasswordViewCommandService
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 账号密码命令应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class PasswordViewCommandService {
}
```

移动文件时必须同步更新：

```text
@BelongsPackage
@ClassName
@Description
```

### 7.3 方法级注释

public、protected 方法必须写 JavaDoc。private 方法如果承载独立逻辑，也要写。如果抛出异常，需要添加 `@throws`
注解。必须解释逻辑、入参、返回值、异常的含义。

```java
/**
 * 判断请求路径是否匹配白名单。
 *
 * @param pattern 白名单路径模式
 * @param path 请求路径
 * @return true 表示匹配
 */
private boolean pathMatches(String pattern, String path) {
}
```

---

## 8. 注解和依赖规范

### 8.1 Lombok

允许使用：

```text
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Slf4j
@With
@Accessors(chain = true)
```

Spring Bean 推荐构造器注入：

```java

@Service
@RequiredArgsConstructor
public class XxxCommandService {
    private final XxxGateway xxxGateway;
}
```

不要新增字段注入：

```java

@Autowired
private XxxService xxxService;
```

### 8.3 统一返回

Web 返回优先使用：

```text
Result<T>
PageResult<T>
```

不要直接返回第三方 SDK 对象、异常堆栈或数据库内部对象。

---

## 9. 方法拆分规范

不要为了形式简洁随机拆方法。

可以保持单方法的情况：

```text
方法少于 100 行
逻辑顺序清晰
职责单一
没有重复代码
调试路径直接
```

必须拆分的情况：

```text
存在重复逻辑
混入多个独立职责
分支复杂到影响阅读
需要单独测试某段逻辑
需要隔离外部系统、数据库、缓存等技术细节
```

避免深层 private 方法链。简单逻辑宁可直观地写在当前方法中，也不要拆成多层只有一两行的私有方法。

---

## 10. 测试规范

单模块推荐测试结构：

```text
src/test/java/<basePackage>
├── architecture
├── application
├── domain
└── infrastructure
```

结构测试用于防止目录退回旧形态：

```text
不允许根 controller
不允许根 service
不允许根 mapper
不允许根 po
不允许 domain.repository
不允许 infrastructure.persistence.impl
```

生成器生成的模块应包含：

```text
ArchitectureTest
DomainTest
ApplicationServiceTest
JpaRepositoryIntegrationTest
MpMapperIntegrationTest
```

涉及数据库、Redis、外部 AI SDK 的测试必须明确环境依赖。默认单元测试不要强依赖本机服务。

---

## 11. 禁止清单

单模块内禁止新增：

```text
根 controller 包
根 service 包
根 mapper 包
根 po 包
根 repository 包
根 configuration 包
根 enums 包
根 utils 包
domain.dto
domain.repository
```

编码时禁止：

```text
无意义重构
为了形式拆小方法
深层 private 方法链
字段注入
Controller 写业务规则
domain 依赖 Spring Web / Mapper / Repository
application 依赖 adapter
infrastructure 反向调用 Controller
JPA ddl-auto 使用 update / create / create-drop
新增文件缺少项目统一注释
移动文件后不更新 @BelongsPackage
```

---

## 12. 修改代码时的顺序

```text
1. 先确认要改的代码属于哪一层。
2. 看同包已有类的注释、命名、注解风格。
3. 新增类先放对目录，再写注释。
4. 只改和需求相关的文件。
5. 不顺手重构无关历史代码。
6. 涉及结构变更时补 architecture 测试。
7. 最后跑模块级测试或至少跑编译。
```

这份规范只约束单个业务模块内部的代码风格。
