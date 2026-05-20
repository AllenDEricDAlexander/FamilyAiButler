package ${packageName};

${imports!""}${classComment}@Data
@Entity
${entityListenersAnnotation!""}@Table(name = "${tableName}")
public class ${className} {
${fields}
}
