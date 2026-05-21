/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper
 * @FileName: AccountRoleMapper.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-17:10
 * @Description: 账号角色关系 Mapper 文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.AccountRolePO;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper
 * @ClassName: AccountRoleMapper
 * @Author: atluofu
 * @CreateTime: 2026-05-20 17:10
 * @Description: 账号角色关系 Mapper
 * @Version: 1.0
 */
@Mapper
public interface AccountRoleMapper extends BaseMapper<AccountRolePO> {
}
