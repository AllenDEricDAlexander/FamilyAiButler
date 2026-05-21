package ${packageName};

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

${classComment}@AnalyzeClasses(packages = "${basePackage}")
public class ${className} {

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
* infrastructure 不允许反向依赖 adapter。
*/
@ArchTest
static final ArchRule INFRA_SHOULD_NOT_DEPEND_ON_ADAPTER = noClasses()
.that()
.resideInAPackage("..infrastructure..")
.should()
.dependOnClassesThat()
.resideInAPackage("..adapter..");

/**
* Controller 只能放在 adapter.web 包。
*/
@ArchTest
static final ArchRule CONTROLLER_SHOULD_ONLY_RESIDE_IN_ADAPTER_WEB = classes()
.that()
.haveSimpleNameEndingWith("Controller")
.should()
.resideInAPackage("..adapter.web..");

/**
* 禁止生成旧根包和旧基础设施包。
*/
@ArchTest
static final ArchRule LEGACY_PACKAGES_SHOULD_NOT_EXIST = noClasses()
.should()
.resideInAnyPackage(
"${basePackage}.controller..",
"${basePackage}.service..",
"${basePackage}.mapper..",
"${basePackage}.po..",
"${basePackage}.do..",
"${basePackage}.repository..",
"..application.dto..",
"..adapter.rpc.grpc..",
"..domain.repository..",
"..infrastructure.gatewayimpl..",
"..infrastructure.persistence.impl.."
);

/**
* Mapper 只能存在于 MP mapper 包。
*/
@ArchTest
static final ArchRule MAPPER_SHOULD_ONLY_EXIST_IN_MP_PACKAGE = classes()
.that()
.areAssignableTo("com.baomidou.mybatisplus.core.mapper.BaseMapper")
.should()
.resideInAPackage("..infrastructure.persistence.mp.mapper..");
}
