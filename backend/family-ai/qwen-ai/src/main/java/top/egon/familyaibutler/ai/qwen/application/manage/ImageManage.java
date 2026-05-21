package top.egon.familyaibutler.ai.qwen.application.manage;

import top.egon.familyaibutler.ai.qwen.application.command.ImageMessageCommand;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.application.manage
 * @ClassName: ImageManage
 * @Author: atluofu
 * @CreateTime: 2026-05-20 11:05
 * @Description: 图片描述 COLA 客户端接口
 * @Version: 1.0
 */
public interface ImageManage {

    /**
     * 根据图片列表生成文本描述。
     *
     * @param command 图片描述命令
     * @return 文本描述
     * @throws Exception 图片处理或模型调用异常
     */
    String image2Message(ImageMessageCommand command) throws Exception;
}
