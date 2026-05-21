/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @FileName: InMemoryDeviceGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: 内存设备网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gatewayimpl;

import top.egon.familyaibutler.uaa.domain.gateway.DeviceGateway;
import top.egon.familyaibutler.uaa.domain.model.entity.Device;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @ClassName: InMemoryDeviceGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: 内存设备网关实现
 * @Version: 1.0
 */
public class InMemoryDeviceGatewayImpl implements DeviceGateway {
    private final Map<String, Device> devices = new ConcurrentHashMap<>();

    /**
     * 保存设备。
     *
     * @param device 设备实体
     * @return 保存后的设备实体
     */
    @Override
    public Device save(Device device) {
        devices.put(device.getDeviceId(), device);
        return device;
    }

    /**
     * 按设备 ID 查询设备。
     *
     * @param deviceId 设备 ID
     * @return 设备实体
     */
    @Override
    public Optional<Device> findByDeviceId(String deviceId) {
        return Optional.ofNullable(devices.get(deviceId));
    }

    /**
     * 按账号查询设备列表。
     *
     * @param accountId 账号 ID
     * @return 设备列表
     */
    @Override
    public List<Device> findByAccountId(String accountId) {
        return devices.values().stream()
                .filter(device -> device.getAccountId().equals(accountId))
                .filter(device -> !device.isRemoved())
                .toList();
    }

    /**
     * 按账号和指纹查询设备。
     *
     * @param accountId   账号 ID
     * @param fingerprint 设备指纹
     * @return 设备实体
     */
    @Override
    public Optional<Device> findByFingerprint(String accountId, String fingerprint) {
        return devices.values().stream()
                .filter(device -> device.getAccountId().equals(accountId))
                .filter(device -> fingerprint != null && fingerprint.equals(device.getFingerprint()))
                .filter(device -> !device.isRemoved())
                .findFirst();
    }
}
