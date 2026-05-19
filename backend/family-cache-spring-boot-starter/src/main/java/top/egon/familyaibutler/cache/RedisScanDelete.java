package top.egon.familyaibutler.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.cache
 * @ClassName: RedisScanDelete
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-31Day-19:18
 * @Description: RedisScanDelete deleteByPrefix
 * @Version: 1.0
 */
@Slf4j
final class RedisScanDelete {
    private RedisScanDelete() {
    }

    static void deleteByPrefix(RedisTemplate<String, Object> redis, String prefix) {
        redis.execute((RedisCallback<Void>) conn -> {
            byte[] pattern = (prefix + "*").getBytes(StandardCharsets.UTF_8);
            ScanOptions options = ScanOptions.scanOptions().match(new String(pattern, StandardCharsets.UTF_8)).count(1000).build();
            List<byte[]> toDel = new ArrayList<>();
            try (var cursor = conn.scan(options)) {
                while (cursor.hasNext()) {
                    toDel.add(cursor.next());
                    if (toDel.size() >= 500) {
                        conn.unlink(toDel.toArray(byte[][]::new));
                        toDel.clear();
                    }
                }
                if (!toDel.isEmpty()) {
                    conn.unlink(toDel.toArray(byte[][]::new));
                }
            } catch (Exception e) {
                log.error("Failed to delete by prefix", e);
            }
            return null;
        });
    }
}