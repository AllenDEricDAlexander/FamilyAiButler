package top.egon.familyaibutler.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.handler
 * @ClassName: MybatisPlusMetaFieldHandler
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-03Day-15:35
 * @Description: 填充公共字段
 * version
 * createTime updateTime
 * todo createAccount modifyAccount
 * @Version: 1.0
 */
@Component
public class MybatisPlusMetaFieldHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        Date date = new Date();
        setNowDateField("createTime", metaObject, date);
        setNowDateField("updateTime", metaObject, date);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        setNowDateField("updateTime", metaObject, new Date());
    }

    private void setNowDateField(String updateTimeKey, MetaObject metaObject, Date date) {
        Object updateTime = getFieldValByName(updateTimeKey, metaObject);
        if (ObjectUtils.isEmpty(updateTime)) {
            setFieldValByName(updateTimeKey, date, metaObject);
        }
    }
}