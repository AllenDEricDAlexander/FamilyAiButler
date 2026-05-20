package ${packageName};

${imports!""}${classComment}@Repository
@RequiredArgsConstructor
public class ${className} implements ${aggregateName}QueryGateway {

/**
* 执行复杂查询。
*
* @param query 查询对象
* @return 查询结果
*/
@Override
public List<?> query(Object query) {
return List.of();
}
}
