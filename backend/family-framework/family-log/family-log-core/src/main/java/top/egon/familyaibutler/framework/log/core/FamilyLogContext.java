/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @FileName: FamilyLogContext.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-20:40
 * @Description: 家庭日志上下文快照文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @ClassName: FamilyLogContext
 * @Author: atluofu
 * @CreateTime: 2026-05-21 20:40
 * @Description: 家庭日志上下文快照
 * @Version: 1.0
 */
public final class FamilyLogContext {

    private final Map<String, String> contextMap;

    private FamilyLogContext(Map<String, String> contextMap) {
        this.contextMap = Collections.unmodifiableMap(new LinkedHashMap<>(contextMap));
    }

    /**
     * 捕获当前线程 MDC 快照。
     *
     * @return FamilyLogContext 返回当前线程 MDC 快照
     */
    public static FamilyLogContext capture() {
        return new FamilyLogContext(FamilyLogUtil.copyContext());
    }

    /**
     * 使用已有上下文构建日志快照。
     *
     * @param contextMap 上下文映射
     * @return FamilyLogContext 返回日志快照
     */
    public static FamilyLogContext fromMap(Map<String, String> contextMap) {
        return new FamilyLogContext(contextMap == null ? Collections.emptyMap() : contextMap);
    }

    /**
     * 获取日志快照明细。
     *
     * @return Map<String, String> 返回日志快照明细
     */
    public Map<String, String> contextMap() {
        return contextMap;
    }

    /**
     * 将快照写回当前线程 MDC。
     */
    public void writeToMdc() {
        FamilyLogUtil.restore(contextMap);
    }

    /**
     * 将快照叠加到当前线程 MDC。
     */
    public void applyToMdc() {
        FamilyLogUtil.apply(contextMap);
    }
}
