package top.egon.familyaibutler.common.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.With;
import lombok.experimental.Accessors;
import top.egon.familyaibutler.common.enums.ResultCode;

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
 * * 泛型类型的返回包装类  不能类注释@Schema openapi3 生成有问题
 * @Version: 1.0
 */
@With
@Builder
@Accessors(chain = true)
public record PageResultRecord<T>(
        @Schema(title = "状态码", name = "code", defaultValue = "10000", type = "int") Integer code,
        @Schema(title = "描述", name = "message", defaultValue = "10000", type = "String") String message,
        @Schema(title = "响应状态", name = "success", defaultValue = "true", type = "bool") Boolean success,
        @Schema(title = "返回结果", name = "data", defaultValue = "test", type = "T") T data,
        @Schema(title = "总量", name = "total", defaultValue = "100", type = "long") Long total,
        @Schema(title = "页号", name = "pageNum", defaultValue = "0", type = "int") Long pageNum,
        @Schema(title = "页大小", name = "pageSize", defaultValue = "10", type = "int") Long pageSize) implements Serializable {
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
