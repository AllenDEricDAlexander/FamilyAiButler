/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.result
 * @FileName: CategoryTypeDTO.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:15
 * @Description: 分类类型 DTO 文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.family.application.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.result
 * @ClassName: CategoryTypeDTO
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:15
 * @Description: 分类类型 DTO
 * @Version: 1.0
 */
@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CategoryTypeDTO", title = "Category Type DTO")
public class CategoryTypeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7041315135873001168L;

    @Schema(title = "id", name = "id", defaultValue = "1", type = "long")
    private Long id;

    @Schema(title = "typeName", name = "typeName", defaultValue = "room", type = "String")
    private String typeName;

    @Schema(title = "description", name = "description", defaultValue = "room", type = "String")
    private String description;

    @Schema(title = "createTime", name = "createTime", defaultValue = "2025-08-01 20:30:40", type = "Date")
    private Date createTime;

    @Schema(title = "updateTime", name = "updateTime", defaultValue = "2025-08-01 20:30:40", type = "Date")
    private Date updateTime;
}
