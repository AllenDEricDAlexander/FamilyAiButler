/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.dubbo
 * @FileName: FamilyDubboTraceFilter.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:35
 * @Description: Dubbo 链路透传过滤器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.dubbo;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import top.egon.familyaibutler.framework.log.core.FamilyLogContext;
import top.egon.familyaibutler.framework.log.core.FamilyLogMdcKeys;
import top.egon.familyaibutler.framework.log.core.FamilyLogUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.dubbo
 * @ClassName: FamilyDubboTraceFilter
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:35
 * @Description: Dubbo 链路透传过滤器
 * @Version: 1.0
 */
@Activate(group = {CommonConstants.CONSUMER, CommonConstants.PROVIDER}, order = -10000)
public class FamilyDubboTraceFilter implements Filter {

    /**
     * 为 Dubbo 调用透传和恢复日志上下文。
     *
     * @param invoker    Dubbo 调用执行器
     * @param invocation Dubbo 调用对象
     * @return Result 返回 Dubbo 调用结果
     * @throws RpcException Dubbo 调用异常
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (isConsumerSide()) {
            writeConsumerAttachments(invocation);
            return invoker.invoke(invocation);
        }
        FamilyLogContext previousContext = FamilyLogContext.capture();
        FamilyLogContext requestContext = FamilyLogContext.fromMap(buildProviderContext(invoker, invocation));
        try {
            requestContext.applyToMdc();
            return invoker.invoke(invocation);
        } finally {
            previousContext.writeToMdc();
        }
    }

    /**
     * 将 consumer 侧上下文透传到 Dubbo attachment。
     *
     * @param invocation Dubbo 调用对象
     */
    private void writeConsumerAttachments(Invocation invocation) {
        Map<String, String> contextMap = FamilyLogUtil.copyFamilyContext();
        for (Map.Entry<String, String> entry : FamilyLogMdcKeys.propagationHeaderToKeyMap().entrySet()) {
            String headerValue = contextMap.get(entry.getValue());
            if (!FamilyLogUtil.isBlank(headerValue)) {
                invocation.setAttachment(entry.getValue(), headerValue);
            }
        }
    }

    /**
     * 构建 provider 侧日志上下文。
     *
     * @param invoker    Dubbo 调用执行器
     * @param invocation Dubbo 调用对象
     * @return Map<String, String> 返回 provider 侧日志上下文
     */
    private Map<String, String> buildProviderContext(Invoker<?> invoker, Invocation invocation) {
        Map<String, String> contextMap = new LinkedHashMap<>();
        contextMap.put(FamilyLogMdcKeys.TRACE_ID, resolveAttachment(invocation, FamilyLogMdcKeys.TRACE_ID));
        contextMap.put(FamilyLogMdcKeys.REQUEST_ID, resolveAttachment(invocation, FamilyLogMdcKeys.REQUEST_ID));
        contextMap.put(FamilyLogMdcKeys.ACCOUNT_ID, resolveAttachment(invocation, FamilyLogMdcKeys.ACCOUNT_ID));
        contextMap.put(FamilyLogMdcKeys.PROFILE_ID, resolveAttachment(invocation, FamilyLogMdcKeys.PROFILE_ID));
        contextMap.put(FamilyLogMdcKeys.CLIENT_ID, resolveAttachment(invocation, FamilyLogMdcKeys.CLIENT_ID));
        contextMap.put(FamilyLogMdcKeys.SESSION_ID, resolveAttachment(invocation, FamilyLogMdcKeys.SESSION_ID));
        contextMap.put(FamilyLogMdcKeys.DEVICE_ID, resolveAttachment(invocation, FamilyLogMdcKeys.DEVICE_ID));
        contextMap.put(FamilyLogMdcKeys.RISK_LEVEL, resolveAttachment(invocation, FamilyLogMdcKeys.RISK_LEVEL));
        contextMap.put(FamilyLogMdcKeys.RPC_SYSTEM, "dubbo");
        contextMap.put(FamilyLogMdcKeys.RPC_SERVICE, invoker.getInterface().getName());
        contextMap.put(FamilyLogMdcKeys.RPC_METHOD, invocation.getMethodName());
        if (FamilyLogUtil.isBlank(contextMap.get(FamilyLogMdcKeys.TRACE_ID))) {
            contextMap.put(FamilyLogMdcKeys.TRACE_ID, FamilyLogUtil.newId());
        }
        if (FamilyLogUtil.isBlank(contextMap.get(FamilyLogMdcKeys.REQUEST_ID))) {
            contextMap.put(FamilyLogMdcKeys.REQUEST_ID, FamilyLogUtil.newId());
        }
        return contextMap;
    }

    /**
     * 读取 attachment 中的上下文字段。
     *
     * @param invocation Dubbo 调用对象
     * @param key        上下文字段键
     * @return String 返回上下文字段值
     */
    private String resolveAttachment(Invocation invocation, String key) {
        String attachmentValue = invocation.getAttachment(key);
        return FamilyLogUtil.isBlank(attachmentValue) ? null : attachmentValue;
    }

    /**
     * 判断当前 Dubbo 调用是否为 consumer 侧。
     *
     * @return boolean 返回 true 表示 consumer 侧
     */
    private boolean isConsumerSide() {
        try {
            return RpcContext.getServiceContext().isConsumerSide();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
