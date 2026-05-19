package top.egon.familyaibutler.gateway.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handlerException(Exception e) {
        return e.getMessage();
    }
}