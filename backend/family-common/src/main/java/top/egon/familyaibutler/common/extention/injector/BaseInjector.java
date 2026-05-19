package top.egon.familyaibutler.common.extention.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.session.Configuration;
import top.egon.familyaibutler.common.extention.function.SelectByBusinessId;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.extention.injector
 * @ClassName: BaseInjector
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-06Day-9:08
 * @Description: 通用的注入
 * @Version: 1.0
 */
public class BaseInjector extends DefaultSqlInjector {
    @Override
    public List<AbstractMethod> getMethodList(Configuration configuration, Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(configuration, mapperClass, tableInfo);
        methodList.add(new SelectByBusinessId("selectByBusinessId"));
        return methodList;
    }
}