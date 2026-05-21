/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.infrastructure.configuration
 * @FileName: GatewayConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-30Day-13:33
 * @Description: gateway 配置文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.infrastructure.configuration;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.infrastructure.configuration
 * @ClassName: GatewayConfiguration
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-30Day-13:33
 * @Description: gateway 配置
 * @Version: 1.0
 */
@Configuration
public class GatewayConfiguration {
    /**
     * 路径键解析器Bean定义 后续转sentinel
     * <p>
     * 该方法创建一个基于请求路径的KeyResolver实例，用于网关限流等场景。
     * 它从交换对象中提取请求路径作为限流的键值。
     *
     * @return KeyResolver 返回一个路径键解析器，该解析器将请求路径转换为字符串键
     */
    @Bean
    public KeyResolver pathKeyResolver() {
        // 从交换对象中获取请求路径，将其转换为字符串并包装为Mono对象返回
        return exchange -> Mono.just(exchange.getRequest().getPath().toString());
    }

}
