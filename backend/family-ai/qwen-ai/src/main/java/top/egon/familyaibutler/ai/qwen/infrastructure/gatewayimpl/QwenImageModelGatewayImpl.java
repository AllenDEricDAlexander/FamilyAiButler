/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.infrastructure.gatewayimpl
 * @FileName: QwenImageModelGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:20
 * @Description: Qwen 图片模型调用网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.ai.qwen.infrastructure.gatewayimpl;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Repository;
import org.springframework.util.MimeTypeUtils;
import top.egon.familyaibutler.ai.qwen.domain.gateway.ImageModelGateway;
import top.egon.familyaibutler.ai.qwen.domain.model.enums.QwenModelModalityEnum;
import top.egon.familyaibutler.ai.qwen.domain.model.valueobject.ImagePayload;
import top.egon.familyaibutler.ai.qwen.domain.service.ImagePromptDomainService;
import top.egon.familyaibutler.ai.qwen.infrastructure.configuration.ModelConfig;
import top.egon.familyaibutler.ai.qwen.infrastructure.model.QwenModelFactory;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.infrastructure.gatewayimpl
 * @ClassName: QwenImageModelGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 10:30
 * @Description: Qwen 图片模型调用网关实现
 * @Version: 1.0
 */
@Repository
@RequiredArgsConstructor
public class QwenImageModelGatewayImpl implements ImageModelGateway {
    private final ImagePromptDomainService imagePromptDomainService;
    private final QwenModelFactory qwenModelFactory;

    /**
     * 根据图片和上下文生成文本描述。
     *
     * @param imagePayload 图片载荷
     * @param context      已有上下文
     * @return 文本描述
     * @throws Exception 图片处理或模型调用异常
     */
    @Override
    public String describe(ImagePayload imagePayload, List<String> context) throws Exception {
        byte[] compressedImage = compressImage(imagePayload.bytes(), 512, 512);
        ChatClient chatClient = qwenModelFactory.createChatClient(null, QwenModelModalityEnum.VISION_UNDERSTANDING);
        return chatClient.prompt()
                .system(ModelConfig.SYSTEM_RECIPE_PROMPT)
                .user(user -> user.text(imagePromptDomainService.buildPrompt(context))
                        .media(MimeTypeUtils.IMAGE_PNG, new ByteArrayResource(compressedImage)))
                .call()
                .content();
    }

    /**
     * 将图片压缩为指定尺寸内的 PNG 字节。
     *
     * @param imageBytes   原始图片字节
     * @param targetWidth  目标宽度
     * @param targetHeight 目标高度
     * @return 压缩后的 PNG 字节
     * @throws IOException 图片读取或压缩异常
     */
    private byte[] compressImage(byte[] imageBytes, int targetWidth, int targetHeight) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (originalImage == null) {
            throw new IOException("图片内容无法解析");
        }
        double scaleWidth = (double) targetWidth / originalImage.getWidth();
        double scaleHeight = (double) targetHeight / originalImage.getHeight();
        double scale = Math.min(scaleWidth, scaleHeight);
        int newWidth = Math.max(1, (int) (originalImage.getWidth() * scale));
        int newHeight = Math.max(1, (int) (originalImage.getHeight() * scale));

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        graphics.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "png", outputStream);
        return outputStream.toByteArray();
    }
}
