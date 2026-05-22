package top.egon.familyaibutler.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.With;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import top.egon.familyaibutler.common.enums.ResultCode;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

import java.io.Serial;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.pojo
 * @ClassName: PageResult
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-01Day-19:14
 * @Description: 接口统一返回分页对象
 * * 泛型类型的返回包装类
 * @Version: 1.0
 */
@Data
@With
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(builderMethodName = "PageResultBuilder")
@DocModel(name = "PageResult", description = "接口统一返回分页对象")
public class PageResult<T> extends Result<List<T>> {
    @Serial
    private static final long serialVersionUID = 8938942437517754689L;

    @DocField(description = "总数", example = "100")
    private Long total;
    @DocField(description = "页码", example = "1")
    private Long pageNum;
    @DocField(description = "页大小", example = "10")
    private Long pageSize;

    public static <T> PageResult<T> success(List<T> data, Long total, Long pageNum, Long pageSize) {
        return new PageResult<>(ResultCode.SUCCESS.getCode(), "success", true, data, pageNum, pageSize, total);
    }

    public static <T> PageResult<T> fail(Integer code, String msg, List<T> data, Long total, Long pageNum, Long pageSize) {
        return new PageResult<>(code, msg, true, data, pageNum, pageSize, total);
    }

    public static <T> PageResult<T> pageResult(Integer code, String msg, Boolean success, List<T> data, Long total, Long pageNum, Long pageSize) {
        return new PageResult<>(code, msg, success, data, pageNum, pageSize, total);
    }

    public PageResult(Integer code, String message, Boolean success, List<T> data, Long pageNum, Long pageSize, Long total) {
        super(code, message, success, System.currentTimeMillis(), data);
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
    }
}
