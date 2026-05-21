/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.grpc
 * @FileName: FamilyGrpcTraceInterceptorTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-12:15
 * @Description: gRPC 链路透传拦截器测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import top.egon.familyaibutler.framework.log.core.FamilyLogMdcKeys;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.grpc
 * @ClassName: FamilyGrpcTraceInterceptorTest
 * @Author: atluofu
 * @CreateTime: 2026-05-21 12:15
 * @Description: gRPC 链路透传拦截器测试
 * @Version: 1.0
 */
class FamilyGrpcTraceInterceptorTest {

    private static final MethodDescriptor<String, String> METHOD = MethodDescriptor.<String, String>newBuilder()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(MethodDescriptor.generateFullMethodName("FamilyTraceService", "route"))
            .setRequestMarshaller(new StringMarshaller())
            .setResponseMarshaller(new StringMarshaller())
            .build();

    /**
     * 清理测试线程 MDC。
     */
    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    /**
     * 校验 gRPC client 侧会把 MDC 写入 Metadata。
     */
    @Test
    void shouldWriteClientMetadataFromMdc() {
        FamilyGrpcTraceClientInterceptor interceptor = new FamilyGrpcTraceClientInterceptor();
        CapturingClientCall<String, String> delegateCall = new CapturingClientCall<>();
        Channel channel = new Channel() {
            /**
             * 创建测试调用。
             */
            @Override
            public <ReqT, RespT> ClientCall<ReqT, RespT> newCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
                                                                 CallOptions callOptions) {
                return (ClientCall<ReqT, RespT>) delegateCall;
            }

            /**
             * 获取测试 Authority。
             */
            @Override
            public String authority() {
                return "family-test";
            }
        };
        MDC.put(FamilyLogMdcKeys.TRACE_ID, "trace_grpc");
        MDC.put(FamilyLogMdcKeys.REQUEST_ID, "request_grpc");

        ClientCall<String, String> clientCall = interceptor.interceptCall(METHOD, CallOptions.DEFAULT, channel);
        clientCall.start(new ClientCall.Listener<>() {
        }, new Metadata());

        assertThat(delegateCall.headers.get(grpcHeaderKey(FamilyLogMdcKeys.HEADER_TRACE_ID)))
                .isEqualTo("trace_grpc");
        assertThat(delegateCall.headers.get(grpcHeaderKey(FamilyLogMdcKeys.HEADER_REQUEST_ID)))
                .isEqualTo("request_grpc");
    }

    /**
     * 校验 gRPC server 侧会从 Metadata 恢复 MDC 并在回调结束后还原。
     */
    @Test
    void shouldReadServerMetadataIntoMdcAndRestoreContext() {
        FamilyGrpcTraceServerInterceptor interceptor = new FamilyGrpcTraceServerInterceptor();
        Metadata headers = new Metadata();
        AtomicReference<String> startTraceId = new AtomicReference<>();
        AtomicReference<String> callbackTraceId = new AtomicReference<>();
        headers.put(grpcHeaderKey(FamilyLogMdcKeys.HEADER_TRACE_ID), "trace_server");
        headers.put(grpcHeaderKey(FamilyLogMdcKeys.HEADER_REQUEST_ID), "request_server");
        MDC.put("customKey", "customValue");
        ServerCallHandler<String, String> handler = (call, requestHeaders) -> {
            startTraceId.set(MDC.get(FamilyLogMdcKeys.TRACE_ID));
            return new ServerCall.Listener<>() {
                /**
                 * 捕获回调中的 MDC。
                 */
                @Override
                public void onHalfClose() {
                    callbackTraceId.set(MDC.get(FamilyLogMdcKeys.TRACE_ID));
                }
            };
        };

        ServerCall.Listener<String> listener = interceptor.interceptCall(new TestServerCall(), headers, handler);
        listener.onHalfClose();

        assertThat(startTraceId.get()).isEqualTo("trace_server");
        assertThat(callbackTraceId.get()).isEqualTo("trace_server");
        assertThat(MDC.get("customKey")).isEqualTo("customValue");
        assertThat(MDC.get(FamilyLogMdcKeys.TRACE_ID)).isNull();
    }

    /**
     * 测试用 ClientCall。
     */
    private static final class CapturingClientCall<ReqT, RespT> extends ClientCall<ReqT, RespT> {
        private Metadata headers;

        /**
         * 捕获启动元数据。
         */
        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
            this.headers = headers;
        }

        /**
         * 测试无需请求消息。
         */
        @Override
        public void request(int numMessages) {
        }

        /**
         * 测试无需取消。
         */
        @Override
        public void cancel(String message, Throwable cause) {
        }

        /**
         * 测试无需半关闭。
         */
        @Override
        public void halfClose() {
        }

        /**
         * 测试无需发送消息。
         */
        @Override
        public void sendMessage(ReqT message) {
        }
    }

    /**
     * 获取 gRPC ASCII 元数据键。
     *
     * @param headerName 透传请求头名称
     * @return Metadata.Key<String> 返回 gRPC 元数据键
     */
    private static Metadata.Key<String> grpcHeaderKey(String headerName) {
        return Metadata.Key.of(headerName, Metadata.ASCII_STRING_MARSHALLER);
    }

    /**
     * 测试用 ServerCall。
     */
    private static final class TestServerCall extends ServerCall<String, String> {

        /**
         * 测试无需请求消息。
         */
        @Override
        public void request(int numMessages) {
        }

        /**
         * 测试无需响应头。
         */
        @Override
        public void sendHeaders(Metadata headers) {
        }

        /**
         * 测试无需响应消息。
         */
        @Override
        public void sendMessage(String message) {
        }

        /**
         * 测试无需关闭。
         */
        @Override
        public void close(Status status, Metadata trailers) {
        }

        /**
         * 测试无需取消状态。
         */
        @Override
        public boolean isCancelled() {
            return false;
        }

        /**
         * 返回测试方法描述。
         */
        @Override
        public MethodDescriptor<String, String> getMethodDescriptor() {
            return METHOD;
        }
    }

    /**
     * 字符串 marshaller。
     */
    private static final class StringMarshaller implements MethodDescriptor.Marshaller<String> {

        /**
         * 序列化字符串。
         */
        @Override
        public InputStream stream(String value) {
            return new ByteArrayInputStream(value == null ? new byte[0] : value.getBytes());
        }

        /**
         * 反序列化字符串。
         */
        @Override
        public String parse(InputStream stream) {
            return "";
        }
    }
}
