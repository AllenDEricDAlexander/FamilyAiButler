package top.egon.familyaibutler.family.domain.category.model.aggregate;

import lombok.Data;

import java.util.Date;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.category.model.aggregate
 * @ClassName: Category
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: category 领域模型
 * @Version: 1.0
 */
@Data
public class Category {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Date createTime;
    private Date updateTime;
}
