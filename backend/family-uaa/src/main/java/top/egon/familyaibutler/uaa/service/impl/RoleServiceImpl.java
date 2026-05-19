package top.egon.familyaibutler.uaa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import top.egon.familyaibutler.uaa.mapper.RoleMapper;
import top.egon.familyaibutler.uaa.po.RolePO;
import top.egon.familyaibutler.uaa.service.RoleService;
import org.springframework.stereotype.Service;

 /**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa
 * @Author: atluofu
 * @CreateTime: 2025-08-13 00:48:15
 * @Description: (Role)表服务实现类
 * @Version: 1.0
 */
@Service("roleService")
public class RoleServiceImpl extends ServiceImpl<RoleMapper, RolePO> implements RoleService {

}

