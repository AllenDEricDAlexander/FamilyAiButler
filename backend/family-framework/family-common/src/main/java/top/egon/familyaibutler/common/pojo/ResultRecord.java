package top.egon.familyaibutler.common.pojo;

import lombok.Builder;
import lombok.With;
import lombok.experimental.Accessors;
import top.egon.familyaibutler.common.enums.ResultCode;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

import java.io.Serial;
import java.io.Serializable;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.pojo
 * @ClassName: Results
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-01Day-18:38
 * @Description: Result Record 类型
 * 泛型类型的返回包装类
 * @Version: 1.0
 */
@With
@Builder
@Accessors(chain = true)
@DocModel(name = "ResultRecord", description = "接口统一返回 Record 对象")
public record ResultRecord<T>(
        @DocField(description = "业务状态码", example = "0")
        Integer code,
        @DocField(description = "提示信息", example = "success")
        String message,
        @DocField(description = "是否成功", example = "true")
        Boolean success,
        @DocField(description = "响应数据")
        T data) implements Serializable {

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
