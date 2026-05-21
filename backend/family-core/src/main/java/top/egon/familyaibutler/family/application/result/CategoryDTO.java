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

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.result
 * @ClassName: CategoryDTO
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-04Day-11:39
 * @Description: 类型
 * @Version: 1.0
 */
@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "CategoryDTO", title = "Category DTO")
public class CategoryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7033547257705498021L;

    @Schema(title = "id", name = "id", defaultValue = "1", type = "long")
    private Long id;
    @Schema(title = "name", name = "name", defaultValue = "house", type = "String")
    private String categoryName;
    @Schema(title = "description", name = "description", defaultValue = "test", type = "String")
    private String description;
    @Schema(title = "parentId", name = "parentId", defaultValue = "0", type = "long")
    private Long parentId;

}
