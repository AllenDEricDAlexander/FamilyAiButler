package top.egon.familyaibutler.gateway.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.gateway.util
 * @ClassName: SecurityUtil
 * @Author: atluofu
 * @CreateTime: 2025Year-08Month-15Day-20:23
 * @Description: 存放用户的登录数据
 * @Version: 1.0
 */
@Slf4j
public class SecurityUtil {

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    public static Object getValue(String name) {
        return THREAD_LOCAL.get().get(name);
    }

    public static String getString(String name) {
        return Objects.isNull(THREAD_LOCAL.get().get(name)) ? "" : THREAD_LOCAL.get().get(name).toString();
    }

    public static String getName() {
        return getString("name");
    }

    public static Long getUserId() {
        return getLongValue("id");
    }

    private static Long getLongValue(String id) {
        return Long.parseLong(THREAD_LOCAL.get().get(id).toString());
    }

    public static void setMap(Map<String, Object> map) {
        THREAD_LOCAL.set(map);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

}