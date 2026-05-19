package top.egon.familyaibutler.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.cache
 * @ClassName: CacheMessageListener
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-31Day-19:08
 * @Description: CacheMessageListener
 * @Version: 1.0
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CacheMessageListener implements MessageListener {

    private final ObjectMapper mapper;
    private final GuavaRedisCacheManager cacheManager;


    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            CacheMessage msg = mapper.readValue(message.getBody(), CacheMessage.class);
            Cache cache = cacheManager.getCache("default");
            Assert.notNull(cache, "Cache must not be null");
            // 先获取操作类型枚举
            CacheStrategyEnum action = CacheStrategyEnum.getByCode(msg.getAction());
            if (action == null) {
                // 处理未知操作类型
                log.error("invalid parameter");
                return;
            }
            // 用枚举进行switch判断
            switch (action) {
                case EVICT, UPDATE:
                    cache.evict(msg.getKey());
                    break;
                case CLEAR:
                    cache.clear();
                    break;
            }
        } catch (Exception e) {
            log.error("Failed to handle cache message", e);
        }
    }
}