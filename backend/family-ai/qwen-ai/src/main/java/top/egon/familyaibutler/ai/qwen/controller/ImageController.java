package top.egon.familyaibutler.ai.qwen.controller;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.egon.familyaibutler.ai.common.utils.FilesUtils;
import top.egon.familyaibutler.ai.qwen.enums.ModelNumberEnums;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.controller
 * @ClassName: ImageController
 * @Author: atluofu
 * @CreateTime: 2025Year-10Month-29Day-14:09
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/ai/v1")
@RequiredArgsConstructor
public class ImageController {

    /**
     * 根据输入的图片生成文本描述。
     *
     * @param file 上传的图片文件。
     * @return 生成的文本描述。
     * @throws Exception 如果处理图片或调用模型时发生异常。
     */
    @RequestMapping("/image2Message")
    public String image2Message(@RequestParam List<MultipartFile> file) throws Exception {
        ArrayList<String> context = new ArrayList<>();
        for (MultipartFile multipartFile : file) {
            MultiModalConversation conv = new MultiModalConversation();

            // 定义目标路径和文件名
            String filePath = "static/image/" + System.currentTimeMillis() + ".png";
            // 压缩图片并保存到目标路径
            FilesUtils.compressImage(multipartFile, filePath, 512, 512);
            String base64Image = encodeImageToBase64(filePath);
            // 1. 单独创建第一个map并赋值
            HashMap<String, Object> imageMap = new HashMap<>();
            imageMap.put("image", "data:image/png;base64," + base64Image);

            // 2. 单独创建第二个map并赋值
            HashMap<String, Object> textMap = new HashMap<>();
            if (ObjectUtils.isNotEmpty(context)) {
                textMap.put("text", "之前的步骤描述为" + context + "。请继续描述图中描绘了做菜步骤的哪一步。");
            } else {
                textMap.put("text", "图中描绘了做菜步骤的哪一步。");
            }

            // 3. 将两个map对象放入数组，再转为List
            List<Map<String, Object>> list = Arrays.asList(
                    imageMap, textMap);

            MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                    .content(list).build();

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(System.getenv("AI_QWEN_KEY"))
                    .model(ModelNumberEnums.QWEN_VL_PLUS.getCode())
                    .messages(Collections.singletonList(userMessage))
                    .build();
            MultiModalConversationResult result = conv.call(param);
            String content = (String) result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text");
            log.info(content);
            context.add(content);
        }
        return "";
    }


    private static String encodeImageToBase64(String imagePath) throws IOException {
        Path path = Paths.get(imagePath);
        byte[] imageBytes = Files.readAllBytes(path);
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}
