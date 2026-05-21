/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @FileName: TraceRouteCallable.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-20:40
 * @Description: 链路透传 Callable 抽象类文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.core;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @param <T> 返回值类型
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @ClassName: TraceRouteCallable
 * @Author: atluofu
 * @CreateTime: 2026-05-21 20:40
 * @Description: 链路透传 Callable 抽象类
 * @Version: 1.0
 */
public abstract class TraceRouteCallable<T> implements Callable<T> {

    private final Map<String, String> parentContext;

    protected TraceRouteCallable() {
        this.parentContext = FamilyLogUtil.copyContext();
    }

    /**
     * 执行带日志上下文的任务。
     *
     * @return T 返回任务执行结果
     * @throws Exception 原始任务异常
     */
    @Override
    public final T call() throws Exception {
        Map<String, String> childContext = FamilyLogUtil.copyContext();
        try {
            FamilyLogUtil.restore(parentContext);
            return doCall();
        } finally {
            FamilyLogUtil.restore(childContext);
        }
    }

    /**
     * 执行实际业务逻辑。
     *
     * @return T 返回任务执行结果
     * @throws Exception 原始任务异常
     */
    protected abstract T doCall() throws Exception;
}
