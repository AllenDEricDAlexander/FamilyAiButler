package top.egon.familyaibutler.ai.qwen.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.advisor
 * @ClassName: ReReadingAdvisor
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-11Day-10:31
 * @Description: ReReadingAdvisor
 * @Version: 1.0
 */
public class ReReadingAdvisor implements BaseAdvisor {

    private static final String DEFAULT_USER_TEXT_ADVISE = """
            {re2_input_query}
            Read the question again: {re2_input_query}
            """;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // 获得用户输入文本
        String inputQuery = chatClientRequest.prompt().getUserMessage().getText();

        // 定义重复输入模版
        String augmentedSystemText = PromptTemplate.builder().template(DEFAULT_USER_TEXT_ADVISE).build()
                .render(Map.of("re2_input_query", inputQuery));

        // 设置请求的提示词
        ChatClientRequest processedChatClientRequest =
                // 不保留
                ChatClientRequest.builder()
                        .prompt(Prompt.builder().content(augmentedSystemText).build())
                        .build();
        return processedChatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        //我们不做任何处理
        return chatClientResponse;
    }
}