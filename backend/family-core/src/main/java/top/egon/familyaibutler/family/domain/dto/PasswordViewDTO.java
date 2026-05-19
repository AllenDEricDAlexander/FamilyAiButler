package top.egon.familyaibutler.family.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import top.egon.familyaibutler.family.enums.PasswordCategoryEnum;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.dto
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
@Schema(name = "PasswordViewDTO", title = "password view api dto")
public class PasswordViewDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 9146111814279753262L;
    @Schema(title = "name", name = "name", defaultValue = "1", type = "String")
    private Long id;
    @Schema(title = "name", name = "name", defaultValue = "test", type = "String")
    private String name;
    @Schema(title = "password", name = "password", defaultValue = "Test123*.+", type = "String")
    @NotNull
    @NotEmpty
    private String password;
    @Schema(title = "description", name = "description", defaultValue = "test content", type = "String")
    private String description;
    @Schema(title = "accountNumber", name = "accountNumber", defaultValue = "test001", type = "String")
    @NotNull
    @NotEmpty
    private String accountNumber;
    @Schema(title = "websit", name = "websit", defaultValue = "www.baidu.com", type = "String")
    @NotNull
    @NotEmpty
    private String websit;
    @Schema(title = "likeStatus", name = "likeStatus", defaultValue = "false", type = "bool")
    private boolean likeStatus;
    @Schema(title = "category", name = "category", defaultValue = "1", type = "int")
    private PasswordCategoryEnum category;
    @Schema(title = "lastViewTime", name = "lastViewTime", defaultValue = "2025-08-01 20:30:40", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime lastViewTime;
}