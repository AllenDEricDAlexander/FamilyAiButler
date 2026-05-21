/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage
 * @FileName: DeviceManage.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 设备应用服务接口文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application.manage;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage
 * @ClassName: DeviceManage
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 设备应用服务接口
 * @Version: 1.0
 */
public interface DeviceManage {

    /**
     * 移除设备。
     *
     * @param deviceId 设备 ID
     * @return true 表示移除成功
     */
    boolean removeDevice(String deviceId);
}
