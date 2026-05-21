package top.egon.familyaibutler.family.domain.model.aggregate;

import lombok.Data;
import top.egon.familyaibutler.family.domain.model.enums.PasswordCategoryEnum;

import java.util.Date;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.model.aggregate
 * @ClassName: PasswordView
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: password_view 领域模型
 * @Version: 1.0
 */
@Data
public class PasswordView {
    private Long id;
    private String businessId;
    private String name;
    private String password;
    private String description;
    private String accountNumber;
    private String websit;
    private Boolean likeStatus;
    private PasswordCategoryEnum category;
    private Date lastViewTime;
}
