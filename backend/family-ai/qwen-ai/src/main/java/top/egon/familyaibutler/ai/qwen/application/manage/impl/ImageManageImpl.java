package top.egon.familyaibutler.ai.qwen.application.manage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.ai.qwen.application.command.ImageMessageCommand;
import top.egon.familyaibutler.ai.qwen.application.executor.command.ImageCommandExe;
import top.egon.familyaibutler.ai.qwen.application.manage.ImageManage;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.application.manage.impl
 * @ClassName: ImageManageImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 11:05
 * @Description: 图片描述 COLA 应用服务
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor
public class ImageManageImpl implements ImageManage {
    private final ImageCommandExe imageCommandExe;

    /**
     * 根据图片列表生成文本描述。
     *
     * @param command 图片描述命令
     * @return 文本描述
     * @throws Exception 图片处理或模型调用异常
     */
    @Override
    public String image2Message(ImageMessageCommand command) throws Exception {
        return imageCommandExe.image2Message(command);
    }
}
