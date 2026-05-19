package top.egon.familyaibutler.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.cache
 * @ClassName: CacheMessage
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-31Day-19:04
 * @Description: redis 订阅消息体
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 5532139615087596734L;
    private String key;
    // update / evict
    private String action;
    private String value;
}