package top.egon.familyaibutler.ai.qwen.configuration;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.egon.familyaibutler.ai.qwen.enums.ModelNumberEnums;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.configuration
 * @ClassName: ModelConfig
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-09Day-11:44
 * @Description: ModelConfig
 * @Version: 1.0
 */
@Configuration
@RequiredArgsConstructor
public class ModelConfig {

    @Qualifier("dashScopeApi")
    private final DashScopeApi qwenApi;

    private static final String SYSTEM_RECIPE_PROMPT = """
            You are a professional culinary editor and visual reasoning expert.
            Your task is to analyze one image at a time and describe what is happening in the current step of the recipe.
            Each new image continues from the previous cooking steps already described.
            Instructions:
            You will receive:
            The current image showing the next step of a recipe.
            The previous step descriptions generated so far.
            Based on what you see in the current image, write a new step description that:
            Continues logically from the previous steps.
            Focuses only on what is clearly visible in this image.
            Write **concise, natural, and instructive** step descriptions, as if for a professional recipe guide.
            Uses imperative, natural cooking instructions (e.g., “Add the chopped onions to the pan and stir well.”).
            Is concise, clear, and realistic (1–2 sentences per step).
            Do not restate previous steps, summarize, or reference “previous images.”
            Use clear, fluent English that feels natural to English-speaking audiences.
            Do **not** repeat information or describe irrelevant background details.
            Just continue the recipe naturally as if you’re writing the next numbered step.
            Avoid describing irrelevant background details or guessing unseen ingredients.
            Output format:
            """;

    @Bean("qwen-plus")
    public ChatClient qwenPlus() {

        DashScopeChatModel qwenPlusModel = DashScopeChatModel.builder()
                .dashScopeApi(qwenApi)
                .defaultOptions(DashScopeChatOptions.builder().withModel(DashScopeModel.ChatModel.QWEN_PLUS.getName()).build())
                .build();

        return ChatClient.builder(qwenPlusModel)
                .defaultSystem(SYSTEM_RECIPE_PROMPT)
                .build();
    }

    @Bean("qwen-vl")
    public ChatClient qwenVL() {

        DashScopeChatModel qwenPlusModel = DashScopeChatModel.builder()
                .dashScopeApi(qwenApi)
                .defaultOptions(DashScopeChatOptions.builder().withModel(ModelNumberEnums.QWEN_VL_PLUS.getCode()).build())
                .build();

        return ChatClient.builder(qwenPlusModel)
                .defaultSystem(SYSTEM_RECIPE_PROMPT)
                .build();
    }


}
