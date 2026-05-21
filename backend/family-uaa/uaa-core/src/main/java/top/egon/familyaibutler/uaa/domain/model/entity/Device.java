/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.model.entity
 * @FileName: Device.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-12:00
 * @Description: 登录设备实体文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.domain.model.entity;

import top.egon.familyaibutler.uaa.domain.model.enums.DeviceType;

import java.util.UUID;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.domain.model.entity
 * @ClassName: Device
 * @Author: atluofu
 * @CreateTime: 2026-05-20 12:00
 * @Description: 登录设备实体
 * @Version: 1.0
 */
public class Device {
    private final String deviceId;
    private final String accountId;
    private final String deviceName;
    private final DeviceType deviceType;
    private final String fingerprint;
    private boolean removed;

    private Device(String deviceId, String accountId, String deviceName, DeviceType deviceType, String fingerprint) {
        this.deviceId = deviceId;
        this.accountId = accountId;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.fingerprint = fingerprint;
    }

    /**
     * 登记登录设备。
     *
     * @param accountId   账号 ID
     * @param deviceName  设备名称
     * @param deviceType  设备类型
     * @param fingerprint 设备指纹
     * @return 登录设备
     */
    public static Device register(String accountId, String deviceName, DeviceType deviceType, String fingerprint) {
        return new Device("dev_" + UUID.randomUUID(), accountId, deviceName, deviceType, fingerprint);
    }

    /**
     * 还原登录设备。
     *
     * @param deviceId    设备 ID
     * @param accountId   账号 ID
     * @param deviceName  设备名称
     * @param deviceType  设备类型
     * @param fingerprint 设备指纹
     * @param removed     是否移除
     * @return 登录设备
     */
    public static Device restore(String deviceId, String accountId, String deviceName, DeviceType deviceType,
                                 String fingerprint, boolean removed) {
        Device device = new Device(deviceId, accountId, deviceName, deviceType, fingerprint);
        device.removed = removed;
        return device;
    }

    /**
     * 移除设备。
     */
    public void remove() {
        this.removed = true;
    }

    /**
     * 获取设备 ID。
     *
     * @return 设备 ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * 获取账号 ID。
     *
     * @return 账号 ID
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * 获取设备名称。
     *
     * @return 设备名称
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * 获取设备类型。
     *
     * @return 设备类型
     */
    public DeviceType getDeviceType() {
        return deviceType;
    }

    /**
     * 获取设备指纹。
     *
     * @return 设备指纹
     */
    public String getFingerprint() {
        return fingerprint;
    }

    /**
     * 判断设备是否已移除。
     *
     * @return true 表示已移除
     */
    public boolean isRemoved() {
        return removed;
    }
}
