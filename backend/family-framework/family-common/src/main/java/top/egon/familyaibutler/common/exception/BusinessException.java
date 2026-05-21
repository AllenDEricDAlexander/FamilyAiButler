package top.egon.familyaibutler.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @BelongsProject: top\egon\familyaibutler\common\exception\BusinessException.java
 * @BelongsPackage: top.egon.familyaibutler.common.exception
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

    public BusinessException(String exception) {
        this.exception = exception;
    }

    /**
     * 不写入堆栈信息，提高性能
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}

