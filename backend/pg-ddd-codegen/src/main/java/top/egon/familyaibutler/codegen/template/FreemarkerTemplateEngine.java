package top.egon.familyaibutler.codegen.template;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.template
 * @ClassName: FreemarkerTemplateEngine
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: Freemarker 模板渲染引擎
 * @Version: 1.0
 */
public class FreemarkerTemplateEngine implements TemplateEngine {
    private final Configuration configuration;

    public FreemarkerTemplateEngine() {
        this.configuration = new Configuration(Configuration.VERSION_2_3_34);
        this.configuration.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "templates");
        this.configuration.setDefaultEncoding("UTF-8");
    }

    /**
     * 渲染 classpath templates 下的模板。
     *
     * @param templateName 模板名称
     * @param model        模板参数
     * @return 渲染结果
     */
    @Override
    public String render(String templateName, Map<String, Object> model) {
        try (StringWriter writer = new StringWriter()) {
            Template template = configuration.getTemplate(templateName);
            template.process(model, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new IllegalStateException("渲染模板失败: " + templateName, e);
        }
    }
}
