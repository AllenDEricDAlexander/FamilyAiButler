package top.egon.familyaibutler.family.domain.category.model.entity;

import lombok.Data;

import java.util.Date;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.category.model.entity
 * @ClassName: CategoryType
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: category_type 领域实体
 * @Version: 1.0
 */
@Data
public class CategoryType {
    private Long id;
    private String typeName;
    private String description;
    private Date createTime;
    private Date updateTime;
}
