package top.egon.familyaibutler.family.po;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
 * @ClassName: CategoryTypePo
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-05Day-13:17
 * @Description: 分类 类型 PO
 * @Version: 1.0
 */
@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
@With
@Accessors(chain = true)
@Builder
@Entity
@Table(name = "category_type")
@Schema(name = "CategoryTypePo", title = "Category Type POJO")
public class CategoryTypePo implements Serializable {

    @Serial
    private static final long serialVersionUID = -7041315135873001168L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "menuSeq")
    @SequenceGenerator(name = "menuSeq", initialValue = 10000, allocationSize = 1, sequenceName = "MENU_SEQUENCE")
    @Schema(title = "id", name = "id", defaultValue = "1", type = "long")
    @Column(name = "id")
    private Long id;
    @Schema(title = "typeName", name = "typeName", defaultValue = "room", type = "String")
    @Column(name = "type_name")
    private String typeName;
    @Schema(title = "description", name = "description", defaultValue = "room", type = "String")
    @Column(name = "description")
    private String description;
    @Schema(title = "createTime", name = "createTime", defaultValue = "2025-08-01 20:30:40", type = "Date")
    @Column(name = "create_time", updatable = false)
    private Date createTime;
    @Column(name = "update_time")
    @Schema(title = "updateTime", name = "updateTime", defaultValue = "2025-08-01 20:30:40", type = "Date")
    private Date updateTime;

    @PrePersist
    protected void onCreate() {
        Date date = new Date();
        this.createTime = date;
        this.updateTime = date;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = new Date();
    }

}