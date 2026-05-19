package top.egon.familyaibutler.family.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import top.egon.familyaibutler.common.pojo.CacheMessage;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.configuration
 * @ClassName: RedisCacheMessageListener
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-13Day-16:47
 * @Description: RedisCacheMessageListener
 * @Version: 1.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisCacheMessageListener implements MessageListener {

    private final Cache<String, Object> guavaCache;
    private final ObjectMapper mapper = new ObjectMapper();

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {

            CacheMessage cacheMessage = mapper.readValue(message.getBody(), CacheMessage.class);
            if ("evict".equals(cacheMessage.getAction())) {
                guavaCache.invalidate(cacheMessage.getCacheKey());
            } else if ("update".equals(cacheMessage.getAction())) {
                guavaCache.put(cacheMessage.getCacheKey(), cacheMessage.getNewValue());
            }
        } catch (Exception e) {
            log.error("处理缓存通知异常", e);
        }
    }
}