package top.egon.familyaibutler.family.domain.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.model.enums
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

    /**
     * 转换接口传入的账号类型。
     *
     * @param source 接口传入的账号类型
     * @return 账号类型枚举
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PasswordCategoryEnum fromJson(Object source) {
        if (source instanceof Number number) {
            return getByValue(number.intValue());
        }
        String category = String.valueOf(source);
        for (PasswordCategoryEnum value : values()) {
            if (value.name().equalsIgnoreCase(category)
                    || value.getCategory().equalsIgnoreCase(category)
                    || String.valueOf(value.getValue()).equals(category)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown password category: " + category);
    }

    /**
     * 根据枚举值获取账号类型。
     *
     * @param value 枚举值
     * @return 账号类型枚举
     */
    public static PasswordCategoryEnum getByValue(Integer value) {
        for (PasswordCategoryEnum passwordCategoryEnum : values()) {
            if (passwordCategoryEnum.getValue().equals(value)) {
                return passwordCategoryEnum;
            }
        }
        throw new IllegalArgumentException("Unknown password category value: " + value);
    }

    /**
     * 获取接口返回的账号类型。
     *
     * @return 账号类型名称
     */
    @JsonValue
    public String getJsonValue() {
        return category;
    }

}
