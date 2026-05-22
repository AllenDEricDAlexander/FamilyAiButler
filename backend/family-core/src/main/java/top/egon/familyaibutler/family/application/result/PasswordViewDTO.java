package top.egon.familyaibutler.family.application.result;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
@DocModel(name = "PasswordViewDTO", description = "password view api dto")
public class PasswordViewDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 9146111814279753262L;
    @DocField(description = "id", example = "1")
    private Long id;
    @DocField(description = "name", example = "GitHub")
    private String name;
    @DocField(description = "password", example = "P@ssw0rd1234")
    @NotNull
    @NotEmpty
    private String password;
    @DocField(description = "description", example = "GitHub 账号")
    private String description;
    @DocField(description = "accountNumber", example = "mario@example.com")
    @NotNull
    @NotEmpty
    private String accountNumber;
    @DocField(description = "websit", example = "https://github.com")
    @NotNull
    @NotEmpty
    private String websit;
    @DocField(description = "likeStatus", example = "true")
    private boolean likeStatus;
    @DocField(description = "category", example = "WORK")
    private PasswordCategoryEnum category;
    @DocField(description = "lastViewTime", example = "2026-05-22T00:00:00")
    private LocalDateTime lastViewTime;
}
