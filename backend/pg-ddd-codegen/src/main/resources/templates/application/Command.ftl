package ${packageName};

${imports!""}${classComment}@Data
@With
@NoArgsConstructor
<#if hasFields>
    @AllArgsConstructor
</#if>
@Accessors(chain = true)
@Builder
@EqualsAndHashCode
@DocModel(name = "${className}", description = "${description}")
public class ${className} {

${fields}
}
