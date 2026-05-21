package top.egon.familyaibutler.common.web.configuration;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.web.configuration
 * @ClassName: LocalDateTimeSerializerConfig
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-15Day-20:12
 * @Description: localDateTime 序列化器
 * @Version: 1.0
 */
@Configuration
public class LocalDateTimeSerializerConfig {

    /**
     * 创建 LocalDateTime 序列化器。
     *
     * @return LocalDateTimeSerializer 返回时间序列化器
     */
    @Bean
    public LocalDateTimeSerializer localDateTimeSerializer() {
        return new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 创建 Jackson 时间序列化定制器。
     *
     * @return Jackson2ObjectMapperBuilderCustomizer 返回 Jackson 定制器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.serializerByType(LocalDateTime.class, localDateTimeSerializer());
    }

}
