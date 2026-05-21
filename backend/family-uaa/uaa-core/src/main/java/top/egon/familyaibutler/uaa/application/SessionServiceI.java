/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @FileName: SessionServiceI.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 会话应用服务接口文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application
 * @ClassName: SessionServiceI
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 会话应用服务接口
 * @Version: 1.0
 */
public interface SessionServiceI {

    /**
     * 撤销会话。
     *
     * @param sessionId 会话 ID
     * @return true 表示撤销成功
     */
    boolean revokeSession(String sessionId);
}
