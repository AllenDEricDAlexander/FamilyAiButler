/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa
 * @FileName: UserAuthenticationAuthorizationApplication.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: UAA 核心服务启动类文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa
 * @ClassName: UserAuthenticationAuthorizationApplication
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: UAA 核心服务启动类
 * @Version: 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper")
@ComponentScan(basePackages = {"top.egon.familyaibutler.uaa", "top.egon.familyaibutler.common"})
public class UserAuthenticationAuthorizationApplication {

    /**
     * UAA 核心服务启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(UserAuthenticationAuthorizationApplication.class, args);
    }
}
