package top.egon.familyaibutler.common.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;

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
@Schema(title = "Cache对象", name = "CacheMessage")
public class CacheMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = -7913819771231281565L;
    private String cacheKey;
    private String action;
    private String newValue;

}