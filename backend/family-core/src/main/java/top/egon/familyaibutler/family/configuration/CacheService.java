package top.egon.familyaibutler.family.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.common.pojo.CacheMessage;

import java.util.concurrent.TimeUnit;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.configuration
 * @ClassName: CacheService
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-13Day-16:48
 * @Description: CacheService
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
public class CacheService {
    private final Cache<String, Object> guavaCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic topic;
    private final ObjectMapper mapper;

    public <T> T get(String key, Class<T> clazz) {
        Object local = guavaCache.getIfPresent(key);
        if (clazz.isInstance(local)) {
            return clazz.cast(local);
        }
        Object rv = redisTemplate.opsForValue().get(key);
        if (clazz.isInstance(rv)) {
            guavaCache.put(key, rv);
            return clazz.cast(rv);
        }
        if (rv != null) {
            try {
                T converted = mapper.convertValue(rv, clazz);
                guavaCache.put(key, converted);
                return converted;
            } catch (IllegalArgumentException ignore) {
                // 转换失败直接返回 null，避免 ClassCastException
            }
        }
        return null;
    }

    public void put(String key, Object value, long ttlSeconds) {
        guavaCache.put(key, value);
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        redisTemplate.convertAndSend(topic.getTopic(), new CacheMessage(key, "update", String.valueOf(value)));
    }

    public void evict(String key) {
        guavaCache.invalidate(key);
        redisTemplate.delete(key);
        redisTemplate.convertAndSend(topic.getTopic(), new CacheMessage(key, "evict", null));
    }

}