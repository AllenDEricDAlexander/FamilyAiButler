package ${packageName};

import java.util.List;

${classComment}
public interface ${className} {

/**
* 执行复杂查询。
*
* @param query 查询对象
* @return 查询结果
*/
List<?> query(Object query);
}
