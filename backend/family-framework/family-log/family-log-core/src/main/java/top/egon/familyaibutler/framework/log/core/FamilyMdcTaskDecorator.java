/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @FileName: FamilyMdcTaskDecorator.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-20:40
 * @Description: 家庭日志线程池装饰器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.core;

import org.springframework.core.task.TaskDecorator;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @ClassName: FamilyMdcTaskDecorator
 * @Author: atluofu
 * @CreateTime: 2026-05-21 20:40
 * @Description: 家庭日志线程池装饰器
 * @Version: 1.0
 */
public class FamilyMdcTaskDecorator implements TaskDecorator {

    /**
     * 为异步任务补齐父线程日志上下文。
     *
     * @param runnable 原始异步任务
     * @return Runnable 返回包装后的异步任务
     */
    @Override
    public Runnable decorate(Runnable runnable) {
        return FamilyLogUtil.wrap(runnable);
    }
}
