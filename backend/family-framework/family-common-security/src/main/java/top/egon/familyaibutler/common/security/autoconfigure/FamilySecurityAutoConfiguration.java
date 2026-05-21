/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.security.autoconfigure
 * @FileName: FamilySecurityAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-18:50
 * @Description: 安全公共能力自动装配文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.common.security.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtProperties;
import top.egon.familyaibutler.common.security.jwt.FamilyJwtService;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.security.autoconfigure
 * @ClassName: FamilySecurityAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-18:50
 * @Description: 安全公共能力自动装配
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass(FamilyJwtService.class)
@EnableConfigurationProperties(FamilyJwtProperties.class)
@ConditionalOnProperty(prefix = "family.security.jwt", name = "enabled", havingValue = "true")
public class FamilySecurityAutoConfiguration {

    /**
     * 创建统一 JWT 服务。
     *
     * @param properties JWT 配置
     * @return FamilyJwtService 返回统一 JWT 服务
     */
    @Bean
    @ConditionalOnMissingBean
    public FamilyJwtService familyJwtService(FamilyJwtProperties properties) {
        return new FamilyJwtService(properties);
    }
}
