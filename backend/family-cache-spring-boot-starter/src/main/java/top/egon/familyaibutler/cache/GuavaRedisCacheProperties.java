package top.egon.familyaibutler.cache;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.cache
 * @ClassName: GuavaRedisCacheProperties
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-31Day-19:03
 * @Description: 自定义配置属性
 * @Version: 1.0
 */
@Data
@ConfigurationProperties(prefix = "top.egon.cache")
public class GuavaRedisCacheProperties {
    private String name = "default";
    /**
     * 默认 TTL 秒
     */
    private long ttl = 300;

    /**
     * Guava 本地缓存最大容量
     */
    private long maxSize = 5000;

    /**
     * Redis 缓存 key 前缀
     */
    private String keyPrefix = "cache";

    /**
     * Redis 通知通道
     */
    private String topic = "guava-redis-cache-topic";
}