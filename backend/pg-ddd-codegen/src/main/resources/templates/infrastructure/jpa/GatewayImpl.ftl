package ${packageName};

${imports!""}${classComment}@Repository
@RequiredArgsConstructor
public class ${className} implements ${aggregateName}Gateway {

/**
* 按主键查找聚合。
*
* @param id 聚合主键
* @return 聚合对象
*/
@Override
public Optional<${aggregateName}> find(${idType} id) {
return Optional.empty();
}

/**
* 保存聚合。
*
* @param aggregate 聚合对象
* @return 保存后的聚合
*/
@Override
public ${aggregateName} save(${aggregateName} aggregate) {
return aggregate;
}
}
