/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.adapter.handler
 * @FileName: GlobalExceptionHandler.java
 * @Author: atluofu
 * @CreateTime: 2025Year-07Month-31Day-22:42
 * @Description: gateway 全局异常处理器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.gateway.adapter.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.adapter.handler
 * @ClassName: GlobalExceptionHandler
 * @Author: atluofu
 * @CreateTime: 2025Year-07Month-31Day-22:42
 * @Description: 全局异常处理
 * @Version: 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理未捕获异常。
     *
     * @param e 异常对象
     * @return String 返回异常消息
     */
    @ExceptionHandler(Exception.class)
    public String handlerException(Exception e) {
        return e.getMessage();
    }
}
