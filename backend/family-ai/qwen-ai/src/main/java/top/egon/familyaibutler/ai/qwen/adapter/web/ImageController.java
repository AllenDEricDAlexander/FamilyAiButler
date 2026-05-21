/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.adapter.web
 * @FileName: ImageController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:25
 * @Description: 图片描述 Web 适配器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.ai.qwen.adapter.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.egon.familyaibutler.ai.qwen.application.command.ImageMessageCommand;
import top.egon.familyaibutler.ai.qwen.application.manage.ImageManage;
import top.egon.familyaibutler.ai.qwen.domain.image.model.valueobject.ImagePayload;

import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.adapter.web
 * @ClassName: ImageController
 * @Author: atluofu
 * @CreateTime: 2025Year-10Month-29Day-14:09
 * @Description: 图片描述 Web 适配器
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/ai/v1")
@Tag(name = "Qwen 图片理解相关接口")
@RequiredArgsConstructor
public class ImageController {
    private final ImageManage imageService;

    /**
     * 根据输入的图片生成文本描述。
     *
     * @param file 上传的图片文件。
     * @return 生成的文本描述。
     * @throws Exception 如果处理图片或调用模型时发生异常。
     */
    @Operation(summary = "图片生成文本描述", description = "上传一组图片，按图片顺序生成连续的做菜步骤文本描述",
            parameters = {
                    @Parameter(name = "file", description = "上传图片文件列表", in = ParameterIn.QUERY, required = true)
            },
            responses = {
                    @ApiResponse(description = "返回生成的文本描述", responseCode = "10000", content = @Content(schema = @Schema(implementation = String.class, description = "图片描述文本", name = "图片描述文本", title = "图片描述文本", example = "Add the chopped onions to the pan and stir well.")))
            }
    )
    @RequestMapping("/image2Message")
    public String image2Message(@RequestParam("file") List<MultipartFile> file) throws Exception {
        List<ImagePayload> images = new ArrayList<>();
        for (MultipartFile multipartFile : file) {
            images.add(new ImagePayload(multipartFile.getOriginalFilename(), multipartFile.getContentType(), multipartFile.getBytes()));
        }
        String message = imageService.image2Message(new ImageMessageCommand(images));
        log.info(message);
        return message;
    }
}
