package top.egon.familyaibutler.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.common.utils
 * @ClassName: BaseConverter
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-22Day-20:43
 * @Description: BaseConverter interface
 * @Version: 1.0
 */
public interface BaseConverter<S, T> {

    /**
     * 单个对象转换
     */
    T toTarget(S source);

    /**
     * 单个对象逆向转换
     */
    S toSource(T target);

    /**
     * 列表转换
     */
    List<T> toTargetList(List<S> sourceList);

    /**
     * 列表逆向转换
     */
    List<S> toSourceList(List<T> targetList);

    /**
     * 日期 → 字符串
     */
    default String map(Date date) {
        return date == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * 字符串 → 日期
     */
    default Date map(String date) {
        try {
            return date == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
        } catch (Exception e) {
            return null;
        }
    }
}
