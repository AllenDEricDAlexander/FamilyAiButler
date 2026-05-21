/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.application.dto
 * @FileName: ImageMessageCommand.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:25
 * @Description: 图片生成文本描述命令对象文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.ai.qwen.application.dto;

import top.egon.familyaibutler.ai.qwen.domain.model.valueobject.ImagePayload;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.application.dto
 * @ClassName: ImageMessageCommand
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 图片生成文本描述命令对象
 * @Version: 1.0
 */
public record ImageMessageCommand(
        List<ImagePayload> images
) {
}
