/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.configuration
 * @FileName: UaaSecurityConfig.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: UAA 安全配置文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.configuration
 * @ClassName: UaaSecurityConfig
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: UAA 安全配置
 * @Version: 1.0
 */
@Configuration
public class UaaSecurityConfig {

    /**
     * 配置 UAA 第一批开放接口安全过滤链。
     *
     * @param http HTTP 安全配置
     * @return 安全过滤链
     * @throws Exception Spring Security 配置异常
     */
    @Bean
    public SecurityFilterChain uaaSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(registry -> registry.anyRequest().permitAll())
                .build();
    }
}
