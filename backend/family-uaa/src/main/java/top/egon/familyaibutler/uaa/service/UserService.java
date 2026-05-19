package top.egon.familyaibutler.uaa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;
import top.egon.familyaibutler.uaa.po.UserPO;
import top.egon.familyaibutler.uaa.vo.UserPermissionsVO;

import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa
 * @Author: atluofu
 * @CreateTime: 2025-08-13 00:48:18
 * @Description: user table(User)表服务接口
 * @Version: 1.0
 */
public interface UserService extends IService<UserPO>, UserDetailsService {
    List<UserPermissionsVO> getPermissionsList();

    List<UserPO> getList();
}

