/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.grpc.autoconfigure
 * @FileName: FamilyGrpcLogAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-21:35
 * @Description: gRPC 日志自动装配文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.framework.log.grpc.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import top.egon.familyaibutler.framework.log.grpc.FamilyGrpcTraceClientInterceptor;
import top.egon.familyaibutler.framework.log.grpc.FamilyGrpcTraceServerInterceptor;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.framework.log.grpc.autoconfigure
 * @ClassName: FamilyGrpcLogAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026-05-21 21:35
 * @Description: gRPC 日志自动装配
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass(name = "io.grpc.ServerInterceptor")
@ConditionalOnProperty(prefix = "family.log", name = {"enabled", "grpc-enabled"},
        havingValue = "true", matchIfMissing = true)
public class FamilyGrpcLogAutoConfiguration {

    /**
     * 注册 gRPC 客户端链路透传拦截器。
     *
     * @return Object 返回 gRPC 客户端链路透传拦截器
     */
    @Bean
    public Object familyGrpcTraceClientInterceptor() {
        return new FamilyGrpcTraceClientInterceptor();
    }

    /**
     * 注册 gRPC 服务端链路透传拦截器。
     *
     * @return Object 返回 gRPC 服务端链路透传拦截器
     */
    @Bean
    public Object familyGrpcTraceServerInterceptor() {
        return new FamilyGrpcTraceServerInterceptor();
    }
}
