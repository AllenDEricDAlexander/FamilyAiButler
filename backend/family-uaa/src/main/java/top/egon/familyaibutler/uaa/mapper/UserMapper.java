package top.egon.familyaibutler.uaa.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import top.egon.familyaibutler.uaa.po.RolePO;
import top.egon.familyaibutler.uaa.po.UserPO;

 /**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa
 * @Author: atluofu
 * @CreateTime: 2025-08-13 00:48:18
 * @Description: user table(User)表数据库访问层
 * @Version: 1.0
 */
@Mapper
public interface UserMapper extends BaseMapper<UserPO> {

    /**
    * 批量新增数据（MyBatis原生foreach方法）
    *
    * @param entities List<User> 实例对象列表
    * @return 影响行数
    */
    int insertBatch(@Param("entities") List<UserPO> entities);
    
    /**
    * 批量新增或按主键更新数据（MyBatis原生foreach方法）
    *
    * @param entities List<User> 实例对象列表
    * @return 影响行数
    * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
    */
    int insertOrUpdateBatch(@Param("entities") List<UserPO> entities);


     @Select("SELECT r.* FROM role r " +
             "JOIN user_role ur ON r.id = ur.role_id " +
             "WHERE ur.user_id = #{userId}")
     List<RolePO> selectRolesByUserId(Long userId);

}

