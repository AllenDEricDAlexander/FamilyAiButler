package top.egon.familyaibutler.ai.qwen.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.ai.tools
 * @ClassName: NameCountsTools
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-11Day-13:07
 * @Description: NameCountsTools
 * @Version: 1.0
 */
@Service
public class NameCountsTools {

    @Tool(description = "长沙有多少名字的数量")
    String LocationNameCounts(@ToolParam(description = "名字") String name) {
        return "10个";
    }

}