package top.egon.familyaibutler.common.mybatis.extention;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.mybatis.extention
 * @ClassName: EgonMapper
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-06Day-9:06
 * @Description: 自定义扩展 mybatis plus base mapper
 * 主要实现一些公共逻辑，比如 select by business id   WARNING: 强制 业务主键采用String类型 且 构建索引 全局统一命名 businessId 单表只有一个，特殊情况单独处理
 * @Version: 1.0
 */
public interface EgonMapper<T> extends BaseMapper<T> {

    /**
     * 根据业务主键查询数据。
     *
     * @param businessId 业务主键
     * @return T 返回业务数据
     */
    T selectByBusinessId(@Param("businessId") String businessId);

}
