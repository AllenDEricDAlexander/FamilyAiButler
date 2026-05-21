/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.infrastructure.configuration
 * @FileName: QwenModelProperties.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:35
 * @Description: Qwen 模型动态配置属性文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.ai.qwen.infrastructure.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import top.egon.familyaibutler.ai.qwen.domain.model.enums.QwenModelKindEnum;
import top.egon.familyaibutler.ai.qwen.domain.model.enums.QwenModelModalityEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.infrastructure.configuration
 * @ClassName: QwenModelProperties
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:35
 * @Description: Qwen 模型动态配置属性
 * @Version: 1.0
 */
@Getter
@Setter
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "family.ai.qwen")
public class QwenModelProperties {

    private String apiKey = "replace-with-your-key";
    private List<Model> models = new ArrayList<>();

    /**
     * Qwen 单个模型配置。
     */
    @Getter
    @Setter
    public static class Model {
        private String name;
        private String displayName;
        private boolean enabled = true;
        private String provider = "dashscope";
        private QwenModelKindEnum modelKind = QwenModelKindEnum.CHAT;
        private List<QwenModelModalityEnum> modalities = new ArrayList<>();
        private List<QwenModelModalityEnum> defaultModalities = new ArrayList<>();
        private List<String> aliases = new ArrayList<>();
        private Map<String, Object> options = new HashMap<>();
    }
}
