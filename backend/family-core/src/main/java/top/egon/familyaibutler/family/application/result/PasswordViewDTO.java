package top.egon.familyaibutler.family.application.result;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import top.egon.familyaibutler.family.domain.passwordview.model.enums.PasswordCategoryEnum;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.application.result
 * @ClassName: PasswordViewDTO
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-02Day-21:56
 * @Description: Password View DTO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@With
@Accessors(chain = true)
@Builder
@EqualsAndHashCode
@DocModel(name = "FamilyPasswordViewDTO", description = "家庭密码视图信息传输对象")
public class PasswordViewDTO implements Serializable {


    @Serial
    /**
     * 序列化版本号。
     */
    private static final long serialVersionUID = 9146111814279753262L;

    /**
     * 账号密码 ID。
     */
    @DocField(description = "账号密码 ID", required = false, example = "1")
    private Long id;

    /**
     * 账号密码名称。
     */
    @DocField(description = "账号密码名称", required = false, example = "GitHub")
    private String name;

    /**
     * 密码明文。
     */
    @DocField(description = "密码明文", required = true, example = "P@ssw0rd1234")
    @NotNull
    @NotEmpty
    private String password;

    /**
     * 账号密码描述。
     */
    @DocField(description = "账号密码描述", required = false, example = "GitHub 账号")
    private String description;

    /**
     * 登录账号。
     */
    @DocField(description = "登录账号", required = true, example = "mario@example.com")
    @NotNull
    @NotEmpty
    private String accountNumber;

    /**
     * 站点地址。
     */
    @DocField(description = "站点地址", required = true, example = "https://github.com")
    @NotNull
    @NotEmpty
    private String websit;

    /**
     * 收藏状态。
     */
    @DocField(description = "收藏状态", required = true, example = "true")
    private boolean likeStatus;

    /**
     * 密码分类。
     */
    @DocField(description = "密码分类", required = false, example = "WORK")
    private PasswordCategoryEnum category;

    /**
     * 最近查看时间。
     */
    @DocField(description = "最近查看时间", required = false, example = "2026-05-22T00:00:00")
    private LocalDateTime lastViewTime;
}
