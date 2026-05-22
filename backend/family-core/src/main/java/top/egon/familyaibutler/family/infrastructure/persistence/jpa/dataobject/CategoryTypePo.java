package top.egon.familyaibutler.family.infrastructure.persistence.jpa.dataobject;

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
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.persistence.jpa.dataobject
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
@DocModel(name = "CategoryTypePo", description = "Category Type POJO")
public class CategoryTypePo implements Serializable {

    @Serial
    private static final long serialVersionUID = -7041315135873001168L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "menuSeq")
    @SequenceGenerator(name = "menuSeq", initialValue = 10000, allocationSize = 1, sequenceName = "MENU_SEQUENCE")
    @DocField(description = "id")
    @Column(name = "id")
    private Long id;
    @DocField(description = "typeName")
    @Column(name = "type_name")
    private String typeName;
    @DocField(description = "description")
    @Column(name = "description")
    private String description;
    @DocField(description = "createTime")
    @Column(name = "create_time", updatable = false)
    private Date createTime;
    @Column(name = "update_time")
    @DocField(description = "updateTime")
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
