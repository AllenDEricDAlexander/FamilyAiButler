/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @FileName: MpDeviceGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:40
 * @Description: MyBatis Plus 设备网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gatewayimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import top.egon.familyaibutler.uaa.domain.gateway.DeviceGateway;
import top.egon.familyaibutler.uaa.domain.model.entity.Device;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.converter.UaaMpConverter;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.DevicePO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper.DeviceMapper;

import java.util.List;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gatewayimpl
 * @ClassName: MpDeviceGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:40
 * @Description: MyBatis Plus 设备网关实现
 * @Version: 1.0
 */
@Repository
public class MpDeviceGatewayImpl implements DeviceGateway {
    private final DeviceMapper deviceMapper;
    private final UaaMpConverter uaaMpConverter;

    /**
     * 创建 MyBatis Plus 设备网关实现。
     *
     * @param deviceMapper   设备 Mapper
     * @param uaaMpConverter UAA 转换器
     */
    public MpDeviceGatewayImpl(DeviceMapper deviceMapper, UaaMpConverter uaaMpConverter) {
        this.deviceMapper = deviceMapper;
        this.uaaMpConverter = uaaMpConverter;
    }

    /**
     * 保存设备。
     *
     * @param device 设备实体
     * @return 保存后的设备实体
     */
    @Override
    public Device save(Device device) {
        DevicePO devicePO = uaaMpConverter.toDevicePO(device);
        if (deviceMapper.selectById(device.getDeviceId()) == null) {
            deviceMapper.insert(devicePO);
        } else {
            deviceMapper.updateById(devicePO);
        }
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
        return Optional.ofNullable(deviceMapper.selectById(deviceId)).map(uaaMpConverter::toDevice);
    }

    /**
     * 按账号查询设备列表。
     *
     * @param accountId 账号 ID
     * @return 设备列表
     */
    @Override
    public List<Device> findByAccountId(String accountId) {
        LambdaQueryWrapper<DevicePO> wrapper = new LambdaQueryWrapper<DevicePO>()
                .eq(DevicePO::getAccountId, accountId)
                .eq(DevicePO::getRemoved, false);
        return deviceMapper.selectList(wrapper).stream().map(uaaMpConverter::toDevice).toList();
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
        if (fingerprint == null || fingerprint.isBlank()) {
            return Optional.empty();
        }
        LambdaQueryWrapper<DevicePO> wrapper = new LambdaQueryWrapper<DevicePO>()
                .eq(DevicePO::getAccountId, accountId)
                .eq(DevicePO::getFingerprint, fingerprint)
                .eq(DevicePO::getRemoved, false)
                .last("limit 1");
        return Optional.ofNullable(deviceMapper.selectOne(wrapper)).map(uaaMpConverter::toDevice);
    }
}
