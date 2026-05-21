package ${packageName};

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

${classComment}@AnalyzeClasses(packages = "${basePackage}")
public class ${className} {

/**
* 校验领域层不依赖外层。
*/
@ArchTest
static final ArchRule domainShouldNotDependOnOuterLayers = noClasses()
.that().resideInAPackage("..domain..")
.should().dependOnClassesThat()
.resideInAnyPackage("..adapter..", "..application..", "..infrastructure..");

/**
* 校验领域层不依赖 JPA 或 MyBatis Plus。
*/
@ArchTest
static final ArchRule domainShouldNotUseJpaOrMp = noClasses()
.that().resideInAPackage("..domain..")
.should().dependOnClassesThat()
.resideInAnyPackage("jakarta.persistence..", "org.springframework.data.jpa..", "com.baomidou.mybatisplus..");

/**
* 校验适配器不依赖应用实现细节和持久化细节。
*/
@ArchTest
static final ArchRule adapterShouldNotDependOnAppImplementation = noClasses()
.that().resideInAPackage("..adapter..")
.should().dependOnClassesThat()
.resideInAnyPackage("..application.executor..", "..infrastructure.persistence..");

/**
* 校验 Controller 不落在旧根 controller 包。
*/
@ArchTest
static final ArchRule controllerShouldNotResideInLegacyRootPackage = noClasses()
.that().haveSimpleNameEndingWith("Controller")
.should().resideInAnyPackage("..controller..", "..adapter.web..");

/**
* 校验 Mapper 只能存在于 MP mapper 包。
*/
@ArchTest
static final ArchRule mapperShouldOnlyExistInMpPackage = classes()
.that().areAssignableTo("com.baomidou.mybatisplus.core.mapper.BaseMapper")
.should().resideInAPackage("..infrastructure.persistence.mp.mapper..");
}
