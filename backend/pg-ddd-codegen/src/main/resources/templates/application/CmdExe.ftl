package ${packageName};

${imports!""}import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

${classComment}@Component
@RequiredArgsConstructor
public class ${className} {

${fields}

/**
* 执行命令用例。
*
* @param command 命令对象
* @return 执行结果
*/
public ${resultClassName} execute(${commandClassName} command) {
return ${applicationAssemblerFieldName}.${assemblerMethodName}(null);
}
}
