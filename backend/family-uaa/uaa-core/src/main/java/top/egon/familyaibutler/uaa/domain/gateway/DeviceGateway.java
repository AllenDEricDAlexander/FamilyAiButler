/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.gateway
 * @FileName: DeviceGateway.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-13:00
 * @Description: 设备领域网关文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.gateway;

import top.egon.familyaibutler.uaa.domain.model.entity.Device;

import java.util.List;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.gateway
 * @ClassName: DeviceGateway
 * @Author: atluofu
 * @CreateTime: 2026-05-20 13:00
 * @Description: 设备领域网关
 * @Version: 1.0
 */
public interface DeviceGateway {

    /**
     * 保存设备。
     *
     * @param device 设备实体
     * @return 保存后的设备实体
     */
    Device save(Device device);

    /**
     * 按设备 ID 查询设备。
     *
     * @param deviceId 设备 ID
     * @return 设备实体
     */
    Optional<Device> findByDeviceId(String deviceId);

    /**
     * 按账号查询设备列表。
     *
     * @param accountId 账号 ID
     * @return 设备列表
     */
    List<Device> findByAccountId(String accountId);

    /**
     * 按账号和指纹查询设备。
     *
     * @param accountId   账号 ID
     * @param fingerprint 设备指纹
     * @return 设备实体
     */
    Optional<Device> findByFingerprint(String accountId, String fingerprint);
}
