/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.grpc
 * @FileName: FamilyGrpcTraceClientInterceptor.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:35
 * @Description: gRPC 客户端链路透传拦截器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import top.egon.familyaibutler.framework.log.core.FamilyLogMdcKeys;
import top.egon.familyaibutler.framework.log.core.FamilyLogUtil;

import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.grpc
 * @ClassName: FamilyGrpcTraceClientInterceptor
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:35
 * @Description: gRPC 客户端链路透传拦截器
 * @Version: 1.0
 */
public class FamilyGrpcTraceClientInterceptor implements ClientInterceptor {

    /**
     * 在 gRPC 客户端请求中写入日志透传元数据。
     *
     * @param method      gRPC 方法描述
     * @param callOptions gRPC 调用选项
     * @param next        gRPC 通道
     * @param <ReqT>      请求类型
     * @param <RespT>     响应类型
     * @return ClientCall<ReqT, RespT> 返回包装后的 gRPC 调用
     */
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
                                                               CallOptions callOptions, Channel next) {
        ClientCall<ReqT, RespT> clientCall = next.newCall(method, callOptions);
        return new ForwardingClientCall.SimpleForwardingClientCall<>(clientCall) {
            /**
             * 写入日志透传元数据后启动调用。
             *
             * @param responseListener 响应监听器
             * @param headers          gRPC 元数据
             */
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                applyHeaders(headers, FamilyLogUtil.copyFamilyContext());
                super.start(responseListener, headers);
            }
        };
    }

    /**
     * 将日志透传字段写入 gRPC Metadata。
     *
     * @param headers    gRPC 元数据
     * @param contextMap 日志上下文
     */
    private void applyHeaders(Metadata headers, Map<String, String> contextMap) {
        for (Map.Entry<String, String> entry : FamilyLogMdcKeys.propagationHeaderToKeyMap().entrySet()) {
            String headerValue = contextMap.get(entry.getValue());
            if (!FamilyLogUtil.isBlank(headerValue)) {
                headers.put(Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER), headerValue);
            }
        }
    }
}
