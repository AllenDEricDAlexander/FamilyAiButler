/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @FileName: DeviceController.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 设备 Web 控制器文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.adapter.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.egon.familyaibutler.common.pojo.Result;
import top.egon.familyaibutler.uaa.application.manage.DeviceManage;
import top.egon.openapi.console.annotation.DocDataKind;
import top.egon.openapi.console.annotation.DocDataType;
import top.egon.openapi.console.annotation.DocOperation;
import top.egon.openapi.console.annotation.DocParam;
import top.egon.openapi.console.annotation.DocParamIn;
import top.egon.openapi.console.annotation.DocParameter;
import top.egon.openapi.console.annotation.DocProtocol;
import top.egon.openapi.console.annotation.DocRequest;
import top.egon.openapi.console.annotation.DocResponse;
import top.egon.openapi.console.annotation.DocService;
import top.egon.openapi.console.annotation.DocWrapper;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.adapter.web
 * @ClassName: DeviceController
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 设备 Web 控制器
 * @Version: 1.0
 */
@RestController
@RequestMapping("/device")
@DocService(groupId = "uaa", groupName = "认证授权服务", serviceId = "uaa-device",
        serviceName = "设备服务", serviceDescription = "登录设备管理能力", protocol = DocProtocol.HTTP)
public class DeviceController {
    private final DeviceManage deviceService;

    /**
     * 创建设备 Web 控制器。
     *
     * @param deviceService 设备应用服务
     */
    public DeviceController(DeviceManage deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * 移除设备。
     *
     * @param deviceId 设备 ID
     * @return true 表示移除成功
     */
    @DeleteMapping("/{deviceId}")
    @DocOperation(summary = "移除设备", description = "按设备 ID 移除登录设备",
            request = @DocRequest(params = {
                    @DocParameter(name = "deviceId", in = DocParamIn.PATH, description = "设备 ID", required = true,
                            dataType = @DocDataType(kind = DocDataKind.STRING), example = "device-001")
            }),
            response = @DocResponse(description = "移除成功",
                    dataType = @DocDataType(kind = DocDataKind.BOOLEAN),
                    wrapper = @DocWrapper(type = Result.class, dataPath = "data")))
    public Result<Boolean> removeDevice(@PathVariable @DocParam(description = "设备 ID", required = true) String deviceId) {
        return Result.success(deviceService.removeDevice(deviceId));
    }
}
