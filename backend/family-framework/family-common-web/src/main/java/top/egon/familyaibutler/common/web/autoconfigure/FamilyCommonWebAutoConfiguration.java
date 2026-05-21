/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.web.autoconfigure
 * @FileName: FamilyCommonWebAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-18:50
 * @Description: Web 公共能力自动装配文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.common.web.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.egon.familyaibutler.common.web.configuration.JacksonConfig;
import top.egon.familyaibutler.common.web.configuration.LocalDateTimeSerializerConfig;
import top.egon.familyaibutler.common.web.handler.GlobalExceptionHandler;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.web.autoconfigure
 * @ClassName: FamilyCommonWebAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-18:50
 * @Description: Web 公共能力自动装配
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass(RestControllerAdvice.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({JacksonConfig.class, LocalDateTimeSerializerConfig.class, GlobalExceptionHandler.class})
public class FamilyCommonWebAutoConfiguration {
}
