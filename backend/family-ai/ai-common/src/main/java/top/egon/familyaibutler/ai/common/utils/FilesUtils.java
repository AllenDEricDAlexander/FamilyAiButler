package top.egon.familyaibutler.ai.common.utils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.common.utils
 * @ClassName: FilesUtils
 * @Author: atluofu
 * @CreateTime: 2025Year-10Month-29Day-14:06
 * @Description: 图片压缩工具类
 * @Version: 1.0
 */
@Slf4j
@NoArgsConstructor
public final class FilesUtils {

    /**
     * 压缩上传的图片并保存到指定路径。
     *
     * @param file         需要压缩的图片文件（MultipartFile类型）
     * @param relativePath 目标保存路径的相对路径（相对于项目根目录）
     * @param targetWidth  压缩后的目标宽度（像素）
     * @param targetHeight 压缩后的目标高度（像素）
     * @throws IOException 文件读写或图片处理异常
     */
    public static void compressImage(MultipartFile file, String relativePath, int targetWidth, int targetHeight) throws IOException {
        /* 读取原始图片 */
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        /* 计算压缩比例 */
        double scaleWidth = (double) targetWidth / originalWidth;
        double scaleHeight = (double) targetHeight / originalHeight;
        double scale = Math.min(scaleWidth, scaleHeight);

        /* 计算新的尺寸 */
        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        /* 创建新的 BufferedImage 并设置渲染参数 */
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        /* 获取项目运行目录 */
        String basePath = new File("").getCanonicalPath();
        log.info("basePath is [{}]", basePath);
        /* 构建完整路径 */
        String fullPath = System.getProperty("user.dir") + File.separator + relativePath;
        log.info("fullPath is [{}]", fullPath);

        // 创建输出目录
        File outputFile = new File(fullPath);
        outputFile.getParentFile().mkdirs();

        /* 保存图片（保存为 PNG 格式） */
        ImageIO.write(resizedImage, "png", outputFile);
    }
}
