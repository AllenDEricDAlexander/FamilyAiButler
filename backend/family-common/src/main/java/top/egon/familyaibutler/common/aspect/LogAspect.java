package top.egon.familyaibutler.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;


/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.aspect
 * @ClassName: logAspect
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-01Day-21:29
 * @Description: 全局统一接口监控
 * PS: 入参出参对象必须脱敏处理，使用 gson提供的 @Expose 去排除字段 如 身份证、手机号、邮箱、密码等
 * @Version: 1.0
 */

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Pointcut("execution(public * top.egon..controller.*Controller.*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature ms = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = ms.getMethod();
        log.info("===============请求内容===============");
        log.info("请求地址:{}", proceedingJoinPoint.getTarget().getClass().getName());
        log.info("请求方式:{}", method);
        log.info("请求类方法:{}", proceedingJoinPoint.getSignature().getName());
        Object[] args = proceedingJoinPoint.getArgs();
        Object[] arguments = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof ServletRequest
                || args[i] instanceof ServletResponse
                || args[i] instanceof MultipartFile) {
                continue;
            }
            arguments[i] = args[i];
        }
        log.info("请求参数: {}", OBJECT_MAPPER.writeValueAsString(arguments));
        log.info("===============请求内容===============");
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        log.info("===============返回内容===============");
        log.info("返回参数: {}", OBJECT_MAPPER.writeValueAsString(result));
        log.info("------------- 结束 耗时：{} ms -------------", System.currentTimeMillis() - startTime);
        return result;
    }

}