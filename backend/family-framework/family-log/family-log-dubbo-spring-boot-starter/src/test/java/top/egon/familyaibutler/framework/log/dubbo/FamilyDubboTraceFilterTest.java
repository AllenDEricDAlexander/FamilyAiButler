/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.dubbo
 * @FileName: FamilyDubboTraceFilterTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-12:15
 * @Description: Dubbo 链路透传过滤器测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.dubbo;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import top.egon.familyaibutler.framework.log.core.FamilyLogMdcKeys;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.dubbo
 * @ClassName: FamilyDubboTraceFilterTest
 * @Author: atluofu
 * @CreateTime: 2026-05-21 12:15
 * @Description: Dubbo 链路透传过滤器测试
 * @Version: 1.0
 */
class FamilyDubboTraceFilterTest {

    /**
     * 清理测试上下文。
     */
    @AfterEach
    void clearContext() {
        MDC.clear();
        RpcContext.removeServiceContext();
    }

    /**
     * 校验 Dubbo consumer 侧会把 MDC 写入 attachment。
     */
    @Test
    void shouldWriteConsumerAttachmentsFromMdc() {
        FamilyDubboTraceFilter filter = new FamilyDubboTraceFilter();
        Result result = resultProxy();
        Invoker<?> invoker = invokerProxy(invocation -> result);
        Invocation invocation = invocationProxy("sayHello");
        RpcContext.getServiceContext().setUrl(URL.valueOf("dubbo://127.0.0.1/demo?side=consumer"));
        MDC.put(FamilyLogMdcKeys.TRACE_ID, "trace_dubbo");
        MDC.put(FamilyLogMdcKeys.REQUEST_ID, "request_dubbo");

        Result actual = filter.invoke(invoker, invocation);

        assertThat(actual).isSameAs(result);
        assertThat(invocation.getAttachment(FamilyLogMdcKeys.TRACE_ID)).isEqualTo("trace_dubbo");
        assertThat(invocation.getAttachment(FamilyLogMdcKeys.REQUEST_ID)).isEqualTo("request_dubbo");
    }

    /**
     * 校验 Dubbo provider 侧会从 attachment 恢复 MDC 并在结束后还原。
     */
    @Test
    void shouldReadProviderAttachmentsIntoMdcAndRestorePreviousContext() {
        FamilyDubboTraceFilter filter = new FamilyDubboTraceFilter();
        Result result = resultProxy();
        Invocation invocation = invocationProxy("sayHello");
        Invoker<DemoService> invoker = invokerProxy(answer -> {
            assertThat(MDC.get(FamilyLogMdcKeys.TRACE_ID)).isEqualTo("trace_provider");
            assertThat(MDC.get(FamilyLogMdcKeys.RPC_SYSTEM)).isEqualTo("dubbo");
            assertThat(MDC.get(FamilyLogMdcKeys.RPC_METHOD)).isEqualTo("sayHello");
            return result;
        });
        RpcContext.getServiceContext().setUrl(URL.valueOf("dubbo://127.0.0.1/demo?side=provider"));
        MDC.put("customKey", "customValue");
        invocation.setAttachment(FamilyLogMdcKeys.TRACE_ID, "trace_provider");
        invocation.setAttachment(FamilyLogMdcKeys.REQUEST_ID, "request_provider");

        Result actual = filter.invoke(invoker, invocation);

        assertThat(actual).isSameAs(result);
        assertThat(MDC.get("customKey")).isEqualTo("customValue");
        assertThat(MDC.get(FamilyLogMdcKeys.TRACE_ID)).isNull();
    }

    /**
     * 测试用 Dubbo 服务接口。
     */
    private interface DemoService {
    }

    /**
     * 创建测试用 Dubbo Invocation。
     *
     * @param methodName 方法名称
     * @return Invocation 返回测试调用对象
     */
    private Invocation invocationProxy(String methodName) {
        Map<String, String> attachments = new HashMap<>();
        return (Invocation) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Invocation.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getMethodName" -> methodName;
                    case "getAttachment" -> attachments.get((String) args[0]);
                    case "setAttachment" -> {
                        attachments.put((String) args[0], String.valueOf(args[1]));
                        yield null;
                    }
                    case "getAttachments" -> attachments;
                    default -> defaultValue(method.getReturnType());
                });
    }

    /**
     * 创建测试用 Dubbo Invoker。
     *
     * @param invokeFunction 调用回调
     * @param <T>            服务接口类型
     * @return Invoker<T> 返回测试执行器
     */
    private <T> Invoker<T> invokerProxy(Function<Invocation, Result> invokeFunction) {
        return (Invoker<T>) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Invoker.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getInterface" -> DemoService.class;
                    case "invoke" -> invokeFunction.apply((Invocation) args[0]);
                    default -> defaultValue(method.getReturnType());
                });
    }

    /**
     * 创建测试用 Dubbo Result。
     *
     * @return Result 返回测试结果
     */
    private Result resultProxy() {
        return (Result) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Result.class},
                (proxy, method, args) -> defaultValue(method.getReturnType()));
    }

    /**
     * 返回接口代理默认值。
     *
     * @param returnType 返回值类型
     * @return Object 返回默认值
     */
    private Object defaultValue(Class<?> returnType) {
        if (Boolean.TYPE.equals(returnType)) {
            return false;
        }
        if (Integer.TYPE.equals(returnType) || Long.TYPE.equals(returnType) || Short.TYPE.equals(returnType)
                || Byte.TYPE.equals(returnType) || Double.TYPE.equals(returnType) || Float.TYPE.equals(returnType)) {
            return 0;
        }
        return null;
    }
}
