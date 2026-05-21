package top.egon.familyaibutler.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway
 * @ClassName: FamilyGateway
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-30Day-11:57
 * @Description: FamilyGateway
 * @Version: 1.0
 */
@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
public class FamilyGateway {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(FamilyGateway.class);
        ConfigurableApplicationContext application = app.run(args);
        Environment env = application.getEnvironment();
        try {
            String port = env.getProperty("server.port");
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            log.info("""
                            ----------------------------------------------------------\n
                            Application '{}' is running! Access URLs:\n
                            Local: \t\thttp://localhost:{}\n
                            External: \thttp://{}:{}\n
                            ----------------------------------------------------------
                            """,
                    env.getProperty("spring.application.name"),
                    port,
                    hostAddress, port);
        } catch (UnknownHostException exception) {
            log.error("启动输出格式化内容报错", exception);
        }
    }

}
