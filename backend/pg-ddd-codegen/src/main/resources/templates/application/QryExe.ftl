package ${packageName};

${imports!""}import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

${classComment}@Component
@RequiredArgsConstructor
public class ${className} {

${fields}

/**
* 执行查询用例。
*
* @param query 查询对象
* @return 查询结果
*/
public ${resultClassName} execute(${queryClassName} query) {
${criteriaClassName} criteria = ${applicationAssemblerFieldName}.${criteriaAssemblerMethodName}(query);
return ${applicationAssemblerFieldName}.${assemblerMethodName}(${queryGatewayFieldName}.${queryGatewayMethodName}(criteria));
}
}
