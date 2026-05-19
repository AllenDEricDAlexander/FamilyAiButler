package top.egon.familyaibutler.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.cache
 * @ClassName: GuavaRedisCacheAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-31Day-19:09
 * @Description: GuavaRedisCacheAutoConfiguration
 * @Version: 1.0
 */
@AutoConfiguration
@EnableConfigurationProperties(GuavaRedisCacheProperties.class)
public class GuavaRedisCacheAutoConfiguration {

    @Bean("familyAiButlerCacheChannel")
    @ConditionalOnMissingBean(name = "familyAiButlerCacheChannel")
    public ChannelTopic cacheTopic(GuavaRedisCacheProperties properties) {
        return new ChannelTopic(properties.getTopic());
    }

    @Bean(name = "distributedCache")
    @ConditionalOnMissingBean(name = "distributedCache")
    public Cache<String, Object> distributedCache(RedisTemplate<String, Object> redis,
                                                  ChannelTopic topic,
                                                  ObjectMapper mapper,
                                                  GuavaRedisCacheProperties props) {
        return new GuavaRedisCache<>(
                props.getName(),
                redis,
                topic,
                mapper,
                props.getTtl(),
                props.getMaxSize(),
                props.getKeyPrefix()
        );
    }

    @Bean
    public RedisMessageListenerContainer twoLevelCacheListenerContainer(
            org.springframework.data.redis.connection.RedisConnectionFactory factory,
            ChannelTopic topic,
            ObjectMapper mapper,
            GuavaRedisCacheManager guavaRedisCacheManager
    ) {
        var container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(
                new CacheMessageListener(mapper, guavaRedisCacheManager),
                topic
        );
        return container;
    }
}