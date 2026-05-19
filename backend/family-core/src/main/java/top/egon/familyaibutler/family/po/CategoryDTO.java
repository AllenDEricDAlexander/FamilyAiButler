package top.egon.familyaibutler.family.po;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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
 * @BelongsPackage: top.egon.familyaibutler.family.po
 * @ClassName: CategoryPo
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
@Schema(name = "CategoryDTP", title = "Category DTO")
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