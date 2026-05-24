package top.egon.familyaibutler.family.application.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

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
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@EqualsAndHashCode
@DocModel(name = "FamilyCategoryDTO", description = "家庭分类信息传输对象")
public class CategoryDTO implements Serializable {

    @Serial
    /**
     * 序列化版本号。
     */
    private static final long serialVersionUID = -7033547257705498021L;

    /**
     * 分类 ID。
     */
    @DocField(description = "分类 ID", required = false, example = "1")
    private Long id;

    /**
     * 分类名称。
     */
    @DocField(description = "分类名称", required = false, example = "生活")
    private String categoryName;

    /**
     * 分类描述。
     */
    @DocField(description = "分类描述", required = false, example = "家庭生活分类")
    private String description;

    /**
     * 父分类 ID。
     */
    @DocField(description = "父分类 ID", required = false, example = "0")
    private Long parentId;

}
