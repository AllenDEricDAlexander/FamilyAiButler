package top.egon.familyaibutler.common.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.configuration
 * @ClassName: JacksonConfig
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-01Day-21:42
 * @Description: Jackson 相关配置
 * @Version: 1.0
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.build();
        // 添加Java 8时间模块支持
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

}