package top.egon.familyaibutler.ai.qwen.application.result;

import top.egon.openapi.console.annotation.DocField;
import top.egon.openapi.console.annotation.DocModel;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.qwen.application.result
 * @ClassName: Address
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-11Day-11:28
 * @Description: Address AI Auto format
 * @Version: 1.0
 */
@DocModel(name = "QwenAddress", description = "AI 地址识别结果")
public record Address(
        /**
         * 联系人姓名。
         */
        @DocField(description = "联系人姓名", required = false, example = "张三")
        String name,
        /**
         * 联系电话。
         */
        @DocField(description = "联系电话", required = false, example = "13800138000")
        String phone,
        /**
         * 省份。
         */
        @DocField(description = "省份", required = false, example = "广东省")
        String province,
        /**
         * 城市。
         */
        @DocField(description = "城市", required = false, example = "深圳市")
        String city,
        /**
         * 区县。
         */
        @DocField(description = "区县", required = false, example = "南山区")
        String district,
        /**
         * 详细地址。
         */
        @DocField(description = "详细地址", required = false, example = "科技园 1 号")
        String detail) {
}
