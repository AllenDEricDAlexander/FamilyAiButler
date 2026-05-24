/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.application.command
 * @FileName: ImageMessageCommand.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:25
 * @Description: 图片生成文本描述命令对象文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.ai.qwen.application.command;

import top.egon.familyaibutler.ai.qwen.domain.image.model.valueobject.ImagePayload;
import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.application.command
 * @ClassName: ImageMessageCommand
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 图片生成文本描述命令对象
 * @Version: 1.0
 */
@DocModel(name = "QwenImageMessageCommand", description = "图片生成文本描述命令对象")
public record ImageMessageCommand(
        /**
         * 图片载荷列表。
         */
        @DocField(description = "图片载荷列表", required = true, example = "[]")
        List<ImagePayload> images
) {
}
