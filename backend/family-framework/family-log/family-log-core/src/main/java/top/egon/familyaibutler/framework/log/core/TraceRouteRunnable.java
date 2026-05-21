/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @FileName: TraceRouteRunnable.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-20:40
 * @Description: 链路透传 Runnable 抽象类文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.core;

import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @ClassName: TraceRouteRunnable
 * @Author: atluofu
 * @CreateTime: 2026-05-21 20:40
 * @Description: 链路透传 Runnable 抽象类
 * @Version: 1.0
 */
public abstract class TraceRouteRunnable implements Runnable {

    private final Map<String, String> parentContext;

    protected TraceRouteRunnable() {
        this.parentContext = FamilyLogUtil.copyContext();
    }

    /**
     * 执行带日志上下文的任务。
     */
    @Override
    public final void run() {
        Map<String, String> childContext = FamilyLogUtil.copyContext();
        try {
            FamilyLogUtil.restore(parentContext);
            doRun();
        } finally {
            FamilyLogUtil.restore(childContext);
        }
    }

    /**
     * 执行实际业务逻辑。
     */
    protected abstract void doRun();
}
