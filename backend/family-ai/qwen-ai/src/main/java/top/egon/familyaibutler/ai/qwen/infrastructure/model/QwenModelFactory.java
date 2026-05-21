/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.infrastructure.model
 * @FileName: QwenModelFactory.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:40
 * @Description: Qwen 模型工厂文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.ai.qwen.infrastructure.model;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import top.egon.familyaibutler.ai.qwen.domain.model.enums.QwenModelModalityEnum;
import top.egon.familyaibutler.ai.qwen.domain.model.valueobject.QwenModelDefinition;
import top.egon.familyaibutler.ai.qwen.infrastructure.configuration.QwenModelProperties;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.infrastructure.model
 * @ClassName: QwenModelFactory
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:40
 * @Description: Qwen 模型工厂
 * @Version: 1.0
 */
@Component
@RequiredArgsConstructor
public class QwenModelFactory {
    private final QwenModelProperties qwenModelProperties;
    private final DashScopeApi dashScopeApi;

    /**
     * 按模型名称、模态或二者组合获取模型定义。
     *
     * @param modelName 模型名称或别名
     * @param modality  模态
     * @return 模型定义
     */
    public QwenModelDefinition getModel(String modelName, QwenModelModalityEnum modality) {
        boolean hasModelName = StringUtils.hasText(modelName);
        if (!hasModelName && modality == null) {
            throw new IllegalArgumentException("模型名称和模态不能同时为空");
        }
        if (hasModelName) {
            QwenModelProperties.Model model = enabledModels().stream()
                    .filter(item -> matchModelName(item, modelName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("未找到可用模型: " + modelName));
            if (modality != null && !model.getModalities().contains(modality)) {
                throw new IllegalArgumentException("模型 " + model.getName() + " 不支持模态: " + modality.getDisplayName());
            }
            return toDefinition(model);
        }

        List<QwenModelProperties.Model> matchedModels = enabledModels().stream()
                .filter(item -> item.getModalities().contains(modality))
                .toList();
        if (matchedModels.isEmpty()) {
            throw new IllegalArgumentException("未找到支持模态的可用模型: " + modality.getDisplayName());
        }
        List<QwenModelProperties.Model> defaultModels = matchedModels.stream()
                .filter(item -> item.getDefaultModalities().contains(modality))
                .toList();
        if (defaultModels.size() == 1) {
            return toDefinition(defaultModels.get(0));
        }
        if (defaultModels.size() > 1 || matchedModels.size() > 1) {
            throw new IllegalArgumentException("模态 " + modality.getDisplayName() + " 存在多个可用模型，请指定模型名称");
        }
        return toDefinition(matchedModels.get(0));
    }

    /**
     * 创建 Spring AI ChatClient。
     *
     * @param modelName 模型名称或别名
     * @param modality  模态
     * @return ChatClient
     */
    public ChatClient createChatClient(String modelName, QwenModelModalityEnum modality) {
        if (dashScopeApi == null) {
            throw new IllegalStateException("DashScopeApi 未配置，无法创建 Qwen ChatClient");
        }
        QwenModelDefinition definition = getModel(modelName, modality);
        if (!definition.modelKind().isChatModel()) {
            throw new IllegalArgumentException("模型 " + definition.modelName() + " 不是 ChatClient 支持的模型类型");
        }

        Map<String, Object> options = definition.options();
        DashScopeChatOptions.DashScopeChatOptionsBuilder optionsBuilder = DashScopeChatOptions.builder()
                .model(definition.modelName());
        if (options.containsKey("temperature")) {
            optionsBuilder.temperature(toDouble(options.get("temperature")));
        }
        if (options.containsKey("topP")) {
            optionsBuilder.topP(toDouble(options.get("topP")));
        }
        if (options.containsKey("topK")) {
            optionsBuilder.topK(toInteger(options.get("topK")));
        }
        if (options.containsKey("maxTokens")) {
            optionsBuilder.maxToken(toInteger(options.get("maxTokens")));
        }
        if (options.containsKey("enableThinking")) {
            optionsBuilder.enableThinking(toBoolean(options.get("enableThinking")));
        }
        Object dashscopeModalitiesOption = options.getOrDefault("dashscopeModalities", options.get("dashscope-modalities"));
        if (dashscopeModalitiesOption instanceof List<?> dashscopeModalities) {
            optionsBuilder.modalities(dashscopeModalities.stream().map(String::valueOf).toList());
        }

        DashScopeChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(optionsBuilder.build())
                .build();
        return ChatClient.builder(chatModel).build();
    }

    /**
     * 获取启用的模型配置列表。
     *
     * @return 启用的模型配置列表
     */
    private List<QwenModelProperties.Model> enabledModels() {
        return qwenModelProperties.getModels().stream()
                .filter(QwenModelProperties.Model::isEnabled)
                .toList();
    }

    /**
     * 判断配置模型名称或别名是否匹配。
     *
     * @param model     模型配置
     * @param modelName 模型名称或别名
     * @return true 表示匹配
     */
    private boolean matchModelName(QwenModelProperties.Model model, String modelName) {
        String normalizedModelName = modelName.toLowerCase(Locale.ROOT);
        return Objects.equals(model.getName().toLowerCase(Locale.ROOT), normalizedModelName)
                || model.getAliases().stream().anyMatch(alias -> alias.toLowerCase(Locale.ROOT).equals(normalizedModelName));
    }

    /**
     * 转换为领域模型定义。
     *
     * @param model 模型配置
     * @return 模型定义
     */
    private QwenModelDefinition toDefinition(QwenModelProperties.Model model) {
        return new QwenModelDefinition(
                model.getName(),
                model.getDisplayName(),
                model.getModelKind(),
                List.copyOf(model.getModalities()),
                List.copyOf(model.getDefaultModalities()),
                List.copyOf(model.getAliases()),
                Map.copyOf(model.getOptions())
        );
    }

    /**
     * 转换为 Double 类型。
     *
     * @param value 配置值
     * @return Double 值
     */
    private Double toDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.valueOf(String.valueOf(value));
    }

    /**
     * 转换为 Integer 类型。
     *
     * @param value 配置值
     * @return Integer 值
     */
    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.valueOf(String.valueOf(value));
    }

    /**
     * 转换为 Boolean 类型。
     *
     * @param value 配置值
     * @return Boolean 值
     */
    private Boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.valueOf(String.valueOf(value));
    }
}
