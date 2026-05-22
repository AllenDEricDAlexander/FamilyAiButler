# 单模块 DDD/COLA 代码规范

> 适用技术栈：Spring Boot、Spring Cloud、Spring Cloud Alibaba、Dubbo Triple、Protobuf、JPA、MyBatis
> Plus、Redis、Nacos、Sentinel、Seata。
>
> 本文档主要约束单个业务实现模块内部的代码风格。如果模块需要对外提供 RPC 能力，推荐额外拆出 `facade` / `api` / `contract`
> 契约模块。

---

## 0. 核心结论

```text
adapter -> application -> domain <- infrastructure
```

```text
adapter         负责协议适配
application     负责用例编排
domain          负责业务规则
infrastructure  负责技术实现
facade/api      负责对外契约
```

RPC 相关统一按下面这句话落地：

```text
gRPC / Triple / Protobuf 是协议和契约表达，Dubbo 是 RPC 实现和服务治理。
facade 放契约，adapter.rpc.dubbo 放 Provider，infrastructure.rpc.dubbo.client 放 Consumer。
```

不要把 Dubbo Provider 写成原生 gRPC Server，也不要把包名写成 `adapter.rpc.grpc`，除非项目真的直接使用 grpc-java 提供服务。

---

## 1. 模块结构

### 1.1 只有 HTTP / MQ / Scheduler 的单模块结构

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
        │       ├── architecture
        │       ├── application
        │       ├── domain
        │       └── infrastructure
        └── resources
```

### 1.2 需要对外提供 RPC 的推荐结构

```text
family-password
├── family-password-facade
│   ├── pom.xml
│   └── src
│       └── main
│           ├── proto
│           │   └── password_view_facade.proto
│           └── java
│               └── <facadeBasePackage>
│                   └── README.md
└── family-password-service
    ├── pom.xml
    └── src
        └── main
            ├── java
            │   └── <basePackage>
            │       ├── <Module>Application.java
            │       ├── adapter
            │       ├── application
            │       ├── domain
            │       └── infrastructure
            └── resources
                ├── application.yml
                └── mapper
```

依赖关系：

```text
调用方模块              -> family-password-facade
family-password-service -> family-password-facade
family-password-service.adapter.rpc.dubbo -> application
application             -> domain
domain                  -> 无技术实现依赖
infrastructure          -> domain
```

禁止：

```text
调用方模块 -> family-password-service
facade    -> service / application / domain / infrastructure
application / domain -> facade proto / stub
```

---

## 2. 根包结构

业务实现模块根包下只保留启动类和四层目录：

```text
<basePackage>
├── <Module>Application.java
├── adapter
├── application
├── domain
└── infrastructure
```

不要在根包下新增这些目录：

```text
controller
service
mapper
repository
po
do
dto
enums
utils
config
configuration
```

这些目录如果需要，必须归属到对应层里。

---

## 3. facade / api / contract 契约模块

### 3.1 职责

`facade` 只描述对外契约，不承载业务实现。

职责：

```text
存放 .proto 文件
生成 Dubbo Triple / Protobuf 相关接口、Stub、Request、Response
定义 RPC 服务名、入参、出参、版本含义
给其他模块作为编译期依赖
```

允许：

```text
.proto
生成后的 Protobuf Java 类
生成后的 Dubbo Triple 接口或 Stub
少量契约常量
契约说明文档
```

禁止：

```text
Controller
Dubbo Provider 实现
Application Service
Domain Model
GatewayImpl
Repository / Mapper
Spring Bean
业务规则
数据库对象
第三方 SDK 调用
```

### 3.2 proto 命名规范

文件名使用小写下划线：

```text
password_view_facade.proto
user_profile_facade.proto
```

服务名使用业务名 + `Facade`：

```text
PasswordViewFacade
UserProfileFacade
```

请求响应命名：

```text
CreatePasswordViewRequest
CreatePasswordViewResponse
GetPasswordViewRequest
GetPasswordViewResponse
PagePasswordViewRequest
PagePasswordViewResponse
```

不要在 proto 里使用这些名字：

```text
DTO
DO
PO
Entity
Aggregate
Command
Query
```

这些是 Java 分层内部概念，不应该泄漏到 RPC 契约里。

### 3.3 proto 示例

```proto
// @BelongsProject: familyaibutler
// @BelongsPackage: top.egon.familyaibutler.family.facade.passwordview
// @FileName: password_view_facade.proto
// @Author: atluofu
// @CreateTime: 2026-05-21 10:00
// @Description: 账号密码 RPC 契约
// @Version: 1.0

syntax = "proto3";

package family.passwordview.v1;

option java_multiple_files = true;
option java_package = "top.egon.familyaibutler.family.facade.passwordview";
option java_outer_classname = "PasswordViewFacadeProto";

service PasswordViewFacade {
  rpc Create(CreatePasswordViewRequest) returns (CreatePasswordViewResponse);
  rpc Get(GetPasswordViewRequest) returns (GetPasswordViewResponse);
}

message CreatePasswordViewRequest {
  string account = 1;
  string password = 2;
  string platform = 3;
  string remark = 4;
}

message CreatePasswordViewResponse {
  bool success = 1;
  string code = 2;
  string message = 3;
  int64 id = 4;
}

message GetPasswordViewRequest {
  int64 id = 1;
}

message GetPasswordViewResponse {
  bool success = 1;
  string code = 2;
  string message = 3;
  PasswordViewDTO data = 4;
}

message PasswordViewDTO {
  int64 id = 1;
  string account = 2;
  string platform = 3;
  string remark = 4;
}
```

说明：

```text
proto 中可以出现 DTO 后缀，但它只是契约数据结构，不等于 Java 分层里的 application.dto。
如果团队容易混淆，也可以改成 PasswordViewData / PasswordViewItem。
```

### 3.4 facade 依赖规则

`facade` 可以被多个模块依赖，所以必须保持轻量。

推荐：

```text
facade 只依赖 Protobuf / Dubbo 契约生成所需依赖
版本由父 pom 统一管理
不要在 facade 中引入 spring-boot-starter-web
不要在 facade 中引入 spring-boot-starter-data-jpa
不要在 facade 中引入 mybatis-plus
不要在 facade 中引入业务 service 模块
```

---

## 4. adapter 层

adapter 是入站适配层。

这一层接收外部请求，然后调用 application。它不能直接调用 domain service，也不能直接访问 infrastructure。

### 4.1 目录

```text
adapter
├── web
│   ├── PasswordViewController.java
│   ├── dto
│   │   ├── CreatePasswordViewRequestDTO.java
│   │   └── PasswordViewDetailVO.java
│   └── assembler
│       └── PasswordViewWebAssembler.java
├── rpc
│   └── dubbo
│       ├── PasswordViewDubboAdapter.java
│       └── assembler
│           └── PasswordViewDubboAssembler.java
├── mq
│   ├── consumer
│   │   └── PasswordViewCreatedConsumer.java
│   └── assembler
│       └── PasswordViewMessageAssembler.java
└── scheduler
    └── PasswordViewCleanScheduler.java
```

如果项目当前已经使用 `adapter.controller`，可以继续用；但新增多协议入口时，推荐改成 `adapter.web`，避免 HTTP、RPC、MQ 混在一起。

### 4.2 职责

```text
接收 HTTP 请求
接收 Dubbo RPC 请求
接收 MQ 消息
接收 Scheduler 调度触发
做协议层参数校验
做协议对象到 Command / Query 的转换
调用 application 接口
将 application 结果转换为 HTTP / RPC / MQ 对应响应
维护 doc 模块自有接口文档注解
维护 Dubbo Provider 注解
```

### 4.3 允许依赖

```text
application.manage.XxxManage
application.command
application.query
application.dto / application.response
adapter 当前协议自己的 assembler
facade proto 生成类，仅限 adapter.rpc.dubbo
common.pojo.Result
common.pojo.PageResult
openapi-debug-console-spring-boot-starter 的文档注解，仅限 adapter 入站接口和 DTO / VO 文档描述
```

### 4.4 禁止

```text
写业务规则
直接访问 Mapper / Repository
直接调用外部 SDK
直接调用 Redis
直接调用 HTTP Client
直接使用 @DubboReference
直接依赖 infrastructure.gateway.impl
直接依赖 application 实现类
新增接口直接返回持久化对象
把 Web DTO 传入 application
把 Proto Request 传入 application
使用 Springdoc 或 Swagger 注解作为项目自有接口文档来源
```

### 4.5 adapter 层接口文档注解规则

HTTP Controller 和 RPC adapter 必须使用 `top.egon.openapi.console.annotation` 包下的 doc 模块自有注解描述接口文档。

要求：

```text
Controller / RPC adapter 使用 @DocService 描述 group 和 service 二级分类
接口方法使用 @DocOperation 描述 summary、description、request、response
请求参数使用 @DocRequest、@DocParameter 或方法参数上的 @DocParam
请求体使用 @DocBody
返回值使用 @DocResponse，固定表达为 dataType + wrapper
DTO / VO 字段使用 @DocField 补充 description、required、example
复杂泛型使用 DocTypeReference<T>
```

禁止：

```text
只依赖方法名、JavaDoc 或框架默认推断生成接口文档
只声明返回 wrapper，不声明真实 dataType
在一个接口方法上声明多个主返回类型
业务模块新增 Springdoc 或 Swagger 注解作为接口文档来源
```

### 4.6 Web Controller 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.adapter.web
 * @ClassName: PasswordViewController
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 账号密码 HTTP 适配器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api/password-views")
@Validated
@DocService(groupId = "core", groupName = "家庭核心服务",
        serviceId = "family-core-password", serviceName = "账号密码管理接口",
        serviceDescription = "账号密码 HTTP 适配器", protocol = DocProtocol.HTTP)
@RequiredArgsConstructor
public class PasswordViewController {

    private final PasswordViewManage passwordViewManage;
    private final PasswordViewWebAssembler passwordViewWebAssembler;

    /**
     * 新增账号密码。
     *
     * @param request Web 新增请求
     * @return 新增结果
     */
    @PostMapping
    @DocOperation(summary = "新增账号密码", description = "新增账号密码",
            request = @DocRequest(body = @DocBody(
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = CreatePasswordViewRequestDTO.class))),
            response = @DocResponse(description = "新增成功",
                    dataType = @DocDataType(kind = DocDataKind.OBJECT, type = PasswordViewCreateVO.class),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<PasswordViewCreateVO> create(@RequestBody @Valid CreatePasswordViewRequestDTO request) {
        CreatePasswordViewCommand command = passwordViewWebAssembler.toCreateCommand(request);
        PasswordViewCreateResult result = passwordViewManage.create(command);
        return Result.success(passwordViewWebAssembler.toCreateVO(result));
    }
}
```

### 4.7 Web DTO 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.adapter.web.dto
 * @ClassName: CreatePasswordViewRequestDTO
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 新增账号密码 Web 请求
 * @Version: 1.0
 */
@Data
public class CreatePasswordViewRequestDTO {

    /**
     * 登录账号。
     */
    @NotBlank(message = "登录账号不能为空")
    @DocField(description = "登录账号", required = true, example = "demo@example.com")
    private String account;

    /**
     * 登录密码。
     */
    @NotBlank(message = "登录密码不能为空")
    @DocField(description = "登录密码", required = true, example = "Demo@123456")
    private String password;

    /**
     * 所属平台。
     */
    @NotBlank(message = "所属平台不能为空")
    @DocField(description = "所属平台", required = true, example = "GitHub")
    private String platform;

    /**
     * 备注。
     */
    @DocField(description = "备注", example = "个人账号")
    private String remark;
}
```

### 4.8 Web Assembler 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.adapter.web.assembler
 * @ClassName: PasswordViewWebAssembler
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 账号密码 Web 对象转换器
 * @Version: 1.0
 */
@Component
public class PasswordViewWebAssembler {

    /**
     * 将 Web 请求转换为新增账号密码命令。
     *
     * @param request Web 请求
     * @return 新增账号密码命令
     */
    public CreatePasswordViewCommand toCreateCommand(CreatePasswordViewRequestDTO request) {
        CreatePasswordViewCommand command = new CreatePasswordViewCommand();
        command.setAccount(request.getAccount());
        command.setPassword(request.getPassword());
        command.setPlatform(request.getPlatform());
        command.setRemark(request.getRemark());
        return command;
    }

    /**
     * 将应用层新增结果转换为 Web 视图对象。
     *
     * @param result 应用层新增结果
     * @return Web 视图对象
     */
    public PasswordViewCreateVO toCreateVO(PasswordViewCreateResult result) {
        PasswordViewCreateVO vo = new PasswordViewCreateVO();
        vo.setId(result.getId());
        return vo;
    }
}
```

### 4.9 Dubbo Provider 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.adapter.rpc.dubbo
 * @ClassName: PasswordViewDubboAdapter
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 账号密码 Dubbo Triple Provider 适配器
 * @Version: 1.0
 */
@DubboService(group = "family", version = "1.0.0")
@RequiredArgsConstructor
public class PasswordViewDubboAdapter implements PasswordViewFacade {

    private final PasswordViewManage passwordViewManage;
    private final PasswordViewDubboAssembler passwordViewDubboAssembler;

    /**
     * 新增账号密码。
     *
     * @param request RPC 新增请求
     * @return RPC 新增响应
     */
    @Override
    public CreatePasswordViewResponse create(CreatePasswordViewRequest request) {
        CreatePasswordViewCommand command = passwordViewDubboAssembler.toCreateCommand(request);
        PasswordViewCreateResult result = passwordViewManage.create(command);
        return passwordViewDubboAssembler.toCreateResponse(result);
    }

    /**
     * 查询账号密码详情。
     *
     * @param request RPC 查询请求
     * @return RPC 查询响应
     */
    @Override
    public GetPasswordViewResponse get(GetPasswordViewRequest request) {
        GetPasswordViewQuery query = passwordViewDubboAssembler.toGetQuery(request);
        PasswordViewDetailResult result = passwordViewManage.get(query);
        return passwordViewDubboAssembler.toGetResponse(result);
    }
}
```

说明：

```text
PasswordViewFacade、CreatePasswordViewRequest、CreatePasswordViewResponse 来自 facade 契约模块。
PasswordViewDubboAdapter 是 Provider 实现，必须放在 adapter.rpc.dubbo。
不要使用 @GrpcService、StreamObserver，除非项目明确直接接入原生 grpc-java。
```

### 4.10 Dubbo Assembler 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.adapter.rpc.dubbo.assembler
 * @ClassName: PasswordViewDubboAssembler
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 账号密码 Dubbo RPC 对象转换器
 * @Version: 1.0
 */
@Component
public class PasswordViewDubboAssembler {

    /**
     * 将 RPC 新增请求转换为应用层命令。
     *
     * @param request RPC 新增请求
     * @return 新增账号密码命令
     */
    public CreatePasswordViewCommand toCreateCommand(CreatePasswordViewRequest request) {
        CreatePasswordViewCommand command = new CreatePasswordViewCommand();
        command.setAccount(request.getAccount());
        command.setPassword(request.getPassword());
        command.setPlatform(request.getPlatform());
        command.setRemark(request.getRemark());
        return command;
    }

    /**
     * 将应用层新增结果转换为 RPC 新增响应。
     *
     * @param result 应用层新增结果
     * @return RPC 新增响应
     */
    public CreatePasswordViewResponse toCreateResponse(PasswordViewCreateResult result) {
        return CreatePasswordViewResponse.newBuilder()
                .setSuccess(true)
                .setCode("SUCCESS")
                .setMessage("新增成功")
                .setId(result.getId())
                .build();
    }

    /**
     * 将 RPC 查询请求转换为应用层查询对象。
     *
     * @param request RPC 查询请求
     * @return 查询对象
     */
    public GetPasswordViewQuery toGetQuery(GetPasswordViewRequest request) {
        GetPasswordViewQuery query = new GetPasswordViewQuery();
        query.setId(request.getId());
        return query;
    }

    /**
     * 将应用层详情转换为 RPC 查询响应。
     *
     * @param result 应用层详情结果
     * @return RPC 查询响应
     */
    public GetPasswordViewResponse toGetResponse(PasswordViewDetailResult result) {
        PasswordViewDTO data = PasswordViewDTO.newBuilder()
                .setId(result.getId())
                .setAccount(result.getAccount())
                .setPlatform(result.getPlatform())
                .setRemark(result.getRemark())
                .build();

        return GetPasswordViewResponse.newBuilder()
                .setSuccess(true)
                .setCode("SUCCESS")
                .setMessage("查询成功")
                .setData(data)
                .build();
    }
}
```

---

## 5. application 层

application 是用例编排层。

这一层负责组织一次业务用例：开启事务、调用 domain service、调用 domain gateway、组装应用层返回值。

### 5.1 目录

推荐结构：

```text
application
├── manage
│   ├── PasswordViewManage.java
│   └── impl
│       └── PasswordViewManageImpl.java
├── executor
│   ├── command
│   │   └── CreatePasswordViewCmdExe.java
│   └── query
│       └── GetPasswordViewQryExe.java
├── command
│   └── CreatePasswordViewCommand.java
├── query
│   └── GetPasswordViewQuery.java
├── result
│   ├── PasswordViewCreateResult.java
│   └── PasswordViewDetailResult.java
└── assembler
    └── PasswordViewApplicationAssembler.java
```

如果团队更喜欢 `service` 命名，也可以使用：

```text
application
├── service
│   ├── PasswordViewServiceI.java
│   └── impl
│       └── PasswordViewServiceImpl.java
```

但是一个模块内只能选择一种命名风格，不要同时出现：

```text
PasswordViewManage
PasswordViewServiceI
PasswordViewCommandService
```

建议当前项目统一用：

```text
XxxManage
XxxManageImpl
```

### 5.2 职责

```text
业务用例编排
事务控制
权限前置校验的编排
幂等校验的编排
调用 domain service
调用 domain gateway
调用 application executor
返回 application result
```

### 5.3 允许依赖

```text
domain.model
domain.service
domain.gateway
application.command
application.query
application.result
common.exception
common.pojo
Spring Transaction
```

### 5.4 禁止

```text
依赖 adapter
依赖 Controller DTO
依赖 Proto Request / Response
依赖 Dubbo Stub
依赖 Mapper
依赖 Repository 实现
依赖 infrastructure.gateway.impl
直接调用外部 SDK
直接调用 Redis
直接调用 HTTP Client
```

### 5.5 Manage 接口示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.manage
 * @ClassName: PasswordViewManage
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 账号密码应用服务接口
 * @Version: 1.0
 */
public interface PasswordViewManage {

    /**
     * 新增账号密码。
     *
     * @param command 新增账号密码命令
     * @return 新增结果
     */
    PasswordViewCreateResult create(CreatePasswordViewCommand command);

    /**
     * 查询账号密码详情。
     *
     * @param query 查询条件
     * @return 账号密码详情
     */
    PasswordViewDetailResult get(GetPasswordViewQuery query);
}
```

### 5.6 Manage 实现示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.manage.impl
 * @ClassName: PasswordViewManageImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 账号密码应用服务实现
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class PasswordViewManageImpl implements PasswordViewManage {

    private final CreatePasswordViewCmdExe createPasswordViewCmdExe;
    private final GetPasswordViewQryExe getPasswordViewQryExe;

    /**
     * 新增账号密码。
     *
     * @param command 新增账号密码命令
     * @return 新增结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PasswordViewCreateResult create(CreatePasswordViewCommand command) {
        return createPasswordViewCmdExe.execute(command);
    }

    /**
     * 查询账号密码详情。
     *
     * @param query 查询条件
     * @return 账号密码详情
     */
    @Override
    public PasswordViewDetailResult get(GetPasswordViewQuery query) {
        return getPasswordViewQryExe.execute(query);
    }
}
```

### 5.7 Command Executor 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.executor.command
 * @ClassName: CreatePasswordViewCmdExe
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 新增账号密码命令执行器
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
public class CreatePasswordViewCmdExe {

    private final PasswordViewGateway passwordViewGateway;
    private final PasswordViewDomainService passwordViewDomainService;
    private final PasswordViewApplicationAssembler passwordViewApplicationAssembler;

    /**
     * 执行新增账号密码用例。
     *
     * @param command 新增命令
     * @return 新增结果
     */
    public PasswordViewCreateResult execute(CreatePasswordViewCommand command) {
        PasswordView passwordView = passwordViewDomainService.create(
                command.getAccount(),
                command.getPassword(),
                command.getPlatform(),
                command.getRemark()
        );

        PasswordView savedPasswordView = passwordViewGateway.save(passwordView);
        return passwordViewApplicationAssembler.toCreateResult(savedPasswordView);
    }
}
```

### 5.8 Query Executor 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.executor.query
 * @ClassName: GetPasswordViewQryExe
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 查询账号密码详情执行器
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
public class GetPasswordViewQryExe {

    private final PasswordViewGateway passwordViewGateway;
    private final PasswordViewApplicationAssembler passwordViewApplicationAssembler;

    /**
     * 查询账号密码详情。
     *
     * @param query 查询条件
     * @return 账号密码详情
     */
    public PasswordViewDetailResult execute(GetPasswordViewQuery query) {
        PasswordView passwordView = passwordViewGateway.findById(query.getId())
                .orElseThrow(() -> new BizException("PASSWORD_VIEW_NOT_FOUND", "账号密码不存在"));
        return passwordViewApplicationAssembler.toDetailResult(passwordView);
    }
}
```

### 5.9 Command 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.command
 * @ClassName: CreatePasswordViewCommand
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 新增账号密码命令
 * @Version: 1.0
 */
@Data
public class CreatePasswordViewCommand {

    /**
     * 登录账号。
     */
    private String account;

    /**
     * 登录密码。
     */
    private String password;

    /**
     * 所属平台。
     */
    private String platform;

    /**
     * 备注。
     */
    private String remark;
}
```

---

## 6. domain 层

domain 是业务核心层。

domain 层需要按业务能力分包，不要把所有业务都堆在一个 `model` 或 `service` 包里。

### 6.1 推荐目录

```text
domain
├── passwordview
│   ├── model
│   │   ├── aggregate
│   │   │   └── PasswordView.java
│   │   ├── entity
│   │   ├── valueobject
│   │   │   ├── PasswordViewId.java
│   │   │   └── PasswordValue.java
│   │   ├── enums
│   │   └── event
│   ├── service
│   │   └── PasswordViewDomainService.java
│   └── gateway
│       └── PasswordViewGateway.java
└── common
    ├── exception
    └── specification
```

如果业务非常简单，也可以使用扁平结构：

```text
domain
├── model
│   ├── aggregate
│   ├── entity
│   ├── valueobject
│   ├── enums
│   └── event
├── service
└── gateway
```

但是不要在 domain 中放：

```text
dto
do
po
pojo
repository
mapper
client
```

`do` 还是 Java 关键字，不建议作为包名。持久化对象可以叫 `PasswordViewDO`，但包名应使用 `dataobject`。

### 6.2 职责

```text
承载核心业务规则
维护聚合一致性
定义领域服务
定义领域网关接口
定义值对象
定义领域事件
定义领域异常
```

### 6.3 允许依赖

```text
JDK
domain 内部对象
少量通用 common exception / assertion
```

如果项目为了 Spring 注入把 domain service 声明为 Bean，只允许使用最小化 Spring 注解，例如：

```text
@Service
@Component
```

但不推荐让 domain 依赖 Web、DB、RPC、MQ、Cache 等技术注解。

### 6.4 禁止

```text
依赖 adapter
依赖 application
依赖 infrastructure
依赖 facade proto / stub
依赖 Controller DTO
依赖 Mapper / Repository 实现
依赖 MyBatis Plus
依赖 JPA EntityManager
依赖 Dubbo
依赖 RedisTemplate
依赖 HTTP Client
```

### 6.5 Aggregate 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.passwordview.model.aggregate
 * @ClassName: PasswordView
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 账号密码聚合根
 * @Version: 1.0
 */
@Getter
public class PasswordView {

    /**
     * 账号密码 ID。
     */
    private PasswordViewId id;

    /**
     * 登录账号。
     */
    private String account;

    /**
     * 密码值。
     */
    private PasswordValue password;

    /**
     * 所属平台。
     */
    private String platform;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 创建账号密码聚合。
     *
     * @param account 登录账号
     * @param password 密码值
     * @param platform 所属平台
     * @param remark 备注
     * @return 账号密码聚合
     */
    public static PasswordView create(String account, PasswordValue password, String platform, String remark) {
        PasswordView passwordView = new PasswordView();
        passwordView.account = account;
        passwordView.password = password;
        passwordView.platform = platform;
        passwordView.remark = remark;
        passwordView.checkCreate();
        return passwordView;
    }

    /**
     * 回填账号密码 ID。
     *
     * @param id 账号密码 ID
     */
    public void initId(PasswordViewId id) {
        this.id = id;
    }

    /**
     * 校验新增账号密码规则。
     */
    private void checkCreate() {
        if (!StringUtils.hasText(account)) {
            throw new DomainException("PASSWORD_ACCOUNT_EMPTY", "登录账号不能为空");
        }
        if (!StringUtils.hasText(platform)) {
            throw new DomainException("PASSWORD_PLATFORM_EMPTY", "所属平台不能为空");
        }
        if (password == null) {
            throw new DomainException("PASSWORD_VALUE_EMPTY", "登录密码不能为空");
        }
    }
}
```

### 6.6 Value Object 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.passwordview.model.valueobject
 * @ClassName: PasswordValue
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 密码值对象
 * @Version: 1.0
 */
@Getter
public class PasswordValue {

    /**
     * 加密后的密码。
     */
    private final String encryptedValue;

    /**
     * 构造密码值对象。
     *
     * @param encryptedValue 加密后的密码
     */
    public PasswordValue(String encryptedValue) {
        if (!StringUtils.hasText(encryptedValue)) {
            throw new DomainException("PASSWORD_VALUE_EMPTY", "密码不能为空");
        }
        this.encryptedValue = encryptedValue;
    }
}
```

### 6.7 Domain Service 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.passwordview.service
 * @ClassName: PasswordViewDomainService
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 账号密码领域服务
 * @Version: 1.0
 */
@Service
public class PasswordViewDomainService {

    /**
     * 创建账号密码聚合。
     *
     * @param account 登录账号
     * @param rawPassword 原始密码
     * @param platform 所属平台
     * @param remark 备注
     * @return 账号密码聚合
     */
    public PasswordView create(String account, String rawPassword, String platform, String remark) {
        PasswordValue passwordValue = new PasswordValue(rawPassword);
        return PasswordView.create(account, passwordValue, platform, remark);
    }
}
```

说明：

```text
如果密码加密依赖外部 KMS / SDK，不要在 domain service 中直接调用。
应在 domain.gateway 定义能力接口，由 infrastructure 实现。
```

### 6.8 Gateway 接口示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.passwordview.gateway
 * @ClassName: PasswordViewGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 账号密码领域网关
 * @Version: 1.0
 */
public interface PasswordViewGateway {

    /**
     * 保存账号密码聚合。
     *
     * @param passwordView 账号密码聚合
     * @return 保存后的账号密码聚合
     */
    PasswordView save(PasswordView passwordView);

    /**
     * 根据 ID 查询账号密码聚合。
     *
     * @param id 账号密码 ID
     * @return 账号密码聚合
     */
    Optional<PasswordView> findById(Long id);
}
```

---

## 7. infrastructure 层

infrastructure 是出站技术实现层。

### 7.1 目录

```text
infrastructure
├── configuration
│   ├── DubboConfiguration.java
│   ├── JpaConfiguration.java
│   └── RedisConfiguration.java
├── security
├── advisor
├── utils
├── gateway
│   └── impl
│       ├── PasswordViewGatewayImpl.java
│       └── UserProfileGatewayImpl.java
├── persistence
│   ├── dataobject
│   │   └── PasswordViewDO.java
│   ├── jpa
│   │   ├── repository
│   │   │   └── PasswordViewJpaRepository.java
│   │   └── converter
│   │       └── PasswordViewJpaConverter.java
│   └── mp
│       ├── mapper
│       │   └── PasswordViewMapper.java
│       ├── service
│       └── converter
│           └── PasswordViewMpConverter.java
├── rpc
│   └── dubbo
│       └── client
│           ├── UserProfileDubboClient.java
│           └── assembler
│               └── UserProfileDubboAssembler.java
└── cache
    └── PasswordViewCacheRepository.java
```

### 7.2 职责

```text
实现 domain gateway
访问数据库
访问缓存
调用外部 HTTP / SDK
调用外部 Dubbo RPC
提供 Spring 配置
完成持久化对象转换
封装技术异常
```

### 7.3 允许

```text
@Configuration
@Repository
@Mapper
Spring Data JPA
MyBatis Plus
Redis / Cache
Dubbo Consumer
HTTP Client
外部 SDK
第三方工具
```

### 7.4 禁止

```text
绕过 domain 写核心业务规则
让 domain 依赖 infrastructure 实现
把外部 SDK 对象返回给 domain
把数据库对象当成领域对象到处传
让 infrastructure 调用 adapter
让 infrastructure 调用 application manage
```

### 7.5 JPA 和 MyBatis Plus 分工

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

禁止在业务模块中使用：

```text
ddl-auto: update
ddl-auto: create
ddl-auto: create-drop
```

### 7.6 Data Object 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.persistence.dataobject
 * @ClassName: PasswordViewDO
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 账号密码持久化对象
 * @Version: 1.0
 */
@Data
@Entity
@Table(name = "password_view")
public class PasswordViewDO {

    /**
     * 主键 ID。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 登录账号。
     */
    private String account;

    /**
     * 登录密码。
     */
    private String password;

    /**
     * 所属平台。
     */
    private String platform;

    /**
     * 备注。
     */
    private String remark;
}
```

### 7.7 GatewayImpl 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.gateway.impl
 * @ClassName: PasswordViewGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 账号密码领域网关实现
 * @Version: 1.0
 */
@Repository
@RequiredArgsConstructor
public class PasswordViewGatewayImpl implements PasswordViewGateway {

    private final PasswordViewJpaRepository passwordViewJpaRepository;
    private final PasswordViewJpaConverter passwordViewJpaConverter;

    /**
     * 保存账号密码聚合。
     *
     * @param passwordView 账号密码聚合
     * @return 保存后的账号密码聚合
     */
    @Override
    public PasswordView save(PasswordView passwordView) {
        PasswordViewDO dataObject = passwordViewJpaConverter.toDataObject(passwordView);
        PasswordViewDO savedDataObject = passwordViewJpaRepository.save(dataObject);
        return passwordViewJpaConverter.toDomain(savedDataObject);
    }

    /**
     * 根据 ID 查询账号密码聚合。
     *
     * @param id 账号密码 ID
     * @return 账号密码聚合
     */
    @Override
    public Optional<PasswordView> findById(Long id) {
        return passwordViewJpaRepository.findById(id)
                .map(passwordViewJpaConverter::toDomain);
    }
}
```

### 7.8 Dubbo Consumer 示例

外部 RPC 调用属于出站能力，放在 `infrastructure.rpc.dubbo.client`。

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.rpc.dubbo.client
 * @ClassName: UserProfileDubboClient
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 用户资料 Dubbo RPC 客户端
 * @Version: 1.0
 */
@Component
public class UserProfileDubboClient {

    /**
     * 用户资料 RPC 契约。
     *
     * DubboReference 只允许出现在 infrastructure.rpc.dubbo.client。
     */
    @DubboReference(group = "family", version = "1.0.0", check = false)
    private UserProfileFacade userProfileFacade;

    /**
     * 根据用户 ID 查询用户资料。
     *
     * @param userId 用户 ID
     * @return 用户资料响应
     */
    public GetUserProfileResponse getByUserId(Long userId) {
        GetUserProfileRequest request = GetUserProfileRequest.newBuilder()
                .setUserId(userId)
                .build();
        return userProfileFacade.getByUserId(request);
    }
}
```

说明：

```text
@DubboReference 是字段注入规则的唯一例外之一，因为 Dubbo 引用通常由框架代理生成。
这个例外只能放在 infrastructure.rpc.dubbo.client。
不要在 adapter、application、domain 中使用 @DubboReference。
```

### 7.9 出站 RPC GatewayImpl 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.gateway.impl
 * @ClassName: UserProfileGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 用户资料领域网关实现
 * @Version: 1.0
 */
@Repository
@RequiredArgsConstructor
public class UserProfileGatewayImpl implements UserProfileGateway {

    private final UserProfileDubboClient userProfileDubboClient;
    private final UserProfileDubboAssembler userProfileDubboAssembler;

    /**
     * 查询用户资料。
     *
     * @param userId 用户 ID
     * @return 用户资料
     */
    @Override
    public UserProfile getByUserId(Long userId) {
        GetUserProfileResponse response = userProfileDubboClient.getByUserId(userId);
        return userProfileDubboAssembler.toDomain(response);
    }
}
```

---

## 8. Spring Boot / Spring Cloud Alibaba / Dubbo 配置规范

### 8.1 application.yml 示例

```yaml
spring:
  application:
    name: family-password-service
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:public}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
      config:
        server-addr: ${NACOS_ADDR:127.0.0.1:8848}
        namespace: ${NACOS_NAMESPACE:public}
        group: ${NACOS_GROUP:DEFAULT_GROUP}
        file-extension: yaml
  jpa:
    generate-ddl: false
    open-in-view: false
    hibernate:
      ddl-auto: validate

dubbo:
  application:
    name: ${spring.application.name}
    qos-enable: false
  protocol:
    name: tri
    port: ${DUBBO_PORT:-1}
  registry:
    address: nacos://${NACOS_ADDR:127.0.0.1:8848}
    group: ${NACOS_GROUP:DEFAULT_GROUP}
  provider:
    group: family
    version: 1.0.0
    timeout: 3000
  consumer:
    group: family
    version: 1.0.0
    check: false
    timeout: 3000
```

### 8.2 配置约束

```text
Spring Boot 负责应用启动和 Bean 管理
Spring Cloud Alibaba 负责 Nacos 配置、Nacos 注册发现、Sentinel、Seata 等能力
Dubbo 负责 RPC Provider / Consumer 和 RPC 服务治理
Triple / Protobuf 负责 RPC 协议和契约表达
```

禁止：

```text
同一个远程服务既用 Dubbo 又用 OpenFeign 调用
在 application / domain 中读取 Nacos 配置完成业务判断
在 domain 中使用 Sentinel / Seata / Dubbo 注解
在 adapter 中手写 Dubbo Consumer 调用外部服务
```

Seata 全局事务如果需要使用，边界放在 application manage 实现方法上，不要放在 domain。

---

## 9. DTO、领域对象、持久化对象边界

| 类型                       | 推荐包                                     | 用途      | 能否跨层                                                            |
|--------------------------|-----------------------------------------|---------|-----------------------------------------------------------------|
| Web Request DTO          | `adapter.web.dto`                       | HTTP 入参 | 只能在 adapter.web 内使用                                             |
| Web VO                   | `adapter.web.dto`                       | HTTP 出参 | 只能在 adapter.web 内使用                                             |
| Proto Request / Response | `facade` 生成包                            | RPC 契约  | 只能在 facade、adapter.rpc.dubbo、infrastructure.rpc.dubbo.client 使用 |
| Command                  | `application.command`                   | 写操作用例入参 | adapter 可转换后传入 application                                      |
| Query                    | `application.query`                     | 读操作用例入参 | adapter 可转换后传入 application                                      |
| Result                   | `application.result`                    | 应用层返回值  | adapter 可读取并转换                                                  |
| Aggregate / Entity / VO  | `domain.*.model`                        | 业务模型    | application、infrastructure 可依赖                                  |
| Gateway                  | `domain.*.gateway`                      | 出站能力接口  | application 可依赖，infrastructure 实现                               |
| DO                       | `infrastructure.persistence.dataobject` | 数据库对象   | 只能在 infrastructure 内使用                                          |
| Mapper / Repository      | `infrastructure.persistence.*`          | 数据访问    | 只能在 infrastructure 内使用                                          |

禁止：

```text
Controller DTO -> application
Proto Request -> application
DO -> domain
DO -> adapter
Mapper -> application
Repository 实现 -> application
Aggregate -> HTTP 直接返回
Aggregate -> RPC 直接返回
```

---

## 10. 命名规范

### 10.1 adapter 命名

```text
HTTP Controller       XxxController
Web 入参              CreateXxxRequestDTO
Web 出参              XxxVO
Web 转换器            XxxWebAssembler
Dubbo Provider        XxxDubboAdapter
Dubbo 转换器          XxxDubboAssembler
MQ Consumer           XxxConsumer
Scheduler             XxxScheduler
```

### 10.2 application 命名

```text
应用服务接口          XxxManage
应用服务实现          XxxManageImpl
命令                  CreateXxxCommand / UpdateXxxCommand / DeleteXxxCommand
查询                  GetXxxQuery / PageXxxQuery
命令执行器            CreateXxxCmdExe
查询执行器            GetXxxQryExe
应用层返回            XxxResult / XxxDetailResult / XxxCreateResult
应用层转换器          XxxApplicationAssembler
```

### 10.3 domain 命名

```text
聚合根                Xxx
实体                  XxxEntity
值对象                XxxValue / XxxId
领域服务              XxxDomainService
领域网关              XxxGateway
领域事件              XxxCreatedEvent
领域异常              XxxDomainException
```

### 10.4 infrastructure 命名

```text
持久化对象            XxxDO
JPA Repository        XxxJpaRepository
MyBatis Mapper        XxxMapper
持久化转换器          XxxJpaConverter / XxxMpConverter
网关实现              XxxGatewayImpl
Dubbo Consumer Client XxxDubboClient
HTTP Client           XxxHttpClient
缓存仓储              XxxCacheRepository
```

### 10.5 facade 命名

```text
契约服务              XxxFacade
RPC 请求              CreateXxxRequest
RPC 响应              CreateXxxResponse
proto 文件            xxx_facade.proto
proto package         business.xxx.v1
```

---

## 11. 注释规范

新增或修改代码必须补齐类级、方法级注释。文件级注释沿用当前项目的 `@BelongsProject` 风格。

### 11.1 文件级注释

新增 Java 文件建议在 `package` 之前添加文件级注释：

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.manage
 * @FileName: PasswordViewManage.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-10:00
 * @Description: 账号密码应用服务接口文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.family.application.manage;
```

如果修改历史文件，不要为了补文件头大面积重排 imports；但新增文件要补齐。

### 11.2 类级注释

类级注释放在 import 之后、类注解之前：

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.manage
 * @ClassName: PasswordViewManage
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 账号密码应用服务接口
 * @Version: 1.0
 */
public interface PasswordViewManage {
}
```

移动文件时必须同步更新：

```text
@BelongsPackage
@ClassName
@Description
```

### 11.3 方法级注释

public、protected 方法必须写 JavaDoc。private 方法如果承载独立逻辑，也要写。

如果方法抛出业务异常或技术异常，需要添加 `@throws` 注解。

```java
/**
 * 判断请求路径是否匹配白名单。
 *
 * @param pattern 白名单路径模式
 * @param path 请求路径
 * @return true 表示匹配
 */
private boolean pathMatches(String pattern, String path) {
    return pathMatcher.match(pattern, path);
}
```

### 11.4 proto 注释

proto 文件使用 `//` 注释，不使用 JavaDoc 风格。

```proto
// @BelongsProject: familyaibutler
// @BelongsPackage: top.egon.familyaibutler.family.facade.passwordview
// @FileName: password_view_facade.proto
// @Author: atluofu
// @CreateTime: 2026-05-21 10:00
// @Description: 账号密码 RPC 契约
// @Version: 1.0
```

---

## 12. 注解和依赖规范

### 12.1 Lombok

允许使用：

```text
@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Slf4j
@With
@Accessors(chain = true)
```

建议：

```text
DTO / Command / Query / Result 可以使用 @Data
Spring Bean 优先使用 @RequiredArgsConstructor
Domain Aggregate / Value Object 优先使用 @Getter，不建议直接使用 @Data
```

### 12.2 依赖注入

Spring Bean 推荐构造器注入：

```java
@Service
@RequiredArgsConstructor
public class PasswordViewManageImpl implements PasswordViewManage {

    private final CreatePasswordViewCmdExe createPasswordViewCmdExe;
}
```

禁止新增 Spring 字段注入：

```java
@Autowired
private PasswordViewManage passwordViewManage;
```

例外：

```text
@DubboReference 可以在 infrastructure.rpc.dubbo.client 中字段注入。
除此之外不要新增字段注入。
```

### 12.3 注解位置约束

| 注解                | 允许位置                                    |
|-------------------|-----------------------------------------|
| `@RestController` | `adapter.web`                           |
| `@RequestMapping` | `adapter.web`                           |
| `@DubboService`   | `adapter.rpc.dubbo`                     |
| `@DubboReference` | `infrastructure.rpc.dubbo.client`       |
| `@Transactional`  | `application.manage.impl`               |
| `@Mapper`         | `infrastructure.persistence.mp.mapper`  |
| `@Repository`     | `infrastructure`                        |
| `@Configuration`  | `infrastructure.configuration`          |
| `@Entity`         | `infrastructure.persistence.dataobject` |

禁止：

```text
application 使用 @RestController
application 使用 @DubboService
application 使用 @DubboReference
domain 使用 @Mapper
domain 使用 @Entity
domain 使用 @DubboReference
adapter 使用 @Transactional
adapter 使用 @DubboReference
```

---

## 13. 统一返回和异常规范

### 13.1 HTTP 返回

Web 返回优先使用：

```text
Result<T>
PageResult<T>
```

不要直接返回：

```text
第三方 SDK 对象
异常堆栈
数据库对象
领域聚合
Proto Response
```

### 13.2 RPC 返回

RPC 返回使用 proto response。

推荐 response 中包含：

```text
success
code
message
data / id / items
```

不要在 RPC response 中返回：

```text
异常堆栈
数据库对象
领域聚合
HTTP Result<T>
PageResult<T>
```

### 13.3 异常处理

```text
DomainException      领域规则异常
BizException         应用业务异常
InfraException       基础设施异常
```

处理规则：

```text
domain 可以抛 DomainException
application 可以抛 BizException
infrastructure 可以抛 InfraException 或转换为 BizException
adapter 负责把异常转换为 HTTP / RPC 响应
```

---

## 14. 方法拆分规范

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
需要隔离外部系统、数据库、缓存、RPC 等技术细节
```

避免深层 private 方法链。

简单逻辑宁可直观地写在当前方法中，也不要拆成多层只有一两行的私有方法。代码不是俄罗斯套娃，别一层套一层还找不到核。

---

## 15. 测试规范

### 15.1 测试目录

```text
src/test/java/<basePackage>
├── architecture
├── application
├── domain
└── infrastructure
```

### 15.2 推荐测试类型

```text
ArchitectureTest
DomainTest
ApplicationManageTest
ApplicationExecutorTest
JpaRepositoryIntegrationTest
MpMapperIntegrationTest
DubboAdapterTest
DubboClientTest
```

涉及数据库、Redis、Nacos、Dubbo、外部 AI SDK 的测试必须明确环境依赖。

默认单元测试不要强依赖本机服务。

### 15.3 Architecture Test 规则

必须防止目录退回旧形态：

```text
不允许根 controller
不允许根 service
不允许根 mapper
不允许根 po
不允许根 do
不允许根 repository
不允许 domain.repository
不允许 infrastructure.persistence.impl
不允许 adapter.rpc.grpc，除非真实使用原生 grpc-java
```

必须防止依赖反转：

```text
adapter 不依赖 infrastructure
application 不依赖 adapter
application 不依赖 infrastructure
application 不依赖 facade proto / stub
domain 不依赖 adapter
domain 不依赖 application
domain 不依赖 infrastructure
domain 不依赖 facade proto / stub
infrastructure 不依赖 adapter
facade 不依赖 service 模块
```

### 15.4 ArchUnit 示例

```java
/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.architecture
 * @ClassName: ArchitectureTest
 * @Author: atluofu
 * @CreateTime: 2026-05-21 10:00
 * @Description: 架构约束测试
 * @Version: 1.0
 */
@AnalyzeClasses(packages = "top.egon.familyaibutler.family")
public class ArchitectureTest {

    /**
     * domain 不允许依赖 adapter、application、infrastructure。
     */
    @ArchTest
    static final ArchRule DOMAIN_SHOULD_NOT_DEPEND_ON_OUTER_LAYER = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..adapter..", "..application..", "..infrastructure..");

    /**
     * application 不允许依赖 adapter 和 infrastructure。
     */
    @ArchTest
    static final ArchRule APPLICATION_SHOULD_NOT_DEPEND_ON_ADAPTER_OR_INFRA = noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..adapter..", "..infrastructure..");

    /**
     * adapter 不允许依赖 infrastructure。
     */
    @ArchTest
    static final ArchRule ADAPTER_SHOULD_NOT_DEPEND_ON_INFRA = noClasses()
            .that()
            .resideInAPackage("..adapter..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infrastructure..");

    /**
     * Dubbo Provider 只能放在 adapter.rpc.dubbo。
     */
    @ArchTest
    static final ArchRule DUBBO_SERVICE_SHOULD_ONLY_IN_ADAPTER_RPC_DUBBO = classes()
            .that()
            .areAnnotatedWith(DubboService.class)
            .should()
            .resideInAPackage("..adapter.rpc.dubbo..");

    /**
     * Dubbo Consumer 引用只能放在 infrastructure.rpc.dubbo.client。
     */
    @ArchTest
    static final ArchRule DUBBO_REFERENCE_SHOULD_ONLY_IN_INFRA_CLIENT = fields()
            .that()
            .areAnnotatedWith(DubboReference.class)
            .should()
            .beDeclaredInClassesThat()
            .resideInAPackage("..infrastructure.rpc.dubbo.client..");
}
```

---

## 16. 禁止清单

### 16.1 单模块内禁止新增

```text
根 controller 包
根 service 包
根 mapper 包
根 po 包
根 do 包
根 repository 包
根 configuration 包
根 enums 包
根 utils 包
domain.dto
domain.do
domain.po
domain.pojo
domain.repository
adapter.rpc.grpc，除非真实使用原生 grpc-java
infrastructure.persistence.impl
```

### 16.2 编码时禁止

```text
无意义重构
为了形式拆小方法
深层 private 方法链
Spring 字段注入
Controller 写业务规则
Dubbo Provider 写业务规则
adapter 直接访问 Mapper / Repository
adapter 使用 @DubboReference
application 依赖 adapter
application 依赖 infrastructure
application 依赖 Proto Request / Response
domain 依赖 Spring Web / Mapper / Repository / Dubbo / Proto
infrastructure 反向调用 Controller
infrastructure 反向调用 application manage
JPA ddl-auto 使用 update / create / create-drop
新增文件缺少项目统一注释
移动文件后不更新 @BelongsPackage
```

### 16.3 RPC 禁止

```text
调用方依赖 service 模块
facade 依赖 service 模块
facade 放业务实现
application 直接实现 Dubbo 接口
application 直接消费 Dubbo Reference
domain 依赖 Protobuf 生成类
RPC response 直接返回 DO / Aggregate
把 gRPC 当实现层命名包名，实际却用 Dubbo
```

---

## 17. 修改代码时的顺序

```text
1. 先确认要改的代码属于哪一层。
2. 看同包已有类的注释、命名、注解风格。
3. 新增类先放对目录，再写注释。
4. 如果涉及 RPC，先确认是 facade 契约、Provider 适配，还是 Consumer 调用。
5. 只改和需求相关的文件。
6. 不顺手重构无关历史代码。
7. 涉及结构变更时补 architecture 测试。
8. 涉及 Dubbo Provider / Consumer 时补 RPC 边界测试。
9. 最后跑模块级测试或至少跑编译。
```

---

## 18. 最终落地检查

每次新增接口前问自己 5 个问题：

```text
1. 这是入站请求吗？是就放 adapter。
2. 这是一个业务用例吗？是就放 application。
3. 这是核心业务规则吗？是就放 domain。
4. 这是数据库、缓存、RPC、SDK、HTTP Client 吗？是就放 infrastructure。
5. 这是给别人依赖的 RPC 契约吗？是就放 facade。
```

RPC 再额外问 3 个问题：

```text
1. 我写的是契约吗？是就放 facade。
2. 我写的是本服务对外提供的 Dubbo Provider 吗？是就放 adapter.rpc.dubbo。
3. 我写的是调用别的服务的 Dubbo Consumer 吗？是就放 infrastructure.rpc.dubbo.client。
```

这份规范只约束业务模块内部的代码风格和 RPC 契约边界。真正落地时，命名可以跟随项目现有习惯，但依赖方向不能乱。方向一乱，项目就会从
DDD 变成“到处都 D 一点”。
