package top.egon.familyaibutler.common.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.egon.familyaibutler.common.enums.ResultCode;
import top.egon.familyaibutler.common.pojo.Result;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.handler
 * @ClassName: GlobalExceptionHandler
 * @Author: atluofu
 * @CreateTime: 2025Year-07Month-31Day-22:42
 * @Description: 全局异常处理
 * @Version: 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handlerException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.INVALID_PARAM.getCode(), e.getMessage(), ResultCode.INVALID_PARAM.getMessage());
    }
}