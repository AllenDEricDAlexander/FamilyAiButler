package top.egon.familyaibutler.common.mybatis.extention;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.mybatis.extention
 * @ClassName: IEgonServiceImpl
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-06Day-9:41
 * @Description: 自定义扩展 mybatis plus IService 接口 实现类
 * 扩展点 参考 EgonMapper
 * @Version: 1.0
 */
public class IEgonServiceImpl<M extends EgonMapper<T>, T> extends ServiceImpl<M, T> implements IEgonService<T> {

    /**
     * 根据业务主键查询数据。
     *
     * @param businessId 业务主键
     * @return T 返回业务数据
     */
    @Override
    public T selectByBusinessId(String businessId) {
        return baseMapper.selectByBusinessId(businessId);
    }
}
