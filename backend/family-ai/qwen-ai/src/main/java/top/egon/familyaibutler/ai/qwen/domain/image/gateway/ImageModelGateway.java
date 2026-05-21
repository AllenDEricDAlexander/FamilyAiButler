/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.domain.image.gateway
 * @FileName: ImageModelGateway.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:20
 * @Description: 图片模型调用网关文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.ai.qwen.domain.image.gateway;

import top.egon.familyaibutler.ai.qwen.domain.image.model.valueobject.ImagePayload;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.domain.image.gateway
 * @ClassName: ImageModelGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: 图片模型调用网关
 * @Version: 1.0
 */
public interface ImageModelGateway {

    /**
     * 根据图片和上下文生成文本描述。
     *
     * @param imagePayload 图片载荷
     * @param context      已有上下文
     * @return 文本描述
     * @throws Exception 图片处理或模型调用异常
     */
    String describe(ImagePayload imagePayload, List<String> context) throws Exception;
}
