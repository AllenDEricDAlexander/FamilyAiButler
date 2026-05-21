package top.egon.familyaibutler.family.infrastructure.persistence.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import top.egon.familyaibutler.common.mybatis.extention.EgonMapper;
import top.egon.familyaibutler.family.infrastructure.persistence.mp.dataobject.PasswordViewPO;

import java.util.List;

/**
 * @BelongsProject: top.egon.familyaibutler.family.infrastructure.persistence.mp.mapper
 * @BelongsPackage: top.egon.familyaibutler.family.infrastructure.persistence.mp.mapper
 * @Author: atluofu
 * @CreateTime: 2025-08-03 09:40:11
 * @Description: (PasswordView)表数据库访问层
 * @Version: 1.0
 */
@Mapper
public interface PasswordViewMapper extends EgonMapper<PasswordViewPO> {

    /**
     * 批量新增数据（MyBatis原生foreach方法）
     *
     * @param entities List<PasswordView> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<PasswordViewPO> entities);

    /**
     * 批量新增或按主键更新数据（MyBatis原生foreach方法）
     *
     * @param entities List<PasswordView> 实例对象列表
     * @return 影响行数
     * @throws org.springframework.jdbc.BadSqlGrammarException 入参是空List的时候会抛SQL语句错误的异常，请自行校验入参
     */
    int insertOrUpdateBatch(@Param("entities") List<PasswordViewPO> entities);

}

