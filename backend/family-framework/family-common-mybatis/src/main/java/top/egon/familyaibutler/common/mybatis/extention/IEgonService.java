package top.egon.familyaibutler.common.mybatis.extention;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.mybatis.extention
 * @ClassName: IEgonService
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-06Day-9:39
 * @Description: 自定义扩展 mybatis plus IService 接口
 * 扩展点 参考 EgonMapper
 * @Version: 1.0
 */
public interface IEgonService<T> extends IService<T> {

    /**
     * 根据业务主键查询数据。
     *
     * @param businessId 业务主键
     * @return T 返回业务数据
     */
    T selectByBusinessId(String businessId);
}
