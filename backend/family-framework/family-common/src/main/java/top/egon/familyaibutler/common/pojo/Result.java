package top.egon.familyaibutler.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import top.egon.familyaibutler.common.enums.ResultCode;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.pojo
 * @ClassName: Result
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-01Day-18:38
 * @Description: 接口统一返回对象
 * * 泛型类型的返回包装类
 * @Version: 1.0
 */
@Data
@With
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@DocModel(name = "Result", description = "接口统一返回对象")
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -4633117999621557173L;

    @DocField(description = "业务状态码", example = "0")
    private Integer code;
    @DocField(description = "提示信息", example = "success")
    private String message;
    @DocField(description = "是否成功", example = "true")
    private Boolean success;
    @DocField(description = "响应时间戳", example = "1770000000000")
    private Long timestamp;
    @DocField(description = "响应数据")
    private T data;

    public static <T> Result<T> success(T res) {
        return new Result<>(ResultCode.SUCCESS.getCode(), "success", true, System.currentTimeMillis(), res);
    }

    public static <T> Result<T> fail(Integer errorCode, String msg, T res) {
        return new Result<>(errorCode, msg, true, System.currentTimeMillis(), res);
    }

    public static <T> Result<T> result(Integer code, String message, Boolean success, T res) {
        return new Result<>(code, message, success, System.currentTimeMillis(), res);
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream stream)
            throws IOException {
        stream.defaultWriteObject();
    }

    @Serial
    private void readObject(java.io.ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }

}
