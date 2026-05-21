/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.domain.model.valueobject
 * @FileName: QwenModelDefinition.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:35
 * @Description: Qwen 模型定义值对象文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.ai.qwen.domain.model.valueobject;

import top.egon.familyaibutler.ai.qwen.domain.model.enums.QwenModelKindEnum;
import top.egon.familyaibutler.ai.qwen.domain.model.enums.QwenModelModalityEnum;

import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.domain.model.valueobject
 * @ClassName: QwenModelDefinition
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:35
 * @Description: Qwen 模型定义值对象
 * @Version: 1.0
 */
public record QwenModelDefinition(
        String modelName,
        String displayName,
        QwenModelKindEnum modelKind,
        List<QwenModelModalityEnum> modalities,
        List<QwenModelModalityEnum> defaultModalities,
        List<String> aliases,
        Map<String, Object> options
) {
}
