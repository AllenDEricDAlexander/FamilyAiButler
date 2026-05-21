package top.egon.familyaibutler.ai.qwen.domain.model.aggregate;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.domain.model.aggregate
 * @ClassName: ImageMessageTask
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 图片描述任务领域模型
 * @Version: 1.0
 */
@Data
public class ImageMessageTask {
    private final List<String> context = new ArrayList<>();

    /**
     * 记录一段模型描述结果。
     *
     * @param message 模型描述结果
     */
    public void addMessage(String message) {
        context.add(message);
    }
}
