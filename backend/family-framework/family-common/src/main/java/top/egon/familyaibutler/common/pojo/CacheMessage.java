package top.egon.familyaibutler.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

import java.io.Serial;
import java.io.Serializable;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.pojo
 * @ClassName: CacheMessage
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-13Day-16:45
 * @Description: CacheMessage
 * @Version: 1.0
 */
@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@DocModel(name = "CacheMessage", description = "缓存变更消息")
public class CacheMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = -7913819771231281565L;
    @DocField(description = "缓存键", example = "family:category:1")
    private String cacheKey;
    @DocField(description = "缓存动作", example = "evict")
    private String action;
    @DocField(description = "变更后的值", example = "{\"id\":1}")
    private String newValue;

}
