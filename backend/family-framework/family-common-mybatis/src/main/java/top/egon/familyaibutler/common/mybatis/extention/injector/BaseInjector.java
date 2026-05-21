package top.egon.familyaibutler.common.mybatis.extention.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.session.Configuration;
import top.egon.familyaibutler.common.mybatis.extention.function.SelectByBusinessId;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.mybatis.extention.injector
 * @ClassName: BaseInjector
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-06Day-9:08
 * @Description: 通用的注入
 * @Version: 1.0
 */
public class BaseInjector extends DefaultSqlInjector {

    /**
     * 获取公共 Mapper 方法列表。
     *
     * @param configuration MyBatis 配置
     * @param mapperClass   Mapper 类型
     * @param tableInfo     表信息
     * @return List<AbstractMethod> 返回 Mapper 方法列表
     */
    @Override
    public List<AbstractMethod> getMethodList(Configuration configuration, Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(configuration, mapperClass, tableInfo);
        methodList.add(new SelectByBusinessId("selectByBusinessId"));
        return methodList;
    }
}
