/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.infrastructure.model
 * @FileName: QwenModelFactoryTest.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:20
 * @Description: Qwen 模型工厂测试文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.ai.qwen.infrastructure.model;

import org.junit.jupiter.api.Test;
import top.egon.familyaibutler.ai.qwen.domain.image.model.enums.QwenModelKindEnum;
import top.egon.familyaibutler.ai.qwen.domain.image.model.enums.QwenModelModalityEnum;
import top.egon.familyaibutler.ai.qwen.infrastructure.configuration.QwenModelProperties;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.infrastructure.model
 * @ClassName: QwenModelFactoryTest
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:20
 * @Description: Qwen 模型工厂测试
 * @Version: 1.0
 */
class QwenModelFactoryTest {

    /**
     * 支持按模型名称或别名获取模型。
     */
    @Test
    void shouldGetModelByNameOrAlias() {
        QwenModelFactory factory = new QwenModelFactory(buildProperties(), null);

        assertThat(factory.getModel("qwen-plus", null).modelName()).isEqualTo("qwen-plus");
        assertThat(factory.getModel("qwen-vl", null).modelName()).isEqualTo("qwen-vl-plus");
    }

    /**
     * 支持按模态获取默认模型。
     */
    @Test
    void shouldGetDefaultModelByModality() {
        QwenModelFactory factory = new QwenModelFactory(buildProperties(), null);

        assertThat(factory.getModel(null, QwenModelModalityEnum.TEXT).modelName()).isEqualTo("qwen-plus");
        assertThat(factory.getModel(null, QwenModelModalityEnum.VISION_UNDERSTANDING).modelName()).isEqualTo("qwen-vl-plus");
        assertThat(factory.getModel(null, QwenModelModalityEnum.OMNI).modelName()).isEqualTo("qwen-omni-turbo");
    }

    /**
     * 同时传模型名称和模态时校验模型是否支持该模态。
     */
    @Test
    void shouldCheckModelNameAndModalityTogether() {
        QwenModelFactory factory = new QwenModelFactory(buildProperties(), null);

        assertThat(factory.getModel("qwen-vl-plus", QwenModelModalityEnum.VISION_UNDERSTANDING).modelName())
                .isEqualTo("qwen-vl-plus");
        assertThatThrownBy(() -> factory.getModel("qwen-plus", QwenModelModalityEnum.VISION_UNDERSTANDING))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持模态");
    }

    /**
     * 模态匹配到多个模型且没有默认模型时返回不唯一错误。
     */
    @Test
    void shouldRejectAmbiguousModalityWithoutDefaultModel() {
        QwenModelProperties properties = buildProperties();
        properties.getModels().add(model("qwen-plus-latest", QwenModelKindEnum.CHAT,
                List.of(QwenModelModalityEnum.TEXT), List.of(), List.of(), true));
        QwenModelFactory factory = new QwenModelFactory(properties, null);

        assertThatThrownBy(() -> factory.getModel(null, QwenModelModalityEnum.TEXT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("存在多个可用模型");
    }

    /**
     * 禁用模型不参与名称和模态匹配。
     */
    @Test
    void shouldIgnoreDisabledModel() {
        QwenModelProperties properties = new QwenModelProperties();
        properties.getModels().add(model("disabled-vl", QwenModelKindEnum.MULTIMODAL_CHAT,
                List.of(QwenModelModalityEnum.VISION_UNDERSTANDING),
                List.of(QwenModelModalityEnum.VISION_UNDERSTANDING), List.of("qwen-vl"), false));
        QwenModelFactory factory = new QwenModelFactory(properties, null);

        assertThatThrownBy(() -> factory.getModel("qwen-vl", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("未找到可用模型");
    }

    /**
     * 构建模型工厂测试配置。
     *
     * @return Qwen 模型配置
     */
    private QwenModelProperties buildProperties() {
        QwenModelProperties properties = new QwenModelProperties();
        properties.getModels().add(model("qwen-plus", QwenModelKindEnum.CHAT,
                List.of(QwenModelModalityEnum.TEXT, QwenModelModalityEnum.TEXT_GENERATION),
                List.of(QwenModelModalityEnum.TEXT_GENERATION), List.of(), true));
        properties.getModels().add(model("qwen-vl-plus", QwenModelKindEnum.MULTIMODAL_CHAT,
                List.of(QwenModelModalityEnum.MULTIMODAL, QwenModelModalityEnum.VISION_UNDERSTANDING),
                List.of(QwenModelModalityEnum.VISION_UNDERSTANDING), List.of("qwen-vl"), true));
        properties.getModels().add(model("qwen-omni-turbo", QwenModelKindEnum.OMNI_CHAT,
                List.of(QwenModelModalityEnum.OMNI, QwenModelModalityEnum.REALTIME_OMNI),
                List.of(QwenModelModalityEnum.OMNI), List.of(), true));
        return properties;
    }

    /**
     * 构建模型配置项。
     *
     * @param name              模型名称
     * @param kind              模型类型
     * @param modalities        支持模态
     * @param defaultModalities 默认模态
     * @param aliases           模型别名
     * @param enabled           是否启用
     * @return 模型配置项
     */
    private QwenModelProperties.Model model(String name,
                                            QwenModelKindEnum kind,
                                            List<QwenModelModalityEnum> modalities,
                                            List<QwenModelModalityEnum> defaultModalities,
                                            List<String> aliases,
                                            boolean enabled) {
        QwenModelProperties.Model model = new QwenModelProperties.Model();
        model.setName(name);
        model.setDisplayName(name);
        model.setModelKind(kind);
        model.setModalities(modalities);
        model.setDefaultModalities(defaultModalities);
        model.setAliases(aliases);
        model.setEnabled(enabled);
        return model;
    }
}
