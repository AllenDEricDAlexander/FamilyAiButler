package top.egon.familyaibutler.ai.qwen.service.impl;

import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.ai.qwen.service.GenImageIteration;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.service.impl
 * @ClassName: GenImageIterationImpl
 * @Author: atluofu
 * @CreateTime: 2025Year-10Month-21Day-10:35
 * @Description: 生成图片迭代方式 实现类
 * @Version: 1.0
 */
@RequiredArgsConstructor
@Service("imageIteration")
public class GenImageIterationImpl implements GenImageIteration {

    private final DashScopeImageModel imageModel;

    private final static String MODEL_NAME = "qwen-image-plus";


    @Override
    public String genImage(String prompt) {
        ImageResponse response = imageModel.call(
                new ImagePrompt(
                        prompt,
                        DashScopeImageOptions.builder()
                                .withModel(MODEL_NAME)
                                .withN(1)
                                .withHeight(1024).withWidth(1024).build())
        );
        return response.getResult().getOutput().getUrl();
    }
}