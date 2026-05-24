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
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
@EqualsAndHashCode
@DocModel(name = "FamilyCategoryTypeDTO", description = "家庭分类类型信息传输对象")
public class CategoryTypeDTO implements Serializable {

    @Serial
    /**
     * 序列化版本号。
     */
    private static final long serialVersionUID = -7041315135873001168L;

    /**
     * 分类类型 ID。
     */
    @DocField(description = "分类类型 ID", required = false, example = "1")
    private Long id;

    /**
     * 分类类型名称。
     */
    @DocField(description = "分类类型名称", required = false, example = "支出")
    private String typeName;

    /**
     * 分类类型描述。
     */
    @DocField(description = "分类类型描述", required = false, example = "家庭支出类型")
    private String description;

    /**
     * 创建时间。
     */
    @DocField(description = "创建时间", required = false, example = "2026-05-22T00:00:00")
    private Date createTime;

    /**
     * 更新时间。
     */
    @DocField(description = "更新时间", required = false, example = "2026-05-22T00:00:00")
    private Date updateTime;
}
