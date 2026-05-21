package ${packageName};

${imports!""}import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

${classComment}@Component
@RequiredArgsConstructor
public class ${className} {

/**
* 执行查询用例。
*
* @param query 查询对象
* @return 查询结果
*/
public Object execute(${queryClassName} query) {
return null;
}
}
