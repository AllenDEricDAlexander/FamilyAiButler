package top.egon.familyaibutler.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.LongAdder;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.cache
 * @ClassName: GuavaRedisCache
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-31Day-19:04
 * @Description: GuavaRedisCache
 * @Version: 1.0
 */
@Slf4j
public class GuavaRedisCache<K, V> implements Cache<K, V> {

    @Getter
    private final String cacheName;

    private final Cache<K, V> local;
    private final RedisTemplate<String, Object> redis;
    private final ChannelTopic topic;
    private final ObjectMapper mapper;
    private final long ttl;
    private final String keyPrefix;

    // 统计（补齐二级命中、加载耗时）
    private final LongAdder redisHits = new LongAdder();
    private final LongAdder redisMisses = new LongAdder();
    private final LongAdder loadSuccess = new LongAdder();
    private final LongAdder loadException = new LongAdder();
    private final LongAdder totalLoadNanos = new LongAdder();

    public GuavaRedisCache(String cacheName,
                           RedisTemplate<String, Object> redis,
                           ChannelTopic topic,
                           ObjectMapper mapper,
                           long ttl,
                           long maxSize,
                           String keyPrefix) {
        this.cacheName = cacheName;
        this.redis = redis;
        this.topic = topic;
        this.mapper = mapper;
        this.ttl = ttl;
        this.keyPrefix = keyPrefix;

        this.local = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(Duration.ofMillis(ttl))
                .recordStats()
                .build();
    }

    // ==================== Guava Cache 接口实现 ====================

    @Override
    public @Nullable V getIfPresent(Object key) {
        @SuppressWarnings("unchecked")
        V val = local.getIfPresent((K) key);
        if (val != null) return val;

        V fromRedis = getFromRedis((K) key);
        if (fromRedis != null) {
            local.put((K) key, fromRedis);
            redisHits.increment();
            return fromRedis;
        }
        redisMisses.increment();
        return null;
    }

    @Override
    public V get(K key, Callable<? extends V> loader) throws ExecutionException {
        V val = getIfPresent(key);
        if (val != null) return val;

        long start = System.nanoTime();
        try {
            val = loader.call();
            long cost = System.nanoTime() - start;
            totalLoadNanos.add(cost);
            if (val != null) {
                put(key, val);
                loadSuccess.increment();
            }
            return val;
        } catch (Exception e) {
            long cost = System.nanoTime() - start;
            totalLoadNanos.add(cost);
            loadException.increment();
            throw new ExecutionException(e);
        }
    }

    @Override
    public ImmutableMap<K, V> getAllPresent(Iterable<? extends Object> keys) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (Object kObj : keys) {
            @SuppressWarnings("unchecked")
            K k = (K) kObj;
            V v = local.getIfPresent(k);
            if (v == null) {
                v = getFromRedis(k);
                if (v != null) {
                    local.put(k, v);
                    redisHits.increment();
                } else {
                    redisMisses.increment();
                }
            }
            if (v != null) builder.put(k, v);
        }
        return builder.build();
    }

    @Override
    public void put(K key, V value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        local.put(key, value);
        setToRedis(key, value);
        publish(new CacheMessage(cacheName, String.valueOf(key), "update"));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void invalidate(Object key) {
        @SuppressWarnings("unchecked")
        K k = (K) key;
        local.invalidate(k);
        deleteFromRedis(k);
        publish(new CacheMessage(cacheName, String.valueOf(key), "evict"));
    }

    @Override
    public void invalidateAll(Iterable<?> keys) {
        for (Object k : keys) {
            invalidate(k);
        }
    }

    @Override
    public void invalidateAll() {
        // 清本地
        local.invalidateAll();
        // 清 Redis（按前缀 + cacheName 扫描删除）
        try {
            RedisScanDelete.deleteByPrefix(redis, redisPrefix());
        } catch (Exception e) {
            log.warn("Redis scan-delete failed for {}", redisPrefix(), e);
        }
        publish(new CacheMessage(cacheName, "*", "clear"));
    }

    @Override
    public long size() {
        return local.size();
    }

    @Override
    public CacheStats stats() {
        CacheStats base = local.stats();
        // 把二级命中/加载信息叠加到 stats（Guava 提供 plus 能力）
        CacheStats secondLevel = new CacheStats(
                redisHits.sum(),
                redisMisses.sum(),
                loadSuccess.sum(),
                loadException.sum(),
                totalLoadNanos.sum(),
                0L
        );
        return base.plus(secondLevel);
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        return local.asMap();
    }

    @Override
    public void cleanUp() {
        local.cleanUp();
    }

    // ==================== 私有工具 ====================

    private String redisKey(K key) {
        return keyPrefix + "::" + cacheName + "::" + key;
    }

    @SuppressWarnings("unchecked")
    private @Nullable V getFromRedis(K key) {
        Object obj = redis.opsForValue().get(redisKey(key));
        if (obj == null) return null;
        try {
            return (V) obj;
        } catch (Exception e) {
            log.warn("Redis value cast failed. key={}, valClass={}", key, obj.getClass().getName());
            return null;
        }
    }

    private void setToRedis(K key, V value) {
        redis.opsForValue().set(redisKey(key), value, ttl);
    }

    private void deleteFromRedis(K key) {
        redis.delete(redisKey(key));
    }

    private String redisPrefix() {
        return keyPrefix + "::" + cacheName + "::";
    }

    private void publish(CacheMessage msg) {
        try {
            redis.convertAndSend(topic.getTopic(), mapper.writeValueAsBytes(msg));
        } catch (Exception e) {
            log.warn("Publish cache message failed: {}", msg, e);
        }
    }
}