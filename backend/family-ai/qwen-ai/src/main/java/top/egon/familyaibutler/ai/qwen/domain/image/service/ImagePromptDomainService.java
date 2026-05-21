package top.egon.familyaibutler.ai.qwen.domain.image.service;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.domain.image.service
 * @ClassName: ImagePromptDomainService
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 图片描述提示词领域服务
 * @Version: 1.0
 */
@Service
public class ImagePromptDomainService {

    /**
     * 根据上下文构造图片描述提示词。
     *
     * @param context 已有上下文
     * @return 提示词
     */
    public String buildPrompt(List<String> context) {
        if (ObjectUtils.isNotEmpty(context)) {
            return "之前的步骤描述为" + context + "。请继续描述图中描绘了做菜步骤的哪一步。";
        }
        return "图中描绘了做菜步骤的哪一步。";
    }
}
