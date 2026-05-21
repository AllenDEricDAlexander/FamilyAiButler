package ${packageName};

${imports!""}${classComment}
public interface ${className} {

/**
* 按主键查找聚合。
*
* @param id 聚合主键
* @return 聚合对象
*/
Optional<${aggregateName}> findById(${idType} id);

/**
* 保存聚合。
*
* @param aggregate 聚合对象
* @return 保存后的聚合
*/
${aggregateName} save(${aggregateName} aggregate);
}
