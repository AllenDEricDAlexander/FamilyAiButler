/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.domain.image.model.enums
 * @FileName: QwenModelKindEnum.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:30
 * @Description: Qwen 模型类型枚举文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.ai.qwen.domain.image.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.domain.image.model.enums
 * @ClassName: QwenModelKindEnum
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:30
 * @Description: Qwen 模型类型枚举
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum QwenModelKindEnum {

    CHAT(true),
    MULTIMODAL_CHAT(true),
    OMNI_CHAT(true),
    IMAGE(false),
    VIDEO(false),
    AUDIO(false),
    EMBEDDING(false),
    REALTIME(false);

    /**
     * 是否可以通过 Spring AI ChatClient 调用。
     */
    private final boolean chatModel;
}
