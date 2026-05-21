/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.mybatis.autoconfigure
 * @FileName: FamilyMybatisAutoConfiguration.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-18:50
 * @Description: MyBatis Plus 公共能力自动装配文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.common.mybatis.autoconfigure;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;
import top.egon.familyaibutler.common.mybatis.configuration.MybatisPlusConfig;
import top.egon.familyaibutler.common.mybatis.handler.MybatisPlusMetaFieldHandler;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.mybatis.autoconfigure
 * @ClassName: FamilyMybatisAutoConfiguration
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-18:50
 * @Description: MyBatis Plus 公共能力自动装配
 * @Version: 1.0
 */
@AutoConfiguration
@ConditionalOnClass(MybatisPlusInterceptor.class)
@Import({MybatisPlusConfig.class, MybatisPlusMetaFieldHandler.class})
public class FamilyMybatisAutoConfiguration {
}
