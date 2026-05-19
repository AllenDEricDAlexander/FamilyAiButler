package top.egon.familyaibutler.uaa.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.uaa.utils.JwtTokenUtil;
import top.egon.familyaibutler.uaa.filter.JwtAuthenticationFilter;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.configuration
 * @ClassName: SecurityConfig
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-13Day-10:41
 * @Description: Spring Security 安全配置类
 * @Version: 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    private final JwtTokenUtil jwtTokenUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenUtil, userDetailsService);
    }

    /**
     * @description: spring security 核心配置
     * @author: atluofu
     * @date: 2025/8/13 10:58
     * @param:
     * @return:
     **/
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // todo 令牌刷新管控
                                "/user/logout",
                                "/user/login",
                                "/user/register",
                                "/doc.html",
                                "/webjars/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/favicon.ico",
                                "/css/**",
                                "/js/**"
                        )
                        .permitAll()
                        .anyRequest()
                        // todo 细化管理 权限
                        .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write(
                                    objectMapper.writeValueAsString(Result.fail(401, "认证失败", "请先登录"))
                            );
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(403);
                            response.getWriter().write(
                                    objectMapper.writeValueAsString(Result.fail(403, "授权失败", "权限不足"))
                            );
                        })
                )
                //--- CORS 跨域配置（按需启用）---
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                //--- 禁用 CSRF 防护（传统 Web 应用建议启用）---  todo 思考是否需要启用 如何透传给前端
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    /**
     * @description: CORS 跨域配置（生产环境应缩小范围）
     * @author: atluofu
     * @date: 2025/8/13 10:43
     * @param:
     * @return:
     **/
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // todo 生产只允许注册中心的实例IP访问
        config.setAllowedOrigins(List.of("http://localhost:8080"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        // todo 允许登录 注册 接口跨域 考虑是否合理
        source.registerCorsConfiguration("/user/login", new CorsConfiguration());
        return source;
    }
}