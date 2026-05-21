/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.grpc
 * @FileName: FamilyGrpcTraceServerInterceptor.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:35
 * @Description: gRPC 服务端链路透传拦截器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.grpc;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import top.egon.familyaibutler.framework.log.core.FamilyLogContext;
import top.egon.familyaibutler.framework.log.core.FamilyLogMdcKeys;
import top.egon.familyaibutler.framework.log.core.FamilyLogUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.grpc
 * @ClassName: FamilyGrpcTraceServerInterceptor
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:35
 * @Description: gRPC 服务端链路透传拦截器
 * @Version: 1.0
 */
public class FamilyGrpcTraceServerInterceptor implements ServerInterceptor {

    /**
     * 在 gRPC 服务端请求中恢复并清理日志上下文。
     *
     * @param call    gRPC 服务端调用
     * @param headers gRPC 元数据
     * @param next    gRPC 服务端调用处理器
     * @param <ReqT>  请求类型
     * @param <RespT> 响应类型
     * @return ServerCall.Listener<ReqT> 返回包装后的服务端监听器
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        FamilyLogContext requestContext = FamilyLogContext.fromMap(buildRequestContext(call, headers));
        FamilyLogContext previousContext = FamilyLogContext.capture();
        requestContext.applyToMdc();
        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);
        previousContext.writeToMdc();
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            /**
             * 在 onMessage 回调内恢复日志上下文。
             *
             * @param message 请求消息
             */
            @Override
            public void onMessage(ReqT message) {
                runWithContext(requestContext, () -> super.onMessage(message));
            }

            /**
             * 在 onHalfClose 回调内恢复日志上下文。
             */
            @Override
            public void onHalfClose() {
                runWithContext(requestContext, super::onHalfClose);
            }

            /**
             * 在 onCancel 回调内恢复日志上下文。
             */
            @Override
            public void onCancel() {
                runWithContext(requestContext, super::onCancel);
            }

            /**
             * 在 onComplete 回调内恢复日志上下文。
             */
            @Override
            public void onComplete() {
                runWithContext(requestContext, super::onComplete);
            }

            /**
             * 在 onReady 回调内恢复日志上下文。
             */
            @Override
            public void onReady() {
                runWithContext(requestContext, super::onReady);
            }
        };
    }

    /**
     * 构建服务端请求日志上下文。
     *
     * @param call    gRPC 服务端调用
     * @param headers gRPC 元数据
     * @param <ReqT>  请求类型
     * @param <RespT> 响应类型
     * @return Map<String, String> 返回服务端请求日志上下文
     */
    private <ReqT, RespT> Map<String, String> buildRequestContext(ServerCall<ReqT, RespT> call, Metadata headers) {
        Map<String, String> contextMap = new LinkedHashMap<>();
        contextMap.put(FamilyLogMdcKeys.TRACE_ID, headers.get(Metadata.Key.of(FamilyLogMdcKeys.HEADER_TRACE_ID, Metadata.ASCII_STRING_MARSHALLER)));
        contextMap.put(FamilyLogMdcKeys.REQUEST_ID, headers.get(Metadata.Key.of(FamilyLogMdcKeys.HEADER_REQUEST_ID, Metadata.ASCII_STRING_MARSHALLER)));
        contextMap.put(FamilyLogMdcKeys.ACCOUNT_ID, headers.get(Metadata.Key.of(FamilyLogMdcKeys.HEADER_ACCOUNT_ID, Metadata.ASCII_STRING_MARSHALLER)));
        contextMap.put(FamilyLogMdcKeys.PROFILE_ID, headers.get(Metadata.Key.of(FamilyLogMdcKeys.HEADER_PROFILE_ID, Metadata.ASCII_STRING_MARSHALLER)));
        contextMap.put(FamilyLogMdcKeys.CLIENT_ID, headers.get(Metadata.Key.of(FamilyLogMdcKeys.HEADER_CLIENT_ID, Metadata.ASCII_STRING_MARSHALLER)));
        contextMap.put(FamilyLogMdcKeys.SESSION_ID, headers.get(Metadata.Key.of(FamilyLogMdcKeys.HEADER_SESSION_ID, Metadata.ASCII_STRING_MARSHALLER)));
        contextMap.put(FamilyLogMdcKeys.DEVICE_ID, headers.get(Metadata.Key.of(FamilyLogMdcKeys.HEADER_DEVICE_ID, Metadata.ASCII_STRING_MARSHALLER)));
        contextMap.put(FamilyLogMdcKeys.RISK_LEVEL, headers.get(Metadata.Key.of(FamilyLogMdcKeys.HEADER_RISK_LEVEL, Metadata.ASCII_STRING_MARSHALLER)));
        contextMap.put(FamilyLogMdcKeys.RPC_SYSTEM, "grpc");
        contextMap.put(FamilyLogMdcKeys.RPC_SERVICE, call.getMethodDescriptor().getServiceName());
        contextMap.put(FamilyLogMdcKeys.RPC_METHOD, call.getMethodDescriptor().getBareMethodName());
        if (FamilyLogUtil.isBlank(contextMap.get(FamilyLogMdcKeys.TRACE_ID))) {
            contextMap.put(FamilyLogMdcKeys.TRACE_ID, FamilyLogUtil.newId());
        }
        if (FamilyLogUtil.isBlank(contextMap.get(FamilyLogMdcKeys.REQUEST_ID))) {
            contextMap.put(FamilyLogMdcKeys.REQUEST_ID, FamilyLogUtil.newId());
        }
        return contextMap;
    }

    /**
     * 在指定日志上下文中执行回调。
     *
     * @param requestContext 请求日志上下文
     * @param action         待执行回调
     */
    private void runWithContext(FamilyLogContext requestContext, Runnable action) {
        FamilyLogContext previousContext = FamilyLogContext.capture();
        try {
            requestContext.applyToMdc();
            action.run();
        } finally {
            previousContext.writeToMdc();
        }
    }
}
