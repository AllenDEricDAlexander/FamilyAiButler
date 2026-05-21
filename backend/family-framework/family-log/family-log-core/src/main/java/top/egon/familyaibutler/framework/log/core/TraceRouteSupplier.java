/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @FileName: TraceRouteSupplier.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-20:40
 * @Description: 链路透传 Supplier 抽象类文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.core;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @param <T> 返回值类型
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @ClassName: TraceRouteSupplier
 * @Author: atluofu
 * @CreateTime: 2026-05-21 20:40
 * @Description: 链路透传 Supplier 抽象类
 * @Version: 1.0
 */
public abstract class TraceRouteSupplier<T> implements Supplier<T> {

    private final Map<String, String> parentContext;

    protected TraceRouteSupplier() {
        this.parentContext = FamilyLogUtil.copyContext();
    }

    /**
     * 执行带日志上下文的任务。
     *
     * @return T 返回任务执行结果
     */
    @Override
    public final T get() {
        Map<String, String> childContext = FamilyLogUtil.copyContext();
        try {
            FamilyLogUtil.restore(parentContext);
            return doGet();
        } finally {
            FamilyLogUtil.restore(childContext);
        }
    }

    /**
     * 执行实际业务逻辑。
     *
     * @return T 返回任务执行结果
     */
    protected abstract T doGet();
}
