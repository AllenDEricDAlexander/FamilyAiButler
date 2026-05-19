package top.egon.familyaibutler.ai.qwen.configuration;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.configuration
 * @ClassName: SaaLLMConfig
 * @Author: atluofu
 * @CreateTime: 2025Year-10Month-29Day-12:57
 * @Description: DashScope API 配置
 * @Version: 1.0
 */
@Configuration
public class SaaLLMConfig {

    @Value("${spring.ai.dashscope.api-key:replace-with-your-key}")
    private String dashScopeApiKey;

    /**
     * @description: 构建 DashScope API 客户端
     * @author: atluofu
     * @date: 2026/5/19 14:05
     * @param:
     * @return:
     **/
    @Bean("dashScopeApi")
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder()
                .apiKey(dashScopeApiKey)
                .build();
    }

}
