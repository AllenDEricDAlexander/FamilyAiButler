/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage.impl
 * @FileName: DeviceManageImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: 设备应用服务实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.application.manage.impl;

import org.springframework.stereotype.Service;
import top.egon.familyaibutler.uaa.application.manage.DeviceManage;
import top.egon.familyaibutler.uaa.domain.auth.gateway.DeviceGateway;
import top.egon.familyaibutler.uaa.domain.auth.gateway.SessionGateway;
import top.egon.familyaibutler.uaa.domain.auth.gateway.TokenGateway;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.application.manage.impl
 * @ClassName: DeviceManageImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: 设备应用服务实现
 * @Version: 1.0
 */
@Service
public class DeviceManageImpl implements DeviceManage {
    private final DeviceGateway deviceGateway;
    private final SessionGateway sessionGateway;
    private final TokenGateway tokenGateway;

    /**
     * 创建设备应用服务实现。
     *
     * @param deviceGateway  设备网关
     * @param sessionGateway 会话网关
     * @param tokenGateway   Token 网关
     */
    public DeviceManageImpl(DeviceGateway deviceGateway, SessionGateway sessionGateway, TokenGateway tokenGateway) {
        this.deviceGateway = deviceGateway;
        this.sessionGateway = sessionGateway;
        this.tokenGateway = tokenGateway;
    }

    /**
     * 移除设备。
     *
     * @param deviceId 设备 ID
     * @return true 表示移除成功
     */
    @Override
    public boolean removeDevice(String deviceId) {
        return deviceGateway.findByDeviceId(deviceId)
                .map(device -> {
                    device.remove();
                    deviceGateway.save(device);
                    sessionGateway.findByDeviceId(deviceId).forEach(session -> {
                        session.revoke();
                        sessionGateway.save(session);
                    });
                    tokenGateway.revokeByDeviceId(deviceId);
                    return true;
                })
                .orElse(false);
    }
}
