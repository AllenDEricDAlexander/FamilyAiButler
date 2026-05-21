package top.egon.familyaibutler.ai.qwen.infrastructure.configuration;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.infrastructure.configuration
 * @ClassName: SaaLLMConfig
 * @Author: atluofu
 * @CreateTime: 2025Year-10Month-29Day-12:57
 * @Description: DashScope API 配置
 * @Version: 1.0
 */
@Configuration
@RequiredArgsConstructor
public class SaaLLMConfig {
    private final QwenModelProperties qwenModelProperties;

    /**
     * 构建 DashScope API 客户端。
     *
     * @return DashScope API 客户端
     */
    @RefreshScope
    @Bean("dashScopeApi")
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder()
                .apiKey(qwenModelProperties.getApiKey())
                .build();
    }

}
