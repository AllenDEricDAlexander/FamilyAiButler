/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.configuration
 * @FileName: UaaDomainServiceConfig.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: UAA 领域服务配置文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.egon.familyaibutler.uaa.domain.service.AccountDomainService;
import top.egon.familyaibutler.uaa.domain.service.CredentialDomainService;
import top.egon.familyaibutler.uaa.domain.service.TokenDomainService;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.configuration
 * @ClassName: UaaDomainServiceConfig
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: UAA 领域服务配置
 * @Version: 1.0
 */
@Configuration
public class UaaDomainServiceConfig {

    /**
     * 创建账号领域服务。
     *
     * @return 账号领域服务
     */
    @Bean
    public AccountDomainService accountDomainService() {
        return new AccountDomainService();
    }

    /**
     * 创建凭证领域服务。
     *
     * @return 凭证领域服务
     */
    @Bean
    public CredentialDomainService credentialDomainService() {
        return new CredentialDomainService();
    }

    /**
     * 创建 Token 领域服务。
     *
     * @return Token 领域服务
     */
    @Bean
    public TokenDomainService tokenDomainService() {
        return new TokenDomainService();
    }
}
