/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.servlet.autoconfigure
 * @FileName: FamilyServletLogAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:10
 * @Description: Servlet 日志自动装配文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.servlet.autoconfigure;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import top.egon.familyaibutler.framework.log.core.FamilyLogProperties;
import top.egon.familyaibutler.framework.log.servlet.FamilyMdcServletFilter;
import top.egon.familyaibutler.framework.log.servlet.FamilyServletLogAspect;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.servlet.autoconfigure
 * @ClassName: FamilyServletLogAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:10
 * @Description: Servlet 日志自动装配
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass(Aspect.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "family.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FamilyServletLogAutoConfiguration {

    /**
     * 注册 Servlet MDC 过滤器。
     *
     * @param properties 日志配置
     * @return FilterRegistrationBean<FamilyMdcServletFilter> 返回 Servlet MDC 过滤器注册器
     */
    @Bean
    @ConditionalOnMissingBean(name = "familyMdcServletFilterRegistration")
    @ConditionalOnProperty(prefix = "family.log", name = {"mdc-enabled", "servlet-enabled"},
            havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<FamilyMdcServletFilter> familyMdcServletFilterRegistration(FamilyLogProperties properties) {
        FilterRegistrationBean<FamilyMdcServletFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new FamilyMdcServletFilter(properties));
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        return registrationBean;
    }

    /**
     * 注册 Servlet 控制器日志切面。
     *
     * @param properties 日志配置
     * @return FamilyServletLogAspect 返回 Servlet 控制器日志切面
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "family.log", name = {"request-log-enabled", "servlet-enabled"},
            havingValue = "true", matchIfMissing = true)
    public FamilyServletLogAspect familyServletLogAspect(FamilyLogProperties properties) {
        return new FamilyServletLogAspect(properties);
    }
}
