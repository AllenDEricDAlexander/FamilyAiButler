/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.domain.image.model.enums
 * @FileName: QwenModelModalityEnum.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:30
 * @Description: Qwen 模型模态枚举文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.ai.qwen.domain.image.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.domain.image.model.enums
 * @ClassName: QwenModelModalityEnum
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:30
 * @Description: Qwen 模型模态枚举
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum QwenModelModalityEnum {

    MULTIMODAL("多模态"),
    OMNI("全模态"),
    TEXT("文本"),
    TEXT_GENERATION("文本生成"),
    DEEP_THINKING("深度思考"),
    VISION("视觉"),
    VISION_UNDERSTANDING("视觉理解"),
    IMAGE_GENERATION("图片生成"),
    VIDEO_GENERATION("视频生成"),
    THREE_D_GENERATION("3D生成"),
    AUDIO("语音"),
    SPEECH_RECOGNITION("语音识别"),
    SPEECH_SYNTHESIS("语音合成"),
    EMBEDDING("向量"),
    MULTIMODAL_EMBEDDING("多模态向量"),
    TEXT_EMBEDDING("文本向量"),
    REALTIME("Realtime"),
    REALTIME_OMNI("实时全模态"),
    REALTIME_SPEECH_SYNTHESIS("实时语音合成"),
    REALTIME_SPEECH_RECOGNITION("实时语音识别"),
    REALTIME_SPEECH_TRANSLATION("实时语音翻译");

    /**
     * 模态展示名称。
     */
    private final String displayName;
}
