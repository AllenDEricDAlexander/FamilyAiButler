package top.egon.familyaibutler.common.pojo;

import lombok.Builder;
import lombok.With;
import lombok.experimental.Accessors;
import top.egon.familyaibutler.common.enums.ResultCode;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.pojo
 * @ClassName: PageResultRecord
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-01Day-19:44
 * @Description: PageResult Record 类型
 * * 泛型类型的返回包装类
 * @Version: 1.0
 */
@With
@Builder
@Accessors(chain = true)
@DocModel(name = "PageResultRecord", description = "接口统一返回分页 Record 对象")
public record PageResultRecord<T>(
        @DocField(description = "业务状态码", example = "0")
        Integer code,
        @DocField(description = "提示信息", example = "success")
        String message,
        @DocField(description = "是否成功", example = "true")
        Boolean success,
        @DocField(description = "响应数据")
        T data,
        @DocField(description = "总数", example = "100")
        Long total,
        @DocField(description = "页码", example = "1")
        Long pageNum,
        @DocField(description = "页大小", example = "10")
        Long pageSize) implements Serializable {
    @Serial
    private static final long serialVersionUID = 8938942437517754680L;

    public static <T> PageResult<T> success(List<T> data, Long total, Long pageNum, Long pageSize) {
        return new PageResult<>(ResultCode.SUCCESS.getCode(), "success", true, data, pageNum, pageSize, total);
    }

    public static <T> PageResult<T> error(Integer code, String msg, List<T> data, Long total, Long pageNum, Long pageSize) {
        return new PageResult<>(code, msg, true, data, pageNum, pageSize, total);
    }

    public static <T> PageResult<T> pageResult(Integer code, String msg, Boolean success, List<T> data, Long total, Long pageNum, Long pageSize) {
        return new PageResult<>(code, msg, success, data, pageNum, pageSize, total);
    }
}
