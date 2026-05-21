/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.domain.image.model.valueobject
 * @FileName: ImagePayload.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:20
 * @Description: 图片载荷值对象文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.ai.qwen.domain.image.model.valueobject;

/**
 * @param filename    图片文件名
 * @param contentType 图片内容类型
 * @param bytes       图片字节内容
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.domain.image.model.valueobject
 * @ClassName: ImagePayload
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:20
 * @Description: 图片载荷值对象
 * @Version: 1.0
 */
public record ImagePayload(
        String filename,
        String contentType,
        byte[] bytes
) {
}
