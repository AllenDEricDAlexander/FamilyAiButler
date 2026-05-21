package top.egon.familyaibutler.common.mybatis.configuration;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import top.egon.familyaibutler.common.mybatis.extention.injector.BaseInjector;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.mybatis.configuration
 * @ClassName: MybatisPlusConfig
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-03Day-15:15
 * @Description: MybatisPlusConfig
 * @Version: 1.0
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 创建 MyBatis Plus 拦截器。
     *
     * @return MybatisPlusInterceptor 返回 MyBatis Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /**
     * 创建开发测试环境防全表更新拦截器。
     *
     * @return MybatisPlusInterceptor 返回防全表更新拦截器
     */
    @Bean
    @Profile({"dev", "test"})
    public MybatisPlusInterceptor blockAttackInnerInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

    /**
     * 创建公共 Mapper 方法注入器。
     *
     * @return BaseInjector 返回公共 Mapper 方法注入器
     */
    @Bean
    public BaseInjector injectBaseInjector() {
        return new BaseInjector();
    }

}
