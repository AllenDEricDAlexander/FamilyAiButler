/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.common.model.valueobject
 * @FileName: PageSlice.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-21Day-14:10
 * @Description: 领域分页切片文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.family.domain.common.model.valueobject;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.family.domain.common.model.valueobject
 * @ClassName: PageSlice
 * @Author: atluofu
 * @CreateTime: 2026-05-21 14:10
 * @Description: 领域分页切片
 * @Version: 1.0
 */
public record PageSlice<T>(
        List<T> records,
        long total,
        long pageNum,
        long pageSize
) {
}
