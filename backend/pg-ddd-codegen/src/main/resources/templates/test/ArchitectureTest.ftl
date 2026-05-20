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
.resideInAnyPackage("..adapter..", "..app..", "..infrastructure..");

/**
* 校验领域层不依赖 JPA 或 MyBatis Plus。
*/
@ArchTest
static final ArchRule domainShouldNotUseJpaOrMp = noClasses()
.that().resideInAPackage("..domain..")
.should().dependOnClassesThat()
.resideInAnyPackage("jakarta.persistence..", "org.springframework.data.jpa..", "com.baomidou.mybatisplus..");

/**
* 校验 Controller 不直接访问 Mapper。
*/
@ArchTest
static final ArchRule controllerShouldNotAccessMapper = noClasses()
.that().resideInAPackage("..adapter.web.controller..")
.should().dependOnClassesThat()
.resideInAnyPackage("..infrastructure.persistence.mp.mapper..");

/**
* 校验 Mapper 只能存在于 MP mapper 包。
*/
@ArchTest
static final ArchRule mapperShouldOnlyExistInMpPackage = classes()
.that().areAssignableTo("com.baomidou.mybatisplus.core.mapper.BaseMapper")
.should().resideInAPackage("..infrastructure.persistence.mp.mapper..");
}
