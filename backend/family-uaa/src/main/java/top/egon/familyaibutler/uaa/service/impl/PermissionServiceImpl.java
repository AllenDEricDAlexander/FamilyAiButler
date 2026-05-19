package top.egon.familyaibutler.uaa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import top.egon.familyaibutler.uaa.mapper.PermissionMapper;
import top.egon.familyaibutler.uaa.po.PermissionPO;
import top.egon.familyaibutler.uaa.service.PermissionService;
import org.springframework.stereotype.Service;

 /**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa
 * @Author: atluofu
 * @CreateTime: 2025-08-13 00:48:18
 * @Description: (Permission)表服务实现类
 * @Version: 1.0
 */
@Service("permissionService")
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, PermissionPO> implements PermissionService {

}

