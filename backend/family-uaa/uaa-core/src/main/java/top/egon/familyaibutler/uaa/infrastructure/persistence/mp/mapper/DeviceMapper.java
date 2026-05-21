/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper
 * @FileName: DeviceMapper.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:25
 * @Description: 设备数据库访问层文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.DevicePO;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper
 * @ClassName: DeviceMapper
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:25
 * @Description: 设备数据库访问层
 * @Version: 1.0
 */
@Mapper
public interface DeviceMapper extends BaseMapper<DevicePO> {
}
