package top.egon.familyaibutler.common.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;
import lombok.experimental.Accessors;
import top.egon.familyaibutler.common.enums.ResultCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.pojo
 * @ClassName: Results
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-01Day-18:38
 * @Description: Result Record 类型
 * 泛型类型的返回包装类  不能类注释@Schema openapi3 生成有问题
 * @Version: 1.0
 */
@With
@Builder
@Accessors(chain = true)
public record ResultRecord<T>(
        @Schema(title = "状态码", name = "code", defaultValue = "10000", type = "int") Integer code,
        @Schema(title = "描述", name = "message", defaultValue = "10000", type = "String") String message,
        @Schema(title = "响应状态", name = "success", defaultValue = "true", type = "bool") Boolean success,
        @Schema(title = "返回结果", name = "data", defaultValue = "test", type = "T") T data) implements Serializable {

    @Serial
    private static final long serialVersionUID = -4633117999621557174L;

    public static <T> ResultRecord<T> success(T res) {
        return new ResultRecord<>(ResultCode.SUCCESS.getCode(), "success", true, res);
    }

    public static <T> ResultRecord<T> error(Integer errorCode, String msg, T res) {
        return new ResultRecord<>(errorCode, msg, true, res);
    }

    public static <T> ResultRecord<T> result(Integer code, String message, Boolean success, T res) {
        return new ResultRecord<>(code, message, success, res);
    }

}
