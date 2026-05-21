/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @FileName: FamilyLogUtilTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-20:40
 * @Description: 家庭日志工具类测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.core
 * @ClassName: FamilyLogUtilTest
 * @Author: atluofu
 * @CreateTime: 2026-05-21 20:40
 * @Description: 家庭日志工具类测试
 * @Version: 1.0
 */
class FamilyLogUtilTest {

    /**
     * 清理测试线程 MDC。
     */
    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    /**
     * 校验工具类可以生成 traceId 并清理 family 日志键。
     */
    @Test
    void shouldGenerateTraceIdAndClearFamilyKeys() {
        String traceId = FamilyLogUtil.putTraceIdIfAbsent();
        FamilyLogUtil.putIfNotBlank(FamilyLogMdcKeys.REQUEST_ID, "request_1");
        FamilyLogUtil.clearFamilyKeys();

        assertThat(traceId).isNotBlank();
        assertThat(MDC.get(FamilyLogMdcKeys.TRACE_ID)).isNull();
        assertThat(MDC.get(FamilyLogMdcKeys.REQUEST_ID)).isNull();
    }

    /**
     * 校验包装 Runnable 时可以透传父线程 MDC 并恢复执行线程原始 MDC。
     */
    @Test
    void shouldWrapRunnableWithParentContextAndRestoreCurrentContext() {
        MDC.put(FamilyLogMdcKeys.TRACE_ID, "trace_parent");
        Runnable runnable = FamilyLogUtil.wrap(() -> {
            assertThat(MDC.get(FamilyLogMdcKeys.TRACE_ID)).isEqualTo("trace_parent");
            MDC.put(FamilyLogMdcKeys.REQUEST_ID, "request_from_child");
        });

        MDC.put(FamilyLogMdcKeys.TRACE_ID, "trace_current");
        MDC.put("customKey", "customValue");
        runnable.run();

        assertThat(MDC.get(FamilyLogMdcKeys.TRACE_ID)).isEqualTo("trace_current");
        assertThat(MDC.get("customKey")).isEqualTo("customValue");
        assertThat(MDC.get(FamilyLogMdcKeys.REQUEST_ID)).isNull();
    }

    /**
     * 校验虚拟线程执行 TraceRouteRunnable 时可以读取父线程 MDC。
     *
     * @throws ExecutionException   任务执行异常
     * @throws InterruptedException 线程中断异常
     */
    @Test
    void shouldPropagateContextToVirtualThreadWithTraceRouteRunnable() throws ExecutionException, InterruptedException {
        AtomicReference<String> childTraceId = new AtomicReference<>();
        MDC.put(FamilyLogMdcKeys.TRACE_ID, "trace_virtual_parent");

        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> future = executorService.submit(new TraceRouteRunnable() {
                /**
                 * 执行虚拟线程断言。
                 */
                @Override
                protected void doRun() {
                    childTraceId.set(MDC.get(FamilyLogMdcKeys.TRACE_ID));
                }
            });
            future.get();
        }

        assertThat(childTraceId.get()).isEqualTo("trace_virtual_parent");
        assertThat(MDC.get(FamilyLogMdcKeys.TRACE_ID)).isEqualTo("trace_virtual_parent");
    }

    /**
     * 校验业务日志构建器按稳定字段顺序输出结构化日志。
     */
    @Test
    void shouldWriteStructuredBusinessInfoLog() {
        ListAppender<ILoggingEvent> appender = attachListAppender();
        Logger logger = (Logger) LoggerFactory.getLogger(FamilyLogUtilTest.class);

        FamilyLogUtil.bizInfo(logger)
                .biz("password")
                .scene("create")
                .step("start")
                .phase(FamilyLogUtil.Phase.START)
                .bizId("password_1")
                .field("phone", "13812345678")
                .success("创建密码记录成功");

        ILoggingEvent event = appender.list.getFirst();
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getFormattedMessage()).contains(
                "biz=password scene=create step=start phase=START biz_id=password_1",
                "result=SUCCESS",
                "msg=创建密码记录成功",
                "phone=138****5678"
        );
    }

    /**
     * 校验业务拒绝和失败日志使用固定级别并带上原因和异常信息。
     */
    @Test
    void shouldWriteRejectAndFailBusinessLogWithExpectedLevel() {
        ListAppender<ILoggingEvent> appender = attachListAppender();
        Logger logger = (Logger) LoggerFactory.getLogger(FamilyLogUtilTest.class);

        FamilyLogUtil.bizWarn(logger)
                .biz("password")
                .scene("create")
                .step("check_permission")
                .reject("无权限创建密码记录");
        FamilyLogUtil.bizError(logger)
                .biz("password")
                .scene("create")
                .step("save")
                .errorCode("PASSWORD_SAVE_FAIL")
                .fail("保存密码记录异常", new IllegalStateException("database down"));

        List<ILoggingEvent> events = appender.list;
        assertThat(events.get(0).getLevel()).isEqualTo(Level.WARN);
        assertThat(events.get(0).getFormattedMessage()).contains("result=REJECT", "reason=无权限创建密码记录");
        assertThat(events.get(1).getLevel()).isEqualTo(Level.ERROR);
        assertThat(events.get(1).getFormattedMessage()).contains(
                "result=FAIL",
                "error_code=PASSWORD_SAVE_FAIL",
                "error_msg=\"database down\"",
                "reason=保存密码记录异常"
        );
        assertThat(events.get(1).getThrowableProxy()).isNotNull();
    }

    /**
     * 绑定测试日志收集器。
     *
     * @return ListAppender<ILoggingEvent> 返回日志收集器
     */
    private ListAppender<ILoggingEvent> attachListAppender() {
        Logger logger = (Logger) LoggerFactory.getLogger(FamilyLogUtilTest.class);
        logger.setLevel(Level.DEBUG);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }
}
