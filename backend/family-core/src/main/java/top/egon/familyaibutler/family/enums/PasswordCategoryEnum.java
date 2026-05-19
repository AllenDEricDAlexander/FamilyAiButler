package top.egon.familyaibutler.family.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.enums
 * @ClassName: PasswordCategoryEnum
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-03Day-21:56
 * @Description: 账号类型
 * @Version: 1.0
 */
@Getter
public enum PasswordCategoryEnum {

    WEB("Web", 1),
    APP("App", 2),
    EMAIL("Email", 3);

    private final String category;
    @EnumValue
    @JsonValue
    private final Integer value;

    PasswordCategoryEnum(String category, Integer value) {
        this.category = category;
        this.value = value;
    }

    public static PasswordCategoryEnum getByCategory(String category) {
        for (PasswordCategoryEnum value : values()) {
            if (value.getCategory().equalsIgnoreCase(category)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown password category: " + category);
    }

}
