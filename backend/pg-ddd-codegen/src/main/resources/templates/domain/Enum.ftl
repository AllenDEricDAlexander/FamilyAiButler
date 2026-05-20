package ${packageName};

<#if detailed>import lombok.Getter;

</#if>
${classComment}<#if detailed>@Getter
</#if>public enum ${className} {
<#if detailed>
    <#list values as value>

        /**
        * ${value.description}
        */
        ${value.name}("${value.code}", "${value.description}")<#if value_has_next>,<#else>;</#if>
    </#list>

    /**
    * 枚举编码。
    */
    private final String code;

    /**
    * 枚举描述。
    */
    private final String description;

    ${className}(String code, String description) {
    this.code = code;
    this.description = description;
    }

    /**
    * 根据编码获取枚举实例。
    *
    * @param code 枚举编码
    * @return 枚举实例
    */
    public static ${className} getByCode(String code) {
    for (${className} value : values()) {
    if (value.code.equalsIgnoreCase(code)) {
    return value;
    }
    }
    return null;
    }
<#else>
    <#list values as value>
        ${value.name}<#if value_has_next>,<#else>;</#if>
    </#list>
</#if>
}
