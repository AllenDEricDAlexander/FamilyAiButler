/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @FileName: MpSessionGatewayImpl.java
 * @Author: atluofu
 * @CreateTime: 2026Year-05Month-20Day-14:40
 * @Description: MyBatis Plus 会话网关实现文件
 * @Version: 1.0
 */
package top.egon.familyaibutler.uaa.infrastructure.gateway.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Repository;
import top.egon.familyaibutler.uaa.domain.auth.gateway.SessionGateway;
import top.egon.familyaibutler.uaa.domain.auth.model.aggregate.AuthSession;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.converter.UaaMpConverter;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.dataobject.AuthSessionPO;
import top.egon.familyaibutler.uaa.infrastructure.persistence.mp.mapper.AuthSessionMapper;

import java.util.List;
import java.util.Optional;

/**
 * @BelongsProject: familyaibutler
 * @BelongsPackage: top.egon.familyaibutler.uaa.infrastructure.gateway.impl
 * @ClassName: MpSessionGatewayImpl
 * @Author: atluofu
 * @CreateTime: 2026-05-20 14:40
 * @Description: MyBatis Plus 会话网关实现
 * @Version: 1.0
 */
@Repository
public class MpSessionGatewayImpl implements SessionGateway {
    private final AuthSessionMapper authSessionMapper;
    private final UaaMpConverter uaaMpConverter;

    /**
     * 创建 MyBatis Plus 会话网关实现。
     *
     * @param authSessionMapper 认证会话 Mapper
     * @param uaaMpConverter    UAA 转换器
     */
    public MpSessionGatewayImpl(AuthSessionMapper authSessionMapper, UaaMpConverter uaaMpConverter) {
        this.authSessionMapper = authSessionMapper;
        this.uaaMpConverter = uaaMpConverter;
    }

    /**
     * 保存会话。
     *
     * @param session 认证会话
     * @return 保存后的认证会话
     */
    @Override
    public AuthSession save(AuthSession session) {
        AuthSessionPO sessionPO = uaaMpConverter.toAuthSessionPO(session);
        if (authSessionMapper.selectById(session.getSessionId()) == null) {
            authSessionMapper.insert(sessionPO);
        } else {
            authSessionMapper.updateById(sessionPO);
        }
        return session;
    }

    /**
     * 按会话 ID 查询会话。
     *
     * @param sessionId 会话 ID
     * @return 认证会话
     */
    @Override
    public Optional<AuthSession> findBySessionId(String sessionId) {
        return Optional.ofNullable(authSessionMapper.selectById(sessionId)).map(uaaMpConverter::toAuthSession);
    }

    /**
     * 按账号查询会话列表。
     *
     * @param accountId 账号 ID
     * @return 会话列表
     */
    @Override
    public List<AuthSession> findByAccountId(String accountId) {
        LambdaQueryWrapper<AuthSessionPO> wrapper = new LambdaQueryWrapper<AuthSessionPO>().eq(AuthSessionPO::getAccountId, accountId);
        return authSessionMapper.selectList(wrapper).stream().map(uaaMpConverter::toAuthSession).toList();
    }

    /**
     * 按设备查询会话列表。
     *
     * @param deviceId 设备 ID
     * @return 会话列表
     */
    @Override
    public List<AuthSession> findByDeviceId(String deviceId) {
        LambdaQueryWrapper<AuthSessionPO> wrapper = new LambdaQueryWrapper<AuthSessionPO>().eq(AuthSessionPO::getDeviceId, deviceId);
        return authSessionMapper.selectList(wrapper).stream().map(uaaMpConverter::toAuthSession).toList();
    }
}
