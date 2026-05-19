package top.egon.familyaibutler.family.configuration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.configuration
 * @ClassName: GuavaCacheConfig
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-13Day-16:41
 * @Description: CacheConfig
 * @Version: 1.0
 */
@Configuration
public class GuavaCacheConfig {
    @Bean
    public Cache<String, Object> guavaCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .build();
    }

}