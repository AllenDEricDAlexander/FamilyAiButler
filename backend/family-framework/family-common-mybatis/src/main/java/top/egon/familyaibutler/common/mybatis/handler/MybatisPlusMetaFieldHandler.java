package top.egon.familyaibutler.common.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.mybatis.handler
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

    /**
     * 插入数据时填充公共时间字段。
     *
     * @param metaObject MyBatis 元数据对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Date date = new Date();
        setNowDateField("createTime", metaObject, date);
        setNowDateField("updateTime", metaObject, date);
    }

    /**
     * 更新数据时填充公共时间字段。
     *
     * @param metaObject MyBatis 元数据对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        setNowDateField("updateTime", metaObject, new Date());
    }

    /**
     * 当前字段为空时填充指定时间。
     *
     * @param updateTimeKey 时间字段名称
     * @param metaObject    MyBatis 元数据对象
     * @param date          填充时间
     */
    private void setNowDateField(String updateTimeKey, MetaObject metaObject, Date date) {
        Object updateTime = getFieldValByName(updateTimeKey, metaObject);
        if (ObjectUtils.isEmpty(updateTime)) {
            setFieldValByName(updateTimeKey, date, metaObject);
        }
    }
}
