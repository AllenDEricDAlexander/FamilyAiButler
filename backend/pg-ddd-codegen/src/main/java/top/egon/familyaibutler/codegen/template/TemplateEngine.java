package top.egon.familyaibutler.codegen.template;

import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.template
 * @ClassName: TemplateEngine
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: 代码模板渲染接口
 * @Version: 1.0
 */
public interface TemplateEngine {

    /**
     * 渲染模板。
     *
     * @param templateName 模板名称
     * @param model        模板参数
     * @return 渲染结果
     */
    String render(String templateName, Map<String, Object> model);
}
