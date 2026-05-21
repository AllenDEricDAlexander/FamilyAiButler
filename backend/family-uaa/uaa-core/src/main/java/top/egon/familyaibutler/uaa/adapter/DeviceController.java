/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter
 * @FileName: DeviceController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 设备 Web 控制器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.uaa.application.DeviceServiceI;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter
 * @ClassName: DeviceController
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 设备 Web 控制器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/device")
public class DeviceController {
    private final DeviceServiceI deviceService;

    /**
     * 创建设备 Web 控制器。
     *
     * @param deviceService 设备应用服务
     */
    public DeviceController(DeviceServiceI deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * 移除设备。
     *
     * @param deviceId 设备 ID
     * @return true 表示移除成功
     */
    @DeleteMapping("/{deviceId}")
    public Result<Boolean> removeDevice(@PathVariable String deviceId) {
        return Result.success(deviceService.removeDevice(deviceId));
    }
}
