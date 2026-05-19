package top.egon.familyaibutler.codegen.schema;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.codegen.schema
 * @ClassName: ConstraintType
 * @Author: atluofu
 * @CreateTime: 2026-05-19 00:00
 * @Description: PostgreSQL 约束类型枚举
 * @Version: 1.0
 */
public enum ConstraintType {
    PRIMARY_KEY,
    UNIQUE,
    FOREIGN_KEY,
    CHECK
}
