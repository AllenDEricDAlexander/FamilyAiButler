/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.application
 * @FileName: ImageCommandService.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:20
 * @Description: 图片描述命令应用服务文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.ai.qwen.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.ai.qwen.application.dto.ImageMessageCommand;
import top.egon.familyaibutler.ai.qwen.domain.gateway.ImageModelGateway;
import top.egon.familyaibutler.ai.qwen.domain.model.aggregate.ImageMessageTask;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.application
 * @ClassName: ImageCommandService
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 图片描述命令应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class ImageCommandService {
    private final ImageModelGateway imageModelGateway;

    /**
     * 根据图片列表生成文本描述。
     *
     * @param command 图片描述命令
     * @return 文本描述
     * @throws Exception 图片处理或模型调用异常
     */
    public String image2Message(ImageMessageCommand command) throws Exception {
        ImageMessageTask task = new ImageMessageTask();
        for (var imagePayload : command.images()) {
            String content = imageModelGateway.describe(imagePayload, task.getContext());
            task.addMessage(content);
        }
        return String.join("\n", task.getContext());
    }
}
