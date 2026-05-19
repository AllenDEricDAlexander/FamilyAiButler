package top.egon.familyaibutler.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.cache
 * @ClassName: GuavaRedisCacheManager
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-31Day-19:07
 * @Description: GuavaRedisCacheManager
 * @Version: 1.0
 */
public class GuavaRedisCacheManager implements CacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper mapper;
    private final ChannelTopic topic;
    private final GuavaRedisCacheProperties properties;

    private final ConcurrentHashMap<String, Cache> cacheMap = new ConcurrentHashMap<>();

    public GuavaRedisCacheManager(RedisTemplate<String, Object> redisTemplate,
                                  ObjectMapper mapper,
                                  ChannelTopic topic,
                                  GuavaRedisCacheProperties properties) {
        this.redisTemplate = redisTemplate;
        this.mapper = mapper;
        this.topic = topic;
        this.properties = properties;
    }

    @Override
    public Cache getCache(String name) {
        return cacheMap.computeIfAbsent(name, n ->
                (Cache) new GuavaRedisCache(
                        n,
                        redisTemplate,
                        topic,
                        mapper,
                        properties.getTtl(),
                        properties.getMaxSize(),
                        properties.getKeyPrefix()
                )
        );
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheMap.keySet();
    }
}