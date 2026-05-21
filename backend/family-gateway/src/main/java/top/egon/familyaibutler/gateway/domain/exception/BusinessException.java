/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.domain.exception
 * @FileName: BusinessException.java
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-01Day-22:15
 * @Description: gateway 业务异常文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.domain.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @BelongsProject: top\egon\familyaibutler\common\exception\BusinessException.java
 * @BelongsPackage: top.egon.familyaibutler.gateway.domain.exception
 * @ClassName: BusinessException
 * @Author: atluofu
 * @CreateTime: 2025/8/1  22:15
 * @Description: 公共异常类
 * @Version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = 2876490965503610798L;

    private final String exception;

    /**
     * 创建 gateway 业务异常。
     *
     * @param exception 异常信息
     */
    public BusinessException(String exception) {
        this.exception = exception;
    }

    /**
     * 不写入堆栈信息，提高性能。
     *
     * @return Throwable 返回当前异常对象
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
