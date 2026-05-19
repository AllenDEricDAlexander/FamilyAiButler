package top.egon.familyaibutler.uaa;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa
 * @ClassName: UserAuthenticationAuthorizationApplication
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-13Day-0:42
 * @Description: UAA 服务
 * @Version: 1.0
 */
@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("top.egon.familyaibutler.uaa.mapper")
@ComponentScan(basePackages = {"top.egon.familyaibutler.uaa", "top.egon.familyaibutler.common"})
public class UserAuthenticationAuthorizationApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(UserAuthenticationAuthorizationApplication.class);
        ConfigurableApplicationContext application = app.run(args);
        Environment env = application.getEnvironment();
        try {
            String port = env.getProperty("server.port");
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            StringBuilder preHost = new StringBuilder().append("----------------------------------------------------------\n\t\n")
                    .append("Application '{}' is running! Access URLs:\n\t\n")
                    .append("Local: \t\thttp://localhost:{}\n\t\n")
                    .append("External: \thttp://{}:{}\n\t\n");
            String property = env.getProperty("knife4j.production");
            if ("false".equals(property)) {
                preHost.append("Doc: \thttp://{}:{}/doc.html\n\n")
                        .append("swagger: \thttp://{}:{}/swagger-ui/index.html#/\n\n");
            }
            preHost.append("----------------------------------------------------------\n");
            log.info(preHost.toString(),
                    env.getProperty("spring.application.name"),
                    port,
                    hostAddress, port,
                    hostAddress, port,
                    hostAddress, port);
        } catch (UnknownHostException exception) {
            log.error("启动输出格式化内容报错", exception);
        }
    }
}
