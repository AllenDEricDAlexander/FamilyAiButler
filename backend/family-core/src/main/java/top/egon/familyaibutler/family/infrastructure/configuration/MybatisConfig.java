package top.egon.familyaibutler.family.infrastructure.configuration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.configuration
 * @ClassName: MybatisConfig
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-02Day-20:54
 * @Description: Mybatis&Plus config
 * @Version: 1.0
 */
@Configuration
@MapperScan("top.egon.familyaibutler.family.infrastructure.persistence.mp.mapper")
public class MybatisConfig {
}