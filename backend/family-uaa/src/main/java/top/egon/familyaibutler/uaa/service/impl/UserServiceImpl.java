package top.egon.familyaibutler.uaa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.egon.familyaibutler.uaa.mapper.RoleMapper;
import top.egon.familyaibutler.uaa.mapper.UserMapper;
import top.egon.familyaibutler.uaa.po.PermissionPO;
import top.egon.familyaibutler.uaa.po.RolePO;
import top.egon.familyaibutler.uaa.po.UserPO;
import top.egon.familyaibutler.uaa.service.UserService;
import top.egon.familyaibutler.uaa.vo.UserPermissionsVO;

import java.util.Collections;
import java.util.List;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa
 * @Author: atluofu
 * @CreateTime: 2025-08-13 00:48:18
 * @Description: user table(User)表服务实现类
 * @Version: 1.0
 */
@Service("userDetailsService")
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPO> implements UserService {

    private final UserMapper userMapper;

    private final RoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserPO user = userMapper.selectOne(
                new LambdaQueryWrapper<UserPO>()
                        .eq(UserPO::getUsername, username)
        );
        if (user == null) throw new UsernameNotFoundException("用户不存在");
        List<RolePO> roles = userMapper.selectRolesByUserId(user.getId());
        user.setRoles(roles != null ? roles : Collections.emptyList());
        user.getRoles().forEach(role -> {
            List<PermissionPO> permissions = roleMapper.selectPermissionsByRoleId(role.getId());
            role.setPermissions(permissions != null ? permissions : Collections.emptyList());
        });
        return user;
    }

    @Override
    public List<UserPermissionsVO> getPermissionsList() {
        List<UserPO> users = this.userMapper.selectList(null);
        return users.stream().map(user -> {
            UserPermissionsVO userVO = new UserPermissionsVO();
            userVO.setId(user.getId());
            userVO.setUsername(user.getUsername());
            userVO.setEnabled(user.getEnable());
            List<RolePO> roles = userMapper.selectRolesByUserId(user.getId());
            List<String> permissions = roles.stream()
                    .flatMap(role ->
                            roleMapper.selectPermissionsByRoleId(role.getId())
                                    .stream()
                                    .map(PermissionPO::getCode)
                    )
                    .distinct()
                    .toList();
            userVO.setPermissions(permissions);
            return userVO;
        }).toList();
    }

    @Override
    public List<UserPO> getList() {
        List<UserPO> users = userMapper.selectList(null);
        return users.stream().peek(user -> {
            List<RolePO> roles = userMapper.selectRolesByUserId(user.getId());
            List<RolePO> roleList = roles.stream().peek(role -> {
                // 查询角色权限并提取权限码
                List<PermissionPO> permissions = roleMapper.selectPermissionsByRoleId(role.getId());
                role.setPermissions(permissions);
            }).toList();
            user.setRoles(roleList);
        }).toList();
    }
}

