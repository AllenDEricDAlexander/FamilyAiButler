/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.servlet
 * @FileName: FamilyServletLogAspect.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:10
 * @Description: Servlet 控制器日志切面文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.multipart.MultipartFile;
import top.egon.familyaibutler.framework.log.core.FamilyLogProperties;

import java.lang.reflect.Method;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.servlet
 * @ClassName: FamilyServletLogAspect
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:10
 * @Description: Servlet 控制器日志切面
 * @Version: 1.0
 */
@Aspect
@Slf4j
public class FamilyServletLogAspect {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final FamilyLogProperties properties;

    public FamilyServletLogAspect(FamilyLogProperties properties) {
        this.properties = properties;
    }

    /**
     * 定义 Controller 接口日志切点。
     */
    @Pointcut("execution(public * top.egon..*Controller.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 记录 Controller 请求入参、出参与耗时。
     *
     * @param proceedingJoinPoint AOP 执行上下文
     * @return Object 返回接口执行结果
     * @throws Throwable 原始接口异常
     */
    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (!properties.isRequestLogEnabled()) {
            return proceedingJoinPoint.proceed();
        }
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = methodSignature.getMethod();
        long startTime = System.currentTimeMillis();
        log.info("===============请求内容===============");
        log.info("请求地址:{}", proceedingJoinPoint.getTarget().getClass().getName());
        log.info("请求方式:{}", method);
        log.info("请求类方法:{}", proceedingJoinPoint.getSignature().getName());
        log.info("请求参数: {}", toJsonSafely(filterArguments(proceedingJoinPoint.getArgs())));
        log.info("===============请求内容===============");
        Object result = proceedingJoinPoint.proceed();
        log.info("===============返回内容===============");
        log.info("返回参数: {}", truncate(toJsonSafely(result)));
        log.info("------------- 结束 耗时：{} ms -------------", System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * 过滤 Servlet 与上传对象，避免日志序列化失败。
     *
     * @param args 原始参数列表
     * @return Object[] 返回过滤后的参数列表
     */
    private Object[] filterArguments(Object[] args) {
        Object[] arguments = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof ServletRequest || arg instanceof ServletResponse || arg instanceof MultipartFile) {
                continue;
            }
            arguments[i] = arg;
        }
        return arguments;
    }

    /**
     * 安全地序列化日志对象。
     *
     * @param target 待序列化对象
     * @return String 返回序列化结果
     */
    private String toJsonSafely(Object target) {
        try {
            return truncate(OBJECT_MAPPER.writeValueAsString(target));
        } catch (Exception exception) {
            return truncate(String.valueOf(target));
        }
    }

    /**
     * 根据配置截断日志载荷。
     *
     * @param payload 原始日志载荷
     * @return String 返回截断后的日志载荷
     */
    private String truncate(String payload) {
        if (payload == null || payload.length() <= properties.getMaxPayloadLength()) {
            return payload;
        }
        return payload.substring(0, properties.getMaxPayloadLength()) + "...";
    }
}
