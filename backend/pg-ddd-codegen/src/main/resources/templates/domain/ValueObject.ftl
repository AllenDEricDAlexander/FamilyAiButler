package ${packageName};

${imports!""}${classComment}
public record ${className}(${valueType} value) {

/**
* 校验值对象基础合法性。
*/
public ${className} {
if (value == null) {
throw new IllegalArgumentException("${className}不能为空");
}
}
}
