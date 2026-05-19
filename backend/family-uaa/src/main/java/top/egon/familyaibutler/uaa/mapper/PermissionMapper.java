package top.egon.familyaibutler.uaa.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import top.egon.familyaibutler.uaa.po.PermissionPO;

 /**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa
 * @Author: atluofu
 * @CreateTime: 2025-08-13 00:48:18
 * @Description: (Permission)表数据库访问层
 * @Version: 1.0
 */
@Mapper
public interface PermissionMapper extends BaseMapper<PermissionPO> {

    /**
    * 批量新增数据（MyBatis原生foreach方法）
    *
    * @param entities List<Permission> 实例对象列表
    * @return 影响行数
    */
    int insertBatch(@Param("entities") List<PermissionPO> entities);
    
    /**
    * 批量新增或按主键更新数据（MyBatis原生foreach方法）
    *
    * @param entities List<Permission> 实例对象列表
    * @return 影响行数
    * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
    */
    int insertOrUpdateBatch(@Param("entities") List<PermissionPO> entities);

}

