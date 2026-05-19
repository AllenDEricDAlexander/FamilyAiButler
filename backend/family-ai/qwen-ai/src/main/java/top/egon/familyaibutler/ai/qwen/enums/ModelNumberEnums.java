package top.egon.familyaibutler.ai.qwen.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.enums
 * @ClassName: ModelNumberEnums
 * @Author: atluofu
 * @CreateTime: 2025Year-10Month-29Day-14:14
 * @Description: 大模型枚举类，包含模型名称、代码编号和描述信息
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum ModelNumberEnums {

    QWEN_VL_PLUS("通义千问VL-Plus", "qwen-vl-plus", "OpenAI推出的轻量级大语言模型，平衡了性能与成本，适用于日常对话、文本生成等场景");

    // 模型名称
    private final String modelName;
    // 代码编号
    private final String code;
    // 描述信息
    private final String description;
}
